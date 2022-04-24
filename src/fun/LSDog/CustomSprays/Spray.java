package fun.LSDog.CustomSprays;

import com.sun.istack.internal.Nullable;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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
        this.intDirection = blockFaceToIntDirection(blockFace);

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

    private static Constructor<?> cPacketPlayOutEntityMetadata;
    private static Method itemFrame_getId;
    private static Method itemFrame_getDataWatcher;
    /**
     * 生成展示框与地图
     * @param playersShowTo 要展示给的玩家, null为默认初始值
     */
    public void spawn(@Nullable Collection<? extends Player> playersShowTo, boolean playSound) throws ReflectiveOperationException {

        if (!valid) return;

        int mapViewId = MapViewId.getId();

        Object mcMap = getMcMap(mapViewId);
        Object mapPacket = getMapPacket(mapViewId, pixels);
        Object itemFrame = getItemFrame(mcMap, location);
        Object spawnPacket = getSpawnPacket(itemFrame);
        // itemFrameId
        if (itemFrame_getId == null) {
            itemFrame_getId = NMS.getMcEntityItemFrameClass().getMethod(CustomSprays.getSubVer()<18?"getId":"ae");
            itemFrame_getId.setAccessible(true);
        }
        itemFrameId = (int) itemFrame_getId.invoke(itemFrame);
        // dataWatcher
        if (itemFrame_getDataWatcher == null) {
            itemFrame_getDataWatcher = NMS.getMcEntityItemFrameClass().getMethod(CustomSprays.getSubVer()<18?"getDataWatcher":"ai");
            itemFrame_getDataWatcher.setAccessible(true);
        }
        Object dataWatcher = itemFrame_getDataWatcher.invoke(itemFrame);
        // dataPacket
        if (cPacketPlayOutEntityMetadata == null) {
            cPacketPlayOutEntityMetadata = NMS.getPacketClass("PacketPlayOutEntityMetadata").getConstructor(int.class, NMS.getMcDataWatcherClass(), boolean.class);
            cPacketPlayOutEntityMetadata.setAccessible(true);
        }
        Object dataPacket = cPacketPlayOutEntityMetadata.newInstance(itemFrameId, dataWatcher, false);

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


    private static Constructor<?> cItem;
    private static Object itemMap;
    private static Constructor<?> cNBTTagCompound;
    private static Method NbtTagCompound_setInt;
    private static Method Map_setTag;
    /**
     * 获取 NMS map
     * @param mapViewId mapViewId, 版本<=1.12时 mapView id 必须是 positive short, 1.13及以上为 int
     * @return NMS map
     */
    public static Object getMcMap(int mapViewId) throws ReflectiveOperationException {
        int subVer = CustomSprays.getSubVer();
        Object mcMap;
        if (subVer <= 12) {
            // MAP
            if (cItem == null) {
                cItem = NMS.getMcItemStackClass().getConstructor(NMS.getMcItemClass(), int.class, int.class);
                cItem.setAccessible(true);
            }
            mcMap = cItem.newInstance(NMS.getMcItemsClass().getField("FILLED_MAP").get(null), 1, (short) mapViewId);
        } else {
            String itemFieldName = "FILLED_MAP";
            switch (subVer) {
                case 17: case 18:
                    itemFieldName = "pp"; break;
            }
            // MAP
            if (cItem == null) {
                cItem = NMS.getMcItemStackClass().getConstructor(NMS.getMcIMaterialClass());
                cItem.setAccessible(true);
            }
            if (itemMap == null) {
                Field _itemMap = NMS.getMcItemsClass().getField(itemFieldName);
                _itemMap.setAccessible(true);
                itemMap = _itemMap.get(null);
            }
            mcMap = cItem.newInstance(itemMap);

            // NBTTagCompound
            if (cNBTTagCompound == null) {
                cNBTTagCompound = NMS.getMcNBTTagCompoundClass().getConstructor();
                cNBTTagCompound.setAccessible(true);
            }
            Object nbtTagCompound = cNBTTagCompound.newInstance();

            // set tag to map
            if (subVer < 18) {
                if (NbtTagCompound_setInt == null) {
                    NbtTagCompound_setInt = NMS.getMcNBTTagCompoundClass().getMethod("setInt", String.class, int.class);
                    NbtTagCompound_setInt.setAccessible(true);
                }
                if (Map_setTag == null) {
                    Map_setTag = mcMap.getClass().getMethod("setTag", NMS.getMcNBTTagCompoundClass());
                    Map_setTag.setAccessible(true);
                }
            } else {
                if (NbtTagCompound_setInt == null) {
                    NbtTagCompound_setInt = NMS.getMcNBTTagCompoundClass().getMethod("a", String.class, int.class);
                    NbtTagCompound_setInt.setAccessible(true);
                }
                if (Map_setTag == null) {
                    Map_setTag = mcMap.getClass().getDeclaredMethod("c", NMS.getMcNBTTagCompoundClass());
                    Map_setTag.setAccessible(true);
                }
            }
            NbtTagCompound_setInt.invoke(nbtTagCompound, "map", mapViewId);
            Map_setTag.invoke(mcMap, nbtTagCompound);
        }

        return mcMap;
    }

    private static Constructor<?> cPacketPlayOutMap;
    private static Class<?> MapData;
    private static Constructor<?> cMapData;
    /**
     * 获取发送 Map图案的包
     * @param mapViewId 版本<=1.12时 mapView id 必须是 positive short, 1.13及以上为 int
     * @param pixels 像素数据
     * @return 发送Map图案的包
     */
    public static Object getMapPacket(int mapViewId, byte[] pixels) throws ReflectiveOperationException {
        int subVer = CustomSprays.getSubVer();
        Object mapPacket;
        if (subVer == 8) {
            if (cPacketPlayOutMap == null) {
                cPacketPlayOutMap = NMS.getPacketClass("PacketPlayOutMap").getConstructor(int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
                cPacketPlayOutMap.setAccessible(true);
            }
            mapPacket = cPacketPlayOutMap.newInstance(mapViewId, (byte) 3, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (subVer < 14) {
            if (cPacketPlayOutMap == null) {
                cPacketPlayOutMap = NMS.getPacketClass("PacketPlayOutMap").getConstructor(int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
                cPacketPlayOutMap.setAccessible(true);
            }
            mapPacket = cPacketPlayOutMap.newInstance(mapViewId, (byte) 3, false, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (subVer < 17) {
            if (cPacketPlayOutMap == null) {
                cPacketPlayOutMap = NMS.getPacketClass("PacketPlayOutMap").getConstructor(int.class, byte.class, boolean.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
                cPacketPlayOutMap.setAccessible(true);
            }
            mapPacket = cPacketPlayOutMap.newInstance(mapViewId, (byte) 3, false, false, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else {
            if (MapData == null) MapData = Class.forName("net.minecraft.world.level.saveddata.maps.WorldMap$b");
            if (cMapData == null) {
                cMapData = MapData.getConstructor(int.class, int.class, int.class, int.class, byte[].class);
                cMapData.setAccessible(true);
            }
            Object mapData = cMapData.newInstance(0, 0, 128, 128, pixels);
            if (cPacketPlayOutMap == null) {
                cPacketPlayOutMap = NMS.getPacketClass("PacketPlayOutMap").getConstructor(int.class, byte.class, boolean.class, Collection.class, MapData);
                cPacketPlayOutMap.setAccessible(true);
            }
            mapPacket = cPacketPlayOutMap.newInstance(mapViewId, (byte) 3, false, Collections.emptyList(), mapData);
        }
        return mapPacket;
    }

    private static Constructor<?> cItemFrame;
    private static Method ItemFrame_setInvisible;
    private static Method ItemFrame_setSilent;
    private static Method ItemFrame_setItem;
    private static Method ItemFrame_setRotation;
    /**
     * 获取 NMS ItemFrame
     * @param mcMap NMS map 物品
     * @param location ItemFrame 的位置, 必须是整数位(Block location)
     * @return NMS ItemFrame
     */
    protected Object getItemFrame(Object mcMap, Location location) throws ReflectiveOperationException {
        int subVer = CustomSprays.getSubVer();
        Object itemFrame;
        if (cItemFrame == null) {
            cItemFrame = NMS.getMcEntityItemFrameClass().getConstructor(NMS.getMcWorldClass(), NMS.getMcBlockPositionClass(), NMS.getMcEnumDirectionClass());
            cItemFrame.setAccessible(true);
        }
        itemFrame = cItemFrame.newInstance(NMS.getMcWorld(world), NMS.getMcBlockPosition(location), blockFaceToEnumDirection(blockFace));

        // set invisible
        if (ItemFrame_setInvisible == null) switch (subVer) {
            case 18:
                ItemFrame_setInvisible = NMS.getMcEntityItemFrameClass().getMethod("j", boolean.class);
                ItemFrame_setInvisible.setAccessible(true);
                break;
            case 16: case 17: default:
                ItemFrame_setInvisible = NMS.getMcEntityItemFrameClass().getMethod("setInvisible", boolean.class);
                ItemFrame_setInvisible.setAccessible(true);
                break;
        }
        ItemFrame_setInvisible.invoke(itemFrame, true);

        // set silent
        if (ItemFrame_setSilent == null) switch (subVer) {
            case 8: ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("b", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
            case 9: ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("c", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
            case 18: ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("d", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
            default: ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("setSilent", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
        }
        ItemFrame_setSilent.invoke(itemFrame, true);

        // set item
        if (subVer < 18) {
            if (ItemFrame_setItem == null) {
                ItemFrame_setItem = NMS.getMcEntityItemFrameClass().getMethod("setItem", NMS.getMcItemStackClass());
                ItemFrame_setItem.setAccessible(true);
            }
            ItemFrame_setItem.invoke(itemFrame, mcMap);
        } else {
            if (ItemFrame_setItem == null) {
                ItemFrame_setItem = NMS.getMcEntityItemFrameClass().getMethod("setItem", NMS.getMcItemStackClass(), boolean.class, boolean.class);
                ItemFrame_setItem.setAccessible(true);
            }
            ItemFrame_setItem.invoke(itemFrame, mcMap, false, false);
        }

        // set rotation
        if (blockFace == BlockFace.DOWN || blockFace == BlockFace.UP) {
            if (ItemFrame_setRotation == null) {
                ItemFrame_setRotation = itemFrame.getClass().getDeclaredMethod(subVer<18?"setRotation":"a", int.class, boolean.class);
                ItemFrame_setRotation.setAccessible(true);
            }
            ItemFrame_setRotation.invoke(itemFrame, getItemFrameRotate(playerLocation, blockFace), false);
        }
        return itemFrame;
    }


    /**
     * 获取生成 ItemFrame 的包
     * @param itemFrame NMS ItemFrame
     * @return 生成 ItemFrame 的包
     */
    protected Object getSpawnPacket(Object itemFrame) throws ReflectiveOperationException {
        int subVer = CustomSprays.getSubVer();
        if (subVer <= 13) {
            /* ItemFrame, ItemFrameID:71, Data:Facing(int) */
            return NMS.getPacketClass("PacketPlayOutSpawnEntity")
                    .getConstructor(NMS.getMcEntityClass(), int.class, int.class)
                    .newInstance(itemFrame, 71, intDirection);
        } else {
            /* ItemFrame, Data:Facing(int) */
            return NMS.getPacketClass("PacketPlayOutSpawnEntity")
                    .getConstructor(NMS.getMcEntityClass(), int.class)
                    .newInstance(itemFrame, intDirection);
        }
    }

    private static Map<String, Object> enumDirectionMap = null;
    protected static Object blockFaceToEnumDirection(BlockFace blockFace) {
        if (enumDirectionMap == null) {
            enumDirectionMap = new HashMap<>();
            Object[] enums = NMS.getMcEnumDirectionClass().getEnumConstants();
            for (Object o : enums) {
                enumDirectionMap.put(o.toString(), o);
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

    protected static int getItemFrameRotate(Location location, BlockFace face) {
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
