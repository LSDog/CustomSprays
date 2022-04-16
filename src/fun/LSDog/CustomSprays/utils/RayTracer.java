package fun.LSDog.CustomSprays.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

import static java.lang.Math.abs;

/**
 * 射线追踪(?) <br>
 * 用法: <br>
 * <p>
 *     1. 获得4.5格内玩家指向的非空气方块: <br>
 *     new RayTracer(player.getEyeLocation.getDirection(), player.getEyeLocation(), 4.5).rayTraceBlock(block -> {return block.getType()!=Material.AIR});
 * </p>
 */
public class RayTracer {

    private final Location start;                   // 起始点
    private final Vector step;                      // 单位向量
    private final double stepX, stepY, stepZ;       // 单位向量坐标
    private final double max;                       // 最大距离
    private final int sbx, sby, sbz;                // 给 getFace() 用的解决向量方向计算什么的数, 坐标正数取0，负数取1
    private Location loc;                           // 当前点
    private double x, y, z, oldx, oldy, oldz;       // 当前点坐标 / 上一个点坐标
    private int bx, by, bz, oldbx, oldby, oldbz;    // 当前点所在格点 / 上一个点所在格点
    private double distance = 0;                    // 已追踪的距离

    private Block block = null;                     // 循环探测中取到的方块
    private BlockFace face;                         // 循环探测中指向的方块面
    private final BlockFace[] faces;                // 可能的方块面, 众所周知在现实生活中最多能看到方块的3个面，除非你带了什么魔镜
    private final World world;                      // 当前世界

    private static final double STEP_LONG = 1;      // 理论在(0,1]可用, 数字越大越快, 但是数字越小也不见得会越精准, 我的建议是不要改
    private static final double ACC = 0.0001;       // 还是不要乱动为好呢... (取值准确度, 过于准确会导致究极死亡无限glitch)

    /**
     *
     * @param direction 视线
     * @param startLocation 起始点
     * @param maxLong 最远距离
     */
    public RayTracer(Vector direction, Location startLocation, double maxLong) {
        this.start = startLocation.clone();
        this.step = direction.clone().normalize().multiply(STEP_LONG);
        this.stepX = this.step.getX();
        this.stepY = this.step.getY();
        this.stepZ = this.step.getZ();
        this.loc = start.clone();
        this.world = start.getWorld();
        this.max = maxLong;
        this.faces = new BlockFace[] {
                direction.getX() >= 0 ? BlockFace.WEST : BlockFace.EAST,
                direction.getY() >= 0 ? BlockFace.DOWN : BlockFace.UP,
                direction.getZ() >= 0 ? BlockFace.NORTH : BlockFace.SOUTH,
        };
        this.sbx = stepX > 0 ? 0 : 1;
        this.sby = stepY > 0 ? 0 : 1;
        this.sbz = stepZ > 0 ? 0 : 1;
    }

    /**
     * 重置所有数据，这样可以重复使用了
     */
    private void clearAll() {
        loc = start;
        distance = 0;
        block = null;
        face = null;
        getNewPoses();
        putPosToOld();
    }

    public BlockRayTraceResult rayTraceBlock(Predicate<Block> blockChecker) {

        // 让新旧数据都变成起始点
        getNewPoses();
        putPosToOld();

        while (distance <= max) {

            /* 获取新的坐标 */
            loc.add(step);
            distance += STEP_LONG;
            putPosToOld();
            getNewPoses();
            
            // 超出方块可放置位置 return null
            if (by > world.getMaxHeight() || by < 0) {
                clearAll();
                return null; 
            }

            /* 所在方块不变 */
            if (bx == oldbx && by == oldby && bz == oldbz) continue; // go go go

            /* 所在方块改变 */
            face = getFace(); // 计算率先接触的方块面
            double m = getAccDouble(getPoint()); // 计算射线和方块面的交点, 更新坐标
            loc.subtract(step.clone().multiply(m));
            distance -= m * STEP_LONG;
            getNewPoses();

            block = loc.getBlock(); // 获取射线接触的方块
            if (blockChecker.test(block)) { // 判断方块合乎要求
                if (distance > max) return null;
                BlockRayTraceResult result = new BlockRayTraceResult(loc.toVector(), block, face);
                clearAll(); // 测完重置一遍
                return result;
            }
        }

        return null; // 超过最大距离
    }


    /**
     * 判断方块面
     */
    private BlockFace getFace() {

        // 如果是正方向直接返回对应面
        if (abs(stepX) < 0.0001) {
            if (abs(stepY) < 0.0001) return faces[2];
            if (abs(stepZ) < 0.0001) return faces[1];
        } else {
            if (abs(stepY) < 0.0001 && abs(stepZ) < 0.0001) return faces[0];
        }
        
        double sx = (bx+sbx-oldx)/stepX, sy = (by+sby-oldy)/stepY, sz = (bz+sbz-oldz)/stepZ;
        // 用 在当前单位向量围出的框里的整数格点 的 相对位置, 判断单位向量率先交于方块的哪个面
        // 总之这回是看谁最小就选哪个轴对应的面

        if (bx != oldbx) {
            if (by != oldby) {
                if (bz != oldbz) {
                    /* x y z */ if (sx < sy && sx < sz) return faces[0]; else if (sy < sx && sy < sz) return faces[1]; else return faces[2];
                } else {
                    /* x y */ return (sx < sy) ? faces[0] : faces[1];
                }
            } else {
                if (bz != oldbz) {
                    /* x z */ return (sx < sz) ? faces[0] : faces[2];
                } else {
                    /* x */ return faces[0];
                }
            }
        } else {
            if (by != oldby) {
                if (bz != oldbz) {
                    /* y z */ return (sy < sz) ? faces[1] : faces[2];
                } else {
                    /* y */ return faces[1];
                }
            } else {
                /* z */ return faces[2];
            }
        }

    }
    
    /**
     * 计算打到面上的交点
     * @return 向量"走过站"的倍数 m, 即当前坐标需减去 m*步长 loc.subtract(step.clone().multiply(m))
     */
    private double getPoint() {
        switch (face) {
            case WEST: return (x-bx)/stepX; // dir↑ East
            case EAST: return (x-oldbx)/stepX; // dir↓ West
            case SOUTH: return (z-oldbz)/stepZ; // dir← North
            case NORTH: return (z-bz)/stepZ; // dir→ South
            case UP: return (y-oldby)/stepY;
            case DOWN: return (y-by)/stepY;
        }
        return 1; // ??but why, u should never touch here....
    }

    /**
     * 获取指定精度的 double <br>
     * 话说这么干其实依然会有精度差, but who cares if there's no bug
     */
    private double getAccDouble(double d) {
        return d - d % ACC;
    }

    private void getNewPoses() {
        x = loc.getX(); y = loc.getY(); z = loc.getZ();
        bx = (int) x; by = (int) y; bz = (int) z;
    }

    private void putPosToOld() {
        oldx = x; oldy = y; oldz = z;
        oldbx = bx; oldby = by; oldbz = bz;
    }

    public static class BlockRayTraceResult {
        public final Vector point;
        public final Block block;
        public final BlockFace blockFace;
        public BlockRayTraceResult(Vector point, Block block, BlockFace blockFace) {
            this.point = point;
            this.block = block;
            this.blockFace = blockFace;
        }
        public boolean isUpOrDown() {
            return (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN);
        }
        public Block getRelativeBlock() {
            return block.getRelative(blockFace);
        }
    }

}
