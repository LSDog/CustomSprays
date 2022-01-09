package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpray implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instant, () -> {
            if (!(sender instanceof Player)) return;
            Player player = ((Player) sender).getPlayer();
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("big")) {
                    CustomSprays.spray(player, true);
                    return;
                }
                player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.TOO_MANY_ARGUMENTS"));
                return;
            }
            CustomSprays.spray(player, false);
        });
        return true;
    }

}
