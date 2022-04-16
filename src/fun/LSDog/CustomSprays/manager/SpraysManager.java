package fun.LSDog.CustomSprays.manager;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Spray;
import fun.LSDog.CustomSprays.utils.RayTracer;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpraysManager {

    public static Map<UUID, List<Spray>> playerSprayMap = new ConcurrentHashMap<>();

    public static Map<Location, List<Spray>> locationSprayMap = new ConcurrentHashMap<>();

    public static void addSpray(Spray spray) {

        List<Spray> list = playerSprayMap.getOrDefault(spray.player.getUniqueId(), new ArrayList<>());
        list.add(spray);
        playerSprayMap.put(spray.player.getUniqueId(), list);

        List<Spray> locList = locationSprayMap.getOrDefault(spray.location, new ArrayList<>());
        locList.add(spray);
        locationSprayMap.put(spray.location, locList);

    }

    public static Spray getSpray(Player player) {

        Location eyeLocation = player.getEyeLocation();
        RayTracer.BlockRayTraceResult targetBlock =
                new RayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.instant.getConfig().getDouble("distance"))
                        .rayTraceBlock(block -> block.getType().isSolid());
        if (targetBlock == null) return null;
        return getSpray(targetBlock.getRelativeBlock().getLocation(), targetBlock.blockFace);

    }

    public static Spray getSpray(Location location, BlockFace blockFace) {

        if (location == null || blockFace == null) return null;

        for (Spray spray : locationSprayMap.getOrDefault(location, Collections.emptyList())) {
            if (blockFace == spray.blockFace) return spray;
        }

        return null;
    }

    /**
     * 清除某玩家的喷漆和记录用map
     * @param spray 喷漆
     */
    public static void removeSpray(Spray spray) {

        spray.remove();

        List<Spray> playerSprayList = playerSprayMap.getOrDefault(spray.player.getUniqueId(), new ArrayList<>());
        if (!playerSprayList.isEmpty()) playerSprayList.remove(spray);
        playerSprayMap.put(spray.player.getUniqueId(), playerSprayList);

        List<Spray> locSprayList = locationSprayMap.getOrDefault(spray.location, new ArrayList<>());
        if (!locSprayList.isEmpty()) locSprayList.remove(spray);
        locationSprayMap.put(spray.location, locSprayList);
    }

    /**
     * 清除所有喷漆和记录用map
     */
    public static void removeAllSpray() {

        playerSprayMap.values().forEach(sprays -> sprays.forEach(Spray::remove));
        // locationSprayMap.values().forEach(sprays -> sprays.forEach(Spray::remove));
        // 我们姑且不去担心两个map不一样的情况，俺尽力了

        playerSprayMap.clear();
        locationSprayMap.clear();
    }

}
