package fun.LSDog.CustomSprays.map;

import fun.LSDog.CustomSprays.util.NMS;

/**
 * 获取一个 用作标识的 MapviewID <br>
 * 1.13 以前最多 32767
 */
public class MapViewId {

    private static int id = 0;

    private static int MAX = 32765;
    private static int MIN = 32467;

    /**
     * /sprays view 所使用的id
     */
    public static int sprayViewId = 0;

    public static int getId() {
        return (++id > MAX || id < MIN) ? (id = MIN) : id;
    }

    public static void setIdRange(int max, int min) {
        if (NMS.getSubVer() < 13) {
            if (max > 32767 || max < -32768 || min > 32767 || min < -32768) return;
        }
        MAX = Math.max(max, min);
        MIN = Math.min(max, min);
    }

}