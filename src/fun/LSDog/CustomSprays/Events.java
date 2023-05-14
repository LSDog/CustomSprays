package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.data.DataMySQL;
import fun.LSDog.CustomSprays.spray.SprayManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 实现双击F喷漆
 */
public class Events implements Listener {

    /**
     * 存储所有玩家按F的时间
     */
    private static final Map<UUID, Long> timeMap = new HashMap<>();

    /**
     * 判断双击F时的时间间隔最长为多久(ms)
     */
    private static final int CD = 350;

    /**
     * 检测双击F
     * @param e {@link PlayerSwapHandItemsEvent}
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onToggleF(PlayerSwapHandItemsEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instance, () -> {
            Player player = e.getPlayer();
            UUID uuid = player.getUniqueId();
            Long t = timeMap.get(uuid);
            if ( t==null || System.currentTimeMillis() > t) {
                timeMap.put(uuid, System.currentTimeMillis() + CD);
            } else {
                timeMap.remove(uuid);
                if (!player.isSneaking()) { // 小喷漆
                    Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instance, () -> SprayManager.spray(player, false));
                } else { // 大喷漆
                    Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instance, () -> SprayManager.spray(player, true));
                }
            }
        });
    }

    /**
     * 玩家加入相关逻辑
     * @param e {@link PlayerJoinEvent}
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        // 初始化账户
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instance, () -> {
            if (e.getPlayer().isOnline() && DataManager.data instanceof DataMySQL) {
                DataMySQL.addAccountIfNotExist(e.getPlayer());
            }
        }, 10L);
        SprayManager.sendExistSprays(e.getPlayer());
        if (CustomSprays.latestVersion != null && e.getPlayer().isOp()) {
            e.getPlayer().sendMessage(CustomSprays.prefix + " §6§l嘿, 管理! CustomSprays 有个更新~~ §7-> §b§l" + CustomSprays.latestVersion);
            e.getPlayer().sendMessage(CustomSprays.prefix + " §6§lHey, OP! CustomSprays has an update~~ §7-> §b§l" + CustomSprays.latestVersion);
            //e.getPlayer().sendRawMessage("[{\"text\":\"*CustomSprays*\",\"color\":\"dark_blue\",\"bold\":true,\"italic\":true,\"underlined\":true,\"strikethrough\":false,\"obfuscated\":false,\"insertion\":\"https://gitee.com/pixelmc/CustomSprays/releases\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://gitee.com/pixelmc/CustomSprays/releases\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"*click*\"}}]");
            // Use less.
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
                        && e.getMaterial().name().equalsIgnoreCase(CustomSprays.instance.getConfig().getString("spray_item"))
        ) {
            if (e.isCancelled()) return;

            ItemStack item = e.getItem();

            String loreText = CustomSprays.instance.getConfig().getString("spray_item_lore");
            String loreTimesUse = ChatColor.translateAlternateColorCodes('&', CustomSprays.instance.getConfig().getString("spray_item_lore_times_use"));
            boolean isSprayItem = loreText == null;

            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                ItemMeta itemMeta = item.getItemMeta();
                List<String> lore = itemMeta.getLore();
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
                        if (!useTimeString.equals(DataManager.getMsg(e.getPlayer(), "INFINITE"))) {
                            try {
                                useTime = Integer.parseInt(line.substring(loreTimesUse.length()));
                            } catch (Exception ex) {
                                //ex.printStackTrace();
                                return;
                            }
                        }
                    }
                }

                if (useTime >= 1 && SprayManager.spray(e.getPlayer(), e.getPlayer().isSneaking())) {
                    useTime -= 1;
                    if (CustomSprays.instance.getConfig().getBoolean("destroy_if_exhausted") && useTime <= 0) item.setType(Material.AIR);
                    e.getPlayer().sendMessage(CustomSprays.prefix + ChatColor.translateAlternateColorCodes('&',
                            DataManager.getMsg(e.getPlayer(), "SPRAY.ITEM_USE")).replace("%use%", useTime+""));
                    lore.set(useTimeLineIndex, loreTimesUse + useTime);
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    switch (e.getHand()) {
                        case HAND:
                            e.getPlayer().getInventory().setItemInMainHand(item);
                            break;
                        case OFF_HAND:
                            e.getPlayer().getInventory().setItemInOffHand(item);
                            break;
                    }
                    e.getPlayer().sendMessage(itemMeta.getLore().toString());
                }
            } else {
                SprayManager.spray(e.getPlayer(), e.getPlayer().isSneaking());
            }
        }
    }

}
