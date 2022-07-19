package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.spray.Spray;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandSpray implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Bukkit.getScheduler().runTaskAsynchronously(CustomSprays.instant, () -> {
            Player player = ((Player) sender).getPlayer();
            if (args.length != 0) {
                // 大喷漆
                if (args[0].equalsIgnoreCase("big")) {
                    Spray.spray(player, true);
                    return;
                } else if (args[0].equalsIgnoreCase("small")) {
                    Spray.spray(player, false);
                    return;
                }
                player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.TOO_MANY_ARGUMENTS"));
                return;
            }
            // 小喷漆
            Spray.spray(player, false);
        });
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> argList = new ArrayList<>();
            argList.add("small");
            argList.add("big");
            return argList;
        }
        return null;
    }

}
