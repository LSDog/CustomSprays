package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    public static double downloadLimit = 4;
    public static String urlRegex = "^https?://.*";
    public static List<String> disableWorlds = null;
    private static byte[] defaultImage = null;
    private static final File defaultImageFile = new File(CustomSprays.instant.getDataFolder() + File.separator + "default.yml");

    public static String getMsg(Player player, String path) {
        String msg = CustomSprays.instant.getConfig().getString("Messages."+path);
        if (usePapi) {
            return PlaceholderAPI.setPlaceholders(player, msg);
        } else return msg;
    }

    public static String getMsg(CommandSender sender, String path) {
        if (sender instanceof Player) {
            return getMsg((Player) sender, path);
        }
        return CustomSprays.instant.getConfig().getString("Messages."+path);
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

    @SuppressWarnings("deprecation")
    public static byte[] get128pxImageBytes(Player player) {

        byte[] bytes384 = data.getImageBytes(player);
        if (bytes384 == null || bytes384.length != 147456) {
            if (defaultImage != null) {
                bytes384 = defaultImage;
            } else {
                return null;
            }
        }

        BufferedImage image384 = new BufferedImage(384, 384, BufferedImage.TYPE_INT_ARGB);
        int[] ints384 = new int[384*384];
        for (int i = 0; i < 384*384; i++) {
            try {
                ints384[i] = bytes384[i]==0 ? 0 : MapPalette.getColor(bytes384[i]).getRGB();
            } catch (IndexOutOfBoundsException e) {
                /* cross-version fix */
                ints384[i] = bytes384[i]==0 ? 0 : MapPalette.getColor(MapPalette.matchColor(new Color(bytes384[i], true))).getRGB();
            }
        }
        image384.setRGB(0,0,384,384,ints384,0,384);

        BufferedImage image128 = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        image128.createGraphics().drawImage(image384, 0, 0, 128, 128, null);
        image128.getGraphics().dispose();
        int[] ints128 = new int[128*128];
        image128.getRGB(0, 0, 128, 128, ints128, 0, 128);

        byte[] bytes128 = new byte[128*128];
        for(int i = 0; i < 128*128; i++) {
            bytes128[i] = MapPalette.matchColor(new Color(ints128[i], true));
        }

        return bytes128;
    }

    public static int saveImageBytes(Player player, byte[] imgBytes) {
        return data.saveImageBytes(player, imgBytes);
    }

    public static void initialize(String method) {
        debug = CustomSprays.instant.getConfig().getBoolean("debug");
        downloadLimit = CustomSprays.instant.getConfig().getInt("download_limit");
        urlRegex = CustomSprays.instant.getConfig().getString("url_regex");
        usePapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        disableWorlds = CustomSprays.instant.getConfig().getStringList("disabled_world");
        CustomSprays.prefix = CustomSprays.instant.getConfig().getString("msg_prefix");
        switch (StorageMethod.getValue(method.toUpperCase())) {
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
                                Paths.get(CustomSprays.instant.getDataFolder() + File.separator + "default.yml")
                        ).get(0)
                ));
                CustomSprays.log("§7Default image loaded!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else defaultImage = null;
        CustomSprays.log("Data loaded!");
    }

    public enum StorageMethod {
        YML, MYSQL;
        public static StorageMethod getValue(String name) {
            try {
                return StorageMethod.valueOf(name);
            } catch (IllegalArgumentException | NullPointerException e) {
                CustomSprays.log("§c| 我们无从得知你的存储方法！设置为yml存储！");
                CustomSprays.log("§c| We couldn't find out your storage method, so we choose YML!");
                return YML;
            }
        }
    }



    public static byte[] compressBytes(byte[] bytes) {
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

    public static byte[] decompressBytes(byte[] bytes) {
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

}
