package fun.LSDog.CustomSprays.data;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.util.ImageUtil;
import fun.LSDog.CustomSprays.util.MapColors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DataManager {

    public static IData data;
    public static boolean debug = true;
    public static boolean usePapi = false;
    public static boolean spray_particle = true;
    public static double downloadLimit = 4;
    public static String urlRegex = "^https?://.*";
    public static List<String> disableWorlds = null;
    private static byte[] defaultImage = null;
    private static final File defaultImageFile = new File(CustomSprays.plugin.getDataFolder() + File.separator + "default.yml");

    private static final Method PlaceholderAPI_setPlaceholders;

    static {
        Method method;
        try {
            Class<?> classPlaceholder = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            method = classPlaceholder.getMethod("setPlaceholders", Player.class, String.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            method = null;
        }
        PlaceholderAPI_setPlaceholders = method;
    }

    public static String getMsg(Player player, String path) {

        String msg = ChatColor.translateAlternateColorCodes('&', CustomSprays.plugin.getConfig().getString("Messages."+path));
        if (usePapi && PlaceholderAPI_setPlaceholders != null) {
            try {
                return (String) PlaceholderAPI_setPlaceholders.invoke(null, player, msg);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else return msg;
    }

    public static String getMsg(CommandSender sender, String path) {

        if (sender instanceof Player) {
            return getMsg((Player) sender, path);
        }
        return ChatColor.translateAlternateColorCodes('&', CustomSprays.plugin.getConfig().getString("Messages."+path));
    }

    public static byte[] getSizedImageBytes(Player player, int width, int height) {

        byte[] bytes384 = data.getImageBytes(player);
        if (bytes384 == null || bytes384.length != 147456) {
            if (defaultImage != null) {
                bytes384 = defaultImage;
            } else {
                return null;
            }
        }

        BufferedImage image384 = new BufferedImage(384, 384, BufferedImage.TYPE_INT_ARGB_PRE);
        Color[] mcColors = MapColors.getMcColors();
        int[] intColorArray = new int[384*384];
        for (int i = 0; i < bytes384.length; i++) {
            int index = bytes384[i];
            intColorArray[i] = mcColors[ index >= 0 ? index : index + 256 ].getRGB();
        }
        image384.setRGB(0, 0, 384, 384, intColorArray, 0, 384);

        BufferedImage imageSized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D graphics2D = imageSized.createGraphics();
        graphics2D.drawImage(image384, 0, 0, width, height, null);
        graphics2D.dispose();

        byte[] bytes;
        try {
            bytes = ImageUtil.getMcColorBytes(imageSized);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return bytes;
    }

    public static byte[] get384pxImageBytes(Player player) {

        byte[] bytes = data.getImageBytes(player);
        if (bytes != null && bytes.length == 147456) return bytes;
        if (defaultImage != null) {
            return defaultImage;
        } else {
            return null;
        }
    }

    public static byte[] get128pxImageBytes(Player player) {

        byte[] bytes384 = data.getImageBytes(player);
        if (bytes384 == null || bytes384.length != 147456) {
            if (defaultImage != null) {
                bytes384 = defaultImage;
            } else {
                return null;
            }
        }

        byte[] bytes128 = new byte[128*128];
        
        for (int i = 0; i < bytes128.length; i++) {
            bytes128[i] = bytes384[ (i % 128) * 3 + (i / 128) * 1152 + 385]; // 384*384 -> 128*128 缩小直接取9*9中间的那个像素
        }

        return bytes128;
    }

    public static int saveImageBytes(Player player, byte[] imgBytes) {
        return data.saveImageBytes(player, imgBytes);
    }

    public static void loadConfig(String storageMethod) {
        if (storageMethod == null) storageMethod = "YML";
        debug = CustomSprays.plugin.getConfig().getBoolean("debug");
        downloadLimit = CustomSprays.plugin.getConfig().getInt("download_limit");
        urlRegex = CustomSprays.plugin.getConfig().getString("url_regex");
        usePapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        spray_particle = CustomSprays.plugin.getConfig().getBoolean("spray_particle");
        disableWorlds = CustomSprays.plugin.getConfig().getStringList("disabled_world");
        CustomSprays.prefix = ChatColor.translateAlternateColorCodes('&', CustomSprays.plugin.getConfig().getString("msg_prefix"));
        switch (StorageMethod.getValue(storageMethod.toUpperCase())) {
            case MYSQL:
                CustomSprays.log("§8use [MYSQL]");
                DataMySQL.createTableIfNotExist();
                data = new DataMySQL();
                break;
            default:
            case YML:
                CustomSprays.log("§8use [YML]");
                try {
                    data = new DataYml();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (defaultImageFile.exists()) {
            try {
                defaultImage = DataManager.decompressBytes(Base64.getDecoder().decode(
                        Files.readAllLines(
                                Paths.get(CustomSprays.plugin.getDataFolder() + File.separator + "default.yml")
                        ).get(0)
                ));
                CustomSprays.log("§7Default image loaded!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else defaultImage = null;
    }

    static byte[] compressBytes(byte[] bytes) {
        if (bytes == null) return null;
        byte[] result = new byte[0];
        Deflater deflater = new Deflater();
        deflater.reset();
        deflater.setInput(bytes);
        deflater.finish();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            while (!deflater.finished()) {
                int i = deflater.deflate(buf);
                out.write(buf, 0, i);
            }
            result = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static byte[] decompressBytes(byte[] bytes) {
        if (bytes == null) return null;
        byte[] result = new byte[0];
        Inflater inflater = new Inflater();
        inflater.reset();
        inflater.setInput(bytes);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            while (!inflater.finished()) {
                int i = inflater.inflate(buf);
                out.write(buf, 0, i);
            }
            result = out.toByteArray();
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }
        inflater.end();
        return result;
    }

    enum StorageMethod {
        YML, MYSQL;
        public static StorageMethod getValue(String name) {
            try {
                return StorageMethod.valueOf(name);
            } catch (IllegalArgumentException | NullPointerException e) {
                CustomSprays.log("§c| 我们无从得知你的存储方法！设置为yml存储！");
                CustomSprays.log("§c| We couldn't find out your storage method, so use YML as default!");
                return YML;
            }
        }
    }

}
