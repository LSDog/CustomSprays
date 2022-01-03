package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class RayTracer {

    private static Method rayTraceMethod = null;
    public static TargetBlock getTargetBlock(Player player) throws ReflectiveOperationException {

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

            if (rayTraceResult == null) return null;

            targetBlock = (Block) rayTraceResult.getClass().getMethod("getHitBlock").invoke(rayTraceResult);
            blockFace = (BlockFace) rayTraceResult.getClass().getMethod("getHitBlockFace").invoke(rayTraceResult);
            if (targetBlock == null || !targetBlock.getType().isSolid()) return null;

        }

        return new TargetBlock(targetBlock, blockFace);
    }


    private static BlockFace enumDirectionToBlockFace(Object notch) throws ReflectiveOperationException {
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

}
