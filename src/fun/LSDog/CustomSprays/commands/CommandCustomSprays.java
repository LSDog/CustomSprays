package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Data.DataManager;
import fun.LSDog.CustomSprays.Spray;
import fun.LSDog.CustomSprays.manager.CoolDownManager;
import fun.LSDog.CustomSprays.manager.SprayManager;
import fun.LSDog.CustomSprays.utils.ImageGetter;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CommandCustomSprays implements TabExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        FileConfiguration config = CustomSprays.instant.getConfig();
        if (args.length == 0) {
            sender.sendMessage(CustomSprays.prefix +
                    "\n    §b/cspray§r §3upload§l <url> §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.UPLOAD") +
                    (sender.isOp() ?
                            "\n    §b/cspray§r §3view§l [player] §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.VIEW") +
                            "\n    §b/cspray§r §3reload§l §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.RELOAD")
                    : "") +
                    "\n\n\n  " +  DataManager.getMsg(sender, "COMMAND_HELP.TIP"));
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload":
                if (!sender.isOp()) {
                    sender.sendMessage(CustomSprays.prefix + DataManager.getMsg(sender, "NO_PERMISSION"));
                    return true;
                }
                CustomSprays.instant.reloadConfig();
                DataManager.initialize(CustomSprays.instant.getConfig().getString("storage"));
                DataManager.urlRegex = CustomSprays.instant.getConfig().getString("url_regex");
                CustomSprays.prefix = CustomSprays.instant.getConfig().getString("msg_prefix");
                DataManager.debug = CustomSprays.instant.getConfig().getBoolean("debug");
                DataManager.usePapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
                sender.sendMessage(CustomSprays.prefix + "OK!");
                break;


            case "upload":
                new BukkitRunnable() {
                    public void run() {
                        if (!(sender instanceof Player)) { sender.sendMessage(CustomSprays.prefix + "player only!"); return; }

                        Player player = (Player) sender;
                        if (!player.hasPermission("CustomSprays.noCD") && CoolDownManager.isUploadCooling(player)) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING") + " §7("+CoolDownManager.getUploadCool(player)+")");
                            return;
                        }
                        CoolDownManager.addUploadCooldown(player);

                        if (args.length == 1) { player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.NO_URL"));return; }
                        String url = args[1];
                        if (!Pattern.compile(StringEscapeUtils.escapeJava(DataManager.urlRegex)).matcher(url.toLowerCase()).matches()) { player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.NOT_URL"));return; }
                        ImageGetter imageGetter = new ImageGetter(url);
                        byte result = imageGetter.checkImage();
                        if (result != 0) {
                            if (result == 1) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.CONNECT_FAILED"));
                            if (result == 2) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.CONNECT_FAILED")+"\n"+CustomSprays.prefix+ DataManager.getMsg(player, "COMMAND_UPLOAD.CONNECT_HTTPS_FAILED"));
                            if (result == 3) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.FILE_TOO_BIG").replace("{size}", imageGetter.size+"").replace("{limit}", config.getInt("file_size_limit")+""));
                            if (result == 4) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.CANT_GET_SIZE"));
                            return;
                        }
                        player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.UPLOADING"));
                        imageGetter.getBufferedImage();
                        /* Debug: 保存下载的图片 */
                        // imageGetter.saveToFile(new File(CustomSprays.instant.getDataFolder() + "\\" + "imageTemp.png"));
                        String imageStr = null;
                        try {
                            imageStr = imageGetter.Get128pxImageBase64();
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.FAILED_GET_IMAGE"));
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        CustomSprays.debug("§4§l" + player.getName() + "§r upload §7->§r (§e§l"+imageGetter.size+" K§r) " + url);
                        DataManager.data.saveImageString(player, imageStr);
                        player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.OK"));
                        imageGetter.close();
                    }
                }.runTask(CustomSprays.instant);
                break;


            case "view":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(CustomSprays.prefix + "player only!");
                    return true;
                }
                new BukkitRunnable() {
                    public void run() {
                        Player player = (Player) sender;
                        if (!player.hasPermission("CustomSprays.view")) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
                            return;
                        }

                        Player targetPlayer;
                        if (args.length <= 1) targetPlayer = player;
                        else targetPlayer = Bukkit.getPlayerExact(args[1]);
                        if (targetPlayer == null) {
                            sender.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_VIEW.NO_PLAYER")); return;
                        }
                        if (DataManager.data.getImageString(targetPlayer) == null) {
                            targetPlayer.sendMessage(CustomSprays.prefix + targetPlayer.getName() + " " + DataManager.getMsg(player, "COMMAND_VIEW.PLAYER_NO_IMAGE")); return;
                        }

                        try {
                            new Spray(player, DataManager.getImage(targetPlayer), Collections.singletonList(player)).create(40);
                            sender.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_VIEW.WARN"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTask(CustomSprays.instant);
                break;

            case "check":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(CustomSprays.prefix + "player only!");
                    return true;
                }
                Player player = (Player) sender;
                if (!player.hasPermission("CustomSprays.check")) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
                    return true;
                }

                new BukkitRunnable() {
                    public void run() {
                        Spray spray = SprayManager.getSpray(player);
                        if (spray != null) player.sendMessage(CustomSprays.prefix + "§7[" + spray.player.getName() + "§7]");
                        else player.sendMessage(CustomSprays.prefix + "§7[§8X§7]");
                    }
                }.runTask(CustomSprays.instant);
                break;

            default:
                sender.sendMessage(CustomSprays.prefix + DataManager.getMsg(sender, "UNKNOWN_COMMAND"));
        }
        return true;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> argList = new ArrayList<>();
            argList.add("upload");
            if (sender.hasPermission("CustomSprays.view")) argList.add("view");
            if (sender.hasPermission("CustomSprays.check")) argList.add("check");
            if (sender.isOp()) argList.add("reload");
            return getTabs(args[0], argList);
        } else {
            List<String> list = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
            return getTabs(args[1], list);
        }
    }


    public static List<String> getTabs(String input, List<String> tabs) {
        if (input == null || "".equals(input)) return tabs;
        ArrayList<String> list = new ArrayList<>();
        for (String s : tabs) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) list.add(s);
        }
        return list;
    }

}