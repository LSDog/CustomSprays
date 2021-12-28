package fun.LSDog.CustomSprays.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

/**
 * 全是 Object 的跨版本NMS适配机器（
 */
public class NMS {

    public static String version;

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

    public static Class<?> getPacketClass() throws ClassNotFoundException {
        return Class.forName("net.minecraft.server."+version+".Packet");
    }
    public static Class<?> getPacketClass(String paketName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server."+version+"."+paketName);
    }

    public static void sendPacket(Player player, Object packet) throws Exception {
        if (packet != null) getMcPlayerConnectionClass().getMethod("sendPacket", getPacketClass()).invoke(getMcPlayerConnection(player), packet);
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Object getField(Class<?> clazz, Object object, String name) {
        try {
            Field field = clazz.getField(name);
            field.setAccessible(true);
            return field.get(clazz.cast(object));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Object getDeclaredField(Object object, String name) {
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
        return getClass("net.minecraft.server."+version+"."+name);
    }

    private static Class<?> fluidCollisionModeClass = null;
    public static Class<?> getFluidCollisionModeClass() {
        return fluidCollisionModeClass == null ? fluidCollisionModeClass = getClass("org.bukkit.FluidCollisionMode") : fluidCollisionModeClass;
    }

    public static Object getFluidCollisionModeClass(String value) throws Exception {
        return getFluidCollisionModeClass().getMethod("valueOf", String.class).invoke(null, value);
    }

    private static Class<?> mcWorldClass = null;
    public static Class<?> getMcWorldClass() {
        return mcWorldClass == null ? mcWorldClass = getMcClass("World") : mcWorldClass;
    }

    private static Class<?> mcWorldServerClass = null;
    public static Class<?> getMcWorldServerClass() {
        return mcWorldServerClass == null ? mcWorldServerClass = getMcClass("WorldServer") : mcWorldServerClass;
    }

    private static Class<?> mcEntityClass = null;
    public static Class<?> getMcEntityClass() {
        return mcEntityClass == null ? mcEntityClass = getMcClass("Entity") : mcEntityClass;
    }

    private static Class<?> mcEntityPlayerClass = null;
    public static Class<?> getMcEntityPlayerClass() {
        return mcEntityPlayerClass == null ? mcEntityPlayerClass = getMcClass("EntityPlayer") : mcEntityPlayerClass;
    }

    private static Class<?> mcPlayerConnectionClass = null;
    public static Class<?> getMcPlayerConnectionClass() {
        return mcPlayerConnectionClass == null ? mcPlayerConnectionClass = getMcClass("PlayerConnection") : mcPlayerConnectionClass;
    }

    private static Class<?> mcVec3DClass = null;
    public static Class<?> getMcVec3DClass() {
        return mcVec3DClass == null ? mcVec3DClass = getMcClass("Vec3D") : mcVec3DClass;
    }

    private static Class<?> mcItemStackClass = null;
    public static Class<?> getMcItemStackClass() {
        return mcItemStackClass == null ? mcItemStackClass = getMcClass("ItemStack") : mcItemStackClass;
    }

    private static Class<?> mcIMaterialClass = null;
    public static Class<?> getMcIMaterialClass() {
        return mcIMaterialClass == null ? mcIMaterialClass = getMcClass("IMaterial") : mcIMaterialClass;
    }

    private static Class<?> mcItemClass = null;
    public static Class<?> getMcItemClass() {
        return mcItemClass == null ? mcItemClass = getMcClass("Item") : mcItemClass;
    }

    private static Class<?> mcItemsClass = null;
    public static Class<?> getMcItemsClass() {
        return mcItemsClass == null ? mcItemsClass = getMcClass("Items") : mcItemsClass;
    }

    private static Class<?> mcDataWatcherClass = null;
    public static Class<?> getMcDataWatcherClass() {
        return mcDataWatcherClass == null ? mcDataWatcherClass = getMcClass("DataWatcher") : mcDataWatcherClass;
    }

    private static Class<?> mcDataWatcherObjectClass = null;
    public static Class<?> getMcDataWatcherObjectClass() {
        return mcDataWatcherObjectClass == null ? mcDataWatcherObjectClass = getMcClass("DataWatcherObject") : mcDataWatcherObjectClass;
    }

    private static Class<?> mcDataWatcherSerializerClass = null;
    public static Class<?> getMcDataWatcherSerializerClass() {
        return mcDataWatcherSerializerClass == null ? mcDataWatcherSerializerClass = getMcClass("DataWatcherSerializer") : mcDataWatcherSerializerClass;
    }

    private static Class<?> mcDataWatcherRegistryClass = null;
    public static Class<?> getMcDataWatcherRegistryClass() {
        return mcDataWatcherRegistryClass == null ? mcDataWatcherRegistryClass = getMcClass("DataWatcherRegistry") : mcDataWatcherRegistryClass;
    }

    private static Class<?> mcBlockPositionClass = null;
    public static Class<?> getMcBlockPositionClass() {
        return mcBlockPositionClass == null ? mcBlockPositionClass = getMcClass("BlockPosition") : mcBlockPositionClass;
    }

    private static Class<?> mcEnumDirectionClass = null;
    public static Class<?> getMcEnumDirectionClass() {
        return mcEnumDirectionClass == null ? mcEnumDirectionClass = getMcClass("EnumDirection") : mcEnumDirectionClass;
    }

    private static Class<?> mcNBTTagCompound = null;
    public static Class<?> getMcNBTTagCompoundClass() {
        return mcNBTTagCompound == null ? mcNBTTagCompound = getMcClass("NBTTagCompound") : mcNBTTagCompound;
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static Object getHandle(Object object) throws Exception {
        return object.getClass().getMethod("getHandle").invoke(object);
    }

    public static Object getMcWorld(World world) throws Exception {
        return getHandle(world);
    }

    public static Object getMcWorldServer(World world) throws Exception {
        return getHandle(world);
    }

    public static Object getMcEntityPlayer(Player player) throws Exception {
        return getHandle(player);
    }

    public static Object getMcPlayerConnection(Player player) throws Exception {
        return getMcEntityPlayerClass().getField("playerConnection").get(getMcEntityPlayer(player));
    }

    public static Object getMcBlockPosition(Location location) throws Exception {
        return getMcBlockPositionClass().getConstructor(int.class,int.class,int.class).newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }


}
