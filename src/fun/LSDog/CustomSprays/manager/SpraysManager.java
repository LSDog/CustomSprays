package fun.LSDog.CustomSprays.manager;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Spray;
import fun.LSDog.CustomSprays.utils.SprayRayTracer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpraysManager {

    public static Map<UUID, List<Spray>> playerSprayMap = new ConcurrentHashMap<>();

    public static Map<Block, List<Spray>> locationSprayMap = new ConcurrentHashMap<>();
    // 注意block指的是喷漆所在的方块而不是依附着的方块

    /**
     * 在喷漆列表中加入新的喷漆, 玩家将会在进入相应世界的时候看到
     */
    public static void addSpray(Spray spray) {

        List<Spray> list = playerSprayMap.getOrDefault(spray.player.getUniqueId(), new ArrayList<>());
        list.add(spray);
        playerSprayMap.put(spray.player.getUniqueId(), list);

        List<Spray> locList = locationSprayMap.getOrDefault(spray.block, new ArrayList<>());
        locList.add(spray);
        locationSprayMap.put(spray.block, locList);

    }

    /**
     * 获取某个玩家视角中的喷漆
     */
    public static Spray getSprayInSight(Player player) {

        Location eyeLocation = player.getEyeLocation();
        return new SprayRayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.instant.getConfig().getDouble("distance")).rayTraceSpray(Spray.blockChecker);
    }

    /**
     * 获取特定方块的特定面上的喷漆
     * @param block 喷漆<b>所在的方块</b>, 而不是依附着的方块
     * @param blockFace 喷漆朝向
     * @return 相应位置的喷漆, 或者没有喷漆返回 null
     */
    public static Spray getSpray(Block block, BlockFace blockFace) {

        if (block == null || blockFace == null) return null;

        for (Spray spray : locationSprayMap.getOrDefault(block, Collections.emptyList())) {
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

        List<Spray> locSprayList = locationSprayMap.getOrDefault(spray.block, new ArrayList<>());
        if (!locSprayList.isEmpty()) locSprayList.remove(spray);
        locationSprayMap.put(spray.block, locSprayList);
    }

    /**
     * 清除所有喷漆和记录用map
     */
    public static void removeAllSpray() {

        playerSprayMap.values().forEach(sprays -> sprays.forEach(Spray::remove));
        // locationSprayMap.values().forEach(sprays -> sprays.forEach(Spray::remove));
        // 我们姑且不去担心两个map不一样的情况，随便吧

        playerSprayMap.clear();
        locationSprayMap.clear();
    }

}