package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检测位置是否处于某个区域 (来自各类领域/领地插件) <br>
 * 测试过的版本: <br>
 * Residence - 5.0.1.3 <br>
 * WorldGuard - 6.2.2 & 7.0.6 <br>
 */
public class RegionChecker {

    private static Plugin pluginWorldGuard;

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
        try {
            Object residenceManager = Class.forName("com.bekvon.bukkit.residence.api.ResidenceApi").getMethod("getResidenceManager").invoke(null);
            Object originRes = residenceManager.getClass().getMethod("getByLoc", Location.class).invoke(residenceManager, loc);
            if (originRes != null) {
                String name = (String) NMS.getDeclaredField(originRes, "resName");
                if (name != null) nameList.add(name);
                Object parentRes = originRes;
                while ((name = (String) NMS.getDeclaredField(parentRes = parentRes.getClass().getMethod("getParent").invoke(parentRes), "resName")) != null) {
                    nameList.add(name);
                }
            }
        } catch (ClassNotFoundException ignore) {
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }


        // WorldEdit
        if (pluginWorldGuard == null) pluginWorldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (pluginWorldGuard != null) {
            try {
                if (pluginWorldGuard.getDescription().getVersion().charAt(0) != '7') {
                    // WorldGuard 6.2.2
                    // WorldGuardPlugin.inst().getRegionManager(loc.getWorld()).getApplicableRegionsIDs(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    Object worldGuardPlugin = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin").getMethod("inst").invoke(null);
                    Object regionManager = worldGuardPlugin.getClass().getMethod("getRegionManager", World.class).invoke(worldGuardPlugin, loc.getWorld());
                    Object vector = Class.forName("com.sk89q.worldedit.Vector").getConstructor(int.class, int.class, int.class).newInstance(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    List<String> nList = (List<String>) regionManager.getClass().getMethod("getApplicableRegionsIDs", Class.forName("com.sk89q.worldedit.Vector")).invoke(regionManager, vector);
                    if (nList != null) nameList.addAll(nList);
                } else {
                    // WorldGuard 7.0.6
                    // WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld())).getApplicableRegionsIDs(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    Object worldGuard = Class.forName("com.sk89q.worldguard.WorldGuard").getMethod("getInstance").invoke(null);
                    Object worldGuardPlatform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
                    Object regionContainer = worldGuardPlatform.getClass().getMethod("getRegionContainer").invoke(worldGuardPlatform);
                    Object regionManager = regionContainer.getClass()
                            .getMethod("get", Class.forName("com.sk89q.worldedit.world.World"))
                            .invoke(regionContainer, Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter").getMethod("adapt", World.class).invoke(null, loc.getWorld()));
                    List<String> nList = (List<String>) regionManager.getClass()
                            .getMethod("getApplicableRegionsIDs", Class.forName("com.sk89q.worldedit.math.BlockVector3"))
                            .invoke(regionManager, Class.forName("com.sk89q.worldedit.math.BlockVector3").getMethod("at", int.class, int.class, int.class).invoke(null, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
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
