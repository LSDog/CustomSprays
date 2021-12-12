package fun.LSDog.CustomSprays.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

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
        getMcPlayerConnectionClass().getMethod("sendPacket", getPacketClass()).invoke(getMcPlayerConnection(player), packet);
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static Object getField(Object object, String name) {
        try {
            Field field = object.getClass().getField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
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
    public static Object getDeclaredField(Class<?> clazz, Object object, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(clazz.cast(object));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static Class<?> getCraftClass(String name) {
        return getClass("org.bukkit.craftbukkit."+version+"."+name);
    }

    public static Class<?> getMcClass(String name) {
        return getClass("net.minecraft.server."+version+"."+name);
    }
    
    private static Class<?> craftItemStackClass = null;
    public static Class<?> getCraftItemStackClass() {
        return craftItemStackClass == null ? craftItemStackClass = getCraftClass("inventory.CraftItemStack") : craftItemStackClass;
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

    private static Class<?> mcEntityLivingClass = null;
    public static Class<?> getMcEntityLivingClass() {
        return mcEntityLivingClass == null ? mcEntityLivingClass = getMcClass("EntityLiving") : mcEntityLivingClass;
    }

    private static Class<?> mcEntityPlayerClass = null;
    public static Class<?> getMcEntityPlayerClass() {
        return mcEntityPlayerClass == null ? mcEntityPlayerClass = getMcClass("EntityPlayer") : mcEntityPlayerClass;
    }

    private static Class<?> mcPlayerConnectionClass = null;
    public static Class<?> getMcPlayerConnectionClass() {
        return mcPlayerConnectionClass == null ? mcPlayerConnectionClass = getMcClass("PlayerConnection") : mcPlayerConnectionClass;
    }

    private static Class<?> mcVec3D = null;
    public static Class<?> getMcVec3DClass() {
        return mcVec3D == null ? mcVec3D = getMcClass("Vec3D") : mcVec3D;
    }

    private static Class<?> mcItemStack = null;
    public static Class<?> getMcItemStackClass() {
        return mcItemStack == null ? mcItemStack = getMcClass("ItemStack") : mcItemStack;
    }

    private static Class<?> mcDataWatcher = null;
    public static Class<?> getMcDataWatcherClass() {
        return mcDataWatcher == null ? mcDataWatcher = getMcClass("DataWatcher") : mcDataWatcher;
    }

    private static Class<?> mcDataWatcherObject = null;
    public static Class<?> getMcDataWatcherObjectClass() {
        return mcDataWatcherObject == null ? mcDataWatcherObject = getMcClass("DataWatcherObject") : mcDataWatcherObject;
    }

    private static Class<?> mcDataWatcherSerializer = null;
    public static Class<?> getMcDataWatcherSerializerClass() {
        return mcDataWatcherSerializer == null ? mcDataWatcherSerializer = getMcClass("DataWatcherSerializer") : mcDataWatcherSerializer;
    }

    private static Class<?> mcDataWatcherRegistry = null;
    public static Class<?> getMcDataWatcherRegistryClass() {
        return mcDataWatcherRegistry == null ? mcDataWatcherRegistry = getMcClass("DataWatcherRegistry") : mcDataWatcherRegistry;
    }

    private static Class<?> mcBlockPosition = null;
    public static Class<?> getMcBlockPositionClass() {
        return mcBlockPosition == null ? mcBlockPosition = getMcClass("BlockPosition") : mcBlockPosition;
    }

    private static Class<?> mcEnumDirection = null;
    public static Class<?> getMcEnumDirectionClass() {
        return mcEnumDirection == null ? mcEnumDirection = getMcClass("EnumDirection") : mcEnumDirection;
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static Object getHandle(Object object) throws Exception {
        return object.getClass().getMethod("getHandle").invoke(object);
    }

    public static Object getMcWorldServer(World world) throws Exception {
        return getHandle(world);
    }

    public static Object getMcServer() throws Exception {
        return getHandle(Bukkit.getServer());
    }

    public static Object getMcWorld(World world) throws Exception {
        return getHandle(world);
    }

    public static Object getMcEntityPlayer(Player player) throws Exception {
        return getHandle(player);
    }

    public static Object getMcEntity(Entity entity) throws Exception {
        return getHandle(entity);
    }

    public static Object getMcEntity(World world, int id) throws Exception {
        return getMcWorldServerClass().getMethod("getEntity", int.class).invoke(getMcWorldServer(world), id);
    }

    public static Object getMcPlayerConnection(Player player) throws Exception {
        return getMcEntityPlayerClass().getField("playerConnection").get(getMcEntityPlayer(player));
    }

    public static Object getMcItemStack(ItemStack itemStack) throws Exception {
        return getCraftItemStackClass().getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
    }

    public static Object getMcBlockPosition(Location location) throws Exception {
        return getMcBlockPositionClass().getConstructor(int.class,int.class,int.class).newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }


}
