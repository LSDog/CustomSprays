package fun.LSDog.CustomSprays.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

/**
 * (方块)射线追踪(?) <br>
 * 用法: <br>
 * <p>
 *     1. 获得4.5格内玩家指向的非空气方块: <br>
 *     new RayTracer(player.getEyeLocation.getDirection(), player.getEyeLocation(), 4.5).rayTraceBlock(block -> {return block.getType()!=Material.AIR});
 * </p>
 * <br>
 * 另带一提: 这个 RayTracer 和 BlockIterator 的耗时就差了 0.3毫秒 左右，我觉得还是挺快的了，你看咱这个还能获取到方块面和交点呢 ε=ε=ε=(~￣▽￣)~
 */
public class RayTracer {

    final double stepX, stepY, stepZ;       // 单位向量坐标
    final double max;                       // 最大距离
    final int sbx, sby, sbz;                // 给 getFace() 用的解决向量方向计算什么的数, 坐标正数取0，负数取1
    final int worldMaxHeight;               // 世界最高高度
    final World world;                      // 当前世界

    double x, y, z;                         // 当前点坐标
    int bx, by, bz, oldbx, oldby, oldbz;    // 当前点所在格点 / 上一个点所在格点
    double distance = 0;                    // 已追踪的距离
    boolean isMultiCross = false;           // 代表一次前进是否跨越了多于一个的面
    BlockFace face;                         // 循环探测中指向的方块面
    final BlockFace[] faces;                // 可能的方块面, 众所周知在现实生活中最多能看到方块的3个面，除非你带了什么魔镜

    static final double STEP_LONG = 1;      // 理论在(0,1]可用, 数字越大越快, 但是数字越小也不见得会越精准, 我的建议是不要改
    static final double ACC = 0.00001;       // 还是不要乱动为好呢... (取值准确度, 过于准确会导致究极死亡无限glitch)


    /**
     * 射线追踪
     * @param direction 视线
     * @param startLocation 起始点
     * @param maxLong 最远距离
     */
    public RayTracer(Vector direction, Location startLocation, double maxLong) {
        this.max = maxLong;
        Location start = startLocation.clone();
        Vector step = direction.clone().normalize().multiply(STEP_LONG); // 算出单位向量, 然后赋值给 stepXYZ
        this.x = start.getX();
        this.y = start.getY();
        this.z = start.getZ();
        this.stepX = step.getX();
        this.stepY = step.getY();
        this.stepZ = step.getZ();
        this.world = start.getWorld();
        this.worldMaxHeight = world == null ? 256 : world.getMaxHeight();
        this.faces = new BlockFace[] {
                this.stepX >= 0 ? BlockFace.WEST : BlockFace.EAST,
                this.stepY >= 0 ? BlockFace.DOWN : BlockFace.UP,
                this.stepZ >= 0 ? BlockFace.NORTH : BlockFace.SOUTH,
        };
        this.sbx = stepX > 0 ? 0 : 1;
        this.sby = stepY > 0 ? 0 : 1;
        this.sbz = stepZ > 0 ? 0 : 1;
        getBlockPoses();
        putPosToOld();
    }

    /**
     * 开始追踪
     * @return 返回追踪结果见 {@link BlockRayTraceResult}
     */
    public BlockRayTraceResult rayTraceBlock(Predicate<Block> blockChecker) {

        // if (result != null) return result; // 复用时直接返回结果

        // 循环向前查找方块
        while (distance <= max) {

            putPosToOld();
            /* 获取新的坐标 */
            x += stepX; y += stepY; z += stepZ;
            distance += STEP_LONG;
            getBlockPoses();

            // 超出方块可放置位置 return null
            if (by > worldMaxHeight || by < 0) {
                return null;
            }

            /* 所在方块不变 */
            if (bx == oldbx && by == oldby && bz == oldbz) {
                continue; // 所在方块没有变, 下一个下一个
            }

            /* 所在方块改变 */
            face = getFace(); // 计算率先接触的方块面

            double m = getPointRedundantMultiples();  // 计算打到面上的交点与当前目标点的倍数差
            m = m - m % ACC;                          // m 降低准度, 目的是防止double计算误差时导致的无敌鬼畜问题
            if (m >= 1) m = 0;                        // 玄学校准, 别问我为什么, 我也不知道, 问就是精度差

            if (isMultiCross) {                                     // 若前进跨越两或三个面, 计算射线和方块面的交点, 然后更新坐标到交点上
                x -= stepX * m; y -= stepY * m; z -= stepZ * m;     // 将目标点减去m倍步长
                distance -= m * STEP_LONG;                          // 长度减去m倍步长
                getBlockPoses();                                    // 获取现在的方块点坐标
            }

            // 循环探测中取到的方块
            Block block = world.getBlockAt(bx, by, bz); // 获取目标点的方块

            if (blockChecker.test(block)) { // 判断方块合乎要求
                /* 比较起点到终点的长度是否超过max限制. 当 isMultiCross == false 时, 因为 distance 此时还未计算长度, 所以计算此时到交点的正确距离，然后再进行比较 */
                if (isMultiCross) {
                    if (distance > max) return null;  // 超过最大距离 寄
                } else {
                    double length = distance -= m * STEP_LONG; // 利用倍数差临时计算到交点的距离
                    if (distance > max) return null;  // 超过最大距离 寄
                    /* 现在就可以确定当前方块是符合要求的, 所以计算长度以及交点 */
                    distance = length;
                    x -= stepX * m; y -= stepY * m; z -= stepZ * m;
                }
                // 方块追踪结果
                return new BlockRayTraceResult(new Vector(x, y, z), block, face, distance);
            }
        }

        return null; // 超过最大距离
    }


    /**
     * 判断方块面
     */
    BlockFace getFace() {

        isMultiCross = false;

        // 如果是正方向直接返回对应面
        if (abs(stepX) < 0.0001) {
            if (abs(stepY) < 0.0001) return faces[2];
            if (abs(stepZ) < 0.0001) return faces[1];
        } else {
            if (abs(stepY) < 0.0001 && abs(stepZ) < 0.0001) return faces[0];
        }

        /*
        判断这次目标点前进时 跨过了哪个整数坐标:
        -   如果只有 bx 改变则表明这次前进仅仅跨越了一个x整数坐标, 也就是穿过了一个 y-z 整数面, 换句话说就是穿过了一个 West/East 面 (x方向对应west/east)
        -   如果 bx, by 都改变则表明这次一下跨越了x和y的整数坐标, 想象一下平面坐标系xOy中一个长1的线段正好跨过了一个整点1*1正方形的脚丫子, 直接穿到斜对面的正方形去了,
        这个时候就要判断这个线段究竟是先穿过的 x (y轴的某个整数平行线) 还是 y (x轴的某个整数平行线) 了
        -   如果 bx, by, bz 都改变了也是一样的, 判断先穿过的面就行了
        */

        // double sx = (bx+sbx-x)/stepX, sy = (by+sby-y)/stepY, sz = (bz+sbz-z)/stepZ;
        // 用 在当前单位向量围出的框里的整数格点 的 相对位置, 判断单位向量率先交于方块的哪个面
        // 总之是看谁最小就选哪个轴对应的面
        // 按需获取更快哦~
        if (bx != oldbx) {
            if (by != oldby) {
                isMultiCross = true;
                double sx = (bx+sbx-x)/stepX, sy = (by+sby-y)/stepY;
                if (bz != oldbz) {
                    double sz = (bz+sbz-z)/stepZ;
                    /* x y z */ if (sx < sy && sx < sz) return faces[0]; else if (sy < sx && sy < sz) return faces[1]; else return faces[2];
                } else {
                    /* x y */ return (sx < sy) ? faces[0] : faces[1];
                }
            } else {
                if (bz != oldbz) {
                    isMultiCross = true;
                    double sx = (bx+sbx-x)/stepX, sz = (bz+sbz-z)/stepZ;
                    /* x z */ return (sx < sz) ? faces[0] : faces[2];
                } else {
                    /* x */ return faces[0];
                }
            }
        } else {
            if (by != oldby) {
                if (bz != oldbz) {
                    isMultiCross = true;
                    double sy = (by+sby-y)/stepY, sz = (bz+sbz-z)/stepZ;
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
     * 计算打到面上的交点与当前目标点的倍数差
     * @return 向量"走过站"的倍数 m, 即当前坐标需减去 m * 步长
     */
    double getPointRedundantMultiples() {
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

    final void getBlockPoses() {
        bx = (int) Math.floor(x);
        by = (int) Math.floor(y);
        bz = (int) Math.floor(z);
    }

    final void putPosToOld() {
        oldbx = bx; oldby = by; oldbz = bz;
    }

    // from JDK lol
    public static double abs(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }

    /**
     * 方块追踪的结果
     */
    public static class BlockRayTraceResult {

        public final Vector point;          // 交点
        public final Block block;           // 方块
        public final BlockFace blockFace;   // 交点所在方块面
        public final double distance;       // 距离

        public BlockRayTraceResult(Vector point, Block block, BlockFace blockFace, double distance) {
            this.point = point;
            this.block = block;
            this.blockFace = blockFace;
            this.distance = distance;
        }

        /**
         * @return 方块面是否为上面或下面
         */
        public boolean isUpOrDown() {
            return (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN);
        }

        /**
         * 返回目标方块的上一个方块
         * @return 目标方块blockFace面向的那个相邻方块
         */
        public Block getRelativeBlock() {
            return block.getRelative(blockFace);
        }
    }

}
