package fun.LSDog.CustomSprays.map;

import fun.LSDog.CustomSprays.CustomSprays;

/**
 * 获取一个 用作标识的 MapviewID <br>
 * 1.13 以前最多 32767
 */
public class MapViewId {

    private static int id = 0;

    private static int MAX = 32765;
    private static int MIN = 32467;

    public static int getId() {
        return (++id > MAX || id < MIN) ? (id = MIN) : id;
    }

    public static void setNumbers(int max, int min) {
        if (CustomSprays.getSubVer() < 13) {
            if (max > 32767 || max < -32768 || min > 32767 || min < -32768) return;
        }
        MAX = Math.max(max, min);
        MIN = Math.min(max, min);
    }

}