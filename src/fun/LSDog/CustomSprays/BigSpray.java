package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.manager.SprayManager;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.utils.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * 3*3大喷漆
 */
public class BigSpray extends Spray {

    private final byte[][] pixelPieces = new byte[9][16384];
    private final int[] itemFrameIds = new int[9];
    private Location[] locs;

    public BigSpray(Player player, byte[] pixels, Collection<? extends Player> showTo) {
        super(player, pixels, showTo);
        breakPixels();
    }

    @Override
    public void spawn(Collection<? extends Player> playersShowTo) throws ReflectiveOperationException {

        setLocations();

        BlockFace opposite = blockFace.getOppositeFace();

        for (int i = 0; i < 9; i++) {
            // jump over if theres no solid block behind
            if (!locs[i].getBlock().getRelative(opposite).getType().isSolid()) continue;

            int mapViewId = MapViewId.getId();

            Object mcMap = getMcMap(mapViewId);
            Object mapPacket = getMapPacket(mapViewId, pixelPieces[i]);
            Object itemFrame = getItemFrame(mcMap, locs[i]);
            Object spawnPacket = getSpawnPacket(itemFrame);
            // get id
            itemFrameIds[i] = (int) itemFrame.getClass().getMethod(CustomSprays.getSubVer() < 18 ? "getId" : "ae").invoke(itemFrame);

            Object dataWatcher = itemFrame.getClass().getMethod(CustomSprays.getSubVer() < 18 ? "getDataWatcher" : "ai").invoke(itemFrame);

            Object dataPacket = NMS.getPacketClass("PacketPlayOutEntityMetadata")
                    .getConstructor(int.class, NMS.getMcDataWatcherClass(), boolean.class)
                    .newInstance(itemFrameIds[i], dataWatcher, false);

            for (Player p : playersShowTo) {
                NMS.sendPacket(p, spawnPacket);  // spawns a itemFrame with map
                NMS.sendPacket(p, dataPacket);  // add dataWatcher for itemFrame
                NMS.sendPacket(p, mapPacket);  // refresh mapView (draw image)
            }

        }
        SoundEffects.playSound(player, SoundEffects.Effect.SPRAY);

    }

    @Override
    public void autoRemove(long tick) {
        Bukkit.getScheduler().runTaskLater(CustomSprays.instant, () -> SprayManager.removeSpray(player, this), tick);
    }

    @Override
    public void destroy() {
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (int id : itemFrameIds) {
                    NMS.sendPacket(p, NMS.getPacketClass("PacketPlayOutEntityDestroy").getConstructor(int[].class).newInstance( new Object[]{new int[]{id}} ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void breakPixels() {
        for (int i = 0; i < 384*384; i++) {
            int x = (i/128)%3;
            int y = i/(128*128*3);
            pixelPieces[x+y*3][(i%384-x*128)+(i/384-y*128)*128] = pixels[i];
        }
    }

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
        int rotation = getItemFrameRotate(player.getLocation(), blockFace);
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
