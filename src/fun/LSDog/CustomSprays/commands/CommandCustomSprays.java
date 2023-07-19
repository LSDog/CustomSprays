package fun.LSDog.CustomSprays.commands;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.spray.MapFrameFactory;
import fun.LSDog.CustomSprays.spray.SprayBase;
import fun.LSDog.CustomSprays.spray.SprayManager;
import fun.LSDog.CustomSprays.utils.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class CommandCustomSprays implements TabExecutor {

    private static final Set<UUID> uploadingSet = new HashSet<>();

    public static List<String> getTabs(String input, List<String> tabs) {
        if (input == null || "".equals(input)) return tabs;
        ArrayList<String> list = new ArrayList<>();
        for (String s : tabs)
            if (s.toLowerCase(Locale.ENGLISH).startsWith(input.toLowerCase(Locale.ENGLISH))) list.add(s);
        return list;
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = CustomSprays.instance.getConfig();
        if (args.length == 0) {
            sender.sendMessage(CustomSprays.prefix + "§8v" + CustomSprays.instance.getDescription().getVersion() + "§r" +
                    "\n    §b/sprays§r §3upload§l <url> §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.UPLOAD") +
                    (sender.hasPermission("CustomSprays.copy") ? "\n    §b/sprays§r §3copy§l <player> §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.COPY") : "") +
                    (sender.hasPermission("CustomSprays.view") ? "\n    §b/sprays§r §3view§l [player] §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.VIEW") : "") +
                    (sender.hasPermission("CustomSprays.check") ? "\n    §b/sprays§r §3check §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.CHECK") : "") +
                    (sender.hasPermission("CustomSprays.delete") ? "\n    §b/sprays§r §3delete §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.DELETE") : "") +
                    (sender.hasPermission("CustomSprays.getitem") ? "\n    §b/sprays§r §3getitem §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.GETITEM") : "") +
                    (sender.isOp() ? "\n    §b/sprays§r §3reload§l §r§7- " + DataManager.getMsg(sender, "COMMAND_HELP.RELOAD") : "") +
                    "\n\n\n  " +  DataManager.getMsg(sender, "COMMAND_HELP.TIP"));
            return true;
        }

        Player player = sender instanceof Player ? (Player) sender : null;

        switch (args[0].toLowerCase(Locale.ENGLISH)) {

            case "reload":
                if (!sender.isOp()) {
                    sender.sendMessage(CustomSprays.prefix + DataManager.getMsg(sender, "NO_PERMISSION"));
                    return true;
                }
                SprayManager.removeAllSpray();
                CustomSprays.instance.reloadConfig();
                DataManager.initialize(CustomSprays.instance.getConfig().getString("storage"));
                CoolDown.reset();
                RegionChecker.reload();
                VaultChecker.reload();
                Bukkit.getScheduler().getActiveWorkers().forEach(bukkitWorker -> {
                    if (bukkitWorker.getOwner().getName().equals("CustomSprays")) //noinspection deprecation
                        bukkitWorker.getThread().stop();
                });
                sender.sendMessage(CustomSprays.prefix + "OK!");
                break;


            case "upload":
                if (player == null) { sender.sendMessage(CustomSprays.prefix + "player only!"); return true; }
                new BukkitRunnable() {
                    public void run() {

                        Player player = (Player) sender;
                        if (uploadingSet.contains(player.getUniqueId())) {
                            player.sendMessage(CustomSprays.prefix + ". . . . . .");
                            return;
                        }
                        if ( !player.hasPermission("CustomSprays.noCD") && CoolDown.isUploadCooling(player) ) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING") + " §7("+ CoolDown.getUploadCool(player)+")");
                            uploadingSet.remove(player.getUniqueId()); return;
                        }

                        uploadingSet.add(player.getUniqueId());
                        /* 上传失败了就缩短冷却时间，所谓人性化是也~~ */
                        CoolDown.setUploadCooldown(player, CustomSprays.instance.getConfig().getDouble("upload_failed_cooldown_multiple"));

                        if (args.length == 1) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.NO_URL"));
                            uploadingSet.remove(player.getUniqueId()); return;
                        }
                        String url = args[1];
                        if (!Pattern.compile(StringEscapeUtils.escapeJava(DataManager.urlRegex)).matcher(url.toLowerCase(Locale.ENGLISH)).matches()) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.NOT_URL"));
                            uploadingSet.remove(player.getUniqueId()); return;
                        }
                        ImageDownloader imageDownloader;
                        try {
                            imageDownloader = new ImageDownloader(url);
                        } catch (ImageDownloader.TooManyDownloadException e) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.IN_BUSY"));
                            uploadingSet.remove(player.getUniqueId()); return;
                        }
                        player.sendMessage(CustomSprays.prefix + "§7♦ ......");
                        byte result = imageDownloader.checkImage();
                        if (result != 0) {
                            if (result == 1) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.CONNECT_FAILED"));
                            if (result == 2) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.CONNECT_FAILED")+"\n"+CustomSprays.prefix+ DataManager.getMsg(player, "COMMAND_UPLOAD.CONNECT_HTTPS_FAILED"));
                            if (result == 3) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.FILE_TOO_BIG").replace("{size}", imageDownloader.size+"").replace("{limit}", config.getDouble("file_size_limit")+""));
                            if (result == 4) player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.CANT_GET_SIZE"));
                            imageDownloader.close();
                            uploadingSet.remove(player.getUniqueId()); return;
                        }
                        player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.UPLOADING"));
                        BufferedImage image = imageDownloader.getBufferedImage();
                        byte[] imgBytes;
                        try {
                            imgBytes = ImageUtil.getMcColorBytes(ImageUtil.resizeImage(image, 384, 384));
                        } catch (IllegalArgumentException | IOException e) {
                            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.FAILED_GET_IMAGE"));
                            imageDownloader.close();
                            uploadingSet.remove(player.getUniqueId()); return;
                        }
                        int size = DataManager.saveImageBytes(player, imgBytes);
                        /* 上传成功了就用原冷却时间，所谓人性化是也~~ */
                        CoolDown.setUploadCooldown(player, 1);
                        CustomSprays.debug("§f§l" + player.getName() + "§b upload §7->§r (§e§l"+ imageDownloader.size+"k§7>>§e§l"+size/1024+"k§r) " + url);
                        player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_UPLOAD.OK"));
                        imageDownloader.close();
                        uploadingSet.remove(player.getUniqueId());
                    }
                }.runTaskAsynchronously(CustomSprays.instance);
                break;

            case "copy":
                if (player == null) { sender.sendMessage(CustomSprays.prefix + "player only!"); return true; }
                new BukkitRunnable() {
                    public void run() {

                        Player player = (Player) sender;
                        if (args.length < 2) {
                            sender.sendMessage(CustomSprays.prefix + "\n" + DataManager.getMsg(sender, "COMMAND_COPY.HELP"));
                        } else {
                            String action = args[1];
                            if (action.equalsIgnoreCase("o")) {
                                DataManager.data.setCopyAllowed(player, true);
                                player.sendMessage(CustomSprays.prefix + " o §2✔");
                            } else if (action.equalsIgnoreCase("x")) {
                                DataManager.data.setCopyAllowed(player, false);
                                player.sendMessage(CustomSprays.prefix + " x §c✘");
                            } else {
                                if (!player.hasPermission("CustomSprays.copy")) {
                                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
                                    return;
                                }
                                Player target = Bukkit.getPlayerExact(action);
                                if (target == null) {
                                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_COPY.NO_PLAYER"));
                                    return;
                                }
                                if (player.getName().equals(target.getName())) {
                                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_COPY.COPY_SELF"));
                                    return;
                                }
                                boolean allow = DataManager.data.getCopyAllowed(target);
                                if (!player.isOp() && !allow) {
                                    player.sendMessage(CustomSprays.prefix + target.getName() + DataManager.getMsg(player, "COMMAND_COPY.NOT_ALLOW"));
                                    return;
                                }
                                if ( !player.hasPermission("CustomSprays.noCD") && CoolDown.isUploadCooling(player) ) {
                                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING") + " §7("+ CoolDown.getUploadCool(player)+")");
                                    uploadingSet.remove(player.getUniqueId()); return;
                                }
                                byte[] data = DataManager.data.getImageBytes(target);
                                if (data == null) {
                                    player.sendMessage(CustomSprays.prefix + target.getName() + DataManager.getMsg(player, "COMMAND_COPY.PLAYER_NO_IMAGE"));
                                    return;
                                }
                                DataManager.saveImageBytes(player, data);
                                CoolDown.setUploadCooldown(player, CustomSprays.instance.getConfig().getDouble("copy_cooldown_multiple"));
                                sender.sendMessage(CustomSprays.prefix + "OK!" + (player.isOp()&&!allow?" §7§l(OP-bypass)":"") );
                            }
                        }
                    }
                }.runTaskAsynchronously(CustomSprays.instance);
                break;

            case "view":
                if (player == null) { sender.sendMessage(CustomSprays.prefix + "player only!"); return true; }
                if (!player.hasPermission("CustomSprays.view")) { player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION")); return true; }
                new BukkitRunnable() {
                    public void run() {

                        Player targetPlayer;
                        if (args.length == 1) {
                            targetPlayer = player;
                        } else {
                            targetPlayer = Bukkit.getPlayerExact(args[1]);
                        }
                        if (targetPlayer == null) {
                            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                                if (args[1].equalsIgnoreCase(offlinePlayer.getName())) {
                                    targetPlayer = (Player) offlinePlayer;
                                    break;
                                }
                            }
                        }
                        if (targetPlayer == null) {
                            sender.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_VIEW.NO_PLAYER")); return;
                        }
                        byte[] imageBytes = DataManager.get128pxImageBytes(targetPlayer);
                        if (imageBytes == null) {
                            targetPlayer.sendMessage(CustomSprays.prefix + targetPlayer.getName() + " " + DataManager.getMsg(player, "COMMAND_VIEW.PLAYER_NO_IMAGE")); return;
                        }
                        // check image by showing item
                        short id = 0;
                        try {
                            if (NMS.getSubVer() < 17) {
                                NMS.sendPacket(player, NMS.getPacketClass("PacketPlayOutSetSlot")
                                        .getConstructor(int.class, int.class, NMS.getMcItemStackClass())
                                        .newInstance(0,36+player.getInventory().getHeldItemSlot(), MapFrameFactory.getMcMap(id)));
                            } else {
                                NMS.sendPacket(player, NMS.getPacketClass("PacketPlayOutSetSlot")
                                        .getConstructor(int.class, int.class, int.class, NMS.getMcItemStackClass())
                                        .newInstance(0,0,36+player.getInventory().getHeldItemSlot(), MapFrameFactory.getMcMap(id)));
                            }
                            NMS.sendPacket(player, MapFrameFactory.getMapPacket(id, imageBytes));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Bukkit.getScheduler().runTaskLater(CustomSprays.instance, () -> {
                            player.updateInventory();
                            MapView mapView = NMS.getSubVer() > 12 ? Bukkit.getMap(id) : null;
                            if (mapView == null) {
                                mapView = Bukkit.createMap(player.getWorld());
                            }
                            player.sendMap(mapView);
                        }, 30);
                        sender.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_VIEW.WARN"));
                    }
                }.runTask(CustomSprays.instance);
                break;

            case "check":
                if (player == null) { sender.sendMessage(CustomSprays.prefix + "player only!"); return true; }
                if (!player.hasPermission("CustomSprays.check")) { player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION")); return true; }

                new BukkitRunnable() {
                    public void run() {
                        SprayBase spray = SprayManager.getSprayInSight(player);
                        if (spray != null) player.sendMessage(CustomSprays.prefix + "§7[" + spray.player.getName() + "§7]");
                        else player.sendMessage(CustomSprays.prefix + "§7[§8X§7]");
                    }
                }.runTaskAsynchronously(CustomSprays.instance);
                break;

            case "delete":
                if (player == null) { sender.sendMessage(CustomSprays.prefix + "player only!"); return true; }
                if (!player.hasPermission("CustomSprays.delete")) { player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION")); return true; }

                new BukkitRunnable() {
                    public void run() {
                        SprayBase spray = SprayManager.getSprayInSight(player);
                        if (spray != null) {
                            player.sendMessage(CustomSprays.prefix + "§7[" + spray.player.getName() + "§7]");
                            spray.remove();
                        }
                        else player.sendMessage(CustomSprays.prefix + "§7[§8X§7]");
                    }
                }.runTaskAsynchronously(CustomSprays.instance);
                break;

            case "getitem":
                if (player == null) { sender.sendMessage(CustomSprays.prefix + "player only!"); return true; }
                if (!player.hasPermission("CustomSprays.check")) { player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION")); return true; }

                int useTime = 0;
                if (args.length >= 2) {
                    try {
                        useTime = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_GETITEM.BAD_NUM"));
                        return true;
                    }
                    if (useTime <= 0) {
                        player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "COMMAND_GETITEM.BAD_NUM"));
                        return true;
                    }
                }
                Material material = Material.matchMaterial(CustomSprays.instance.getConfig().getString("spray_item"));
                ItemStack item = new ItemStack(material);
                ItemMeta itemMeta = item.getItemMeta();
                String loreText = CustomSprays.instance.getConfig().getString("spray_item_lore");
                String loreTimesUse = CustomSprays.instance.getConfig().getString("spray_item_lore_times_use");
                List<String> lore = new ArrayList<>();
                if (loreText != null) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', loreText));
                    lore.add("");
                }
                if (loreTimesUse != null) lore.add(
                        ChatColor.translateAlternateColorCodes('&', loreTimesUse) +
                                (useTime > 0 ? useTime : DataManager.getMsg(player, "INFINITE"))
                );
                itemMeta.setLore(lore);
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', CustomSprays.instance.getConfig().getString("spray_item_name")));
                item.setItemMeta(itemMeta);
                player.getInventory().addItem(item);
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
            if (sender.hasPermission("CustomSprays.copy")) argList.add("copy");
            if (sender.hasPermission("CustomSprays.view")) argList.add("view");
            if (sender.hasPermission("CustomSprays.check")) argList.add("check");
            if (sender.hasPermission("CustomSprays.delete")) argList.add("delete");
            if (sender.hasPermission("CustomSprays.getitem")) argList.add("getitem");
            if (sender.isOp()) argList.add("reload");
            return getTabs(args[0], argList);
        } else {
            List<String> list = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
            return getTabs(args[1], list);
        }
    }

}