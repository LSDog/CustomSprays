package fun.LSDog.CustomSprays.manager;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Spray;
import fun.LSDog.CustomSprays.utils.SprayRayTracer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
     * 在喷漆列表中加入新的喷漆, 玩家将会在进入相应世界的时候看到列表中的喷漆
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
     * 发送玩家所有已存在的喷漆
     */
    public static void sendExistSprays(Player player) {

        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, () -> SpraysManager.playerSprayMap.forEach((uuid, sprays) -> sprays.forEach(spray -> {
            try {
                spray.spawn(Collections.singletonList(player), false);
            } catch (ReflectiveOperationException exception) {
                exception.printStackTrace();
            }
        })), 20L);
    }

    /**
     * 获取某个玩家视角中的喷漆
     */
    public static Spray getSprayInSight(Player player) {

        Location eyeLocation = player.getEyeLocation();
        return new SprayRayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.instant.getConfig().getDouble("distance")).rayTraceSpray(SpraysManager::isSpraySurfaceBlock);
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
     * 清除喷漆和记录用map中的spray
     * @param spray 喷漆
     */
    public static void removeSpray(Spray spray) {

        // spray.remove();

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

        Set<Spray> deleteSprays = new HashSet<>();

        locationSprayMap.values().forEach(deleteSprays::addAll);
        // 我们姑且不去担心两个map不一样的情况，随便吧

        deleteSprays.forEach(Spray::remove);

        playerSprayMap.clear();
        locationSprayMap.clear();
    }

    /**
     * 判断是否为无法喷漆的方块
     */
    public static boolean isSpraySurfaceBlock(Block block) {
        if (block.isEmpty() || block.isLiquid()) return false;
        Material type = block.getType();
        String name = type.name();
        switch (name) {
            case "SIGN":
            case "WALL_SIGN":
            case "STRING":
            case "LIGHT":
            case "BUBBLE_COLUMN":
            case "CONDUIT":
            case "KELP":
            case "KELP_PLANT":
            case "SEAGRASS":
            case "TALL_SEAGRASS":
            case "SEA_PICKLE":
            case "TURTLE_EGG":
            case "BAMBOO":
            case "BELL":
            case "LANTERN":
            case "SWEET_BERRIES":
            case "SOUL_LANTERN":
            case "SOUL_FIRE":
            case "SOUL_WALL_TORCH":
            case "WARPED_FUNGUS":
            case "CRIMSON_FUNGUS":
            case "WEEPING_VINES":
            case "WEEPING_VINES_PLANT":
            case "TWISTING_VINES":
            case "TWISTING_VINES_PLANT":
            case "NETHER_SPROUTS":
            case "CRIMSON_ROOTS":
            case "WARPED_ROOTS":
            case "AMETHYST_BUD":
            case "AMETHYST_CLUSTER":
            case "LIGHTNING_ROD":
            case "POINTED_DRIPSTONE":
            case "GLOW_LICHEN":
            case "FLOWERING_AZALEA":
            case "MOSS_CARPET":
            case "CAVE_VINES":
            case "CAVE_VINES_PLANT":
            case "GLOW_BERRIES":
            case "BIG_DRIPLEAF":
            case "BIG_DRIPLEAF_STEM":
            case "SMALL_DRIPLEAF":
            case "HANGING_ROOTS":
            case "SPORE_BLOSSOM":
                return false;
        }
        if (
                (           name.contains("CORAL")
                        || name.contains("AMETHYST")) && !name.contains("BLOCK")
                        || name.contains("CARPET")
                        || name.contains("CANDLE")
                        || name.contains("SIGN")
                        || name.contains("PLATE")
        ) {
            return false;
        }
        return !type.isTransparent();
    }
}