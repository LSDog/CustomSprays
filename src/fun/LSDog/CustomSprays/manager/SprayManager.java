package fun.LSDog.CustomSprays.manager;

import fun.LSDog.CustomSprays.Spray;
import fun.LSDog.CustomSprays.utils.RayTracer;
import fun.LSDog.CustomSprays.utils.TargetBlock;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SprayManager {

    public static Map<UUID, List<Spray>> playerSprayMap = new HashMap<>();

    public static Map<Location, List<Spray>> locationSprayMap = new ConcurrentHashMap<>();

    public static void addSpray(Player player, Spray spray) {

        List<Spray> list = playerSprayMap.getOrDefault(player.getUniqueId(), new ArrayList<>());
        list.add(spray);
        playerSprayMap.put(player.getUniqueId(), list);

        List<Spray> locList = locationSprayMap.getOrDefault(spray.location, new ArrayList<>());
        locList.add(spray);
        locationSprayMap.put(spray.location, locList);
    }

    public static Spray getSpray(Player player) {

        try {
            TargetBlock targetBlock = RayTracer.getTargetBlock(player);
            if (targetBlock == null) return null;
            return getSpray(targetBlock.getRelativeBlock().getLocation(), targetBlock.getBlockFace());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Spray getSpray(Location location, BlockFace blockFace) {
        if (location == null || blockFace == null) return null;

        for (Spray spray : locationSprayMap.getOrDefault(location, Collections.emptyList())) {
            if (blockFace == spray.blockFace) return spray;
        }
        return null;
    }

    public static void removeSpray(Player player, Spray spray) {

        spray.destroy();

        List<Spray> list = playerSprayMap.getOrDefault(player.getUniqueId(), new ArrayList<>());
        list.remove(spray);
        playerSprayMap.put(player.getUniqueId(), list);

        List<Spray> locList = locationSprayMap.getOrDefault(spray.location, new ArrayList<>());
        locList.remove(spray);
        locationSprayMap.put(spray.location, locList);
    }

    public static void destroyAllSpray() {

        playerSprayMap.values().forEach(sprays -> sprays.forEach(Spray::destroy));
        locationSprayMap.values().forEach(sprays -> sprays.forEach(Spray::destroy));
    }

}
