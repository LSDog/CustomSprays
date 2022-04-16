package fun.LSDog.CustomSprays.map;

import fun.LSDog.CustomSprays.CustomSprays;

/**
 * 获取一个 用作标识的 MapviewID <br>
 * 保持在 32467 ~ 32765 (300张"缓存") <br>
 * 1.13+ 是 -2047483645 到 -2047483047
 */
public class MapViewId {

    private static short id = 0;

    private static short MAX = 32765;
    private static short MIN = 32467;

    public static short getId() {
        return (++id > MAX || id < MIN) ? (id = MIN) : id;
    }

    public static void setNumbers(int max, int min) {
        if (CustomSprays.getSubVer() < 13) {
            if (max > 32767 || max < -32768 || min > 32767 || min < -32768) return;
        }
        MAX = (short) Math.max(max, min);
        MIN = (short) Math.min(max, min);
    }

}