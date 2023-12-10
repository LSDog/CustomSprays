package fun.LSDog.CustomSprays.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全是 Object 的跨版本NMS适配机器（
 */
public class NMS {

    private static final Map<String, Class<?>> mcClassMap = new HashMap<>();

    private static String version = null;
    private static int subVer = 0;
    private static int subRVer = 0;
    private static Method Entity_getId;
    private static Method Entity_getDataWatcher;
    private static Constructor<?> cPacketPlayOutEntityMetadata;
    
    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Method PlayerConnection_SendPacket;
    private static Method DataWatcher_getNonDefaultValues;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Object getDeclaredField(Object object, String name) {
        if (object == null || name == null) return null;
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static Class<?> getMcClass(String name) {
        Class<?> clazz = mcClassMap.get(name);
        if (clazz == null) {
            clazz = getClass("net.minecraft."+name);
            mcClassMap.put(name, clazz);
        }
        return clazz;
    }
    private static Constructor<?> cPacketPlayOutEntityDestroy;
    private static Class<?> mcWorldClass = null;
    private static Class<?> mcEntityClass = null;
    private static Class<?> mcEntityPlayerClass = null;
    private static Class<?> mcEntityItemFrameClass = null;
    private static Class<?> mcPlayerConnectionClass = null;
    private static Class<?> mcItemStackClass = null;
    private static Class<?> mcIMaterialClass = null;
    private static Class<?> mcItemClass = null;
    private static Class<?> mcItemsClass = null;
    private static Class<?> mcDataWatcherClass = null;
    private static Class<?> mcBlockPositionClass = null;
    private static Class<?> mcEnumDirectionClass = null;
    private static Class<?> mcNBTTagCompound = null;

    /**
     * 返回 NMS 版本 (例如 v1_12_R1)
     */
    public static String getMcVer() {
        return version == null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
    }

    /**
     * 返回版本号的第二项<br>
     * v1_12_R1 -> 12
     */
    public static int getSubVer() {
        return subVer == 0 ? subVer = Integer.parseInt(getMcVer().split("_")[1]) : subVer;
    }

    /**
     * 返回版本号的第三项 (R开头的那个)<br>
     * v1_12_R1 -> 1
     */
    public static int getSubRVer() {
        return subRVer == 0 ? subRVer = Integer.parseInt(getMcVer().split("_")[2].substring(1)) : subRVer;
    }

    public static void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        if (PlayerConnection_SendPacket == null) {
            if (getSubVer() <= 17) PlayerConnection_SendPacket = getMcPlayerConnectionClass().getMethod("sendPacket", getPacketClass());
            else if (getSubVer() < 20 || (getSubVer() == 20 && getSubRVer() <= 1)) PlayerConnection_SendPacket = getMcPlayerConnectionClass().getMethod("a", getPacketClass());
            else PlayerConnection_SendPacket = getMcPlayerConnectionClass().getMethod("b", getPacketClass()); // wtf 你为什么要在1.20.2这个小版本改这个 mojang你丧尽天良啊啊啊啊啊啊
        }
        PlayerConnection_SendPacket.invoke(getMcPlayerConnection(player), packet);
    }

    // "Legacy" 指的是版本 < 1.17, 因为 NMS 在1.17后相应类的路径大改
    public static Class<?> getLegacyMcClass(String name) {
        Class<?> clazz = mcClassMap.get(name);
        if (clazz == null) {
            clazz = getClass("net.minecraft.server."+getMcVer()+"."+name);
            mcClassMap.put(name, clazz);
        }
        return clazz;
    }

    public static Class<?> getPacketClass() {
        if (getSubVer() < 17) return getLegacyMcClass("Packet");
        else return getMcClass("network.protocol.Packet");
    }

    public static Class<?> getPacketClass(String paketName) {
        if (getSubVer() < 17) return getLegacyMcClass(paketName);
        else return getMcClass("network.protocol.game."+paketName);
    }

    public static Class<?> getMcWorldClass() {
        if (getSubVer() < 17) return mcWorldClass == null ? mcWorldClass = getLegacyMcClass("World") : mcWorldClass;
        else return mcWorldClass == null ? mcWorldClass = getMcClass("world.level.World") : mcWorldClass;
    }

    public static Class<?> getMcEntityClass() {
        if (getSubVer() < 17) return mcEntityClass == null ? mcEntityClass = getLegacyMcClass("Entity") : mcEntityClass;
        else return mcEntityClass == null ? mcEntityClass = getMcClass("world.entity.Entity") : mcEntityClass;
    }

    public static Class<?> getMcEntityPlayerClass() {
        if (getSubVer() < 17) return mcEntityPlayerClass == null ? mcEntityPlayerClass = getLegacyMcClass("EntityPlayer") : mcEntityPlayerClass;
        else return mcEntityPlayerClass == null ? mcEntityPlayerClass = getMcClass("server.level.EntityPlayer") : mcEntityPlayerClass;
    }

    public static Class<?> getMcEntityItemFrameClass() {
        if (getSubVer() < 17) return mcEntityItemFrameClass == null ? mcEntityItemFrameClass = getLegacyMcClass("EntityItemFrame") : mcEntityItemFrameClass;
        else return mcEntityItemFrameClass == null ? mcEntityItemFrameClass = getMcClass("world.entity.decoration.EntityItemFrame") : mcEntityItemFrameClass;
    }

    public static Class<?> getMcPlayerConnectionClass() {
        if (getSubVer() < 17) return mcPlayerConnectionClass == null ? mcPlayerConnectionClass = getLegacyMcClass("PlayerConnection") : mcPlayerConnectionClass;
        else return mcPlayerConnectionClass == null ? mcPlayerConnectionClass = getMcClass("server.network.PlayerConnection") : mcPlayerConnectionClass;
    }

    public static Class<?> getMcItemStackClass() {
        if (getSubVer() < 17) return mcItemStackClass == null ? mcItemStackClass = getLegacyMcClass("ItemStack") : mcItemStackClass;
        else return mcItemStackClass == null ? mcItemStackClass = getMcClass("world.item.ItemStack") : mcItemStackClass;
    }

    public static Class<?> getMcIMaterialClass() {
        if (getSubVer() < 17) return mcIMaterialClass == null ? mcIMaterialClass = getLegacyMcClass("IMaterial") : mcIMaterialClass;
        else return mcIMaterialClass == null ? mcIMaterialClass = getMcClass("world.level.IMaterial") : mcIMaterialClass;
    }

    public static Class<?> getMcItemClass() {
        if (getSubVer() < 17) return mcItemClass == null ? mcItemClass = getLegacyMcClass("Item") : mcItemClass;
        else return mcItemClass == null ? mcItemClass = getMcClass("world.item.Item") : mcItemClass;
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static Object getHandle(Object object) throws ReflectiveOperationException {
        return object.getClass().getMethod("getHandle").invoke(object);
    }

    public static Object getMcWorld(World world) throws ReflectiveOperationException {
        return getHandle(world);
    }

    public static Object getMcEntityPlayer(Player player) throws ReflectiveOperationException {
        return getHandle(player);
    }

    public static Class<?> getMcItemsClass() {
        if (getSubVer() < 17) return mcItemsClass == null ? mcItemsClass = getLegacyMcClass("Items") : mcItemsClass;
        else return mcItemsClass == null ? mcItemsClass = getMcClass("world.item.Items") : mcItemsClass;
    }

    public static Object getMcBlockPosition(Location location) throws ReflectiveOperationException {
        return getMcBlockPositionClass().getConstructor(int.class,int.class,int.class).newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Class<?> getMcDataWatcherClass() {
        if (getSubVer() < 17) return mcDataWatcherClass == null ? mcDataWatcherClass = getLegacyMcClass("DataWatcher") : mcDataWatcherClass;
        else return mcDataWatcherClass == null ? mcDataWatcherClass = getMcClass("network.syncher.DataWatcher") : mcDataWatcherClass;
    }

    public static Class<?> getMcBlockPositionClass() {
        if (getSubVer() < 17) return mcBlockPositionClass == null ? mcBlockPositionClass = getLegacyMcClass("BlockPosition") : mcBlockPositionClass;
        else return mcBlockPositionClass == null ? mcBlockPositionClass = getMcClass("core.BlockPosition") : mcBlockPositionClass;
    }

    public static Class<?> getMcEnumDirectionClass() {
        if (getSubVer() < 17) return mcEnumDirectionClass == null ? mcEnumDirectionClass = getLegacyMcClass("EnumDirection") : mcEnumDirectionClass;
        else return mcEnumDirectionClass == null ? mcEnumDirectionClass = getMcClass("core.EnumDirection") : mcEnumDirectionClass;
    }

    public static Class<?> getMcNBTTagCompoundClass() {
        if (getSubVer() < 17) return mcNBTTagCompound == null ? mcNBTTagCompound = getLegacyMcClass("NBTTagCompound") : mcNBTTagCompound;
        else return mcNBTTagCompound == null ? mcNBTTagCompound = getMcClass("nbt.NBTTagCompound") : mcNBTTagCompound;
    }
    
    public static Object getMcPlayerConnection(Player player) throws ReflectiveOperationException {
        int subVer = getSubVer();
        String fieldName = "playerConnection";
        if (subVer >= 18) switch (subVer) {
            case 18:
            case 19:
                fieldName = "b"; break;
            case 20:
            default:
                fieldName = "c"; break;
        }
        return getMcEntityPlayerClass().getField(fieldName).get(getMcEntityPlayer(player));
    }

    public static int getMcEntityId(Object mcEntity) throws ReflectiveOperationException {
        int subVer = getSubVer();
        int subRVer = getSubRVer();
        if (Entity_getId == null) {
            String methodName = "getId";
            if (subVer >= 18) switch (subVer) {
                case 18:
                    methodName = "ae";
                    break;
                case 19:
                    switch (subRVer) {
                        case 1: methodName = "ae"; break;
                        case 2: methodName = "ah"; break;
                        // 天杀的 mojang 在 1_19_R2 的时候改了 Entity.class, 导致获取id的方法名字变了
                        case 3: methodName = "af"; break;
                        // 真牛逼他又改了 1_19_R3
                    } break;
                case 20:
                    switch (subRVer) {
                        case 1: methodName = "af"; break;
                        case 2: methodName = "ah"; break;
                        case 3: methodName = "aj"; break;
                        // ... fine
                    } break;
                default:
                    methodName = "aj"; break;
            }
            Entity_getId = NMS.getMcEntityClass().getMethod(methodName);
            Entity_getId.setAccessible(true);
        }
        return (int) Entity_getId.invoke(mcEntity);
    }

    public static Object getDataWatcher(Object entity) throws ReflectiveOperationException {
        int subVer = getSubVer();
        if (Entity_getDataWatcher == null) {
            String methodName = "getDataWatcher";
            if (subVer >= 18) switch (subVer) {
                case 18:
                    methodName = "ai";
                    break;
                case 19:
                    switch (subRVer) {
                        case 1: methodName = "ai"; break;
                        case 2: methodName = "al"; break;
                        case 3: methodName = "aj"; break;
                    }
                    break;
                case 20:
                    switch (subRVer) {
                        case 1: methodName = "aj"; break;
                        case 2: methodName = "al"; break;
                        case 3: methodName = "an"; break;
                    } break;
                default:
                    methodName = "an"; break;
            }
            Entity_getDataWatcher = NMS.getMcEntityClass().getMethod(methodName);
            Entity_getDataWatcher.setAccessible(true);
        }
        return Entity_getDataWatcher.invoke(entity);
    }

    public static Object getPacketPlayOutEntityMetadata(Object entity) throws ReflectiveOperationException {
        if (getSubVer() < 19 || (getSubVer() == 19 && getSubRVer() == 1)) {
            /* <= 1_19_R1 */
            if (cPacketPlayOutEntityMetadata == null) {
                cPacketPlayOutEntityMetadata = NMS.getPacketClass("PacketPlayOutEntityMetadata").getConstructor(int.class, NMS.getMcDataWatcherClass(), boolean.class);
                cPacketPlayOutEntityMetadata.setAccessible(true);
            }
            return cPacketPlayOutEntityMetadata.newInstance(getMcEntityId(entity), getDataWatcher(entity), false);
        } else {
            if (cPacketPlayOutEntityMetadata == null) {
                cPacketPlayOutEntityMetadata = NMS.getPacketClass("PacketPlayOutEntityMetadata").getConstructor(int.class, List.class);
                cPacketPlayOutEntityMetadata.setAccessible(true);
            }
            if (DataWatcher_getNonDefaultValues == null) {
                DataWatcher_getNonDefaultValues = NMS.getMcDataWatcherClass().getMethod("c");
            }
            return cPacketPlayOutEntityMetadata.newInstance(getMcEntityId(entity), DataWatcher_getNonDefaultValues.invoke(getDataWatcher(entity)));
        }
    }

    public static void sendDestroyEntities(int[] entityIds, Collection<Player> toPlayers) {
        if (cPacketPlayOutEntityDestroy == null) {
            try {
                cPacketPlayOutEntityDestroy = NMS.getPacketClass("PacketPlayOutEntityDestroy").getConstructor(int[].class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        try {
            for (Player p : (toPlayers != null ? toPlayers : Bukkit.getOnlinePlayers())) {
                if (!p.isOnline()) continue;
                NMS.sendPacket(p, cPacketPlayOutEntityDestroy.newInstance( new Object[]{entityIds} ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Field fPacketSpawnEntity_x = null;
    private static Field fPacketSpawnEntity_y = null;
    private static Field fPacketSpawnEntity_z = null;
    public static void setSpawnPacketLocation(Object packetSpawnEntity, Location loc) {
        if (fPacketSpawnEntity_x == null) {
            try {
                fPacketSpawnEntity_x = getPacketClass("PacketPlayOutSpawnEntity").getDeclaredField("b");
                fPacketSpawnEntity_y = getPacketClass("PacketPlayOutSpawnEntity").getDeclaredField("c");
                fPacketSpawnEntity_z = getPacketClass("PacketPlayOutSpawnEntity").getDeclaredField("d");
                fPacketSpawnEntity_x.setAccessible(true);
                fPacketSpawnEntity_y.setAccessible(true);
                fPacketSpawnEntity_z.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            fPacketSpawnEntity_x.set(packetSpawnEntity, (int) (loc.getX()*32.0));
            fPacketSpawnEntity_y.set(packetSpawnEntity, (int) (loc.getY()*32.0));
            fPacketSpawnEntity_z.set(packetSpawnEntity, (int) (loc.getZ()*32.0));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
