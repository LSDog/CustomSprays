package fun.LSDog.CustomSprays.util;

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
 * Tested Version: <br>
 * Residence - 5.0.1.3 <br>
 * WorldGuard - 6.2.2 & 7.0.6 <br>
 * GriefDefender - 2.1.4
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


    private static Object GriefDefenderAPI_core;
    private static Method GriefDefenderAPI_Core_getClaimAt;
    private static Method GriefDefenderAPI_Claim_getParents;
    private static Method GriefDefenderAPI_Claim_getDisplayName;

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
                Object WorldGuard = Class.forName("com.sk89q.worldguard.WorldGuard").getMethod("getInstance").invoke(null);
                Object worldGuardPlatform = WorldGuard.getClass().getMethod("getPlatform").invoke(WorldGuard);
                regionContainer = worldGuardPlatform.getClass().getMethod("getRegionContainer").invoke(worldGuardPlatform);
                WorldGuard_RegionContainer_get = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World"));
                WorldEdit_BukkitAdapter_adapt = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter").getMethod("adapt", World.class);
                WorldEdit_BlockVector3 = Class.forName("com.sk89q.worldedit.math.BlockVector3");
                WorldEdit_BlockVector3_at = WorldEdit_BlockVector3.getMethod("at", int.class, int.class, int.class);
            }
        } catch (ReflectiveOperationException | NullPointerException ignore) {
        }
        try {
            GriefDefenderAPI_core = Class.forName("com.griefdefender.api.GriefDefender").getMethod("getCore").invoke(null);
            GriefDefenderAPI_Core_getClaimAt = GriefDefenderAPI_core.getClass().getMethod("getClaimAt", Object.class);
            GriefDefenderAPI_Claim_getParents = Class.forName("com.griefdefender.api.claim").getMethod("getParents", boolean.class);
            GriefDefenderAPI_Claim_getDisplayName = Class.forName("com.griefdefender.api.claim").getMethod("getDisplayName");
        } catch (ReflectiveOperationException | NullPointerException ignore) {
        }
    }

    /**
     * 检测某个位置是否处于禁止的区域
     */
    public static boolean isLocInDisabledRegion(Location loc) {
        List<String> disableList = CustomSprays.instance.getConfig().getStringList("disabled_region");
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
                            .getMethod("getApplicableRegionsIDs", WorldEdit_BlockVector3)
                            .invoke(regionManager, WorldEdit_BlockVector3_at.invoke(null, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    if (nList != null) nameList.addAll(nList);
                }
            } catch (ClassNotFoundException ignore) {
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }


        // GriefDefender
        if (GriefDefenderAPI_core != null) {
            try {
                Object claim = GriefDefenderAPI_Core_getClaimAt.invoke(GriefDefenderAPI_core, loc);
                // GriefDefender.getCore().getClaimAt(loc).getDisplayName();
                nameList.add((String) GriefDefenderAPI_Claim_getDisplayName.invoke(claim));
                // GriefDefender.getCore().getClaimAt(loc).getParents(true);
                List<?> parentClaimList = ((List<?>) GriefDefenderAPI_Claim_getParents.invoke(claim, true));
                for (Object parentClaim : parentClaimList) {
                    nameList.add((String) GriefDefenderAPI_Claim_getDisplayName.invoke(parentClaim));
                }
            } catch (NullPointerException | IllegalAccessException | InvocationTargetException ignore) {
            }
        }

        return nameList;
    }

}