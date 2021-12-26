package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.utils.ImageGetter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class DataManager {

    public static IData data;
    public static boolean debug = true;
    public static boolean usePapi = false;

    public static String getMsg(Player player, String path) {
        String msg = CustomSprays.instant.getConfig().getString("Messages."+path);
        if (usePapi) {
            return PlaceholderAPI.setPlaceholders(player, msg);
        } else return msg;
    }

    public static String getMsg(CommandSender sender, String path) {
        if (sender instanceof Player) {
            getMsg((Player) sender, path);
        }
        return CustomSprays.instant.getConfig().getString("Messages."+path);
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

    public static BufferedImage getImage(Player player) throws IOException {
        String string = data.getImageString(player);
        if (string == null) return null;
        return ImageGetter.getBufferedImage(string);
    }

}
