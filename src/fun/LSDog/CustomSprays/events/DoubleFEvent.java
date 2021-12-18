package fun.LSDog.CustomSprays.events;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.commands.CommandSpray;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 实现双击F喷漆
 */
public class DoubleFEvent implements Listener {

    private static final Map<UUID, Long> timeMap = new HashMap<>();

    private static final int CD = 350;
    // double click in 350 ms

    @EventHandler
    public void onToggleF(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (!timeMap.containsKey(player.getUniqueId()) || System.currentTimeMillis() > timeMap.get(player.getUniqueId())) {
            timeMap.put(player.getUniqueId(), System.currentTimeMillis() + CD);
        } else {
            timeMap.remove(player.getUniqueId());
            Bukkit.getScheduler().runTask(CustomSprays.instant, () -> CommandSpray.spray(player));
        }
    }

}
