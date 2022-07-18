package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.manager.SpraysManager;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.utils.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * 3*3或5*5大喷漆
 */
public class SprayBig extends Spray {

    private final int length;
    private final int[] itemFrameIds; // 九宫格展示框ID (0 ~ length*length)
    private byte[][] pixelPieces; // 颜色像素

    /**
     * BigSpray 的构造器 <br>
     *
     * @see fun.LSDog.CustomSprays.Data.DataManager#get384pxImageBytes(Player)
     * @see fun.LSDog.CustomSprays.Data.DataManager#getSizedImageBytes(Player, int, int)
     * @param player 喷漆的玩家
     * @param pixels Byte color array <b>必为 384*384 或 640*640</b>
     * @param showTo 能看到这个喷漆的玩家
     */
    public SprayBig(Player player, int length, byte[] pixels, Collection<? extends Player> showTo) {
        super(player, pixels, showTo);
        this.length = length;
        itemFrameIds = new int[length * length];
        breakPixels();
    }

    @Override
    public void spawn(Collection<? extends Player> playersShowTo, boolean playSound) throws ReflectiveOperationException {

        if (!valid) return;

        Location[] locs = SprayFactory.getBigSprayLocations(length, location, playerLocation, blockFace);

        BlockFace opposite = blockFace.getOppositeFace();

        Collection<? extends Player> $playersShowTo = playersShown;

        if (playersShowTo != null) {
            $playersShowTo = playersShowTo;
            playersShown.addAll($playersShowTo); // 重新生成的也要加到可见玩家里
        }

        int big_mode;
        big_mode = CustomSprays.instant.getConfig().getInt("big_mode");
        if (big_mode < 0 || big_mode > 2) big_mode = 1;

        for (int i = 0; i < length * length; i++) {

            if (big_mode == 1) {

                if (!locs[i].getBlock().getRelative(opposite).getType().isSolid()) {
                    continue;
                }
            } else if (big_mode == 2) {

                if (!locs[i].getBlock().getRelative(opposite).getType().isSolid()) {
                    Block nextBlock = locs[i].getBlock().getRelative(opposite).getRelative(opposite);
                    if (nextBlock.getType().isSolid()) {
                        // 喷到它的后面的方块
                        locs[i] = nextBlock.getRelative(blockFace).getLocation();
                    } else {
                        continue;
                    }
                }

                if (locs[i].getBlock().getType().isSolid()) {
                    Block frontBlock = locs[i].getBlock().getRelative(blockFace);
                    if (!frontBlock.getType().isSolid()) {
                        // 喷到它的前面的方块
                        locs[i] = frontBlock.getLocation();
                    } else {
                        continue;
                    }
                }
            }


            int mapViewId = MapViewId.getId();

            Object mcMap = SprayFactory.getMcMap(mapViewId);
            Object mapPacket = SprayFactory.getMapPacket(mapViewId, pixelPieces[i]);
            Object itemFrame = SprayFactory.getItemFrame(mcMap, locs[i], blockFace, playerLocation);
            Object spawnPacket = SprayFactory.getSpawnPacket(itemFrame, intDirection);
            itemFrameIds[i] = (int) itemFrame.getClass().getMethod(CustomSprays.getSubVer() < 18 ? "getId" : "ae").invoke(itemFrame);
            Object dataWatcher = itemFrame.getClass().getMethod(CustomSprays.getSubVer() < 18 ? "getDataWatcher" : "ai").invoke(itemFrame);
            Object dataPacket = NMS.getPacketClass("PacketPlayOutEntityMetadata")
                    .getConstructor(int.class, NMS.getMcDataWatcherClass(), boolean.class)
                    .newInstance(itemFrameIds[i], dataWatcher, false);


            for (Player p : $playersShowTo) {
                NMS.sendPacket(p, spawnPacket);  // spawns a itemFrame with map
                NMS.sendPacket(p, dataPacket);  // add dataWatcher for itemFrame
                NMS.sendPacket(p, mapPacket);  // refresh mapView (draw image)
            }

        }

        if (playSound) Spray.playSpraySound(player);

    }

    @Override
    public void autoRemove(long tick) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, this::remove, tick);
    }

    @Override
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
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOnline()) continue;
                NMS.sendPacket(p, cPacketPlayOutEntityDestroy.newInstance( new Object[]{itemFrameIds} ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

}
