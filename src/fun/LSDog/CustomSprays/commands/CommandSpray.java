package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandSpray implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instant, () -> {
            Player player = ((Player) sender).getPlayer();
            if (args.length != 0) {
                // 大喷漆
                if (args[0].equalsIgnoreCase("big")) {
                    CustomSprays.spray(player, true, players);
                    return;
                }
                player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.TOO_MANY_ARGUMENTS"));
                return;
            }
            // 小喷漆
            CustomSprays.spray(player, false, players);
        });
        return true;
    }

}
