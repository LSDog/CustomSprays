package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class SprayUtils {

    /**
     * 获取MySQL连接
     */
    public static Connection getConnection() {
        ConfigurationSection config = CustomSprays.instant.getConfig();
        Connection connection;
        String url = "jdbc:mysql://" + config.getString("MySQL.host") + ":" + config.getString("MySQL.port") + "/" + config.getString("MySQL.database") + "?useSSL=false?rewriteBatchedStatements=true";
        try {
            connection = DriverManager.getConnection(url, config.getString("MySQL.user"), config.getString("MySQL.password") );
        } catch (SQLException e) {
            CustomSprays.log("§c############################################");
            CustomSprays.log("§c==== 无法获取MySQL连接！ ====");
            CustomSprays.log("§c请检查你的数据库!");
            CustomSprays.log("§c############################################");
            e.printStackTrace();
            return null;
        }
        return connection;
    }

    public static boolean checkConnectionIsNull(Connection connection) {
        if (connection == null) {
            CustomSprays.log("无法获取SQL数据库连接！请检查你的配置！");
            return true;
        }
        return false;
    }

    public static boolean isURI(String str) {
        return str.toLowerCase().startsWith("http");
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static String version = null;
    private static String getVersion() {
        return version == null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
    }
    private static Method rayTraceMethod = null;
    public static Map.Entry<Block, BlockFace> getTargetBlock(Player player) throws Exception {
        Block targetBlock = player.getTargetBlock(null, CustomSprays.instant.getConfig().getInt("distance"));
        if (targetBlock == null || !targetBlock.getType().isSolid()) return null;
        Location playerLoc = player.getLocation().clone();

        Class<?> ClassVec3d = NMS.getMcVec3DClass();
        if (rayTraceMethod == null)
            rayTraceMethod = NMS.getMcWorldServerClass().getMethod("rayTrace", ClassVec3d, ClassVec3d, boolean.class);

        float f1 = playerLoc.getPitch();
        float f2 = playerLoc.getYaw();
        double d0 = playerLoc.getX();
        double d1 = playerLoc.getY() + player.getEyeHeight();
        double d2 = playerLoc.getZ();
        Object vec3d = ClassVec3d.getConstructor(double.class,double.class,double.class).newInstance(d0, d1, d2);

        double one = Math.PI/180;
        double f3 = Math.cos(-f2 * one - Math.PI);
        double f4 = Math.sin(-f2 * one - Math.PI);
        double f5 = -Math.cos(-f1 * one);
        double f6 = Math.sin(-f1 * one);
        double f7 = f4 * f5;
        double f8 = f3 * f5;
        double d3 = player.getGameMode() == GameMode.CREATIVE ? 5.0D : 4.5D;
        Object vec3d1 = ClassVec3d.getMethod("add", double.class,double.class,double.class).invoke(vec3d, f7 * d3, f6 * d3, f8 * d3);
        Object movingobjectposition =
                rayTraceMethod.invoke(
                        NMS.getMcWorldServer(playerLoc.getWorld()),
                        vec3d, vec3d1, false
                );

        if (movingobjectposition == null) return null;

        BlockFace blockFace = enumDirectionToBlockFace(NMS.getDeclaredField(movingobjectposition, "direction"));
        //Block voidBlock = targetBlock.getRelative(blockFace);
        return new AbstractMap.SimpleEntry<>(targetBlock, blockFace);
    }

    private static BlockFace enumDirectionToBlockFace(Object notch) throws Exception {
        if (notch == null) return BlockFace.SELF;
        String name = (String) Class.forName("net.minecraft.server."+getVersion()+".EnumDirection").getMethod("name").invoke(notch);
        switch (name) {
            case "DOWN": return BlockFace.DOWN;
            case "UP": return BlockFace.UP;
            case "NORTH": return BlockFace.NORTH;
            case "SOUTH": return BlockFace.SOUTH;
            case "WEST": return BlockFace.WEST;
            case "EAST": return BlockFace.EAST;
            default: return BlockFace.SELF;
        }
    }

    private static Map<String, Object> enumDirectionMap = null;
    public static Object blockFaceToEnumDirection(BlockFace blockFace) throws Exception {
        if (enumDirectionMap == null) {
            enumDirectionMap = new HashMap<>();
            Object[] enums = Class.forName("net.minecraft.server."+getVersion()+".EnumDirection").getEnumConstants();
            for (Object o : enums) {
                enumDirectionMap.put((String) o.getClass().getMethod("getName").invoke(o), o);
            }
        }
        switch (blockFace) {
            case DOWN: return enumDirectionMap.get("down");
            case UP: return enumDirectionMap.get("up");
            case NORTH: return enumDirectionMap.get("north");
            case SOUTH: return enumDirectionMap.get("south");
            case WEST: return enumDirectionMap.get("west");
            case EAST: return enumDirectionMap.get("east");
            default: return BlockFace.SELF;
        }
    }

    public static int getYawFromPositiveBlockFace(BlockFace face) {
        switch (face) {
            case SOUTH: return 0;
            case WEST: return 90;
            case NORTH: return 180;
            case EAST: return 270;
        }
        return 0;
    }

}
