package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.manager.CoolDownManager;
import fun.LSDog.CustomSprays.utils.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToggleFEvent implements Listener {

    private static final Map<UUID, Long> timeMap = new HashMap<>();

    private static final int CD = 350;
    // double click in 350 ms

    @EventHandler
    public void onToggleF(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (!timeMap.containsKey(player.getUniqueId())) {
            timeMap.put(player.getUniqueId(), System.currentTimeMillis() + CD);
        } else if (System.currentTimeMillis() > timeMap.get(player.getUniqueId())) {
            timeMap.put(player.getUniqueId(), System.currentTimeMillis() + CD);
        } else {
            Bukkit.getScheduler().runTask(CustomSprays.instant, () -> {
                if (player.isPermissionSet("CustomSprays.canSpray") && !player.hasPermission("CustomSprays.canSpray")) {
                    player.sendMessage(CustomSprays.prefix + "§c无权限！");
                    return;
                }
                if ((!player.isOp() || !player.hasPermission("CustomSprays.nocooldown")) && CoolDownManager.isSprayCooling(player)) {
                    player.sendMessage(CustomSprays.prefix + "§c冷却中! §7("+CoolDownManager.getSprayCool(player)+")");
                    return;
                }
                CoolDownManager.addSprayCooldown(player);
                if (Data.getImageString(player.getUniqueId()) == null) {
                    player.sendMessage(CustomSprays.prefix + "笨蛋！你还没有上传图片呢！！！");
                    player.sendMessage(CustomSprays.prefix + "使用 §4/cspray§r upload <url> §r来上传图片！");
                    return;
                }
                try {
                    new Spray(player).create();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            timeMap.remove(player.getUniqueId());
        }
    }

}
