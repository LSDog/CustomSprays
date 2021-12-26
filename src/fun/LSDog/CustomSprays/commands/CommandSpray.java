package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Data.DataManager;
import fun.LSDog.CustomSprays.Spray;
import fun.LSDog.CustomSprays.manager.CoolDownManager;
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
            if (args.length != 0) { player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.TOO_MANY_ARGUMENTS"));return; }
            spray(player);
        });
        return true;
    }

    public static void spray(Player player) {
        if (player.isPermissionSet("CustomSprays.canSpray") && !player.hasPermission("CustomSprays.canSpray")) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
            return;
        }
        if (!player.isOp() && CoolDownManager.isSprayCooling(player)) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING")+" §7("+CoolDownManager.getSprayCool(player)+")");
            return;
        }
        CoolDownManager.addSprayCooldown(player);
        if (DataManager.data.getImageString(player) == null) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
            return;
        }
        try {
            new Spray(player, DataManager.getImage(player), Bukkit.getOnlinePlayers()).create(CustomSprays.instant.getConfig().getInt("destroy")*20L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
