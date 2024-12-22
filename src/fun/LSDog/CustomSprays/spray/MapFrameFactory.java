package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.util.NMS;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.*;

public class MapFrameFactory {


    /**
     * Just call this method and automatically run static{} to init
     */
    public static void init() {}


    private static Map<String, Object> enumDirectionMap = null;

    private static boolean usingSpigot = false;

    // Object
    private static final Object itemMap,
            DataComponents_MapID;
    // Class
    private static final Class<?>
            mcMapDataClass =
                NMS.VER_1_21_R2 ? NMS.getMcClassNew("world.level.saveddata.maps.WorldMap$c") :
                NMS.VER_1_17 ? NMS.getMcClassNew("world.level.saveddata.maps.WorldMap$b") : null,
            mcMapIdClass = NMS.VER_1_20_R4 ? NMS.getMcClassNew("world.level.saveddata.maps.MapId") : null,
            mcDataComponentHolderClass = NMS.VER_1_20_R4 ? NMS.getMcClassNew("core.component.DataComponentHolder") : null,
            mcDataComponentTypeClass = NMS.VER_1_20_R4 ? NMS.getMcClassNew("core.component.DataComponentType") : null,
            mcDataComponentMapClass = NMS.VER_1_20_R4 ? NMS.getMcClassNew("core.component.DataComponentMap") : null,
            mcPatchedDataComponentMapClass = NMS.VER_1_20_R4 ? NMS.getMcClassNew("core.component.PatchedDataComponentMap") : null;
    // Constructor
    private static final MethodHandle
            cItemFrame,
            cPacketPlayOutSpawnEntity,
            cItemStack,
            cNBTTagCompound,
            cPacketPlayOutMap,
            cMapData,
            cMapId;
    // Method
    private static final MethodHandle ItemFrame_setInvisible,
            ItemFrame_setSilent,
            ItemFrame_setItem,
            ItemFrame_setRotation,
            NbtTagCompound_setInt,
            Map_setTag,
            DataComponentHolder_getComponents,
            PatchedDataComponentMap_set;

    static {

        int subVer = NMS.getSubVer();
        int subRVer = NMS.getSubRVer();

        try {
            if (Thread.currentThread().getContextClassLoader().loadClass("org.bukkit.Server$Spigot") != null) {
                usingSpigot = true;
            }
        } catch (ClassNotFoundException e) {
            usingSpigot = false;
        }

        try {

            String name;

            name = "sI";
            if (subVer <= 16) {
                name = "FILLED_MAP";
            } else if (subVer <= 18) {
                name = "pp";
            } else if (subVer == 19) {
                switch (subRVer) {
                    case 1: name = "qc"; break;
                    case 2: name = "qE"; break;
                    default:
                    case 3: name = "rb"; break;
                }
            } else if (subVer == 20) switch (subRVer) {
                case 1: case 2: name = "rf"; break;
                case 3: name = "rR"; break;
                case 4: name = "rU"; break;
            } else if (subVer == 21) switch (subRVer) {
                case 1: name = "rU"; break;
                case 2: name = "sI"; break;
                case 3: name = "sR"; break;
            }
            itemMap = NMS.getDeclaredFieldObject(NMS.mcItemsClass, name, null);

            DataComponents_MapID =
                    NMS.VER_1_20_R4 ? NMS.getDeclaredFieldObject(NMS.getMcClassNew("core.component.DataComponents"), NMS.VER_1_21_R2 ? "L" : "B", null) : null;

            if (subVer <= 7) {
                cItemFrame = NMS.getConstructor(NMS.mcEntityItemFrameClass, MethodType.methodType(void.class, int.class, int.class, int.class, int.class));
            } else {
                cItemFrame = NMS.getConstructor(NMS.mcEntityItemFrameClass, MethodType.methodType(void.class, NMS.mcWorldClass, NMS.mcBlockPositionClass, NMS.mcEnumDirectionClass));
            }

            cPacketPlayOutSpawnEntity = NMS.getConstructor(NMS.getPacketClass("PacketPlayOutSpawnEntity"),
                    (subVer <= 13) ? MethodType.methodType(void.class, NMS.mcEntityClass, int.class, int.class)
                            : (subVer <= 20) ? MethodType.methodType(void.class, NMS.mcEntityClass, int.class)
                            : MethodType.methodType(void.class, NMS.mcEntityClass, int.class, NMS.mcBlockPositionClass));

            if (subVer <= 12) {
                cItemStack = NMS.getConstructor(NMS.mcItemStackClass, MethodType.methodType(void.class, NMS.mcItemClass, int.class, int.class));
            } else {
                cItemStack = NMS.getConstructor(NMS.mcItemStackClass, MethodType.methodType(void.class, NMS.mcIMaterialClass));
            }

            if (subVer <= 19 || (subVer == 20 && subRVer <= 3)) {
                cNBTTagCompound = NMS.getConstructor(NMS.mcNBTTagCompoundClass, MethodType.methodType(void.class));
                NbtTagCompound_setInt = NMS.getMethodVirtual(NMS.mcNBTTagCompoundClass,
                        subVer <= 17 ? "setInt" : "a", MethodType.methodType(void.class, String.class, int.class));
                Map_setTag = NMS.getMethodVirtual(NMS.mcItemStackClass, (subVer <= 17 ? "setTag" : "c"), MethodType.methodType(void.class, NMS.mcNBTTagCompoundClass));
            } else {
                cNBTTagCompound = null; NbtTagCompound_setInt = null; Map_setTag = null;
            }

            cMapId = NMS.VER_1_20_R4 ? NMS.getConstructor(mcMapIdClass, MethodType.methodType(void.class, int.class)) : null;

            cPacketPlayOutMap = NMS.getConstructor(NMS.getPacketClass("PacketPlayOutMap"),
                    (subVer == 7) ? (usingSpigot ? MethodType.methodType(int.class, byte[].class, byte.class) : MethodType.methodType(int.class, byte[].class)):
                    (subVer == 8) ? MethodType.methodType(void.class, int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class):
                    (subVer <= 13) ? MethodType.methodType(void.class, int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class):
                    (subVer <= 16) ? MethodType.methodType(void.class, int.class, byte.class, boolean.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class):
                    !NMS.VER_1_20_R4 ? MethodType.methodType(void.class, int.class, byte.class, boolean.class, Collection.class, mcMapDataClass):
                    MethodType.methodType(void.class, mcMapIdClass, byte.class, boolean.class, Optional.class, Optional.class)
            );

            cMapData = NMS.VER_1_17 ? NMS.getConstructor(mcMapDataClass, MethodType.methodType(void.class, int.class, int.class, int.class, int.class, byte[].class)):null;

            if (subVer <= 17) {
                ItemFrame_setInvisible = NMS.getMethodVirtual(NMS.mcEntityItemFrameClass, "setInvisible", MethodType.methodType(void.class, boolean.class));
            } else {
                ItemFrame_setInvisible = NMS.getMethodVirtual(NMS.mcEntityItemFrameClass,
                        (subVer <= 19 || (subVer == 20 && NMS.getSubRVer() <= 3)) ? "j" : "k",
                        MethodType.methodType(void.class, boolean.class));
            }

            switch (subVer) {
                case 7: name = "e"; break;
                case 8: name = "b"; break;
                case 9: name = "c"; break;
                case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17:
                    name = "setSilent"; break;
                default: name =
                        (subVer <= 19 || (subVer == 20 && NMS.getSubRVer() <= 3)) ? "d" : "e"; break;
            }
            ItemFrame_setSilent = NMS.getMethodVirtual(NMS.mcEntityClass, name, MethodType.methodType(void.class, boolean.class));

            if (subVer <= 17) {
                ItemFrame_setItem = NMS.getMethodVirtual(NMS.mcEntityItemFrameClass, "setItem", MethodType.methodType(void.class, NMS.mcItemStackClass));
            } else {
                ItemFrame_setItem = NMS.getMethodVirtual(NMS.mcEntityItemFrameClass, "setItem", MethodType.methodType(void.class, NMS.mcItemStackClass, boolean.class, boolean.class));
            }

            ItemFrame_setRotation = NMS.getMethodVirtual(NMS.mcEntityItemFrameClass, subVer<=17 ? "setRotation" : subVer == 18 ? "a" : "b", MethodType.methodType(void.class, int.class));

            DataComponentHolder_getComponents = NMS.VER_1_20_R4 ? NMS.getMethodVirtual(mcDataComponentHolderClass, "a", MethodType.methodType(mcDataComponentMapClass)) : null;

            PatchedDataComponentMap_set = NMS.VER_1_20_R4 ? NMS.getMethodVirtual(mcPatchedDataComponentMapClass, "b", MethodType.methodType(Object.class, mcDataComponentTypeClass, Object.class)) : null;

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取 NMS ItemFrame
     * @param itemStack NMS itemstack
     * @param location must be int location
     * @param rotation The item rotation in itemframe
     * @return NMS ItemFrame
     */
    protected static Object getItemFrame(Object itemStack, Location location, BlockFace blockFace, int rotation) throws Throwable {
        int subVer = NMS.getSubVer();
        Object itemFrame;
        if (subVer <= 7) {
            itemFrame = cItemFrame.invoke(
                    NMS.getMcWorld(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockFaceToIntDirection(blockFace));
        } else {
            itemFrame = cItemFrame.invoke(NMS.getMcWorld(location.getWorld()), NMS.getMcBlockPosition(location), blockFaceToEnumDirection(blockFace));
        }

        // 设为隐形（展示框1.16以上为真隐形）
        ItemFrame_setInvisible.invoke(itemFrame, true);

        // 设为静音（展示框）
        ItemFrame_setSilent.invoke(itemFrame, true);

        // 设置物品
        if (subVer <= 17) {
            ItemFrame_setItem.invoke(itemFrame, itemStack);
        } else {
            ItemFrame_setItem.invoke(itemFrame, itemStack, false, false);
        }

        // 设置旋转
        if (blockFace == BlockFace.DOWN || blockFace == BlockFace.UP) {
            ItemFrame_setRotation.invoke(itemFrame, rotation);
        }
        return itemFrame;
    }


    /**
     * Get spawn packet of ItemFrame
     */
    protected static Object getSpawnPacket(Object itemFrame, int intDirection) throws Throwable {
        int subVer = NMS.getSubVer();
        if (subVer <= 13) {
            /* ItemFrame, ItemFrameID:71, Data:Facing(int) */
            return cPacketPlayOutSpawnEntity.invoke(itemFrame, 71, intDirection);
        } else if (subVer <= 20) {
            /* ItemFrame, Data:Facing(int) */
            return cPacketPlayOutSpawnEntity.invoke(itemFrame, intDirection);
        } else {
            throw new RuntimeException("SpawnPacket in 1.21+ requires a BlockPosition!");
        }
    }

    /**
     * version >= 1.21
     * Get spawn packet of ItemFrame
     */
    protected static Object getSpawnPacket(Object itemFrame, int intDirection, Object blockPosition) throws Throwable {
        return cPacketPlayOutSpawnEntity.invoke(itemFrame, intDirection, blockPosition);
    }

    /**
     * 获取 NMS map
     */
    public static Object getMcMap(int mapViewId) throws Throwable {
        int subVer = NMS.getSubVer();
        int subRVer = NMS.getSubRVer();
        Object mcMap;
        if (subVer <= 7) {
            mcMap = cItemStack.invoke(NMS.mcItemsClass.getField("MAP").get(null), 1, (short) mapViewId);
        } else if (subVer <= 12) {
            mcMap = cItemStack.invoke(NMS.mcItemsClass.getField("FILLED_MAP").get(null), 1, (short) mapViewId);
        } else {
            mcMap = cItemStack.invoke(itemMap);
            // set map id tag to map
            if (subVer <= 19 || (subVer == 20 && subRVer <= 3)) {
                Object nbtTagCompound = cNBTTagCompound.invoke();
                NbtTagCompound_setInt.invoke(nbtTagCompound, "map", mapViewId);
                Map_setTag.invoke(mcMap, nbtTagCompound);
            } else {
                // ((PatchedDataComponentMapClass)mcMap.getComponents()).set(DataComponents.MAP_ID, new MapId(id))
                PatchedDataComponentMap_set.invoke(
                        mcPatchedDataComponentMapClass.cast(DataComponentHolder_getComponents.invoke(mcMap))
                        ,DataComponents_MapID, cMapId.invoke(mapViewId)
                );
            }
        }

        return mcMap;
    }

    /**
     * 获取发送 Map图案的包
     * @param mapViewId 版本<=1.12时 mapView id 必须是 positive short, 1.13及以上为 int
     * @param pixels 像素数据
     * @return 发送Map图案的包
     */
    public static Object getMapPacket(int mapViewId, byte[] pixels) throws Throwable {
        int subVer = NMS.getSubVer();
        Object mapPacket;
        if (subVer == 8) {
            mapPacket = cPacketPlayOutMap.invoke(mapViewId, (byte) 3, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (subVer <= 13) {
            mapPacket = cPacketPlayOutMap.invoke(mapViewId, (byte) 3, false, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else if (subVer <= 16) {
            mapPacket = cPacketPlayOutMap.invoke(mapViewId, (byte) 3, false, false, Collections.emptyList(), pixels, 0, 0, 128, 128);
        } else {
            Object mapData = cMapData.invoke(0, 0, 128, 128, pixels);
            if (!NMS.VER_1_20_R4) {
                mapPacket = cPacketPlayOutMap.invoke(mapViewId, (byte) 3, false, Collections.emptyList(), mapData);
            } else {
                mapPacket = cPacketPlayOutMap.invoke(cMapId.invoke(mapViewId), (byte) 3, false, Optional.empty(), Optional.of(mapData));
            }
        }
        return mapPacket;
    }

    /**
     * 获取 1.7.10 发送 Map图案的包 <br>
     * 1.7.10及以前的地图图案是按行发送的，一次128个颜色 <br>
     * [包类型(0表示颜色数组), 起始 x, 起始 y, ...颜色数组] <br>
     * 详见 <a href="https://wiki.vg/index.php?title=Protocol&oldid=6003#Maps">wiki.vg</a> 以及客户端源码 1.7.10.jar ayi#a(byte[] byArray) 方法
     * @param mapViewId mapView的id (short)
     * @param pixels 像素数据
     * @return 发送Map图案的包
     */
    public static Object[] getMapPackets_7(short mapViewId, byte[] pixels) throws Throwable {
        Object[] mapPackets = new Object[128];
        for (byte x = 0; x != -128; x++) {
            byte[] bytes = new byte[131];
            bytes[1] = x;
            for (int y = 0; y < 128; ++y) {
                bytes[y + 3] = pixels[y * 128 + x];
            }
            if (usingSpigot) mapPackets[x] = cPacketPlayOutMap.invoke(mapViewId, bytes, (byte) 0);
            else mapPackets[x] = cPacketPlayOutMap.invoke(mapViewId, bytes);
        }
        return mapPackets;
    }

    protected static Object blockFaceToEnumDirection(BlockFace blockFace) {
        if (enumDirectionMap == null) {
            enumDirectionMap = new HashMap<>();
            Object[] enums = NMS.mcEnumDirectionClass.getEnumConstants();
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
}
