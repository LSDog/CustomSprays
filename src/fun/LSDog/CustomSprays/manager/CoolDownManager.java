package fun.LSDog.CustomSprays.manager;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoolDownManager {

    private static final Map<UUID, Long> sprayCooldown = new HashMap<>();
    private static final Map<UUID, Long> uploadCooldown = new HashMap<>();

    private static long time() {
        return System.currentTimeMillis();
    }

    public static void addSprayCooldown(Player player) {
        sprayCooldown.put(player.getUniqueId(), time() + CustomSprays.instant.getConfig().getLong("spray_cooldown")*1000);
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

    public static void addUploadCooldown(Player player) {
        uploadCooldown.put(player.getUniqueId(), time() + CustomSprays.instant.getConfig().getLong("upload_cooldown")*1000);
    }

    public static void addUploadCooldown(Player player, int second) {
        uploadCooldown.put(player.getUniqueId(), time() + second*1000L);
    }

    public static boolean isUploadCooling(Player player) {
        if (uploadCooldown.containsKey(player.getUniqueId())) {
            return time() < uploadCooldown.get(player.getUniqueId());
        } else {
            uploadCooldown.put(player.getUniqueId(), time());
            return false;
        }
    }

    public static long getUploadCool(Player player) {
        return (uploadCooldown.getOrDefault(player.getUniqueId(), time()) - time())/1000;
    }

}
