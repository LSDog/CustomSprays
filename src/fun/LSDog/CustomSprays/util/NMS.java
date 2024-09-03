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
 * Support 1.7.10 ~ 1.20.6
 */
public class NMS {


    /**
     * Just call this method and automatically run static{} to init
     */
    public static void init() {}

    /**
     * @return Simple version number like 1.20.6
     */
    private static String getVersionNumber() {
        if (simpleVersionNumber != null) return simpleVersionNumber;
        String strVer = Bukkit.getServer().getBukkitVersion();
        strVer = strVer.substring(0, strVer.indexOf("-"));
        return simpleVersionNumber = strVer;
    }

    private static String simpleVersionNumber = null;
    private static String rversion = null;
    private static int subVer = -1;
    private static int subRVer = -1;

    public static final Map<String, String> rVerMap = new HashMap<String, String>(){{
        put("1.20.5", "1_20_R4");
        put("1.20.6", "1_20_R4");
        put("1.21", "1_21_R1");
        put("1.21.1", "1_21_R1");
    }};
    public static final boolean VER_1_17, VER_1_20_R4;
    /** Using spigot mapping (paper 1.20.4-) or Mojang mapping (paper 1.20.5+). */
    public static final boolean SP = Double.parseDouble(getVersionNumber().split("\\.",2)[1]) >= 20.5 && Package.getPackage("com.destroystokyo.paper") == null;

    /**
     * get NMS version string (e.g. 1_12_R1)
     */
    public static String getMcVer() {
        if (rversion != null) return rversion;

        String versionString = Bukkit.getServer().getClass().getPackage().getName(); // e.g. org.bukkit.craftbukkit.v1_20_R2.CraftServer
        if (versionString.contains("R")) {
            return rversion = versionString.split("\\.")[3].substring(1); // -> 1_20_R2
        } else if ((rversion = rVerMap.get(getVersionNumber())) != null) {
            return rversion;
        } else {
            throw new RuntimeException("Can't get CraftBukkit revision number! Only got '" + versionString + "' instead.");
        }
    }

    /**
     * Get big version number (v1_12_R1 -> 12)
     */
    public static int getSubVer() {
        return subVer == -1 ? subVer = Integer.parseInt(getMcVer().split("_")[1]) : subVer;
    }

    /**
     * Get revision number (v1_12_R1 -> 1)
     */
    public static int getSubRVer() {
        return subRVer == -1 ? subRVer = Integer.parseInt(getMcVer().split("_")[2].substring(1)) : subRVer;
    }

    // ClassSimpleName - Class
    private static final Map<String, Class<?>> mcClassMap = new HashMap<>();

    // Class
    public static final Class<?>
            mcWorldClass = getMcClass("world.level.World", "World"),
            mcWorldServerClass = getMcClass("server.level.WorldServer", "WorldServer"),
            mcEntityClass = getMcClass("world.entity.Entity", "Entity"),
            mcEntityPlayerClass = getMcClass("server.level.EntityPlayer", "EntityPlayer"),
            mcEntityItemFrameClass = getMcClass("world.entity.decoration.EntityItemFrame", "EntityItemFrame"),
            mcPlayerConnectionClass = getMcClass("server.network.PlayerConnection", "PlayerConnection"),
            mcServerCommonPacketListenerImplClass = (getSubVer() >= 21 || (getSubVer()==20 && getSubRVer()>=2)) ?
                    getMcClassNew("server.network.ServerCommonPacketListenerImpl") : null,
            mcNetworkManagerClass = getMcClass("network.NetworkManager", "NetworkManager"),
            mcItemStackClass = getMcClass("world.item.ItemStack", "ItemStack"),
            mcIMaterialClass = getSubVer() >= 13 ? getMcClass("world.level.IMaterial", "IMaterial") : null,
            mcItemClass = getMcClass("world.item.Item", "Item"),
            mcItemsClass = getMcClass("world.item.Items", "Items"),
            mcDataWatcherClass = getMcClass("network.syncher.DataWatcher", "DataWatcher"),
            mcBlockPositionClass = getMcClass("core.BlockPosition", "BlockPosition"),
            mcEnumDirectionClass = getMcClass("core.EnumDirection", "EnumDirection"),
            mcNBTTagCompoundClass = getMcClass("nbt.NBTTagCompound", "NBTTagCompound");

    // Field
    private static final MethodHandle
            fEntityPlayer_playerConnection,
            fPlayerConnection_networkManager,
            fNetworkManager_channel,
            fPacketSpawnEntity_x,
            fPacketSpawnEntity_y,
            fPacketSpawnEntity_z;

    // Constructor
    private static final MethodHandle
            cBlockPosition,
            cPacketPlayOutEntityDestroy,
            cPacketPlayOutEntityMetadata;
    // Method
    private static final MethodHandle CraftWorld_getHandle,
            CraftPlayer_getHandle,
            Entity_getId,
            Entity_getDataWatcher,
            PlayerConnection_sendPacket,
            DataWatcher_getNonDefaultValues;

    static {

        String name;

        VER_1_17 = subVer >= 17;
        VER_1_20_R4 = subVer > 20 || (subVer==20 && subRVer>=4);

        try {

            name = "playerConnection";
            int subVer = getSubVer();
            if (subVer >= 17) switch (subVer) {
                case 17: case 18: case 19:
                    name = "b"; break;
                case 20:
                default:
                    name = "c"; break;
            }
            fEntityPlayer_playerConnection = getFieldGetter(mcEntityPlayerClass, name, mcPlayerConnectionClass);


            name = "networkManager";
            if (subVer >= 17) switch (subVer) {
                case 17: case 18:
                    name = "a"; break;
                case 19:
                    name = "h"; break;
                case 20:
                    name = (subRVer <= 1) ? "h" : ((subRVer <= 3) ? "c" : "e"); break;
                default:
                    name = "e"; break;
            }
            if (subVer>=20) {
                //noinspection DataFlowIssue
                fPlayerConnection_networkManager = getFieldGetterUnReflect(
                        (subVer == 20 && subRVer == 1) ? mcPlayerConnectionClass : mcServerCommonPacketListenerImplClass, name);
            } else {
                fPlayerConnection_networkManager = getFieldGetter(mcPlayerConnectionClass, name, mcNetworkManagerClass);
            }

            name = "channel";
            if (subVer <= 7 || subVer >= 17) switch (subVer) {
                case 17:
                    name = "k"; break;
                case 7: case 18: case 19:
                    name = "m"; break;
                case 20:
                    name = (subRVer == 1) ? "m" : "n"; break;
                case 21:
                default:
                    name = "n"; break;
            }
            fNetworkManager_channel = getFieldGetter(mcNetworkManagerClass, name,
                    (subVer <= 7) ?
                            Class.forName("net.minecraft.util.io.netty.channel.Channel") :
                            Class.forName("io.netty.channel.Channel"));

            Class<?> packetClass = getPacketClass("PacketPlayOutSpawnEntity");
            fPacketSpawnEntity_x = subVer <= 7 ? getFieldGetter(packetClass, "b", int.class):null;
            fPacketSpawnEntity_y = subVer <= 7 ? getFieldGetter(packetClass, "c", int.class):null;
            fPacketSpawnEntity_z = subVer <= 7 ? getFieldGetter(packetClass, "d", int.class):null;

            // Constructor

            cBlockPosition = getConstructor(mcBlockPositionClass, MethodType.methodType(void.class, int.class, int.class, int.class));

            cPacketPlayOutEntityDestroy = getConstructor(getPacketClass("PacketPlayOutEntityDestroy"), MethodType.methodType(void.class, int[].class));

            cPacketPlayOutEntityMetadata = getConstructor(NMS.getPacketClass("PacketPlayOutEntityMetadata"),
                    subVer < 19 || (subVer == 19 && subRVer == 1) ?
                            MethodType.methodType(void.class, int.class, mcDataWatcherClass, boolean.class) :
                            MethodType.methodType(void.class, int.class, List.class)
                    );

            // Method

            CraftWorld_getHandle = getMethodVirtual(getCraftClass("CraftWorld"), "getHandle", MethodType.methodType(mcWorldServerClass));
            CraftPlayer_getHandle = getMethodVirtual(getCraftClass("entity.CraftPlayer"), "getHandle", MethodType.methodType(mcEntityPlayerClass));

            name = "getId";
            if (subVer >= 18) switch (subVer) {
                case 18: name = "ae"; break;
                case 19: name = subRVer == 1 ? "ae" : subRVer == 2 ? "ah" : "af"; break;
                case 20: name = subRVer == 1 ? "af" : subRVer == 2 ? "ah" : subRVer == 3 ? "aj" : "al"; break;
                case 21:
                default: name = "an"; break;
            }
            Entity_getId = getMethodVirtual(mcEntityClass, name, MethodType.methodType(int.class));

            name = "getDataWatcher"; // mojang: SyncedEntityData
            if (subVer >= 18) switch (subVer) {
                case 18: name = "ai"; break;
                case 19: name = subRVer == 1 ? "ai" : subRVer == 2 ? "al" : "aj"; break;
                case 20: name = subRVer == 1 ? "aj" : subRVer == 2 ? "al" : subRVer == 3 ? "an" : "ap"; break;
                case 21:
                default: name = "ar"; break;
            }
            Entity_getDataWatcher = getMethodVirtual(mcEntityClass, name, MethodType.methodType(mcDataWatcherClass));

            if (subVer <= 17) name = "sendPacket";
            else if (subVer < 20 || (subVer == 20 && subRVer <= 1)) name = "a";
            else name = "b"; // wtf 你为什么要在1.20.2这个小版本改这个 mojang你丧尽天良啊啊啊啊啊啊
            PlayerConnection_sendPacket = getMethodVirtual(mcPlayerConnectionClass, name, MethodType.methodType(void.class, getPacketClass()));

            DataWatcher_getNonDefaultValues = (subVer > 19 || (subVer == 19 && subRVer > 1)) ?
                    getMethodVirtual(mcDataWatcherClass, "c", MethodType.methodType(List.class)) : null;

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (clazz == null || name == null) return null;
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static Object getDeclaredFieldObject(Class<?> clazz, String name, Object object) {
        if (clazz == null || name == null) return null;
        try {
            return getDeclaredField(clazz, name).get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getDeclaredFieldObject(Object object, String name) {
        return getDeclaredFieldObject(object.getClass(), name, object);
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

    public static MethodHandle getFieldGetter(Class<?> refc, String name, Class<?> type) throws Throwable {
        return MethodHandles.lookup().findGetter(refc, name, type);
    }

    public static MethodHandle getFieldGetterUnReflect(Class<?> refc, String name) throws Throwable {
        Field field = refc.getDeclaredField(name);
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(field);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    public static void sendPacket(Player player, Object packet) throws Throwable {
        PlayerConnection_sendPacket.invoke(getMcPlayerConnection(player), packet);
    }

    public static Class<?> getCraftClass(String name) {
        return getClass("org.bukkit.craftbukkit.v"+getMcVer()+"."+name);
    }

    public static Class<?> getMcClass(String newName, String legacyName) {
        return getSubVer() <= 16 ? getMcClassLegacy(legacyName) : getMcClassNew(newName);
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
            clazz = getClass("net.minecraft.server.v"+getMcVer()+"."+name);
            mcClassMap.put(name, clazz);
        }
        return clazz;
    }

    public static Class<?> getPacketClass() {
        return getMcClass("network.protocol.Packet", "Packet");
    }

    public static Class<?> getPacketClass(String packetName) {
        return getMcClass("network.protocol.game."+packetName, packetName);
    }

    public static Object getMcWorld(World world) throws Throwable {
        return CraftWorld_getHandle.invoke(world);
    }

    public static Object getMcEntityPlayer(Player player) throws Throwable {
        return CraftPlayer_getHandle.invoke(player);
    }

    public static Object getMcPlayerConnection(Player player) throws Throwable {
        return fEntityPlayer_playerConnection.invoke(getMcEntityPlayer(player));
    }

    public static Object getMcPlayerNetworkManager(Player player) throws Throwable {
        return fPlayerConnection_networkManager.invoke(getMcPlayerConnection(player));
    }

    public static Object getMcPlayerNettyChannel(Player player) throws Throwable {
        return fNetworkManager_channel.invoke(getMcPlayerNetworkManager(player));
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

    public static Object getMcBlockPosition(Location location) throws Throwable {
        return cBlockPosition.invoke(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static void setSpawnPacketLocation_7(Object packetSpawnEntity, Location loc) throws Throwable {
        fPacketSpawnEntity_x.invoke(packetSpawnEntity, (int) (loc.getX()*32.0));
        fPacketSpawnEntity_y.invoke(packetSpawnEntity, (int) (loc.getY()*32.0));
        fPacketSpawnEntity_z.invoke(packetSpawnEntity, (int) (loc.getZ()*32.0));
    }
}
