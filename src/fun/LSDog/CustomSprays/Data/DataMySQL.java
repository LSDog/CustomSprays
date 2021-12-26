package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.*;

public class DataMySQL implements IData {

    @Override
    @SuppressWarnings("all")
    public void saveImageString(Player player, String imageString) {
        try (Connection con = getConnection(); PreparedStatement stat = con.prepareStatement("REPLACE INTO sprays(UUID,name,image) VALUES(?,?,?);")) {
            stat.setString(1, player.getUniqueId().toString());
            stat.setString(2, player.getName());
            stat.setString(3, imageString);
            stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("all")
    public String getImageString(Player player) {
        try (Connection con = getConnection(); Statement stat = con.createStatement()) {
            ResultSet resultSet = stat.executeQuery("SELECT image FROM sprays WHERE UUID = '"+player.getUniqueId().toString()+"';");
            if (resultSet.next()) return resultSet.getString("image");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("all")
    public static void createTableIfNotExist() {
        Connection connection = getConnection();
        if (checkConnectionIsNull(connection)) return;
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
    
    /**
     * 获取MySQL连接
     */
    private static Connection getConnection() {
        ConfigurationSection config = CustomSprays.instant.getConfig();
        Connection connection;
        String url = "jdbc:mysql://" + config.getString("MySQL.host") + ":" + config.getString("MySQL.port") + "/" + config.getString("MySQL.database") + "?useSSL=false&rewriteBatchedStatements=true";
        try {
            connection = DriverManager.getConnection(url, config.getString("MySQL.user"), config.getString("MySQL.password") );
        } catch (SQLException e) {
            CustomSprays.log("\n\n\n\n");
            CustomSprays.log("§c############################################");
            CustomSprays.log("§c==== 无法获取MySQL连接！ ====");
            CustomSprays.log("§c==== We cant get your SQL connection! ====");
            CustomSprays.log("§c############################################");
            e.printStackTrace();
            CustomSprays.log("\n\n\n\n");
            Bukkit.shutdown();
            return null;
        }
        return connection;
    }

    private static boolean checkConnectionIsNull(Connection connection) {
        if (connection == null) {
            CustomSprays.log("无法获取SQL数据库连接！请检查你的配置！| We cant get your SQL connection! Please check your config!");
            return true;
        }
        return false;
    }

}
