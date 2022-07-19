package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.CoolDown;
import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Spray {

    /**
     * 让玩家喷漆，若玩家进行大喷漆(3*3)却没有权限，则会变为小喷漆(1*1)，默认展示给全服玩家 <br>
     * <b>务必使用 runTaskAsynchronously 异步执行, 否则可能造成卡顿！！</b>
     * @param player 喷漆玩家
     * @param isBigSpray 是否为大型喷漆
     */
    public static void spray(Player player, boolean isBigSpray) {

        // 检测喷漆权限
        if (player.isPermissionSet("CustomSprays.spray") && !player.hasPermission("CustomSprays.spray")) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
            return;
        }
        // 检测禁止的世界
        if (!player.hasPermission("CustomSprays.nodisable") && DataManager.disableWorlds != null && DataManager.disableWorlds.contains(player.getWorld().getName())) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.DISABLED_WORLD"));
            return;
        }
        // 检测CD
        if (!player.hasPermission("CustomSprays.nocd") && CoolDown.isSprayCooling(player)) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING")+" §7("+ CoolDown.getSprayCD(player)+")");
            return;
        }


        try {
            // 如果 [不是大喷漆  或者  (是大喷漆却)没有大喷漆权限]
            if (!isBigSpray || (player.isPermissionSet("CustomSprays.bigspray") && !player.hasPermission("CustomSprays.bigspray"))) {

                // 小喷漆
                byte[] bytes = DataManager.get128pxImageBytes(player);
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return;
                }
                SpraySmall spray = new SpraySmall(player, bytes, Bukkit.getOnlinePlayers());
                if (spray.create((long) (CustomSprays.instant.getConfig().getDouble("destroy")*20L))) {
                    CoolDown.setSprayCooldown(player,1);
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spray.location.getX() + " " + spray.location.getY() + " " + spray.location.getZ());
                }

            } else {

                // 大喷漆
                int length = CustomSprays.instant.getConfig().getInt("big_size");
                byte[] bytes;
                if (length == 3) {
                    bytes = DataManager.get384pxImageBytes(player);
                } else if (length == 5) {
                    bytes = DataManager.getSizedImageBytes(player, 640, 640);
                } else {
                    return;
                }
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return;
                }

                SpraySmall spray = new SprayBig(player, length, bytes, Bukkit.getOnlinePlayers());
                if (spray.create((long) (CustomSprays.instant.getConfig().getDouble("destroy")*20L))) {
                    CoolDown.setSprayCooldown(player, CustomSprays.instant.getConfig().getDouble("big_spray_cooldown_multiple"));
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spray.location.getX() + " " + spray.location.getY() + " " + spray.location.getZ() + " (big)");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
