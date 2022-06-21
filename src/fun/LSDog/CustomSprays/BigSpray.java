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
 * 3*3大喷漆
 */
public class BigSpray extends Spray {

    private final byte[][] pixelPieces = new byte[9][16384]; // 九宫格 (0:0 ~ 8:16383)
    private final int[] itemFrameIds = new int[9]; // 九宫格展示框ID (0 ~ 8)
    private Location[] locs; // 九宫格位置 (0 ~ 8)

    /**
     * The constructor of BigSpray <br>
     * <b>pixels must be 384*384</b>
     * @param player The sprayer
     * @param pixels Byte color array
     * @param showTo The players who can see this spray (in spraying).
     */
    public BigSpray(Player player, byte[] pixels, Collection<? extends Player> showTo) {
        super(player, pixels, showTo);
        breakPixels();
    }

    @Override
    public void spawn(Collection<? extends Player> playersShowTo, boolean playSound) throws ReflectiveOperationException {

        if (!valid) return;

        setLocations();

        BlockFace opposite = blockFace.getOppositeFace();

        Collection<? extends Player> $playersShowTo = playersShown;

        if (playersShowTo != null) {
            $playersShowTo = playersShowTo;
            playersShown.addAll($playersShowTo); // 重新生成的也要加到可见玩家里
        }

        for (int i = 0; i < 9; i++) {

            if (!locs[i].getBlock().getRelative(opposite).getType().isSolid()) {
                Block nextBlock = locs[i].getBlock().getRelative(opposite).getRelative(opposite);
                if (nextBlock.getType().isSolid()) {
                    // spray to the next(next) block of it
                    locs[i] = nextBlock.getRelative(blockFace).getLocation();
                } else {
                    continue;
                }
            }
            if (locs[i].getBlock().getType().isSolid()) {
                Block frontBlock = locs[i].getBlock().getRelative(blockFace);
                if (!frontBlock.getType().isSolid()) {
                    // spray to the front block of it
                    locs[i] = frontBlock.getLocation();
                } else {
                    continue;
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

        if (playSound) SoundEffects.spray(player);

    }

    @Override
    public void autoRemove(long tick) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.instant, () -> SpraysManager.removeSpray(this), tick);
    }

    @Override
    public void remove() {
        valid = false;
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOnline()) continue;
                for (int id : itemFrameIds) {
                    NMS.sendPacket(p, NMS.getPacketClass("PacketPlayOutEntityDestroy").getConstructor(int[].class).newInstance( new Object[]{new int[]{id}} ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拆九宫格
     */
    private void breakPixels() {
        for (int i = 0; i < 384*384; i++) {
            int x = (i/128)%3;        // x:0~2 "列"
            int y = i/(128*128*3);    // y:0~2 "行"
            pixelPieces[x+y*3][(i%384-x*128)+(i/384-y*128)*128] = pixels[i];
            //pixelPieces[第几张][的第几个像素点] = pixels[i];
        }
    }

    /**
     * 找九宫格各自的位置
     */
    private void setLocations() {
        switch (blockFace) {
            case SOUTH:
                locs = new Location[]{
                        l(-1,1,0),l(0,1,0),l(1,1,0),
                        l(-1,0,0),l(0,0,0),l(1,0,0),
                        l(-1,-1,0),l(0,-1,0),l(1,-1,0),
                }; return;
            case NORTH:
                locs = new Location[]{
                        l(1,1,0),l(0,1,0),l(-1,1,0),
                        l(1,0,0),l(0,0,0),l(-1,0,0),
                        l(1,-1,0),l(0,-1,0),l(-1,-1,0),
                }; return;
            case EAST:
                locs = new Location[]{
                        l(0,1,1),l(0,1,0),l(0,1,-1),
                        l(0,0,1),l(0,0,0),l(0,0,-1),
                        l(0,-1,1),l(0,-1,0),l(0,-1,-1),
                }; return;
            case WEST:
                locs = new Location[]{
                        l(0,1,-1),l(0,1,0),l(0,1,1),
                        l(0,0,-1),l(0,0,0),l(0,0,1),
                        l(0,-1,-1),l(0,-1,0),l(0,-1,1),
                }; return;
        }
        int rotation = SprayFactory.getItemFrameRotate(playerLocation, blockFace);
        if (blockFace == BlockFace.UP) {
            switch (rotation) {
                case 0:
                    locs = new Location[]{
                            l(-1,0,-1),l(0,0,-1),l(1,0,-1),
                            l(-1,0,0),l(0,0,0),l(1,0,0),
                            l(-1,0,1),l(0,0,1),l(1,0,1),
                    }; return;
                case 1:
                    locs = new Location[]{
                            l(1,0,-1),l(1,0,0),l(1,0,1),
                            l(0,0,-1),l(0,0,0),l(0,0,1),
                            l(-1,0,-1),l(-1,0,0),l(-1,0,1),
                    }; return;
                case 2:
                    locs = new Location[]{
                            l(1,0,1),l(0,0,1),l(-1,0,1),
                            l(1,0,0),l(0,0,0),l(-1,0,0),
                            l(1,0,-1),l(0,0,-1),l(-1,0,-1),
                    }; return;
                case 3:
                    locs = new Location[]{
                            l(-1,0,1),l(-1,0,0),l(-1,0,-1),
                            l(0,0,1),l(0,0,0),l(0,0,-1),
                            l(1,0,1),l(1,0,0),l(1,0,-1),
                    };
            }
        } else if (blockFace == BlockFace.DOWN) {
            switch (rotation) {
                case 0:
                    locs = new Location[]{
                            l(-1,0,1),l(0,0,1),l(1,0,1),
                            l(-1,0,0),l(0,0,0),l(1,0,0),
                            l(-1,0,-1),l(0,0,-1),l(1,0,-1),
                    }; return;
                case 1:
                    locs = new Location[]{
                            l(1,0,1),l(1,0,0),l(1,0,-1),
                            l(0,0,1),l(0,0,0),l(0,0,-1),
                            l(-1,0,1),l(-1,0,0),l(-1,0,-1),
                    }; return;
                case 2:
                    locs = new Location[]{
                            l(1,0,-1),l(0,0,-1),l(-1,0,-1),
                            l(1,0,0),l(0,0,0),l(-1,0,0),
                            l(1,0,1),l(0,0,1),l(-1,0,1),
                    }; return;
                case 3:
                    locs = new Location[]{
                            l(-1,0,-1),l(-1,0,0),l(-1,0,1),
                            l(0,0,-1),l(0,0,0),l(0,0,1),
                            l(1,0,-1),l(1,0,0),l(1,0,1),
                    };
            }
        }
    }
    private Location l(double x, double y, double z) {
        return location.clone().add(x,y,z);
    }
}
