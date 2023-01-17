package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.utils.CoolDown;
import fun.LSDog.CustomSprays.utils.SprayRayTracer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SprayManager {

    public static Map<UUID, List<SprayBase>> playerSprayMap = new ConcurrentHashMap<>();

    public static Map<Block, List<SprayBase>> locationSprayMap = new ConcurrentHashMap<>();
    // 注意block指的是喷漆所在的方块而不是依附着的方块

    /**
     * 让玩家喷漆，若玩家进行大喷漆(3*3)却没有权限，则会变为小喷漆(1*1)，默认展示给全服玩家 <br>
     * <b>务必使用 runTaskAsynchronously 异步执行, 否则可能造成卡顿！！</b>
     * @param player 喷漆玩家
     * @param isBigSpray 是否为大型喷漆
     */
    public static void spray(Player player, boolean isBigSpray) {

        // 检测喷漆权限
        if (player.isPermissionSet("CustomSprays.spray") && !player.hasPermission("CustomSprays.spray")) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
            return;
        }
        // 检测禁止的世界
        if (!player.hasPermission("CustomSprays.nodisable") && DataManager.disableWorlds != null && DataManager.disableWorlds.contains(player.getWorld().getName())) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.DISABLED_WORLD"));
            return;
        }
        // 检测CD
        if (!player.hasPermission("CustomSprays.nocd") && CoolDown.isSprayCooling(player)) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING")+" §7("+ CoolDown.getSprayCD(player)+")");
            return;
        }


        try {
            // 如果 [不是大喷漆  或者  (是大喷漆却)没有大喷漆权限]
            if (!isBigSpray || (player.isPermissionSet("CustomSprays.bigspray") && !player.hasPermission("CustomSprays.bigspray"))) {

                // 小喷漆
                byte[] bytes = DataManager.get128pxImageBytes(player);
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return;
                }
                SprayBase spray = new SprayBase(player, bytes, Bukkit.getOnlinePlayers());
                if (spray.create((long) (CustomSprays.instance.getConfig().getDouble("destroy")*20L))) {
                    CoolDown.setSprayCooldown(player,1);
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spray.location.getX() + " " + spray.location.getY() + " " + spray.location.getZ());
                }

            } else {

                // 大喷漆
                int length = CustomSprays.instance.getConfig().getInt("big_size");
                byte[] bytes;
                if (length == 3) {
                    bytes = DataManager.get384pxImageBytes(player);
                } else if (length == 5) {
                    bytes = DataManager.getSizedImageBytes(player, 640, 640);
                } else {
                    return;
                }
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return;
                }

                SprayBase spray = new SprayBig(player, length, bytes, Bukkit.getOnlinePlayers());
                if (spray.create((long) (CustomSprays.instance.getConfig().getDouble("destroy")*20L))) {
                    CoolDown.setSprayCooldown(player, CustomSprays.instance.getConfig().getDouble("big_spray_cooldown_multiple"));
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spray.location.getX() + " " + spray.location.getY() + " " + spray.location.getZ() + " (big)");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在喷漆列表中加入新的喷漆, 玩家将会在进入相应世界的时候看到列表中的喷漆
     */
    public static void addSpray(SprayBase spray) {

        List<SprayBase> list = playerSprayMap.getOrDefault(spray.player.getUniqueId(), new ArrayList<>());
        list.add(spray);
        playerSprayMap.put(spray.player.getUniqueId(), list);

        List<SprayBase> locList = locationSprayMap.getOrDefault(spray.block, new ArrayList<>());
        locList.add(spray);
        locationSprayMap.put(spray.block, locList);

    }

    /**
     * 发送玩家所有已存在的喷漆
     */
    public static void sendExistSprays(Player player) {

        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instance, () -> SprayManager.playerSprayMap.forEach((uuid, sprays) -> sprays.forEach(spray -> {
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
    public static SprayBase getSprayInSight(Player player) {

        Location eyeLocation = player.getEyeLocation();
        return new SprayRayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.instance.getConfig().getDouble("distance")).rayTraceSpray(SprayManager::isSpraySurfaceBlock);
    }

    /**
     * 获取特定方块的特定面上的喷漆
     * @param block 喷漆<b>所在的方块</b>, 而不是依附着的方块
     * @param blockFace 喷漆朝向
     * @return 相应位置的喷漆, 或者没有喷漆返回 null
     */
    public static SprayBase getSpray(Block block, BlockFace blockFace) {

        if (block == null || blockFace == null) return null;

        for (SprayBase spray : locationSprayMap.getOrDefault(block, Collections.emptyList())) {
            if (blockFace == spray.blockFace) return spray;
        }

        return null;
    }

    /**
     * 清除喷漆和记录用map中的spray
     * @param spray 喷漆
     */
    public static void removeSpray(SprayBase spray) {

        // spray.remove();

        List<SprayBase> playerSprayList = playerSprayMap.getOrDefault(spray.player.getUniqueId(), new ArrayList<>());
        if (!playerSprayList.isEmpty()) playerSprayList.remove(spray);
        playerSprayMap.put(spray.player.getUniqueId(), playerSprayList);

        List<SprayBase> locSprayList = locationSprayMap.getOrDefault(spray.block, new ArrayList<>());
        if (!locSprayList.isEmpty()) locSprayList.remove(spray);
        locationSprayMap.put(spray.block, locSprayList);
    }

    /**
     * 清除所有喷漆和记录用map
     */
    public static void removeAllSpray() {

        Set<SprayBase> deleteSprays = new HashSet<>();

        locationSprayMap.values().forEach(deleteSprays::addAll);
        // 我们姑且不去担心两个map不一样的情况，随便吧

        deleteSprays.forEach(SprayBase::remove);

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