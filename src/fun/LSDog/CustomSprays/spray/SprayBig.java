package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.util.NMS;
import fun.LSDog.CustomSprays.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * 3*3或5*5大喷漆
 */
public class SprayBig extends SprayBase {

    private final int length;
    private byte[][] pixelPieces; // 颜色像素

    private final int[] itemFrameIds; // 九宫格展示框ID (0 ~ length*length)
    private final Location[] offLocs;
    private final Object[] itemFrames;
    private final Object[] spawnPackets;
    private final Object[] mapPackets;
    private final Object[][] mapPackets_7s;
    private final Object[] dataPackets;


    /**
     * BigSpray 的构造器 <br>
     *
     * @see fun.LSDog.CustomSprays.data.DataManager#get384pxImageBytes(Player)
     * @see fun.LSDog.CustomSprays.data.DataManager#getSizedImageBytes(Player, int, int)
     * @param player Sprayer
     * @param length Side length
     * @param pixels Byte color array <b>size of 384*384 or 640*640</b>
     * @param showTo Players who can see this spray
     */
    public SprayBig(Player player, byte[] pixels, Collection<? extends Player> showTo, int length) {
        super(player, pixels, showTo);
        this.length = length;
        int size = length * length;
        itemFrameIds = new int[size];
        offLocs = new Location[size];
        itemFrames = new Object[size];
        spawnPackets = new Object[size];
        mapPackets = new Object[size];
        mapPackets_7s = new Object[size][];
        dataPackets = new Object[size];
        breakPixels();
    }

    @Override
    public void valid() throws Throwable {

        Location[] locs = getBigSprayLocations();

        int big_mode;
        big_mode = CustomSprays.plugin.getConfig().getInt("big_mode");
        if (big_mode < 0 || big_mode > 2) big_mode = 1;

        BlockFace opposite = blockFace.getOppositeFace();

        for (int i = 0; i < length * length; i++) {

            // calculate loc of spray according to the big_mode
            Location reLoc = locs[i];
            if (big_mode == 1) {
                if (!reLoc.getBlock().getRelative(opposite).getType().isSolid()) continue;
            } else if (big_mode == 2) {
                if (!reLoc.getBlock().getRelative(opposite).getType().isSolid()) {
                    Block nextBlock = reLoc.getBlock().getRelative(opposite).getRelative(opposite);
                    if (nextBlock.getType().isSolid()) reLoc = nextBlock.getRelative(blockFace).getLocation(); // 喷到它的后面的方块
                    else continue;
                }
                if (reLoc.getBlock().getType().isSolid()) {
                    Block frontBlock = reLoc.getBlock().getRelative(blockFace);
                    if (!frontBlock.getType().isSolid()) reLoc = frontBlock.getLocation(); // 喷到它的前面的方块
                    else continue;
                }
            }

            int mapViewId = MapViewId.getId();

            Object mcMap = MapFrameFactory.getMcMap(mapViewId);
            offLocs[i] = NMS.getSubVer() >= 8 ? reLoc : reLoc.add(-blockFace.getModX(), 0, -blockFace.getModZ());
            itemFrames[i] = MapFrameFactory.getItemFrame(mcMap, offLocs[i], blockFace, intRotation);
            itemFrameIds[i] = NMS.getMcEntityId(itemFrames[i]);
            if (NMS.getSubVer() <= 20) spawnPackets[i] = MapFrameFactory.getSpawnPacket(itemFrames[i], intDirection);
            else spawnPackets[i] = MapFrameFactory.getSpawnPacket(itemFrames[i], intDirection, NMS.getMcBlockPosition(locs[i]));
            if (NMS.getSubVer() <= 7) {
                NMS.setSpawnPacketLocation_7(spawnPackets[i], offLocs[i]);
                mapPackets_7s[i] = MapFrameFactory.getMapPackets_7((short) mapViewId, pixelPieces[i]);
            } else {
                mapPackets[i] = MapFrameFactory.getMapPacket(mapViewId, pixelPieces[i]);
            }
            dataPackets[i] = NMS.getPacketPlayOutEntityMetadata(itemFrames[i]);

        }

        // 设置 itemframeId 为最中间的 id
        int frameCount = length * length;
        if (itemFrameId == -1) itemFrameId = itemFrameIds[(frameCount + frameCount%2)/2 - 1];

        valid = true;

    }

    @Override
    public void spawn(Collection<? extends Player> playersShowTo, boolean playSound, boolean spawnParticle) throws Throwable {

        if (!valid) return;

        Collection<? extends Player> $playersShowTo = playersShown;

        if (playersShowTo != null) {
            $playersShowTo = playersShowTo;
            playersShown.addAll($playersShowTo); // 重新生成的也要加到可见玩家里
        }

        for (int i = 0; i < length * length; i++) {

            for (Player p : $playersShowTo) {
                NMS.sendPacket(p, spawnPackets[i]);
                NMS.sendPacket(p, dataPackets[i]);
                if (NMS.getSubVer() >= 8) NMS.sendPacket(p, mapPackets[i]);
                else for (Object packet : mapPackets_7s[i]) NMS.sendPacket(p, packet);
            }

        }

        if (spawnParticle && NMS.getSubVer() >= 9) ParticleUtil.playSprayParticleEffect(this, 4, 2, length/2.0, 40);
        if (playSound) SprayManager.playSpraySound(player);
    }

    @Override
    public void remove() {
        if (!valid) return;
        valid = false;
        SprayManager.removeSpray(this);
        NMS.sendDestroyEntities(itemFrameIds, playersShown);
    }

    /**
     * 拆n宫格
     */
    private void breakPixels() {
        byte[][] bytes = new byte[length*length][16384];
        for (int i = 0; i < 16384*length*length; i++) {
            int x = (i/128)%length; // "列"
            int y = i/(16384*length); // "行"
            bytes[x+y*length][(i%(128*length)-x*128)+(i/(128*length)-y*128)*128] = pixels[i];
            //pixelPieces[第几张][的第几个像素点] = pixels[i];
        }
        pixelPieces = bytes;
    }



    /**
     * 计算任意边长大喷漆的展示框【从左往右、从上往下】的位置
     */
    private Location[] getBigSprayLocations() {

        if (length % 2 == 0) return new Location[0];
        boolean isUpOrDown = (blockFace == BlockFace.UP) || (blockFace == BlockFace.DOWN);
        int modX = blockFace.getModX();
        int modZ = blockFace.getModZ();
        int count = length * length;
        int half = (length - 1) / 2;
        Location[] locs = new Location[count];

        if (!isUpOrDown) {
            if (blockFace == BlockFace.WEST || blockFace == BlockFace.EAST) {
                modX = - modX;
                modZ = - modZ;
            }
            for (int i = 0; i < count; i++) {
                int flatX = i % length - half;
                int flatY = - i / length + half;
                locs[i] = location.clone().add(flatX * modZ, flatY, flatX * modX);
            }
        } else {
            boolean isNS = intRotation % 2 == 0;
            int mulX;
            int mulY;
            if (blockFace == BlockFace.DOWN) {
                if (intRotation == 1 || intRotation == 2) mulX = -1; else mulX = 1;
                if (intRotation == 2 || intRotation == 3) mulY = -1; else mulY = 1;
            } else {
                if (intRotation == 0 || intRotation == 1) mulX = 1; else mulX = -1;
                if (intRotation == 1 || intRotation == 2) mulY = 1; else mulY = -1;
            }
            for (int i = 0; i < count; i++) {
                int flatX = i % length - half;
                int flatY = - i / length + half;
                if (isNS) {
                    locs[i] = location.clone().add(flatX * mulX, 0, flatY * mulY);
                } else {
                    locs[i] = location.clone().add(flatY * mulY, 0, flatX * mulX);
                }
            }
        }
        return locs;
    }

}
