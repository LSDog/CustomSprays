package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Objects;

@SuppressWarnings("all")
public class DataMySQL implements IData {

    @Override
    public int saveImageBytes(Player player, byte[] imgBytes) {
        byte[] data = DataManager.compressBytes(imgBytes);
        try (Connection con = getConnection(); PreparedStatement stat = Objects.requireNonNull(con).prepareStatement("UPDATE sprays set name=?, image=? WHERE UUID=?;")) {
            stat.setString(3, player.getUniqueId().toString());
            stat.setString(1, player.getName());
            stat.setBytes(2, data);
            stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data.length;
    }

    @Override
    public byte[] getImageBytes(Player player) {
        ResultSet resultSet = null;
        try (Connection con = getConnection(); Statement stat = Objects.requireNonNull(con).createStatement()) {
            resultSet = stat.executeQuery("SELECT image FROM sprays WHERE UUID = '"+player.getUniqueId().toString()+"';");
            if (resultSet != null && resultSet.next()) {
                return DataManager.decompressBytes(resultSet.getBytes("image"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void setCopyAllowed(Player player, boolean flag) {
        try (Connection con = getConnection(); PreparedStatement stat = Objects.requireNonNull(con).prepareStatement("UPDATE sprays SET name=?, allow_copy=? WHERE UUID=?;")) {
            stat.setString(3, player.getUniqueId().toString());
            stat.setString(1, player.getName());
            stat.setBoolean(2, flag);
            stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean getCopyAllowed(Player player) {
        ResultSet resultSet = null;
        try (Connection con = getConnection(); Statement stat = Objects.requireNonNull(con).createStatement()) {
            resultSet = stat.executeQuery("SELECT allow_copy FROM sprays WHERE UUID = '"+player.getUniqueId().toString()+"';");
            if (resultSet != null && resultSet.next()) return resultSet.getBoolean("allow_copy");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static void createTableIfNotExist() {
        try(Connection connection = getConnection(); Statement stat = Objects.requireNonNull(connection).createStatement()) {
            stat.executeUpdate("CREATE TABLE IF NOT EXISTS sprays(" +
                    "UUID varchar(64) NOT NULL , " +
                    "name varchar(64) NOT NULL , " +
                    "image MEDIUMBLOB DEFAULT null , " +
                    "allow_copy BOOLEAN NOT NULL DEFAULT 1 , " +
                    "primary key (UUID))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addAccountIfNotExist(Player player) {
        ResultSet resultSet = null;
        try (Connection con = getConnection(); Statement stat = Objects.requireNonNull(con).createStatement()) {
            resultSet = stat.executeQuery("SELECT name FROM sprays WHERE UUID = '"+player.getUniqueId().toString()+"';");
            if (resultSet != null && resultSet.next()) return; // 存在就不管了
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        // 添加账户
        try (Connection con = getConnection(); PreparedStatement stat = con.prepareStatement("INSERT sprays(UUID,name,allow_copy) VALUES(?,?,?);")) {
            stat.setString(1, player.getUniqueId().toString());
            stat.setString(2, player.getName());
            stat.setBoolean(3, true);
            stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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

}
