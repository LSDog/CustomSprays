package fun.LSDog.CustomSprays.manager;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoolDownManager {

    private static final Map<UUID, Long> sprayCooldown = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> uploadCooldown = new ConcurrentHashMap<>();

    public static void reset() {
        sprayCooldown.clear();
        uploadCooldown.clear();
    }

    private static long time() {
        return System.currentTimeMillis();
    }

    public static void setSprayCooldown(Player player, double multiple) {
        sprayCooldown.put(player.getUniqueId(), (long) (time() + CustomSprays.instant.getConfig().getDouble("spray_cooldown") *multiple*1000));
    }

    public static boolean isSprayCooling(Player player) {
        if (sprayCooldown.containsKey(player.getUniqueId())) {
            return time() < sprayCooldown.get(player.getUniqueId());
        } else {
            sprayCooldown.put(player.getUniqueId(), time());
            return false;
        }
    }

    public static long getSprayCool(Player player) {
        return (sprayCooldown.getOrDefault(player.getUniqueId(), time()) - time())/1000;
    }



    public static void setUploadCooldown(Player player, double multiple) {
        uploadCooldown.put(player.getUniqueId(), (long) (time() + CustomSprays.instant.getConfig().getDouble("upload_cooldown") *multiple*1000));
    }

    public static boolean isUploadCooling(Player player) {
        if (uploadCooldown.containsKey(player.getUniqueId())) {
            return time() <= uploadCooldown.get(player.getUniqueId());
        } else {
            uploadCooldown.put(player.getUniqueId(), time());
            return false;
        }
    }

    public static long getUploadCool(Player player) {
        return (uploadCooldown.getOrDefault(player.getUniqueId(), time()) - time())/1000;
    }

}
