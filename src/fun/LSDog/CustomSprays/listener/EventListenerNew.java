package fun.LSDog.CustomSprays.listener;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.spray.SprayManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 1.9+ 版本的 Listeners
 */
public class EventListenerNew implements Listener {


    /**
     * 存储所有玩家按F的时间
     */
    private static final Map<UUID, Long> timeMap = new HashMap<>();

    /**
     * 判断双击F时的时间间隔最长为多久(ms)
     */
    private static final int CD = 350;

    protected static void setItemInHandNew(PlayerInteractEvent e, ItemStack item) {
        switch (e.getHand()) {
            case HAND:
                e.getPlayer().getInventory().setItemInMainHand(item);
                break;
            case OFF_HAND:
                e.getPlayer().getInventory().setItemInOffHand(item);
                break;
        }
    }

    /**
     * 检测双击F
     * @param e {@link PlayerSwapHandItemsEvent}
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleF(PlayerSwapHandItemsEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.plugin, () -> {
            Player player = e.getPlayer();
            UUID uuid = player.getUniqueId();
            Long t = timeMap.get(uuid);
            if ( t==null || System.currentTimeMillis() > t) {
                timeMap.put(uuid, System.currentTimeMillis() + CD);
            } else {
                timeMap.remove(uuid);
                if (!player.isSneaking()) { // 小喷漆
                    Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.plugin, () -> SprayManager.spray(player, false));
                } else { // 大喷漆
                    Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.plugin, () -> SprayManager.spray(player, true));
                }
            }
        });
    }

}
