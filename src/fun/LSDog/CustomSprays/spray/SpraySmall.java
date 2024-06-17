package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Small spray (1*1)
 */
public class SpraySmall extends SprayBase {


    private Object spawnPacket = null;
    private Object mapPacket = null;
    private Object[] mapPackets_7 = null;
    private Object dataPacket = null;

    /**
     * The constructor of a spray.
     * Use valid() to initialize and calculate the spray, use {@link #spawn(Collection, boolean, boolean)} to send packet.
     * @param player The sprayer
     * @param pixels Byte color array <b>size of 128*128</b>
     * @param playerShown The players who can see this spray (in spraying).
     */
    public SpraySmall(Player player, byte[] pixels, Collection<? extends Player> playerShown) {
        super(player, pixels, playerShown);
    }

    @Override
    public void valid() throws Throwable {

        int mapViewId = MapViewId.getId();

        Location offLoc = NMS.getSubVer() >= 8 ? location : location.add(-blockFace.getModX(), 0, -blockFace.getModZ());
        Object mcMap = MapFrameFactory.getMcMap(mapViewId);
        Object itemFrame = MapFrameFactory.getItemFrame(mcMap, offLoc, blockFace, intRotation);
        itemFrameId = NMS.getMcEntityId(itemFrame);
        if (NMS.getSubVer() <= 20) spawnPacket = MapFrameFactory.getSpawnPacket(itemFrame, intDirection);
        else spawnPacket = MapFrameFactory.getSpawnPacket(itemFrame, intDirection, NMS.getMcBlockPosition(location));
        if (NMS.getSubVer() <= 7) {
            NMS.setSpawnPacketLocation_7(spawnPacket, offLoc);
            mapPackets_7 = MapFrameFactory.getMapPackets_7((short) mapViewId, pixels);
        } else {
            mapPacket = MapFrameFactory.getMapPacket(mapViewId, pixels);
        }
        dataPacket = NMS.getPacketPlayOutEntityMetadata(itemFrame);

        valid = true;
    }

    /**
     * 生成展示框与地图
     * @param playersShowTo 要展示给的玩家, null为默认初始值
     */
    @Override
    public void spawn(Collection<? extends Player> playersShowTo, boolean playSound, boolean spawnParticle) throws Throwable {

        if (!valid) return;

        Collection<? extends Player> $playersShowTo = playersShown;

        if (playersShowTo != null) {
            $playersShowTo = playersShowTo;
            playersShown.addAll($playersShowTo); // 重新生成的也要加到可见玩家里
        }

        for (Player p : $playersShowTo) {
            NMS.sendPacket(p, spawnPacket);  // 生成带地图的展示框
            NMS.sendPacket(p, dataPacket);  // 为展示框添加 dataWatcher
            // 刷新 mapView (也就是"画图")
            if (NMS.getSubVer() >= 8) NMS.sendPacket(p, mapPacket);
            else for (Object packet : mapPackets_7) NMS.sendPacket(p, packet);
        }

        if (spawnParticle && NMS.getSubVer() >= 9) ParticleUtil.playSprayParticleEffect(this, 3, 1, 0.8, 80);
        if (playSound) SprayManager.playSpraySound(player);

    }


    /**
     * 自毁
     */
    @Override
    public void remove() {
        if (!valid) return;
        valid = false;
        SprayManager.removeSpray(this);
        NMS.sendDestroyEntities(new int[]{itemFrameId}, playersShown);
    }

}
