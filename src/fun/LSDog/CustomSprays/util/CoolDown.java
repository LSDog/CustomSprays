package fun.LSDog.CustomSprays.util;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoolDown {

    private static final Map<UUID, Long> sprayCdMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> uploadCdMap = new ConcurrentHashMap<>();

    public static void reset() {
        sprayCdMap.clear();
        uploadCdMap.clear();
    }

    private static long time() {
        return System.currentTimeMillis();
    }

    public static void setSprayCd(Player player, long millisecond) {
        sprayCdMap.put(player.getUniqueId(), time() + millisecond);
    }

    public static void setSprayCdMultiple(Player player, double multiple) {
        setSprayCd(player, Math.round(CustomSprays.plugin.getConfig().getDouble("spray_cooldown")*multiple*1000));
    }

    public static boolean isSprayInCd(Player player) {
        if (sprayCdMap.containsKey(player.getUniqueId())) {
            return time() < sprayCdMap.get(player.getUniqueId());
        } else {
            sprayCdMap.put(player.getUniqueId(), time());
            return false;
        }
    }

    /**
     * Get spray cd time in millisecond
     * @return cd time in millisecond
     */
    public static long getSprayCd(Player player) {
        return sprayCdMap.getOrDefault(player.getUniqueId(), time()) - time();
    }

    public static String getSprayCdFormat(Player player) {
        return getTimeSecondFormat(getSprayCd(player));
    }


    public static void setUploadCd(Player player, long millisecond) {
        uploadCdMap.put(player.getUniqueId(), time() + millisecond);
    }

    public static void setUploadCdMultiple(Player player, double multiple) {
        setUploadCd(player, Math.round(CustomSprays.plugin.getConfig().getDouble("upload_cooldown")*multiple*1000));
    }

    public static boolean isUploadInCd(Player player) {
        if (uploadCdMap.containsKey(player.getUniqueId())) {
            return time() <= uploadCdMap.get(player.getUniqueId());
        } else {
            uploadCdMap.put(player.getUniqueId(), time());
            return false;
        }
    }

    /**
     * Get upload cd time in millisecond
     * @return cd time in millisecond
     */
    public static long getUploadCd(Player player) {
        return (uploadCdMap.getOrDefault(player.getUniqueId(), time()) - time())/1000;
    }

    public static String getUploadCdFormat(Player player) {
        return getTimeSecondFormat(getUploadCd(player));
    }

    private static String getTimeSecondFormat(long ms) {
        if (ms > 1000 || ms < -1000) return String.valueOf(ms/1000);
        else return String.format("%.1f", ms/1000.0);
    }

}
