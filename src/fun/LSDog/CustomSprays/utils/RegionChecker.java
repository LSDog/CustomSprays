package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 检测位置是否处于某个区域 (来自各类领域/领地插件) <br>
 * 测试过的版本: <br>
 * Residence - 5.0.1.3 <br>
 * WorldGuard - 6.2.2 & 7.0.6 <br>
 */
public class RegionChecker {

    private static Object residenceManager;
    private static Method ResidenceManager_getByLoc;
    private static Method ClaimedResidence_getParent;


    private static Plugin WorldGuardPlugin;
    private static double worldGuardVersionFirst;
    private static Method WorldGuard_getRegionManager;

    private static Object regionContainer;
    private static Method WorldGuard_RegionContainer_get;
    private static Method WorldEdit_BukkitAdapter_adapt;
    private static Constructor<?> cWorldEdit_Vector;
    private static Class<?> WorldEdit_BlockVector3;
    private static Method WorldEdit_BlockVector3_at;

    static {
        reload();
    }

    public static void reload() {
        try {
            residenceManager = Class.forName("com.bekvon.bukkit.residence.api.ResidenceApi").getMethod("getResidenceManager").invoke(null);
            ResidenceManager_getByLoc = residenceManager.getClass().getMethod("getByLoc", Location.class);
            ClaimedResidence_getParent = Class.forName("com.bekvon.bukkit.residence.protection.ClaimedResidence").getMethod("getParent");
        } catch (ReflectiveOperationException | NullPointerException ignore) {
        }
        try {
            WorldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
            worldGuardVersionFirst = Double.parseDouble(String.valueOf(WorldGuardPlugin.getDescription().getVersion().charAt(0)));
            if (worldGuardVersionFirst < 7) {
                WorldGuard_getRegionManager = WorldGuardPlugin.getClass().getMethod("getRegionManager", World.class);
                cWorldEdit_Vector = Class.forName("com.sk89q.worldedit.Vector").getConstructor(int.class, int.class, int.class);
            } else {
                Object worldGuardPlatform = WorldGuardPlugin.getClass().getMethod("getPlatform").invoke(WorldGuardPlugin);
                regionContainer = worldGuardPlatform.getClass().getMethod("getRegionContainer").invoke(worldGuardPlatform);
                WorldGuard_RegionContainer_get = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World"));
                WorldEdit_BukkitAdapter_adapt = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter").getMethod("adapt", World.class);
                WorldEdit_BlockVector3 = Class.forName("com.sk89q.worldedit.math.BlockVector3");
                WorldEdit_BlockVector3_at = WorldEdit_BlockVector3.getMethod("at", int.class, int.class, int.class);
            }
        } catch (ReflectiveOperationException | NullPointerException ignore) {
        }
    }

    public static boolean isLocInDisabledRegion(Location loc) {
        List<String> disableList = CustomSprays.instant.getConfig().getStringList("disabled_region");
        for (String name : getRegionNames(loc)) {
            if (disableList.contains(name)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getRegionNames(Location loc) {
        List<String> nameList = new ArrayList<>();

        // Residence
        if (residenceManager != null) {
            try {
                Object residence = ResidenceManager_getByLoc.invoke(residenceManager, loc);
                if (residence != null) {
                    String name = (String) NMS.getDeclaredField(residence, "resName");
                    if (name != null) nameList.add(name);
                    while ((name = (String) NMS.getDeclaredField(residence = ClaimedResidence_getParent.invoke(residence), "resName")) != null) {
                        nameList.add(name);
                    }
                }
            } catch (NullPointerException | IllegalAccessException | InvocationTargetException ignore) {
            }
        }


        // WorldGuard
        if (WorldGuardPlugin != null) {
            try {
                if (worldGuardVersionFirst < 7) {
                    // WorldGuard 6.2.2
                    // WorldGuardPlugin.inst().getRegionManager(loc.getWorld()).getApplicableRegionsIDs(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    Object regionManager = WorldGuard_getRegionManager.invoke(WorldGuardPlugin, loc.getWorld());
                    Object vector = cWorldEdit_Vector.newInstance(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    List<String> nList = (List<String>) regionManager.getClass().getMethod("getApplicableRegionsIDs", Class.forName("com.sk89q.worldedit.Vector")).invoke(regionManager, vector);
                    if (nList != null) nameList.addAll(nList);
                } else {
                    // WorldGuard 7.0.6
                    // WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld())).getApplicableRegionsIDs(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    Object regionManager = WorldGuard_RegionContainer_get.invoke(regionContainer, WorldEdit_BukkitAdapter_adapt.invoke(null, loc.getWorld()));
                    List<String> nList = (List<String>) regionManager.getClass()
                            .getMethod("getApplicableRegionsIDs", WorldEdit_BlockVector3) // 偷个小懒不改了
                            .invoke(regionManager, WorldEdit_BlockVector3_at.invoke(null, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    if (nList != null) nameList.addAll(nList);
                }
            } catch (ClassNotFoundException ignore) {
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }


        return nameList;
    }

}
