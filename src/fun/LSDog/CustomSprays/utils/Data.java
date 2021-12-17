package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.ImageGetter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class Data {

    public enum SaveType {
        YML, MYSQL, SQLITE
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
            Connection con = SprayUtils.getConnection();
            if (SprayUtils.checkConnectionIsNull(con)) return;
            try (PreparedStatement stat = con.prepareStatement("REPLACE INTO profile(UUID,name,image) VALUES(?,?,?)")) {
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
    public static String getImageString(UUID uuid) {
        if (!CustomSprays.instant.getConfig().getBoolean("use_MySQL")) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(CustomSprays.instant.pluginData);
            return config.getString(uuid.toString()+".image");
        } else {
            Connection con = SprayUtils.getConnection();
            if (SprayUtils.checkConnectionIsNull(con)) return "";
            try (PreparedStatement stat = con.prepareStatement("SELECT image FROM sprays WHERE uuid=?")) {
                stat.setString(1, uuid.toString());
                ResultSet resultSet = stat.executeQuery();
                return resultSet.getString(1);
            } catch (SQLException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    public static BufferedImage getImage(UUID uuid) throws IOException {
        return ImageGetter.getBufferedImage(getImageString(uuid));
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
        }
    }

}
