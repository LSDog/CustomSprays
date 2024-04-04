package fun.LSDog.CustomSprays.listener;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.command.CommandCustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.data.DataMySQL;
import fun.LSDog.CustomSprays.spray.SprayManager;
import fun.LSDog.CustomSprays.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * 实现双击F喷漆
 */
public class ListenerBasic implements Listener {

    /**
     * 玩家加入相关逻辑
     * @param e {@link PlayerJoinEvent}
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        // 初始化账户
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.plugin, () -> {
            if (e.getPlayer().isOnline() && DataManager.data instanceof DataMySQL) {
                DataMySQL.addAccountIfNotExist(e.getPlayer());
            }
        }, 10L);
        SprayManager.sendExistSprays(e.getPlayer());
        if (CustomSprays.latestVersion != null && e.getPlayer().isOp()) {
            e.getPlayer().sendMessage(CustomSprays.prefix + " §6§l嘿, 管理! CustomSprays 有个更新~~ §7-> §b§l" + CustomSprays.latestVersion);
            e.getPlayer().sendMessage(CustomSprays.prefix + " §6§lHey, OP! CustomSprays has an update~~ §7-> §b§l" + CustomSprays.latestVersion);
        }
    }

    /**
     * 喷漆物品的使用
     * @param e {@link PlayerInteractEvent}
     */
    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (
                e.getAction().name().contains("RIGHT")
                        && e.hasItem()
                        && e.getMaterial().name().equalsIgnoreCase(CommandCustomSprays.getSprayItemMaterialName())
        ) {
            if (e.isCancelled()) return;

            ItemStack item = e.getItem();

            String loreText = CustomSprays.plugin.getConfig().getString("spray_item_lore");
            String loreTimesUse = ChatColor.translateAlternateColorCodes('&', CustomSprays.plugin.getConfig().getString("spray_item_lore_times_use"));
            boolean isSprayItem = loreText == null;

            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                ItemMeta itemMeta = item.getItemMeta();
                List<String> lore = itemMeta.getLore();
                boolean isInfinite = false;
                int useTime = 0;
                int useTimeLineIndex = -1;
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.isEmpty()) continue;
                    if (!isSprayItem) {
                        if (line.contains(ChatColor.translateAlternateColorCodes('&', loreText))) {
                            isSprayItem = true;
                        }
                    }
                    if (useTimeLineIndex == -1 && line.startsWith(loreTimesUse)) {
                        useTimeLineIndex = i;
                        String useTimeString = line.substring(loreTimesUse.length());
                        if (useTimeString.equals(DataManager.getMsg(e.getPlayer(), "INFINITE"))) {
                            isInfinite = true;
                        } else {
                            try {
                                useTime = Integer.parseInt(line.substring(loreTimesUse.length()));
                            } catch (Exception ex) {
                                //ex.printStackTrace();
                                return;
                            }
                        }
                    }
                }

                if (isInfinite) {
                    SprayManager.spray(e.getPlayer(), e.getPlayer().isSneaking());
                    return;
                }

                if (useTime >= 1 && SprayManager.spray(e.getPlayer(), e.getPlayer().isSneaking())) {
                    useTime -= 1;
                    if (CustomSprays.plugin.getConfig().getBoolean("destroy_if_exhausted") && useTime <= 0) item.setType(Material.AIR);
                    e.getPlayer().sendMessage(CustomSprays.prefix + ChatColor.translateAlternateColorCodes('&',
                            DataManager.getMsg(e.getPlayer(), "SPRAY.ITEM_USE")).replace("%use%", useTime+""));
                    lore.set(useTimeLineIndex, loreTimesUse + useTime);
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    if (NMS.getSubVer() <= 8) {
                        //noinspection deprecation
                        e.getPlayer().setItemInHand(item);
                    } else {
                        ListenerBasicNew.setItemInHandNew(e, item);
                    }
                }
            }
        }
    }


}
