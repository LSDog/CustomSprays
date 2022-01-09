package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.manager.SprayManager;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.utils.NMS;
import fun.LSDog.CustomSprays.utils.RayTracer;
import fun.LSDog.CustomSprays.utils.TargetBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Spray {

    public final Player player;
    protected final World world;
    protected final byte[] pixels;
    protected final Collection<? extends Player> players;

    public Location location;
    public BlockFace blockFace;

    private int itemFrameId;

    public Spray(Player player, byte[] pixels, Collection<? extends Player> showTo) {
        this.player = player;
        this.world = player.getWorld();
        this.pixels = pixels;
        this.players = showTo;
    }

    /**
     * @param removeTick Negative number will disable auto remove
     */
    public boolean create(long removeTick) {
        try {

            TargetBlock targetBlock = RayTracer.getTargetBlock(player);
            if (targetBlock == null) return false;

            /* ↓不符合放置条件就取消 */
            if (!targetBlock.getBlock().getType().isSolid()) return false;
            if (targetBlock.isUpOrDown() && ( CustomSprays.getSubVer() < 13 || !CustomSprays.instant.getConfig().getBoolean("spray_on_ground") )) return false;

            this.location = targetBlock.getRelativeBlock().getLocation();
            this.blockFace = targetBlock.getBlockFace();

            /* ↓喷漆有占用就取消 */
            if (SprayManager.getSpray(location, blockFace) != null) return false;

            spawn(players);
            SprayManager.addSpray(this);

            if (removeTick >= 0) autoRemove(removeTick);

        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void spawn(Collection<? extends Player> playersShowTo) throws ReflectiveOperationException {

        int mapViewId = MapViewId.getId();
        Object mcMap = getMcMap(mapViewId);
        Object mapPacket = getMapPacket(mapViewId, pixels);
        Object itemFrame = getItemFrame(mcMap, location);
        Object spawnPacket = getSpawnPacket(itemFrame);
        // get id
        itemFrameId = (int) itemFrame.getClass().getMethod(CustomSprays.getSubVer()<18?"getId":"ae").invoke(itemFrame);

        Object dataWatcher = itemFrame.getClass().getMethod(CustomSprays.getSubVer()<18?"getDataWatcher":"ai").invoke(itemFrame);

        Object dataPacket = NMS.getPacketClass("PacketPlayOutEntityMetadata")
                .getConstructor(int.class, NMS.getMcDataWatcherClass(), boolean.class)
                .newInstance(itemFrameId, dataWatcher, false);

        for (Player p : playersShowTo) {
            NMS.sendPacket(p, spawnPacket);  // spawns a itemFrame with map
            NMS.sendPacket(p, dataPacket);  // add dataWatcher for itemFrame
            NMS.sendPacket(p, mapPacket);  // refresh mapView (draw image)
        }
        SoundEffects.playSound(player, SoundEffects.Effect.SPRAY);

    }

    public void autoRemove(long tick) {
        Bukkit.getScheduler().runTaskLater(CustomSprays.instant, () -> SprayManager.removeSpray(player, this), tick);
    }

    public void destroy() {
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                NMS.sendPacket(p, NMS.getPacketClass("PacketPlayOutEntityDestroy").getConstructor(int[].class).newInstance( new Object[]{new int[]{itemFrameId}} ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getMcMap(int mapViewId) throws ReflectiveOperationException {
        /* 版本<=1.12时 mapView id 必须是 positive short, 1.13及以上为 int*/
        Object mcMap;
        if (CustomSprays.getSubVer() < 13) {
            mcMap = NMS.getMcItemStackClass()
                    .getConstructor(NMS.getMcItemClass(), int.class, int.class)
                    .newInstance(NMS.getMcItemsClass().getField("FILLED_MAP").get(null), 1, (short) mapViewId);
        } else {
            String itemFieldName = "FILLED_MAP";
            switch (CustomSprays.getSubVer()) {
                case 17: case 18:
                    itemFieldName = "pp"; break;
            }
            mcMap = NMS.getMcItemStackClass()
                    .getConstructor(NMS.getMcIMaterialClass())
                    .newInstance(NMS.getMcItemsClass().getField(itemFieldName).get(null));

            Object nbtTagCompound = NMS.getMcNBTTagCompoundClass().getConstructor().newInstance();
            if (CustomSprays.getSubVer() < 18) {
                nbtTagCompound.getClass().getMethod("setInt", String.class, int.class).invoke(nbtTagCompound, "map", mapViewId);
                mcMap.getClass().getMethod("setTag", NMS.getMcNBTTagCompoundClass()).invoke(mcMap, nbtTagCompound);
            } else {
                nbtTagCompound.getClass().getMethod("a", String.class, int.class).invoke(nbtTagCompound, "map", mapViewId);
                mcMap.getClass().getDeclaredMethod("c", NMS.getMcNBTTagCompoundClass()).invoke(mcMap, nbtTagCompound);
            }
        }
        return mcMap;
    }

    public static Object getMapPacket(int mapViewId, byte[] pixels) throws ReflectiveOperationException {
        Object mapPacket;
        if (CustomSprays.getSubVer() == 8) {
            mapPacket = NMS.getPacketClass("PacketPlayOutMap")
                    .getConstructor(int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)
                    .newInstance(mapViewId, (byte) 3, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (CustomSprays.getSubVer() < 14) {
            mapPacket = NMS.getPacketClass("PacketPlayOutMap")
                    .getConstructor(int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)
                    .newInstance(mapViewId, (byte) 3, false, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (CustomSprays.getSubVer() < 17) {
            mapPacket = NMS.getPacketClass("PacketPlayOutMap")
                    .getConstructor(int.class, byte.class, boolean.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)
                    .newInstance(mapViewId, (byte) 3, false, false, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else {
            Class<?> mapDataClass = Class.forName("net.minecraft.world.level.saveddata.maps.WorldMap$b");
            Object mapData = mapDataClass
                    .getConstructor(int.class, int.class, int.class, int.class, byte[].class)
                    .newInstance(0, 0, 128, 128, pixels);
            mapPacket = NMS.getPacketClass("PacketPlayOutMap")
                    .getConstructor(int.class, byte.class, boolean.class, Collection.class, mapDataClass)
                    .newInstance(mapViewId, (byte) 3, false, Collections.emptyList(), mapData);
        }
        return mapPacket;
    }

    protected Object getItemFrame(Object mcMap, Location location) throws ReflectiveOperationException {
        Object itemFrame =
                NMS.getMcEntityItemFrameClass()
                .getConstructor(NMS.getMcWorldClass(), NMS.getMcBlockPositionClass(), NMS.getMcEnumDirectionClass())
                .newInstance(
                        NMS.getMcWorld(world),
                        NMS.getMcBlockPosition(location),
                        blockFaceToEnumDirection(blockFace)
                );
        // set invisible
        switch (CustomSprays.getSubVer()) {
            case 18:
                itemFrame.getClass().getMethod("j", boolean.class).invoke(itemFrame, true); break;
            case 16: case 17: default:
                itemFrame.getClass().getMethod("setInvisible", boolean.class).invoke(itemFrame, true);
        }
        //set silent
        switch (CustomSprays.getSubVer()) {
            case 8: NMS.getMcEntityClass().getMethod("b", boolean.class).invoke(itemFrame, true); break;
            case 9: NMS.getMcEntityClass().getMethod("c", boolean.class).invoke(itemFrame, true); break;
            case 18: NMS.getMcEntityClass().getMethod("d", boolean.class).invoke(itemFrame, true); break;
            default: NMS.getMcEntityClass().getMethod("setSilent", boolean.class).invoke(itemFrame, true);
        }
        // set item
        if (CustomSprays.getSubVer() < 18) {
            itemFrame.getClass().getMethod("setItem", NMS.getMcItemStackClass()).invoke(itemFrame, mcMap);
        } else {
            itemFrame.getClass().getMethod("setItem", NMS.getMcItemStackClass(), boolean.class, boolean.class).invoke(itemFrame, mcMap, false, false);
        }
        // set rotation
        if (blockFace == BlockFace.DOWN || blockFace == BlockFace.UP) {
            Method setRotation = itemFrame.getClass().getDeclaredMethod(CustomSprays.getSubVer()<18?"setRotation":"a", int.class, boolean.class);
            setRotation.setAccessible(true);
            setRotation.invoke(itemFrame, getItemFrameRotate(player.getLocation(), blockFace), false);
        }
        return itemFrame;
    }

    protected Object getSpawnPacket(Object itemFrame) throws ReflectiveOperationException {
        if (CustomSprays.getSubVer() < 14) {
            /* ItemFrame.class, ItemFrameID:71, Data:Facing(int) */
            return NMS.getPacketClass("PacketPlayOutSpawnEntity")
                    .getConstructor(NMS.getMcEntityClass(), int.class, int.class)
                    .newInstance(itemFrame, 71, blockFaceToIntDirection(blockFace));
        } else {
            /* ItemFrame.class, Data:Facing(int) */
            return NMS.getPacketClass("PacketPlayOutSpawnEntity")
                    .getConstructor(NMS.getMcEntityClass(), int.class)
                    .newInstance(itemFrame, blockFaceToIntDirection(blockFace));
        }
    }

    private static Map<String, Object> enumDirectionMap = null;
    protected static Object blockFaceToEnumDirection(BlockFace blockFace) throws ReflectiveOperationException {
        if (enumDirectionMap == null) {
            enumDirectionMap = new HashMap<>();
            Object[] enums = NMS.getMcEnumDirectionClass().getEnumConstants();
            for (Object o : enums) {
                enumDirectionMap.put((String) o.getClass().getMethod("toString").invoke(o), o);
            }
        }
        switch (blockFace) {
            case DOWN: return enumDirectionMap.get("down");
            case UP: return enumDirectionMap.get("up");
            case NORTH: return enumDirectionMap.get("north");
            case SOUTH: return enumDirectionMap.get("south");
            case WEST: return enumDirectionMap.get("west");
            case EAST: return enumDirectionMap.get("east");
            default: return BlockFace.SELF;
        }
    }

    protected static int blockFaceToIntDirection(BlockFace face) {
        if (face == null) return 0;
        if (CustomSprays.getSubVer() < 13) {
            switch (face) {
                case SOUTH: return 0;
                case WEST: return 1;
                case NORTH: return 2;
                case EAST:
                default: return 3;
            }
        } else {
            switch (face) {
                case DOWN: return 0;
                case UP: return 1;
                case NORTH: return 2;
                case SOUTH: return 3;
                case WEST: return 4;
                case EAST:
                default: return 5;
            }
        }
    }

    protected int getItemFrameRotate(Location location, BlockFace face) {
        if (CustomSprays.getSubVer() < 17) {
            float yaw = location.getYaw() % 360;
            if (135 < yaw && yaw <= 225) return 0;
            else if (225 < yaw && yaw <= 315) return face==BlockFace.DOWN ? 3 : 1;
            else if (45 < yaw && yaw <= 135) return face==BlockFace.DOWN ? 1 : 3;
            else return 2;
        } else {
            float yaw = location.getYaw() % 360;
            if (135 < yaw || yaw <= -135) return 0;
            else if (-135 < yaw && yaw <= -45) return face==BlockFace.DOWN ? 3 : 1;
            else if (45 < yaw && yaw <= 135) return face==BlockFace.DOWN ? 1 : 3;
            else return 2;
        }
    }

}
