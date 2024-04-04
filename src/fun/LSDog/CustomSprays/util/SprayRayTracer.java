package fun.LSDog.CustomSprays.util;

import fun.LSDog.CustomSprays.spray.SprayBase;
import fun.LSDog.CustomSprays.spray.SprayManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class SprayRayTracer extends RayTracer {

    /**
     * 检测喷漆专用的射线追踪，因为只是给指令用，所以不要求性能了，随缘吧
     *
     * @param direction     视线
     * @param startLocation 起始点
     * @param maxLong       最远距离
     */
    public SprayRayTracer(Vector direction, Location startLocation, double maxLong) {
        super(direction, startLocation, maxLong);
    }

    /**
     * 检测视线中的喷漆
     * @param blockChecker 算遮挡方块
     */
    public SprayBase rayTraceSpray(Predicate<Block> blockChecker) {

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

            SprayBase spray = SprayManager.getSpray(block.getRelative(face), face); // 获取指向的可能存在的喷漆
            SprayBase backSpray = SprayManager.getSpray(block, face.getOppositeFace()); // 获取指向的可能存在的背对着视线的喷漆
            if (backSpray != null) return backSpray; // 背向视线的喷漆总是先被"指向"
            if (spray != null) return spray; // 如果没有就看看背向视角的
            if (blockChecker.test(block)) return null; // 如果是遮挡方块则返回null
        }

        return null; // 超过最大距离
    }

}
