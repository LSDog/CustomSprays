package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Spray;
import fun.LSDog.CustomSprays.manager.CoolDownManager;
import fun.LSDog.CustomSprays.utils.Data;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpray implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTask(CustomSprays.instant, () -> {
            if (!(sender instanceof Player)) return;
            Player player = ((Player) sender).getPlayer();
            if (args.length != 0) { player.sendMessage(CustomSprays.prefix + "此为喷漆指令 上传图片请使用 §4/cspray§r upload §r哦！");return; }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }

}
