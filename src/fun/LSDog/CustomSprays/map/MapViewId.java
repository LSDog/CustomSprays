package fun.LSDog.CustomSprays.map;

/**
 * 获取一个 用作标识的 MapviewID <br>
 * 保持在 32467 ~ 32767 (302张"缓存")
 */
public class MapViewId {

    private static short id = 0;

    private static short MAX = 32767;
    private static short MIN = 32467;

    public static short getId() {
        return (++id > MAX || id < MIN) ? (id = MIN) : id;
    }

    public static short getNowId() {
        return id;
    }

    public static void setNumbers(int max, int min) {
        if (max > 32767 || max < -32768 || min > 32767 || min < -32768) return;
        MAX = (short) Math.max(max, min);
        MIN = (short) Math.min(max, min);
    }

}