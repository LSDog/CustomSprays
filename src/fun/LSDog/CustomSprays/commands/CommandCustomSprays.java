package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.ImageGetter;
import fun.LSDog.CustomSprays.MapGetter;
import fun.LSDog.CustomSprays.manager.CoolDownManager;
import fun.LSDog.CustomSprays.utils.Data;
import fun.LSDog.CustomSprays.utils.SprayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandCustomSprays implements TabExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        FileConfiguration config = CustomSprays.instant.getConfig();
        if (args.length == 0) {
            sender.sendMessage(CustomSprays.prefix +
                    "\n        §c/customsprays§r §3upload§l <url> §r§7- 上传图片链接 (尺寸§c建议为128*128px§r)" +
                    "\n        §c/customsprays§r §3view§l [player] §r§7- 查看玩家的自定义喷漆" +
                    "\n        §7TIP: §7§l\"在线图床 和 在线压缩图片 很有用哦~\"");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.isOp()) {
                    sender.sendMessage(CustomSprays.prefix + "§c无权限！");
                    return true;
                }
                CustomSprays.instant.reloadConfig();
                CustomSprays.prefix = CustomSprays.instant.getConfig().getString("custom_prefix");
                sender.sendMessage(CustomSprays.prefix + "重载成功!");
                break;


            case "upload":
                new BukkitRunnable() {
                    public void run() {
                        if (!(sender instanceof Player)) { sender.sendMessage(CustomSprays.prefix + "player only!");return; }
                        Player player = (Player) sender;
                        if ((!player.isOp() || !player.hasPermission("CustomSprays.nocooldown")) && CoolDownManager.isUploadCooling(player)) {
                            player.sendMessage(CustomSprays.prefix + "§c冷却中! §7("+CoolDownManager.getUploadCool(player)+")");
                            return;
                        }
                        CoolDownManager.addUploadCooldown(player);

                        if (args.length == 1) { player.sendMessage(CustomSprays.prefix + "你忘了写图片的地址了！ 笨蛋！！");return; }
                        String url = args[1];
                        if (!SprayUtils.isURI(url)) { player.sendMessage(CustomSprays.prefix + "你给的URL完全无效啊！ 笨蛋！！！！");return; }
                        ImageGetter imageGetter = new ImageGetter(url);
                        byte result = imageGetter.checkImage();
                        if (result != 0) {
                            if (result == 1) player.sendMessage(CustomSprays.prefix + "url连接失败了！ 可恶！！！");
                            if (result == 2) player.sendMessage(CustomSprays.prefix + "url连接失败了！\n"+CustomSprays.prefix+"如果你提供的是§e§l https§r 那么可以改成§e§l http§r 链接尝试！");
                            if (result == 3) player.sendMessage(CustomSprays.prefix + "你提供的文件太大了！ 有足足"+imageGetter.size+"K！ §7(最大可以上传 §f§l"+config.getInt("file_size_limit")+"k§r§7 的图片！)");
                            if (result == 4) player.sendMessage(CustomSprays.prefix + "我们无法获取文件的大小！请换一个以图片后缀§7(.png/.jpg)的url试试吧！");
                            return;
                        }
                        player.sendMessage(CustomSprays.prefix + "§7加载中... 请稍候......");
                        imageGetter.getBufferedImage();
                        // Debug: 保存下载的图片
                        // imageGetter.saveToFile(new File(CustomSprays.instant.getDataFolder() + "\\" + "imageTemp.png"));
                        String imageStr = null;
                        try {
                            imageStr = imageGetter.GetImageStr();
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(CustomSprays.prefix + "获取文件失败！ 可恶！！！");
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        CustomSprays.debug("§4§l" + player.getName() + "§r uploaded spray §7->§r (§e§l"+imageGetter.size+" K§r) " + url);
                        Data.saveImageString(player, imageStr);
                        player.sendMessage(CustomSprays.prefix + "喷图案保存成功！");
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
                        if ((!player.isOp() || !player.hasPermission("CustomSprays.nocooldown")) && CoolDownManager.isSprayCooling(player)) {
                            player.sendMessage(CustomSprays.prefix + "§c冷却中! §7("+CoolDownManager.getSprayCool(player)+")");
                            return;
                        }
                        CoolDownManager.addSprayCooldown(player);

                        if (args.length <= 1 && !player.hasPermission("CustomSprays.viewself")) {
                            player.sendMessage(CustomSprays.prefix + "§c无权限！");
                            return;
                        }
                        if (args.length > 1 && !player.hasPermission("CustomSprays.viewothers")) {
                            player.sendMessage(CustomSprays.prefix + "§c无权限！");
                            return;
                        }

                        Player targetPlayer;
                        if (args.length <= 1) targetPlayer = player;
                        else targetPlayer = Bukkit.getPlayerExact(args[1]);
                        if (targetPlayer == null) {
                            sender.sendMessage(CustomSprays.prefix + "查无此人啦...");
                            return;
                        }
                        if (Data.getImageString(targetPlayer.getUniqueId()) == null) { targetPlayer.sendMessage(CustomSprays.prefix + targetPlayer.getName() + " 还没有上传图片呢");return; }
                        try {
                            targetPlayer.getInventory().addItem(
                                    MapGetter.getMap(MapGetter.getMapView(
                                            ImageGetter.getBufferedImage(Data.getImageString(targetPlayer.getUniqueId()))
                                    ))
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTask(CustomSprays.instant);
                return true;
        }
        return true;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getTabs(args[0], "upload", "view", "reload");
        } else {
            List<String> list = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
            return getTabs(args[1], list.toArray(new String[0]));
        }
    }


    public static List<String> getTabs(String input, String... tabs) {
        if (input == null || "".equals(input)) return Arrays.asList(tabs);
        ArrayList<String> list = new ArrayList<>();
        for (String s : tabs) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) list.add(s);
        }
        return list;
    }

}
