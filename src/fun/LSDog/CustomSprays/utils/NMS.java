package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 全是 Object 的跨版本NMS适配机器（
 */
public class NMS {

    public static String version;

    private static final Map<String, Class<?>> mcClassMap = new HashMap<>();

    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
    
    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Method PlayerConnection_SendPacket;
    public static void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        if (PlayerConnection_SendPacket == null) {
            if (CustomSprays.getSubVer() < 18) PlayerConnection_SendPacket = getMcPlayerConnectionClass().getMethod("sendPacket", getPacketClass());
            else PlayerConnection_SendPacket = getMcPlayerConnectionClass().getMethod("a", getPacketClass());
        }
        PlayerConnection_SendPacket.invoke(getMcPlayerConnection(player), packet);
    }


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

    // "Legacy" means version < 1.17
    public static Class<?> getLegacyMcClass(String name) {
        Class<?> clazz = mcClassMap.get(name);
        if (clazz == null) {
            clazz = getClass("net.minecraft.server."+version+"."+name);
            mcClassMap.put(name, clazz);
        }
        return clazz;
    }

    public static Class<?> getPacketClass() {
        if (CustomSprays.getSubVer() < 17) return getLegacyMcClass("Packet");
        else return getMcClass("network.protocol.Packet");
    }

    public static Class<?> getPacketClass(String paketName) {
        if (CustomSprays.getSubVer() < 17) return getLegacyMcClass(paketName);
        else return getMcClass("network.protocol.game."+paketName);
    }

    private static Class<?> fluidCollisionModeClass = null;
    public static Class<?> getFluidCollisionModeClass() {
        return fluidCollisionModeClass == null ? fluidCollisionModeClass = getClass("org.bukkit.FluidCollisionMode") : fluidCollisionModeClass;
    }

    public static Object getFluidCollisionModeClass(String value) throws ReflectiveOperationException {
        return getFluidCollisionModeClass().getMethod("valueOf", String.class).invoke(null, value);
    }

    private static Class<?> mcWorldClass = null;
    public static Class<?> getMcWorldClass() {
        if (CustomSprays.getSubVer() < 17) return mcWorldClass == null ? mcWorldClass = getLegacyMcClass("World") : mcWorldClass;
        else return mcWorldClass == null ? mcWorldClass = getMcClass("world.level.World") : mcWorldClass;
    }

    private static Class<?> mcWorldServerClass = null;
    public static Class<?> getMcWorldServerClass() {
        if (CustomSprays.getSubVer() < 17) return mcWorldServerClass == null ? mcWorldServerClass = getLegacyMcClass("WorldServer") : mcWorldServerClass;
        else return mcWorldServerClass == null ? mcWorldServerClass = getMcClass("server.level.WorldServer") : mcWorldServerClass;
    }

    private static Class<?> mcEntityClass = null;
    public static Class<?> getMcEntityClass() {
        if (CustomSprays.getSubVer() < 17) return mcEntityClass == null ? mcEntityClass = getLegacyMcClass("Entity") : mcEntityClass;
        else return mcEntityClass == null ? mcEntityClass = getMcClass("world.entity.Entity") : mcEntityClass;
    }

    private static Class<?> mcEntityLivingClass = null;
    public static Class<?> getMcEntityLivingClass() {
        if (CustomSprays.getSubVer() < 17) return mcEntityLivingClass == null ? mcEntityLivingClass = getLegacyMcClass("EntityLiving") : mcEntityLivingClass;
        else return mcEntityLivingClass == null ? mcEntityLivingClass = getMcClass("world.entity.EntityLiving") : mcEntityLivingClass;
    }

    private static Class<?> mcEntityPlayerClass = null;
    public static Class<?> getMcEntityPlayerClass() {
        if (CustomSprays.getSubVer() < 17) return mcEntityPlayerClass == null ? mcEntityPlayerClass = getLegacyMcClass("EntityPlayer") : mcEntityPlayerClass;
        else return mcEntityPlayerClass == null ? mcEntityPlayerClass = getMcClass("server.level.EntityPlayer") : mcEntityPlayerClass;
    }

    private static Class<?> mcEntityItemFrameClass = null;
    public static Class<?> getMcEntityItemFrameClass() {
        if (CustomSprays.getSubVer() < 17) return mcEntityItemFrameClass == null ? mcEntityItemFrameClass = getLegacyMcClass("EntityItemFrame") : mcEntityItemFrameClass;
        else return mcEntityItemFrameClass == null ? mcEntityItemFrameClass = getMcClass("world.entity.decoration.EntityItemFrame") : mcEntityItemFrameClass;
    }

    private static Class<?> mcPlayerConnectionClass = null;
    public static Class<?> getMcPlayerConnectionClass() {
        if (CustomSprays.getSubVer() < 17) return mcPlayerConnectionClass == null ? mcPlayerConnectionClass = getLegacyMcClass("PlayerConnection") : mcPlayerConnectionClass;
        else return mcPlayerConnectionClass == null ? mcPlayerConnectionClass = getMcClass("server.network.PlayerConnection") : mcPlayerConnectionClass;
    }

    private static Class<?> mcVec3DClass = null;
    public static Class<?> getMcVec3DClass() {
        if (CustomSprays.getSubVer() < 17) return mcVec3DClass == null ? mcVec3DClass = getLegacyMcClass("Vec3D") : mcVec3DClass;
        else return mcVec3DClass == null ? mcVec3DClass = getMcClass("world.phys.Vec3D") : mcVec3DClass;
    }

    private static Class<?> mcItemStackClass = null;
    public static Class<?> getMcItemStackClass() {
        if (CustomSprays.getSubVer() < 17) return mcItemStackClass == null ? mcItemStackClass = getLegacyMcClass("ItemStack") : mcItemStackClass;
        else return mcItemStackClass == null ? mcItemStackClass = getMcClass("world.item.ItemStack") : mcItemStackClass;
    }

    private static Class<?> mcIMaterialClass = null;
    public static Class<?> getMcIMaterialClass() {
        if (CustomSprays.getSubVer() < 17) return mcIMaterialClass == null ? mcIMaterialClass = getLegacyMcClass("IMaterial") : mcIMaterialClass;
        else return mcIMaterialClass == null ? mcIMaterialClass = getMcClass("world.level.IMaterial") : mcIMaterialClass;
    }

    private static Class<?> mcItemClass = null;
    public static Class<?> getMcItemClass() {
        if (CustomSprays.getSubVer() < 17) return mcItemClass == null ? mcItemClass = getLegacyMcClass("Item") : mcItemClass;
        else return mcItemClass == null ? mcItemClass = getMcClass("world.item.Item") : mcItemClass;
    }

    private static Class<?> mcItemsClass = null;
    public static Class<?> getMcItemsClass() {
        if (CustomSprays.getSubVer() < 17) return mcItemsClass == null ? mcItemsClass = getLegacyMcClass("Items") : mcItemsClass;
        else return mcItemsClass == null ? mcItemsClass = getMcClass("world.item.Items") : mcItemsClass;
    }

    private static Class<?> mcDataWatcherClass = null;
    public static Class<?> getMcDataWatcherClass() {
        if (CustomSprays.getSubVer() < 17) return mcDataWatcherClass == null ? mcDataWatcherClass = getLegacyMcClass("DataWatcher") : mcDataWatcherClass;
        else return mcDataWatcherClass == null ? mcDataWatcherClass = getMcClass("network.syncher.DataWatcher") : mcDataWatcherClass;
    }

    private static Class<?> mcBlockPositionClass = null;
    public static Class<?> getMcBlockPositionClass() {
        if (CustomSprays.getSubVer() < 17) return mcBlockPositionClass == null ? mcBlockPositionClass = getLegacyMcClass("BlockPosition") : mcBlockPositionClass;
        else return mcBlockPositionClass == null ? mcBlockPositionClass = getMcClass("core.BlockPosition") : mcBlockPositionClass;
    }

    private static Class<?> mcEnumDirectionClass = null;
    public static Class<?> getMcEnumDirectionClass() {
        if (CustomSprays.getSubVer() < 17) return mcEnumDirectionClass == null ? mcEnumDirectionClass = getLegacyMcClass("EnumDirection") : mcEnumDirectionClass;
        else return mcEnumDirectionClass == null ? mcEnumDirectionClass = getMcClass("core.EnumDirection") : mcEnumDirectionClass;
    }

    private static Class<?> mcNBTTagCompound = null;
    public static Class<?> getMcNBTTagCompoundClass() {
        if (CustomSprays.getSubVer() < 17) return mcNBTTagCompound == null ? mcNBTTagCompound = getLegacyMcClass("NBTTagCompound") : mcNBTTagCompound;
        else return mcNBTTagCompound == null ? mcNBTTagCompound = getMcClass("nbt.NBTTagCompound") : mcNBTTagCompound;
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static Object getHandle(Object object) throws ReflectiveOperationException {
        return object.getClass().getMethod("getHandle").invoke(object);
    }

    public static Object getMcWorld(World world) throws ReflectiveOperationException {
        return getHandle(world);
    }

    public static Object getMcWorldServer(World world) throws ReflectiveOperationException {
        return getHandle(world);
    }

    public static Object getMcEntityPlayer(Player player) throws ReflectiveOperationException {
        return getHandle(player);
    }

    public static Object getMcPlayerConnection(Player player) throws ReflectiveOperationException {
        if (CustomSprays.getSubVer() < 17) return getMcEntityPlayerClass().getField("playerConnection").get(getMcEntityPlayer(player));
        else return getMcEntityPlayerClass().getField("b").get(getMcEntityPlayer(player));
    }

    public static Object getMcBlockPosition(Location location) throws ReflectiveOperationException {
        return getMcBlockPositionClass().getConstructor(int.class,int.class,int.class).newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }


}
