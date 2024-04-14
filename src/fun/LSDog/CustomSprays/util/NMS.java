package fun.LSDog.CustomSprays.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NMS Util for {@link net.minecraft.server} related actions
 * Support 1.7.10 ~ 1.20.4
 */
public class NMS {

    // ClassSimpleName - Class<?>
    private static final Map<String, Class<?>> mcClassMap = new HashMap<>();

    private static String version = null;
    private static int subVer = -1;
    private static int subRVer = -1;
    // Constructor
    private static final MethodHandle cPacketPlayOutEntityDestroy;
    private static final MethodHandle cPacketPlayOutEntityMetadata;
    // Method
    private static final MethodHandle Entity_getId;
    private static final MethodHandle Entity_getDataWatcher;
    private static final MethodHandle PlayerConnection_sendPacket;
    private static final MethodHandle DataWatcher_getNonDefaultValues;

    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    static {

        getSubVer();
        getSubRVer();
        String name;

        try {

            cPacketPlayOutEntityDestroy = getConstructor(getPacketClass("PacketPlayOutEntityDestroy"), MethodType.methodType(void.class, int[].class));

            cPacketPlayOutEntityMetadata = getConstructor(NMS.getPacketClass("PacketPlayOutEntityMetadata"),
                    subVer < 19 || (subVer == 19 && subRVer == 1) ?
                            MethodType.methodType(void.class, int.class, NMS.getMcDataWatcherClass(), boolean.class) :
                            MethodType.methodType(void.class, int.class, List.class)
                    );

            name = "getId";
            if (subVer >= 18) switch (subVer) {
                case 18: name = "ae"; break;
                case 19: name = subRVer == 1 ? "ae" : subRVer == 2 ? "ah" : "af"; break;
                case 20: name = subRVer == 1 ? "af" : subRVer == 2 ? "ah" : "aj"; break;
                default: name = "aj"; break;
            }
            Entity_getId = getMethodVirtual(getMcEntityClass(), name, MethodType.methodType(int.class));

            name = "getDataWatcher";
            if (subVer >= 18) switch (subVer) {
                case 18: name = "ai"; break;
                case 19: name = subRVer == 1 ? "ai" : subRVer == 2 ? "al" : "aj"; break;
                case 20: name = subRVer == 1 ? "aj" : subRVer == 2 ? "al" : "an"; break;
                default: name = "an"; break;
            }
            Entity_getDataWatcher = getMethodVirtual(getMcEntityClass(), name, MethodType.methodType(getMcDataWatcherClass()));

            if (subVer <= 17) name = "sendPacket";
            else if (subVer < 20 || (subVer == 20 && subRVer <= 1)) name = "a";
            else name = "b"; // wtf 你为什么要在1.20.2这个小版本改这个 mojang你丧尽天良啊啊啊啊啊啊
            PlayerConnection_sendPacket = getMethodVirtual(
                    getMcPlayerConnectionClass(), name, MethodType.methodType(void.class, getPacketClass()));

            DataWatcher_getNonDefaultValues = (subVer > 19 || (subVer == 19 && subRVer > 1)) ?
                    getMethodVirtual(getMcDataWatcherClass(), "c", MethodType.methodType(List.class)) : null;

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (clazz == null || name == null) return null;
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static Object getDeclaredFieldObject(Object object, String name) {
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

    public static Object getDeclaredFieldObject(Object object, Field field) {
        if (object == null || field == null) return null;
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle getMethodVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return MethodHandles.lookup().findVirtual(refc, name, type);
    }

    public static MethodHandle getConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return MethodHandles.lookup().findConstructor(refc, type);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Class<?> mcWorldClass = null;
    private static Class<?> mcEntityClass = null;
    private static Class<?> mcEntityPlayerClass = null;
    private static Class<?> mcEntityItemFrameClass = null;
    private static Class<?> mcPlayerConnectionClass = null;
    private static Class<?> mcServerCommonPacketListenerImplClass = null;
    private static Class<?> mcNetworkManagerClass = null;
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
        return subVer == -1 ? subVer = Integer.parseInt(getMcVer().split("_")[1]) : subVer;
    }

    /**
     * 返回版本号的第三项 (R开头的那个)<br>
     * v1_12_R1 -> 1
     */
    public static int getSubRVer() {
        return subRVer == -1 ? subRVer = Integer.parseInt(getMcVer().split("_")[2].substring(1)) : subRVer;
    }

    public static void sendPacket(Player player, Object packet) throws Throwable {
        PlayerConnection_sendPacket.invoke(getMcPlayerConnection(player), packet);
    }

    public static Class<?> getMcClass(String newName, String legacyName) {
        return getSubVer() < 17 ? getMcClassLegacy(legacyName) : getMcClassNew(newName);
    }

    public static Class<?> getMcClassNew(String name) {
        Class<?> clazz = mcClassMap.get(name);
        if (clazz == null) {
            clazz = getClass("net.minecraft."+name);
            mcClassMap.put(name, clazz);
        }
        return clazz;
    }

    // "Legacy" 指的是版本 < 1.17, 因为 NMS 在1.17后相应类的路径大改
    public static Class<?> getMcClassLegacy(String name) {
        Class<?> clazz = mcClassMap.get(name);
        if (clazz == null) {
            clazz = getClass("net.minecraft.server."+getMcVer()+"."+name);
            mcClassMap.put(name, clazz);
        }
        return clazz;
    }

    public static Class<?> getPacketClass() {
        return getMcClass("network.protocol.Packet", "Packet");
    }

    public static Class<?> getPacketClass(String paketName) {
        return getMcClass("network.protocol.game."+paketName, paketName);
    }

    public static Class<?> getMcWorldClass() {
        return mcWorldClass == null ? mcWorldClass = getMcClass("world.level.World", "World") : mcWorldClass;
    }

    public static Class<?> getMcEntityClass() {
        return mcEntityClass == null ? mcEntityClass = getMcClass("world.entity.Entity", "Entity") : mcEntityClass;
    }

    public static Class<?> getMcEntityPlayerClass() {
        return mcEntityPlayerClass == null ? mcEntityPlayerClass = getMcClass("server.level.EntityPlayer", "EntityPlayer") : mcEntityPlayerClass;
    }

    public static Class<?> getMcEntityItemFrameClass() {
        return mcEntityItemFrameClass == null ? mcEntityItemFrameClass = getMcClass("world.entity.decoration.EntityItemFrame", "EntityItemFrame") : mcEntityItemFrameClass;
    }

    public static Class<?> getMcPlayerConnectionClass() {
        return mcPlayerConnectionClass == null ? mcPlayerConnectionClass = getMcClass("server.network.PlayerConnection", "PlayerConnection") : mcPlayerConnectionClass;
    }

    public static Class<?> getMcServerCommonPacketListenerImplClass() {
        return mcServerCommonPacketListenerImplClass == null ? mcServerCommonPacketListenerImplClass = getMcClassNew("server.network.ServerCommonPacketListenerImpl") : mcServerCommonPacketListenerImplClass;
    }

    public static Class<?> getMcNetworkManagerClass() {
        return mcNetworkManagerClass == null ? mcNetworkManagerClass = getMcClass("network.NetworkManager", "NetworkManager") : mcNetworkManagerClass;
    }

    public static Class<?> getMcItemStackClass() {
        return mcItemStackClass == null ? mcItemStackClass = getMcClass("world.item.ItemStack", "ItemStack") : mcItemStackClass;
    }

    public static Class<?> getMcIMaterialClass() {
        return mcIMaterialClass == null ? mcIMaterialClass = getMcClass("world.level.IMaterial", "IMaterial") : mcIMaterialClass;
    }

    public static Class<?> getMcItemClass() {
        return mcItemClass == null ? mcItemClass = getMcClass("world.item.Item", "Item") : mcItemClass;
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
        return mcItemsClass == null ? mcItemsClass = getMcClass("world.item.Items", "Items") : mcItemsClass;
    }

    public static Object getMcBlockPosition(Location location) throws ReflectiveOperationException {
        return getMcBlockPositionClass().getConstructor(int.class,int.class,int.class).newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Class<?> getMcDataWatcherClass() {
        return mcDataWatcherClass == null ? mcDataWatcherClass = getMcClass("network.syncher.DataWatcher", "DataWatcher") : mcDataWatcherClass;
    }

    public static Class<?> getMcBlockPositionClass() {
        return mcBlockPositionClass == null ? mcBlockPositionClass = getMcClass("core.BlockPosition", "BlockPosition") : mcBlockPositionClass;
    }

    public static Class<?> getMcEnumDirectionClass() {
        return mcEnumDirectionClass == null ? mcEnumDirectionClass = getMcClass("core.EnumDirection", "EnumDirection") : mcEnumDirectionClass;
    }

    public static Class<?> getMcNBTTagCompoundClass() {
        return mcNBTTagCompound == null ? mcNBTTagCompound = getMcClass("nbt.NBTTagCompound", "NBTTagCompound") : mcNBTTagCompound;
    }

    private static Field fEntityPlayer_playerConnection = null;
    public static Object getMcPlayerConnection(Player player) throws ReflectiveOperationException {
        if (fEntityPlayer_playerConnection == null) {
            String fieldName = "playerConnection";
            int subVer = getSubVer();
            if (subVer >= 17) switch (subVer) {
                case 17:
                case 18:
                case 19:
                    fieldName = "b"; break;
                case 20:
                default:
                    fieldName = "c"; break;
            }
            fEntityPlayer_playerConnection = getDeclaredField(getMcEntityPlayerClass(), fieldName);
        }
        return fEntityPlayer_playerConnection.get(getMcEntityPlayer(player));
    }

    private static Field fPlayerConnection_networkManager = null;
    public static Object getMcPlayerNetworkManager(Player player) throws ReflectiveOperationException {
        if (fPlayerConnection_networkManager == null) {
            int subVer = getSubVer();
            String fieldName = "networkManager";
            if (subVer >= 17) switch (subVer) {
                case 17:
                case 18:
                    fieldName = "a"; break;
                case 19:
                    fieldName = "h"; break;
                case 20:
                default:
                    fieldName = "c"; break;
            }
            if (subVer >= 20) {
                fPlayerConnection_networkManager = getDeclaredField(getMcServerCommonPacketListenerImplClass(), fieldName);
            } else {
                fPlayerConnection_networkManager = getDeclaredField(getMcPlayerConnectionClass(), fieldName);
            }
        }
        return fPlayerConnection_networkManager.get(getMcPlayerConnection(player));
    }

    private static Field fNetworkManager_channel = null;
    public static Object getMcPlayerNettyChannel(Player player) throws ReflectiveOperationException {
        if (fNetworkManager_channel == null) {
            int subVer = getSubVer();
            String fieldName = "channel";
            if (subVer <= 7 || subVer >= 17) switch (subVer) {
                case 17: fieldName = "k"; break;
                case 7:
                case 18:
                case 19: fieldName = "m"; break;
                case 20:
                default: fieldName = "n"; break;
            }
            fNetworkManager_channel = getDeclaredField(getMcNetworkManagerClass(), fieldName);
        }
        if (fNetworkManager_channel == null) return null;
        return fNetworkManager_channel.get(getMcPlayerNetworkManager(player));
    }

    public static int getMcEntityId(Object mcEntity) throws Throwable {
        return (int) Entity_getId.invoke(mcEntity);
    }

    public static Object getDataWatcher(Object entity) throws Throwable {
        return Entity_getDataWatcher.invoke(entity);
    }

    public static Object getPacketPlayOutEntityMetadata(Object entity) throws Throwable {
        if (subVer < 19 || (subVer == 19 && subRVer == 1))
            return cPacketPlayOutEntityMetadata.invoke(getMcEntityId(entity), getDataWatcher(entity), false);
        else
            return cPacketPlayOutEntityMetadata.invoke(
                    getMcEntityId(entity),
                    DataWatcher_getNonDefaultValues.invoke(getDataWatcher(entity))
            );
    }

    public static void sendDestroyEntities(int[] entityIds, Collection<Player> toPlayers) {
        try {
            for (Player p : toPlayers) {
                if (!p.isOnline()) continue;
                NMS.sendPacket(p, cPacketPlayOutEntityDestroy.invoke( entityIds ));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static Field fPacketSpawnEntity_x = null;
    private static Field fPacketSpawnEntity_y = null;
    private static Field fPacketSpawnEntity_z = null;
    public static void setSpawnPacketLocation(Object packetSpawnEntity, Location loc) {
        if (fPacketSpawnEntity_x == null) {
            try {
                Class<?> packetClass = getPacketClass("PacketPlayOutSpawnEntity");
                fPacketSpawnEntity_x = getDeclaredField(packetClass, "b");
                fPacketSpawnEntity_y = getDeclaredField(packetClass, "c");
                fPacketSpawnEntity_z = getDeclaredField(packetClass, "d");
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
