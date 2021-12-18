package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;

public class Data {

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

    @SuppressWarnings("all")
    public static void saveImageString(Player player, String imageString) {
        if (!CustomSprays.instant.getConfig().getBoolean("use_MySQL")) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(CustomSprays.instant.pluginData);
            String uuid = player.getUniqueId().toString();
            config.set(uuid+".name", player.getName());
            config.set(uuid+".image", imageString);
            try {
                config.save(CustomSprays.instant.pluginData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection con = SprayUtils.getConnection(); PreparedStatement stat = con.prepareStatement("REPLACE INTO sprays(UUID,name,image) VALUES(?,?,?);")) {
                stat.setString(1, player.getUniqueId().toString());
                stat.setString(2, player.getName());
                stat.setString(3, imageString);
                stat.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("all")
    public static String getImageString(Player player) {
        if (!CustomSprays.instant.getConfig().getBoolean("use_MySQL")) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(CustomSprays.instant.pluginData);
            return config.getString(player.getUniqueId().toString()+".image");
        } else {
            try (Connection con = SprayUtils.getConnection(); Statement stat = con.createStatement()) {
                ResultSet resultSet = stat.executeQuery("SELECT image FROM sprays WHERE UUID = '"+player.getUniqueId().toString()+"';");
                if (resultSet.next()) return resultSet.getString("image");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static BufferedImage getImage(Player player) throws IOException {
        String string = getImageString(player);
        if (string != null) return ImageGetter.getBufferedImage(string);
        else return null;
    }


    /**
     * 创建不存在的SQL表
     */
    @SuppressWarnings("all")
    public static void createTableIfNotExist(Connection connection) {
        if (SprayUtils.checkConnectionIsNull(connection)) return;
        try(Statement stat = connection.createStatement()) {
            stat.executeUpdate("CREATE TABLE IF NOT EXISTS sprays(" +
                    "UUID varchar(64) not null , " +
                    "name varchar(64) not null , " +
                    "image mediumtext not null , " +
                    "primary key (UUID))");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
