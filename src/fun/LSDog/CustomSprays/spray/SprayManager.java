package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.event.PlayerSprayEvent;
import fun.LSDog.CustomSprays.util.CoolDown;
import fun.LSDog.CustomSprays.util.NMS;
import fun.LSDog.CustomSprays.util.SprayRayTracer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SprayManager {

    public static Map<UUID, List<SprayBase>> playerSprayMap = new ConcurrentHashMap<>();

    public static Map<Block, List<SprayBase>> locationSprayMap = new ConcurrentHashMap<>();
    // 注意block指的是喷漆所在的方块而不是依附着的方块

    public static Map<Integer, SprayBase> itemframeIdMap = new ConcurrentHashMap<>();

    /**
     * 让玩家喷漆，若玩家进行大喷漆(3*3)却没有权限，则会变为小喷漆(1*1)，默认展示给全服玩家 <br>
     * <b>务必使用 runTaskAsynchronously 异步执行, 否则可能造成卡顿！！</b>
     * @param player 喷漆玩家
     * @param isBigSpray 是否为大型喷漆
     */
    public static boolean spray(Player player, boolean isBigSpray) {

        boolean result = false;

        // 检测喷漆权限
        if (player.isPermissionSet("CustomSprays.spray") && !player.hasPermission("CustomSprays.spray")) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
            return false;
        }
        // 检测禁止的世界
        if (!player.hasPermission("CustomSprays.nodisable") && DataManager.disableWorlds != null && DataManager.disableWorlds.contains(player.getWorld().getName())) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.DISABLED_WORLD"));
            return false;
        }
        // 检测CD
        if (!player.hasPermission("CustomSprays.nocd") && CoolDown.isSprayInCd(player)) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING")+" §7("+ CoolDown.getSprayCdFormat(player)+"s)");
            return false;
        }


        try {
            // 如果 [不是大喷漆  或者  (是大喷漆却)没有大喷漆权限]
            if (!isBigSpray || (player.isPermissionSet("CustomSprays.bigspray") && !player.hasPermission("CustomSprays.bigspray"))) {

                // 小喷漆
                byte[] bytes = DataManager.get128pxImageBytes(player);
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return false;
                }
                SpraySmall spraySmall = new SpraySmall(player, bytes, Bukkit.getOnlinePlayers());
                result = spraySmall.init((long) (CustomSprays.plugin.getConfig().getDouble("destroy") * 20L));
                if (result) {
                    CoolDown.setSprayCdMultiple(player,1);
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spraySmall.location.getX() + " " + spraySmall.location.getY() + " " + spraySmall.location.getZ());
                }

            } else {

                // 大喷漆
                int length = CustomSprays.plugin.getConfig().getInt("big_size");
                byte[] bytes;
                if (length == 3) {
                    bytes = DataManager.get384pxImageBytes(player);
                } else if (length == 5) {
                    bytes = DataManager.getSizedImageBytes(player, 640, 640);
                } else {
                    return false;
                }
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return false;
                }

                SprayBig spray = new SprayBig(player, bytes, Bukkit.getOnlinePlayers(), length);
                result = spray.init((long) (CustomSprays.plugin.getConfig().getDouble("destroy") * 20L));
                if (result) {
                    CoolDown.setSprayCdMultiple(player, CustomSprays.plugin.getConfig().getDouble("big_spray_cd_multiple"));
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spray.location.getX() + " " + spray.location.getY() + " " + spray.location.getZ() + " (big)");
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 向某个玩家播放喷漆音效
     */
    public static void playSpraySound(Player player) {
        String soundName = CustomSprays.plugin.getConfig().getString("spray_sound");
        if (soundName == null || "default".equals(soundName)) {
            Sound sound = Sound.valueOf(NMS.getSubVer() <= 8 ? "SILVERFISH_HIT" : "ENTITY_SILVERFISH_HURT");
            Bukkit.getScheduler().runTask(CustomSprays.plugin, () ->
                    player.getWorld().playSound(player.getLocation(), sound, 1, 0.8F));
        } else {
            String[] strings = soundName.split("-");
            if (strings.length != 3) return;
            Bukkit.getScheduler().runTask(CustomSprays.plugin, () ->
                    player.getWorld().playSound(player.getLocation(), strings[0], Float.parseFloat(strings[1]), Float.parseFloat(strings[2])));
        }
    }

    /**
     * 移除喷漆音效
     */
    public static void playRemoveSound(Player player) {
        int subVer = NMS.getSubVer();
        String soundName;
        if (subVer <= 8) soundName = "DIG_WOOL";
        else if (subVer <= 12) soundName = "BLOCK_CLOTH_HIT";
        else soundName = "BLOCK_WOOL_HIT";
        Sound sound = Sound.valueOf(soundName);
        Bukkit.getScheduler().runTask(CustomSprays.plugin, () ->
                player.getWorld().playSound(player.getLocation(), sound, 1, 1.2F));

    }


    public static boolean callSprayEvent(Player player, ISpray spray) {
        PlayerSprayEvent event = new PlayerSprayEvent(player, spray);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
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

        itemframeIdMap.put(spray.itemFrameId, spray);

    }

    /**
     * Send player existed sprays that in the player's world
     */
    public static void sendExistSprays(Player player) {

        World playerWorld = player.getWorld();
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.plugin, () -> SprayManager.playerSprayMap.values().forEach(sprays -> sprays.forEach(spray -> {
            if (spray.world == playerWorld) try {
                spray.spawn(Collections.singletonList(player), false, false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        })), 20L);
    }

    /**
     * 获取某个玩家视角中的喷漆
     */
    public static SprayBase getSprayInSight(Player player) {

        Location eyeLocation = player.getEyeLocation();
        return new SprayRayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.plugin.getConfig().getDouble("distance")).rayTraceSpray(SprayManager::isSpraySurfaceBlock);
    }

    /**
     * 检查特定方块的特定面上有没有喷漆
     * @param block 喷漆<b>所在的方块</b>, 而不是依附着的方块
     * @param blockFace 喷漆朝向
     * @return 有无喷漆
     */
    public static boolean hasSpray(Block block, BlockFace blockFace) {

        if (block == null || blockFace == null) return false;

        List<SprayBase> list = locationSprayMap.get(block);

        if (list != null) for (SprayBase spray : list) {
            if (blockFace == spray.blockFace) return true;
        }

        return false;
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

    public static SprayBase getSpray(int itemframeEntityId) {
        return itemframeIdMap.get(itemframeEntityId);
    }

    /**
     * 清除喷漆和记录用map中的spray
     * @param spray 喷漆
     */
    public static void removeSpray(SprayBase spray) {

        List<SprayBase> playerSprayList = playerSprayMap.getOrDefault(spray.player.getUniqueId(), new ArrayList<>());
        if (!playerSprayList.isEmpty()) playerSprayList.remove(spray);
        playerSprayMap.put(spray.player.getUniqueId(), playerSprayList);

        List<SprayBase> locSprayList = locationSprayMap.getOrDefault(spray.block, new ArrayList<>());
        if (!locSprayList.isEmpty()) locSprayList.remove(spray);
        locationSprayMap.put(spray.block, locSprayList);

        itemframeIdMap.remove(spray.itemFrameId);
    }

    /**
     * 清除所有喷漆和记录用map
     */
    public static void removeAllSpray() {

        Set<SprayBase> deleteSprays = new HashSet<>();

        locationSprayMap.values().forEach(deleteSprays::addAll);
        // 我们姑且不去担心两个map不一样的情况，随便吧

        deleteSprays.forEach(ISpray::remove);

        playerSprayMap.clear();
        locationSprayMap.clear();
        itemframeIdMap.clear();
    }

    /**
     * 判断是否为无法喷漆的方块
     */
    @SuppressWarnings("SpellCheckingInspection")
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
            case "BAMBOO_SAPLING":
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
            case "CHAIN":
            case "CANDLE":
            case "BEETROOT_BLOCK":
            case "PITCHER_PLANT":
            case "PITCHER_CROP":
            case "TUBE_CORAL":
            case "BRAIN_CORAL":
            case "BUBBLE_CORAL":
            case "FIRE_CORAL":
            case "HORN_CORAL":
            case "DEAD_BRAIN_CORAL":
            case "DEAD_BUBBLE_CORAL":
            case "DEAD_FIRE_CORAL":
            case "DEAD_HORN_CORAL":
            case "DEAD_TUBE_CORAL":
            case "TUBE_CORAL_FAN":
            case "BRAIN_CORAL_FAN":
            case "BUBBLE_CORAL_FAN":
            case "FIRE_CORAL_FAN":
            case "HORN_CORAL_FAN":
            case "DEAD_TUBE_CORAL_FAN":
            case "DEAD_BRAIN_CORAL_FAN":
            case "DEAD_BUBBLE_CORAL_FAN":
            case "DEAD_FIRE_CORAL_FAN":
            case "DEAD_HORN_CORAL_FAN":
            case "WHITE_CANDLE":
            case "ORANGE_CANDLE":
            case "MAGENTA_CANDLE":
            case "LIGHT_BLUE_CANDLE":
            case "YELLOW_CANDLE":
            case "LIME_CANDLE":
            case "PINK_CANDLE":
            case "GRAY_CANDLE":
            case "LIGHT_GRAY_CANDLE":
            case "CYAN_CANDLE":
            case "PURPLE_CANDLE":
            case "BLUE_CANDLE":
            case "BROWN_CANDLE":
            case "GREEN_CANDLE":
            case "RED_CANDLE":
            case "BLACK_CANDLE":
            case "FENCE":
            case "OAK_FENCE":
            case "SPRUCE_FENCE":
            case "BIRCH_FENCE":
            case "JUNGLE_FENCE":
            case "ACACIA_FENCE":
            case "CHERRY_FENCE":
            case "DARK_OAK_FENCE":
            case "MANGROVE_FENCE":
            case "BAMBOO_FENCE":
            case "CRIMSON_FENCE":
            case "WARPED_FENCE":
            case "NETHER_BRICK_FENCE":
            case "IRON_FENCE":
            case "NETHER_FENCE":
            case "SPRUCE_FENCE_GATE":
            case "BIRCH_FENCE_GATE":
            case "JUNGLE_FENCE_GATE":
            case "DARK_OAK_FENCE_GATE":
            case "ACACIA_FENCE_GATE":
            case "OAK_FENCE_GATE":
            case "CHERRY_FENCE_GATE":
            case "MANGROVE_FENCE_GATE":
            case "BAMBOO_FENCE_GATE":
            case "CRIMSON_FENCE_GATE":
            case "WARPED_FENCE_GATE":
            case "STANDING_BANNER":
            case "WALL_BANNER":
            case "BANNER":
            case "WHITE_BANNER":
            case "ORANGE_BANNER":
            case "MAGENTA_BANNER":
            case "LIGHT_BLUE_BANNER":
            case "YELLOW_BANNER":
            case "LIME_BANNER":
            case "PINK_BANNER":
            case "GRAY_BANNER":
            case "LIGHT_GRAY_BANNER":
            case "CYAN_BANNER":
            case "PURPLE_BANNER":
            case "BLUE_BANNER":
            case "BROWN_BANNER":
            case "GREEN_BANNER":
            case "RED_BANNER":
            case "BLACK_BANNER":
            case "WHITE_WALL_BANNER":
            case "ORANGE_WALL_BANNER":
            case "MAGENTA_WALL_BANNER":
            case "LIGHT_BLUE_WALL_BANNER":
            case "YELLOW_WALL_BANNER":
            case "LIME_WALL_BANNER":
            case "PINK_WALL_BANNER":
            case "GRAY_WALL_BANNER":
            case "LIGHT_GRAY_WALL_BANNER":
            case "CYAN_WALL_BANNER":
            case "PURPLE_WALL_BANNER":
            case "BLUE_WALL_BANNER":
            case "BROWN_WALL_BANNER":
            case "GREEN_WALL_BANNER":
            case "RED_WALL_BANNER":
            case "BLACK_WALL_BANNER":
            case "CARPET":
            case "MOOS_CARPET":
            case "WHITE_CARPET":
            case "ORANGE_CARPET":
            case "MAGENTA_CARPET":
            case "LIGHT_BLUE_CARPET":
            case "YELLOW_CARPET":
            case "LIME_CARPET":
            case "PINK_CARPET":
            case "GRAY_CARPET":
            case "LIGHT_GRAY_CARPET":
            case "CYAN_CARPET":
            case "PURPLE_CARPET":
            case "BLUE_CARPET":
            case "BROWN_CARPET":
            case "GREEN_CARPET":
            case "RED_CARPET":
            case "BLACK_CARPET":
            case "WOOD_BUTTON":
            case "STONE_BUTTON":
            case "POLISHED_BLACKSTONE_BUTTON":
            case "OAK_BUTTON":
            case "SPRUCE_BUTTON":
            case "BIRCH_BUTTON":
            case "JUNGLE_BUTTON":
            case "ACACIA_BUTTON":
            case "CHERRY_BUTTON":
            case "DARK_OAK_BUTTON":
            case "MANGROVE_BUTTON":
            case "BAMBOO_BUTTON":
            case "CRIMSON_BUTTON":
            case "WARPED_BUTTON":
            case "SKULL":
            case "PLAYER_HEAD":
            case "ZOMBIE_HEAD":
            case "CREEPER_HEAD":
            case "DRAGON_HEAD":
            case "PIGLIN_HEAD":
            case "PISTON_HEAD":
            case "ZOMBIE_WALL_HEAD":
            case "PLAYER_WALL_HEAD":
            case "CREEPER_WALL_HEAD":
            case "DRAGON_WALL_HEAD":
            case "PIGLIN_WALL_HEAD":
            case "POTTED_TORCHFLOWER":
            case "POTTED_OAK_SAPLING":
            case "POTTED_SPRUCE_SAPLING":
            case "POTTED_BIRCH_SAPLING":
            case "POTTED_JUNGLE_SAPLING":
            case "POTTED_ACACIA_SAPLING":
            case "POTTED_CHERRY_SAPLING":
            case "POTTED_DARK_OAK_SAPLING":
            case "POTTED_MANGROVE_PROPAGULE":
            case "POTTED_FERN":
            case "POTTED_DANDELION":
            case "POTTED_POPPY":
            case "POTTED_BLUE_ORCHID":
            case "POTTED_ALLIUM":
            case "POTTED_AZURE_BLUET":
            case "POTTED_RED_TULIP":
            case "POTTED_ORANGE_TULIP":
            case "POTTED_WHITE_TULIP":
            case "POTTED_PINK_TULIP":
            case "POTTED_OXEYE_DAISY":
            case "POTTED_CORNFLOWER":
            case "POTTED_LILY_OF_THE_VALLEY":
            case "POTTED_WITHER_ROSE":
            case "POTTED_RED_MUSHROOM":
            case "POTTED_BROWN_MUSHROOM":
            case "POTTED_DEAD_BUSH":
            case "POTTED_CACTUS":
            case "POTTED_BAMBOO":
            case "POTTED_CRIMSON_FUNGUS":
            case "POTTED_WARPED_FUNGUS":
            case "POTTED_CRIMSON_ROOTS":
            case "POTTED_WARPED_ROOTS":
            case "POTTED_AZALEA_BUSH":
            case "POTTED_FLOWERING_AZALEA_BUSH":
            case "REPEATER":
            case "COMPARATOR":
            case "REDSTONE_COMPARATOR_ON":
            case "REDSTONE_COMPARATOR_OFF":
            case "REDSTONE_TORCH":
            case "REDSTONE_WALL_TORCH":
            case "REDSTONE_WIRE":
            case "STONE_PLATE":
            case "WOOD_PLATE":
            case "GOLD_PLATE":
            case "IRON_PLATE":
            case "STONE_PRESSURE_PLATE":
            case "POLISHED_BLACKSTONE_PRESSURE_PLATE":
            case "LIGHT_WEIGHTED_PRESSURE_PLATE":
            case "HEAVY_WEIGHTED_PRESSURE_PLATE":
            case "OAK_PRESSURE_PLATE":
            case "SPRUCE_PRESSURE_PLATE":
            case "BIRCH_PRESSURE_PLATE":
            case "JUNGLE_PRESSURE_PLATE":
            case "ACACIA_PRESSURE_PLATE":
            case "CHERRY_PRESSURE_PLATE":
            case "DARK_OAK_PRESSURE_PLATE":
            case "MANGROVE_PRESSURE_PLATE":
            case "BAMBOO_PRESSURE_PLATE":
            case "CRIMSON_PRESSURE_PLATE":
            case "WARPED_PRESSURE_PLATE":
            case "OAK_SIGN":
            case "SPRUCE_SIGN":
            case "BIRCH_SIGN":
            case "JUNGLE_SIGN":
            case "ACACIA_SIGN":
            case "CHERRY_SIGN":
            case "DARK_OAK_SIGN":
            case "MANGROVE_SIGN":
            case "BAMBOO_SIGN":
            case "CRIMSON_SIGN":
            case "WARPED_SIGN":
            case "OAK_HANGING_SIGN":
            case "SPRUCE_HANGING_SIGN":
            case "BIRCH_HANGING_SIGN":
            case "JUNGLE_HANGING_SIGN":
            case "ACACIA_HANGING_SIGN":
            case "CHERRY_HANGING_SIGN":
            case "DARK_OAK_HANGING_SIGN":
            case "MANGROVE_HANGING_SIGN":
            case "BAMBOO_HANGING_SIGN":
            case "CRIMSON_HANGING_SIGN":
            case "WARPED_HANGING_SIGN":
            case "OAK_WALL_SIGN":
            case "SPRUCE_WALL_SIGN":
            case "BIRCH_WALL_SIGN":
            case "ACACIA_WALL_SIGN":
            case "CHERRY_WALL_SIGN":
            case "JUNGLE_WALL_SIGN":
            case "DARK_OAK_WALL_SIGN":
            case "MANGROVE_WALL_SIGN":
            case "BAMBOO_WALL_SIGN":
            case "OAK_WALL_HANGING_SIGN":
            case "SPRUCE_WALL_HANGING_SIGN":
            case "BIRCH_WALL_HANGING_SIGN":
            case "ACACIA_WALL_HANGING_SIGN":
            case "CHERRY_WALL_HANGING_SIGN":
            case "JUNGLE_WALL_HANGING_SIGN":
            case "DARK_OAK_WALL_HANGING_SIGN":
            case "MANGROVE_WALL_HANGING_SIGN":
            case "CRIMSON_WALL_HANGING_SIGN":
            case "WARPED_WALL_HANGING_SIGN":
            case "BAMBOO_WALL_HANGING_SIGN":
            case "CRIMSON_WALL_SIGN":
            case "WARPED_WALL_SIGN":
            case "CRIMSON_STEM":
            case "WARPED_STEM":
            case "STRIPPED_CRIMSON_STEM":
            case "STRIPPED_WARPED_STEM":
            case "ATTACHED_PUMPKIN_STEM":
            case "ATTACHED_MELON_STEM":
            case "PUMPKIN_STEM":
            case "MELON_STEM":
            case "VINE":
            case "TRIPWIRE":
            case "TRIPWIRE_HOOK":
            case "COCOA":
            case "NETHER_PORTAL":
            case "ENDER_PORTAL":
            case "CORNFLOWER":
                return false;
        }
        return !type.isTransparent();
    }
}