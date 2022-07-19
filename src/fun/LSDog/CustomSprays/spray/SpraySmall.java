package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.utils.NMS;
import fun.LSDog.CustomSprays.utils.RayTracer;
import fun.LSDog.CustomSprays.utils.RegionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 喷漆本体，包括所有的反射发包方法
 */
public class SpraySmall {

    public final Player player;
    protected final World world;
    protected final byte[] pixels;
    protected final Set<Player> playersShown;

    public Block block;
    public Location location;
    public BlockFace blockFace;
    protected Location playerLocation;
    protected int intDirection;

    private int itemFrameId;
    protected boolean valid = true;

    protected Constructor<?> cPacketPlayOutEntityDestroy;

    /**
     * 喷漆的构造器
     * @param player The sprayer
     * @param pixels Byte color array <b>必为 128*128</b>
     * @param showTo The players who can see this spray (in spraying).
     */
    public SpraySmall(Player player, byte[] pixels, Collection<? extends Player> showTo) {
        this.player = player;
        this.world = player.getWorld();
        this.pixels = pixels;
        this.playersShown = new HashSet<>(showTo);
    }

    /**
     * 向某个玩家播放喷漆音效
     */
    public static void playSpraySound(Player player) {
        String sound = CustomSprays.instant.getConfig().getString("spray_sound");
        if (sound == null || "default".equals(sound)) {
            if (CustomSprays.getSubVer() == 8) player.getWorld().playSound(player.getLocation(), Sound.valueOf("SILVERFISH_HIT"), 1, 0.8F);
            else player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1, 0.8F);
        } else {
            String[] strings = sound.split("-");
            if (strings.length == 3) {
                player.getWorld().playSound(player.getLocation(), strings[0], Float.parseFloat(strings[1]), Float.parseFloat(strings[2]));
            }
        }
    }

    /**
     * @param removeTick 自动移除时长, 负数将不会自动移除
     */
    public boolean create(long removeTick) {

        Location eyeLocation = player.getEyeLocation();
        RayTracer.BlockRayTraceResult targetBlock =
                new RayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.instant.getConfig().getDouble("distance")).rayTraceBlock(SpraysManager::isSpraySurfaceBlock);
        if (targetBlock == null) return false;

        // 禁止在1.13以下, 在方块上下面喷漆
        if (targetBlock.isUpOrDown() && ( CustomSprays.getSubVer() < 13 || !CustomSprays.instant.getConfig().getBoolean("spray_on_ground") )) return false;

        this.block = targetBlock.getRelativeBlock();
        this.location = block.getLocation();
        this.blockFace = targetBlock.blockFace;
        this.playerLocation = player.getLocation();
        this.intDirection = SprayFactory.blockFaceToIntDirection(blockFace);

        // ↓喷漆占用就取消
        if (SpraysManager.getSpray(targetBlock.getRelativeBlock(), blockFace) != null) return false;

        // 喷漆在禁止区域就取消
        if (!player.hasPermission("CustomSprays.nodisable") && RegionChecker.isLocInDisabledRegion(location)) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.DISABLED_REGION"));
            return false;
        }

        try {
            spawn(playersShown, true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
        SpraysManager.addSpray(this);
        if (removeTick >= 0) autoRemove(removeTick);

        return true;
    }

    /**
     * 生成展示框与地图
     * @param playersShowTo 要展示给的玩家, null为默认初始值
     */
    public void spawn(Collection<? extends Player> playersShowTo, boolean playSound) throws ReflectiveOperationException {

        if (!valid) return;

        int mapViewId = MapViewId.getId();

        Object mcMap = SprayFactory.getMcMap(mapViewId);
        Object mapPacket = SprayFactory.getMapPacket(mapViewId, pixels);
        Object itemFrame = SprayFactory.getItemFrame(mcMap, location, blockFace, playerLocation);
        Object spawnPacket = SprayFactory.getSpawnPacket(itemFrame, intDirection);

        // itemFrameId
        if (SprayFactory.itemFrame_getId == null) {
            SprayFactory.itemFrame_getId = NMS.getMcEntityItemFrameClass().getMethod(CustomSprays.getSubVer()<18?"getId":"ae");
            SprayFactory.itemFrame_getId.setAccessible(true);
        }
        itemFrameId = (int) SprayFactory.itemFrame_getId.invoke(itemFrame);

        // dataWatcher
        if (SprayFactory.itemFrame_getDataWatcher == null) {
            SprayFactory.itemFrame_getDataWatcher = NMS.getMcEntityItemFrameClass().getMethod(CustomSprays.getSubVer()<18?"getDataWatcher":"ai");
            SprayFactory.itemFrame_getDataWatcher.setAccessible(true);
        }
        Object dataWatcher = SprayFactory.itemFrame_getDataWatcher.invoke(itemFrame);

        // dataPacket
        if (SprayFactory.cPacketPlayOutEntityMetadata == null) {
            SprayFactory.cPacketPlayOutEntityMetadata = NMS.getPacketClass("PacketPlayOutEntityMetadata").getConstructor(int.class, NMS.getMcDataWatcherClass(), boolean.class);
            SprayFactory.cPacketPlayOutEntityMetadata.setAccessible(true);
        }
        Object dataPacket = SprayFactory.cPacketPlayOutEntityMetadata.newInstance(itemFrameId, dataWatcher, false);

        Collection<? extends Player> $playersShowTo = playersShown;

        if (playersShowTo != null) {
            $playersShowTo = playersShowTo;
            playersShown.addAll($playersShowTo); // 重新生成的也要加到可见玩家里
        }

        for (Player p : $playersShowTo) {
            NMS.sendPacket(p, spawnPacket);  // 生成带地图的展示框
            NMS.sendPacket(p, dataPacket);  // 为展示框添加 dataWatcher
            NMS.sendPacket(p, mapPacket);  // 刷新 mapView (也就是"画图")
        }

        if (playSound) playSpraySound(player);

    }

    /**
     * 自动自毁
     * @param tick 延迟Tick后自毁
     */
    public void autoRemove(long tick) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, this::remove, tick);
    }

    /**
     * 自毁
     */
    public void remove() {
        if (!valid) return;
        valid = false;
        SpraysManager.removeSpray(this);
        if (cPacketPlayOutEntityDestroy == null) {
            try {
                cPacketPlayOutEntityDestroy = NMS.getPacketClass("PacketPlayOutEntityDestroy").getConstructor(int[].class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        try {
            for (Player p : playersShown) {
                NMS.sendPacket(p, cPacketPlayOutEntityDestroy.newInstance( new Object[]{new int[]{itemFrameId}} ));
            }
            valid = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
