package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.utils.NMS;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapFrameFactory {

    static Constructor<?> cItemFrame;
    static Method ItemFrame_setInvisible;
    static Method ItemFrame_setSilent;
    static Method ItemFrame_setItem;
    static Method ItemFrame_setRotation;
    private static Constructor<?> cItem;
    private static Object itemMap;
    private static Constructor<?> cNBTTagCompound;
    private static Method NbtTagCompound_setInt;
    private static Method Map_setTag;
    private static Constructor<?> cPacketPlayOutMap;
    private static Class<?> MapData;
    private static Constructor<?> cMapData;
    private static Map<String, Object> enumDirectionMap = null;


    /**
     * 获取 NMS ItemFrame
     * @param mcMap NMS map 物品
     * @param location ItemFrame 的位置, 必须是整数位(Block location)
     * @return NMS ItemFrame
     */
    protected static Object getItemFrame(Object mcMap, Location location, BlockFace blockFace, Location playerLocation) throws ReflectiveOperationException {
        int subVer = NMS.getSubVer();
        Object itemFrame;
        if (cItemFrame == null) {
            cItemFrame = NMS.getMcEntityItemFrameClass().getConstructor(NMS.getMcWorldClass(), NMS.getMcBlockPositionClass(), NMS.getMcEnumDirectionClass());
            cItemFrame.setAccessible(true);
        }
        itemFrame = cItemFrame.newInstance(NMS.getMcWorld(location.getWorld()), NMS.getMcBlockPosition(location), blockFaceToEnumDirection(blockFace));

        // 设为隐形（展示框、1.16以上为真隐形）
        if (ItemFrame_setInvisible == null)
            if (subVer <= 17) {
                ItemFrame_setInvisible = NMS.getMcEntityItemFrameClass().getMethod("setInvisible", boolean.class);
                ItemFrame_setInvisible.setAccessible(true);
            } else {
                ItemFrame_setInvisible = NMS.getMcEntityItemFrameClass().getMethod("j", boolean.class);
                ItemFrame_setInvisible.setAccessible(true);
            }
        ItemFrame_setInvisible.invoke(itemFrame, true);

        // 设为静音（展示框）
        if (ItemFrame_setSilent == null) switch (subVer) {
            case 8: ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("b", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
            case 9: ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("c", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
            case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17:
                ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("setSilent", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
            default: ItemFrame_setSilent = NMS.getMcEntityClass().getMethod("d", boolean.class); ItemFrame_setSilent.setAccessible(true); break;
        }
        ItemFrame_setSilent.invoke(itemFrame, true);

        // 设置物品
        if (subVer <= 17) {
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

        // 设置旋转
        if (blockFace == BlockFace.DOWN || blockFace == BlockFace.UP) {
            if (ItemFrame_setRotation == null) {
                ItemFrame_setRotation = itemFrame.getClass().getDeclaredMethod( subVer<=17 ? "setRotation" : "a", int.class, boolean.class);
                ItemFrame_setRotation.setAccessible(true);
            }
            ItemFrame_setRotation.invoke(itemFrame, getItemFrameRotate(playerLocation, blockFace), false);
        }
        return itemFrame;
    }


    /**
     * 获取生成 ItemFrame 的包
     */
    protected static Object getSpawnPacket(Object itemFrame, int intDirection) throws ReflectiveOperationException {
        int subVer = NMS.getSubVer();
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

    /**
     * 获取 NMS map
     */
    public static Object getMcMap(int mapViewId) throws ReflectiveOperationException {
        int subVer = NMS.getSubVer();
        Object mcMap;
        if (subVer <= 12) {
            // MAP
            if (cItem == null) {
                cItem = NMS.getMcItemStackClass().getConstructor(NMS.getMcItemClass(), int.class, int.class);
                cItem.setAccessible(true);
            }
            mcMap = cItem.newInstance(NMS.getMcItemsClass().getField("FILLED_MAP").get(null), 1, (short) mapViewId);
        } else {
            String itemFieldName;
            if (subVer <= 16) {
                itemFieldName = "FILLED_MAP";
            } else if (subVer <= 18) {
                itemFieldName = "pp";
            } else if (subVer == 19 && NMS.getSubRVer() ==1) {
                itemFieldName = "qc";
            } else {
                itemFieldName = "qE";
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
            if (subVer <= 17) {
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

    /**
     * 获取发送 Map图案的包
     * @param mapViewId 版本<=1.12时 mapView id 必须是 positive short, 1.13及以上为 int
     * @param pixels 像素数据
     * @return 发送Map图案的包
     */
    public static Object getMapPacket(int mapViewId, byte[] pixels) throws ReflectiveOperationException {
        int subVer = NMS.getSubVer();
        Object mapPacket;
        if (subVer == 8) {
            if (cPacketPlayOutMap == null) {
                cPacketPlayOutMap = NMS.getPacketClass("PacketPlayOutMap").getConstructor(int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
                cPacketPlayOutMap.setAccessible(true);
            }
            mapPacket = cPacketPlayOutMap.newInstance(mapViewId, (byte) 3, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (subVer <= 13) {
            if (cPacketPlayOutMap == null) {
                cPacketPlayOutMap = NMS.getPacketClass("PacketPlayOutMap").getConstructor(int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
                cPacketPlayOutMap.setAccessible(true);
            }
            mapPacket = cPacketPlayOutMap.newInstance(mapViewId, (byte) 3, false, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (subVer <= 16) {
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
        if (NMS.getSubVer() <= 12) {
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

    /**
     * 根据玩家朝向和方块的上下面计算展示框的旋转
     */
    protected static int getItemFrameRotate(Location location, BlockFace face) {
        if (NMS.getSubVer() <= 16) {
            float yaw = location.getYaw() % 360;
            if (135 < yaw && yaw <= 225) return 0;
            else if (225 < yaw && yaw <= 315) return face==BlockFace.DOWN ? 3 : 1;
            else if (45 < yaw && yaw <= 135) return face==BlockFace.DOWN ? 1 : 3;
            else return 2;
        } else {
            float yaw = location.getYaw() % 360;
            if (135 < yaw || yaw <= -135) return 0;
            else if (-135 < yaw && yaw <= -45) return face==BlockFace.DOWN ? 3 : 1;
            else if (45 < yaw) return face==BlockFace.DOWN ? 1 : 3;
            //else if (45 < yaw && yaw <= 135) return face==BlockFace.DOWN ? 1 : 3;
            else return 2;
        }
    }

    /**
     * 计算任意边长大喷漆的展示框【从左往右、从上往下】的位置
     */
    protected static Location[] getBigSprayLocations(int length, Location center, Location playerLocation, BlockFace face) {

        if (length % 2 == 0) return new Location[0];
        boolean isUpOrDown = (face == BlockFace.UP) || (face == BlockFace.DOWN);
        int modX = face.getModX();
        int modZ = face.getModZ();
        int count = length * length;
        int half = (length - 1) / 2;
        Location[] locs = new Location[count];

        if (!isUpOrDown) {
            if (face == BlockFace.WEST || face == BlockFace.EAST) {
                modX = - modX;
                modZ = - modZ;
            }
            for (int i = 0; i < count; i++) {
                int flatX = i % length - half;
                int flatY = - i / length + half;
                locs[i] = center.clone().add(flatX * modZ, flatY, flatX * modX);
            }
        } else {
            int rotation = getItemFrameRotate(playerLocation, face);
            boolean isNS = rotation % 2 == 0;
            int mulX;
            int mulY;
            if (face == BlockFace.DOWN) {
                if (rotation == 1 || rotation == 2) mulX = -1; else mulX = 1;
                if (rotation == 2 || rotation == 3) mulY = -1; else mulY = 1;
            } else {
                if (rotation == 0 || rotation == 1) mulX = 1; else mulX = -1;
                if (rotation == 1 || rotation == 2) mulY = 1; else mulY = -1;
            }
            for (int i = 0; i < count; i++) {
                int flatX = i % length - half;
                int flatY = - i / length + half;
                if (isNS) {
                    locs[i] = center.clone().add(flatX * mulX, 0, flatY * mulY);
                } else {
                    locs[i] = center.clone().add(flatY * mulY, 0, flatX * mulX);
                }
            }
        }
        return locs;
    }
}
