package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.manager.SprayManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.*;

/**
 * 实现双击F喷漆
 */
public class Events implements Listener {

    private static final Map<UUID, Long> timeMap = new HashMap<>();

    private static final int CD = 350;
    // double click in 350 ms

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onToggleF(PlayerSwapHandItemsEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instant, () -> {
            Player player = e.getPlayer();
            if (!timeMap.containsKey(player.getUniqueId()) || System.currentTimeMillis() > timeMap.get(player.getUniqueId())) {
                timeMap.put(player.getUniqueId(), System.currentTimeMillis() + CD);
            } else {
                timeMap.remove(player.getUniqueId());
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                if (!player.isSneaking()) { // 小喷漆
                    Bukkit.getScheduler().runTask(CustomSprays.instant, () -> CustomSprays.spray(player, false, players));
                } else { // 大喷漆
                    Bukkit.getScheduler().runTask(CustomSprays.instant, () -> CustomSprays.spray(player, true, players));
                }
            }
        });
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, () -> SprayManager.playerSprayMap.forEach((uuid, sprays) -> sprays.forEach(spray -> {
            try {
                spray.spawn(Collections.singletonList(e.getPlayer()));
            } catch (ReflectiveOperationException reflectiveOperationException) {
                reflectiveOperationException.printStackTrace();
            }
        })), 20L);
    }

}
