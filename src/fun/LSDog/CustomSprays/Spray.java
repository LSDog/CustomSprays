package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.Data.DataManager;
import fun.LSDog.CustomSprays.manager.SpraysManager;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.utils.BlockUtil;
import fun.LSDog.CustomSprays.utils.NMS;
import fun.LSDog.CustomSprays.utils.RayTracer;
import fun.LSDog.CustomSprays.utils.RegionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 喷漆本体，包括所有的反射发包方法
 */
public class Spray {

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

    /**
     * The constructor of Spray <br>
     * <b>pixels must be 128*128</b>
     * @param player The sprayer
     * @param pixels Byte color array
     * @param showTo The players who can see this spray (in spraying).
     */
    public Spray(Player player, byte[] pixels, Collection<? extends Player> showTo) {
        this.player = player;
        this.world = player.getWorld();
        this.pixels = pixels;
        this.playersShown = new HashSet<>(showTo);
    }

    /**
     * @param removeTick 自动移除时长, 负数将不会自动移除
     */
    public boolean create(long removeTick) {

        Location eyeLocation = player.getEyeLocation();
        RayTracer.BlockRayTraceResult targetBlock =
                new RayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.instant.getConfig().getDouble("distance")).rayTraceBlock(BlockUtil::isSpraySurfaceBlock);
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
            NMS.sendPacket(p, spawnPacket);  // spawns a itemFrame with map
            NMS.sendPacket(p, dataPacket);  // add dataWatcher for itemFrame
            NMS.sendPacket(p, mapPacket);  // refresh mapView (draw image)
        }

        if (playSound) SoundEffects.spray(player);

    }

    /**
     * 自动自毁
     * @param tick 延迟Tick后自毁
     */
    public void autoRemove(long tick) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, () -> SpraysManager.removeSpray(this), tick);
    }

    /**
     * 自毁
     */
    public void remove() {
        if (!valid) return;
        try {
            for (Player p : playersShown) {
                if (!p.isOnline()) continue;
                NMS.sendPacket(p, NMS.getPacketClass("PacketPlayOutEntityDestroy").getConstructor(int[].class).newInstance( new Object[]{new int[]{itemFrameId}} ));
            }
            valid = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
