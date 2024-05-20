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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Listening to player's events
 */
public class EventListener implements Listener {

    /**
     * Player joins
     * @param e {@link PlayerJoinEvent}
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        playerJoin(player);
    }

    public static void playerJoin(Player player) {
        // Initialize account
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.plugin, () -> {
            if (player.isOnline() && DataManager.data instanceof DataMySQL) {
                DataMySQL.addAccountIfNotExist(player);
            }
        }, 10L);
        // Send existing spray
        SprayManager.sendExistSprays(player);
        // Start monitoring player's packets
        if (NMS.getSubVer() >= 8) PacketListener.addPlayer(player);
        else PacketListener7.addPlayer(player);
        if (CustomSprays.latestVersion != null && player.isOp()) {
            player.sendMessage(CustomSprays.prefix + " §6§l嘿, 管理! CustomSprays 有个更新~~ §7-> §b§l" + CustomSprays.latestVersion);
            player.sendMessage(CustomSprays.prefix + " §6§lHey, OP! CustomSprays has an update~~ §7-> §b§l" + CustomSprays.latestVersion);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        SprayManager.sendExistSprays(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (NMS.getSubVer() >= 8) PacketListener.removePlayer(player);
        else PacketListener7.removePlayer(player);
    }

    /**
     * Check if using spray item
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

            Player player = e.getPlayer();
            ItemStack item = e.getItem();
            if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

            String loreText = ChatColor.translateAlternateColorCodes('&', CustomSprays.plugin.getConfig().getString("spray_item_lore"));
            String loreTimesUse = ChatColor.translateAlternateColorCodes('&', CustomSprays.plugin.getConfig().getString("spray_item_lore_times_use"));

            ItemMeta itemMeta = item.getItemMeta();
            List<String> lore = itemMeta.getLore();
            boolean isSprayItem = false;
            boolean isInfinite = false;
            final int[] useTime = {0};
            int useTimeLineIndex = -1;
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                if (line.isEmpty()) continue;
                if (!isSprayItem && line.contains(loreText)) isSprayItem = true;
                if (useTimeLineIndex == -1 && line.startsWith(loreTimesUse)) {
                    useTimeLineIndex = i;
                    String useTimeString = line.substring(loreTimesUse.length());
                    if (useTimeString.equals(DataManager.getMsg(player, "INFINITE"))) {
                        isInfinite = true;
                    } else try {
                        useTime[0] = Integer.parseInt(line.substring(loreTimesUse.length()));
                    } catch (Exception ignore) { return; }
                }
            }

            if (!isSprayItem) return;
            e.setCancelled(true);

            boolean finalIsInfinite = isInfinite;
            int finalUseTimeLineIndex = useTimeLineIndex;
            Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.plugin, () -> {
                if (finalIsInfinite) {
                    SprayManager.spray(player, player.isSneaking());
                    return;
                }
                if (useTime[0] >= 1 && SprayManager.spray(player, player.isSneaking())) {
                    useTime[0] -= 1;
                    if (CustomSprays.plugin.getConfig().getBoolean("destroy_if_exhausted") && useTime[0] <= 0) item.setType(Material.AIR);
                    player.sendMessage(CustomSprays.prefix + ChatColor.translateAlternateColorCodes('&',
                            DataManager.getMsg(player, "SPRAY.ITEM_USE")).replace("%use%", useTime[0] +""));
                    lore.set(finalUseTimeLineIndex, loreTimesUse + useTime[0]);
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    if (NMS.getSubVer() <= 8) {
                        //noinspection deprecation
                        player.setItemInHand(item);
                    } else {
                        EventListenerNew.setItemInHandNew(e, item);
                    }
                }
            });

        }
    }


}
