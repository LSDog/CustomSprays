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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
                SpraySmall spraySmall = new SpraySmall(player, bytes, Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList()));
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

                SprayBig spray = new SprayBig(player, bytes, Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList()), length);
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
            Sound sound = Sound.valueOf(NMS.getmainVer() > 1 || NMS.getSubVer() >= 9 ? "ENTITY_SILVERFISH_HURT" : "SILVERFISH_HIT");
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
        if (NMS.getmainVer() > 1 || subVer >= 13) soundName = "BLOCK_WOOL_HIT";
        else if (subVer >= 9) soundName = "BLOCK_CLOTH_HIT";
        else soundName = "DIG_WOOL";
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
     * Send player existing sprays that in the player's world
     */
    public static void sendExistSprays(Player player) {

        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.plugin, () -> SprayManager.playerSprayMap.values().forEach(sprays -> sprays.forEach(spray -> {
            try {
                spray.spawn(Collections.singletonList(player.getUniqueId()), false, false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        })), 20L);
    }

    /**
     * Remove a player from every spray's shown player
     */
    public static void removeShownPlayer(Player player) {
        itemframeIdMap.values().forEach(spray -> spray.playersShown.remove(player.getUniqueId()));
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
        String name = type.name().toUpperCase(Locale.ENGLISH);
        if (
                name.startsWith("POTTED_") || // 花盆+植物
                name.endsWith("CARPET") ||
                name.endsWith("BUTTON") ||
                name.endsWith("GATE") ||
                name.endsWith("FENCE") ||
                name.endsWith("SIGN") ||
                name.endsWith("BANNER") ||
                name.endsWith("CANDLE") ||
                name.endsWith("PLATE") ||
                name.endsWith("HEAD") ||
                name.endsWith("CORAL") ||
                name.endsWith("VINES") ||
                name.endsWith("PLANT") ||
                name.endsWith("CROP")
        ) return false;
        switch (name) {
            case "WALL_SIGN":
            case "STRING":
            case "LIGHT":
            case "BUBBLE_COLUMN":
            case "CONDUIT":
            case "KELP":
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
            case "CORNFLOWER":
            case "LEAF_LITTER":
            case "TORCH_FLOWER":
            case "CACTUS_FLOWER":
            case "OPEN_EYEBLOSSOM":
            case "CLOSED_EYEBLOSSOM":
            case "PINK_PETALS":
            case "WILDFLOWERS":
            case "WARPED_FUNGUS":
            case "CRIMSON_FUNGUS":
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
            case "GLOW_BERRIES":
            case "BIG_DRIPLEAF":
            case "BIG_DRIPLEAF_STEM":
            case "SMALL_DRIPLEAF":
            case "HANGING_ROOTS":
            case "SPORE_BLOSSOM":
            case "CHAIN":
            case "BEETROOT_BLOCK":
            case "SKULL":
            case "REPEATER":
            case "COMPARATOR":
            case "TRIPWIRE":
            case "TRIPWIRE_HOOK":
            case "REDSTONE_COMPARATOR_ON":
            case "REDSTONE_COMPARATOR_OFF":
            case "REDSTONE_TORCH":
            case "REDSTONE_WALL_TORCH":
            case "REDSTONE_WIRE":
            case "STRIPPED_CRIMSON_STEM":
            case "STRIPPED_WARPED_STEM":
            case "ATTACHED_PUMPKIN_STEM":
            case "ATTACHED_MELON_STEM":
            case "PUMPKIN_STEM":
            case "MELON_STEM":
            case "VINE":
            case "COCOA":
            case "NETHER_PORTAL":
            case "ENDER_PORTAL":
                return false;
        }
        return !type.isTransparent();
    }
}