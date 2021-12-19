package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
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

    public static boolean checkConnectionIsNull(Connection connection) {
        if (connection == null) {
            CustomSprays.log("无法获取SQL数据库连接！请检查你的配置！| We cant get your SQL connection! Please check your config!");
            return true;
        }
        return false;
    }

    public static boolean isURI(String str) {
        return str.toLowerCase().startsWith("http");
    }



    private static Method rayTraceMethod = null;
    public static Map.Entry<Block, BlockFace> getTargetBlock(Player player) throws Exception {

        double distance = CustomSprays.instant.getConfig().getDouble("distance");
        Location playerLoc = player.getLocation().clone();

        Block targetBlock = player.getTargetBlock(null, (int) Math.ceil(distance));
        BlockFace blockFace;

        if (CustomSprays.getSubVer() < 13) {

            float pitch = playerLoc.getPitch();
            float yaw = playerLoc.getYaw();
            double x = playerLoc.getX();
            double y = playerLoc.getY() + player.getEyeHeight();
            double z = playerLoc.getZ();

            double one = Math.PI/180;
            double f0 = -Math.cos(-pitch * one);
            double vy = Math.sin(-pitch * one);
            double vx = Math.sin(-yaw * one - Math.PI) * f0;
            double vz = Math.cos(-yaw * one - Math.PI) * f0;

            Class<?> ClassVec3d = NMS.getMcVec3DClass();
            if (rayTraceMethod == null) rayTraceMethod = NMS.getMcWorldServerClass().getMethod("rayTrace", ClassVec3d, ClassVec3d, boolean.class);

            Object vec3d = ClassVec3d.getConstructor(double.class,double.class,double.class).newInstance(x, y, z);
            Object vec3d1 = ClassVec3d.getMethod("add", double.class,double.class,double.class).invoke(vec3d, vx * distance, vy * distance, vz * distance);
            Object movingObjectPosition = rayTraceMethod.invoke(NMS.getMcWorldServer(playerLoc.getWorld()), vec3d, vec3d1, true);
            if (movingObjectPosition == null) return null;

            blockFace = enumDirectionToBlockFace(NMS.getDeclaredField(movingObjectPosition, "direction"));
            if (blockFace == null) return null;

        } else {

            Object rayTraceResult = player.getClass()
                    .getMethod("rayTraceBlocks", double.class, NMS.getFluidCollisionModeClass())
                    .invoke(player, distance, NMS.getFluidCollisionModeClass("NEVER"));

            targetBlock = (Block) rayTraceResult.getClass().getMethod("getHitBlock").invoke(rayTraceResult);
            blockFace = (BlockFace) rayTraceResult.getClass().getMethod("getHitBlockFace").invoke(rayTraceResult);
            if (targetBlock == null || !targetBlock.getType().isSolid()) return null;

        }

        CustomSprays.debug(new AbstractMap.SimpleEntry<>(targetBlock, blockFace).toString());

        return new AbstractMap.SimpleEntry<>(targetBlock, blockFace);
    }


    private static BlockFace enumDirectionToBlockFace(Object notch) throws Exception {
        if (notch == null) return BlockFace.SELF;
        String name = (String) Class.forName("net.minecraft.server."+CustomSprays.getMcVer()+".EnumDirection").getMethod("name").invoke(notch);
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
            Object[] enums = Class.forName("net.minecraft.server."+CustomSprays.getMcVer()+".EnumDirection").getEnumConstants();
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
