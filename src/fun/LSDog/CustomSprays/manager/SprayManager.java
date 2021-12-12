package fun.LSDog.CustomSprays.manager;

import fun.LSDog.CustomSprays.Spray;
import org.bukkit.entity.Player;

import java.util.*;

public class SprayManager {

    public static Map<UUID, List<Spray>> sprayMap = new HashMap<>();

    public static void addSpray(Player player, Spray spray) {
        List<Spray> list = sprayMap.getOrDefault(player.getUniqueId(), new ArrayList<>());
        list.add(spray);
        sprayMap.put(player.getUniqueId(), list);
    }

    public static void removeSpray(Player player, Spray spray) {
        List<Spray> list = sprayMap.getOrDefault(player.getUniqueId(), new ArrayList<>());
        list.remove(spray);
        sprayMap.put(player.getUniqueId(), list);
    }

    public static void destroyAllSpray() {
        sprayMap.forEach((uuid, sprays) -> sprays.forEach(Spray::destroy));
    }

}
