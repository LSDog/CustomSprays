package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.utils.ImageGetter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DataManager {

    private static IData data;
    public static boolean debug = true;
    public static boolean usePapi = false;
    public static String urlRegex = "^https?://.*";

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
        return null;
    }

    @SuppressWarnings("deprecation")
    public static byte[] get128pxImageBytes(Player player) {

        byte[] bytes = data.getImageBytes(player);
        if (bytes == null || bytes.length != 147456) return null;

        BufferedImage bufferedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.createGraphics().drawImage(ImageGetter.getImageFromPixels(384,384, bytes), 0, 0, 128, 128, null);
        bufferedImage.getGraphics().dispose();

        int[] pixels = new int[128*128];
        bufferedImage.getRGB(0, 0, 128, 128, pixels, 0, 128);
        byte[] result = new byte[128*128];
        for(int i = 0; i < pixels.length; ++i) {
            result[i] = MapPalette.matchColor(new Color(pixels[i], true));
        }

        return result;
    }

    public static int saveImageBytes(Player player, byte[] imgBytes) {
        return data.saveImageBytes(player, imgBytes);
    }

    public static void initialize(String method) {
        switch (StorageMethod.getValue(method.toUpperCase())) {
            case MYSQL:
                CustomSprays.log("§8use [MYSQL]");
                DataMySQL.createTableIfNotExist();
                data = new DataMySQL();
                break;
            default:
            case YML:
                if (!CustomSprays.instant.pluginData.exists()) {
                    CustomSprays.instant.saveResource("imageData.yml", false);
                }
                CustomSprays.log("§8use [YML]");
                data = new DataYml();
        }
        debug = CustomSprays.instant.getConfig().getBoolean("debug");
        urlRegex = CustomSprays.instant.getConfig().getString("url_regex");
        usePapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
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
