package fun.LSDog.CustomSprays.spray;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.util.NMS;
import fun.LSDog.CustomSprays.util.RayTracer;
import fun.LSDog.CustomSprays.util.RegionChecker;
import fun.LSDog.CustomSprays.util.VaultChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public abstract class SprayBase implements ISpray {

    public boolean valid = false;

    public final Player player;
    protected final World world;
    protected final Set<Player> playersShown = new HashSet<>();

    public Block block;
    public Location location;
    public BlockFace blockFace;
    public double distance;
    protected int intDirection;
    protected int intRotation;

    protected final byte[] pixels;

    /**
     * This is the representation item frame id, every entity-related actions are based on this id.
     */
    protected int itemFrameId = -1;

    protected SprayBase(Player player, byte[] pixels, Collection<? extends Player> playersShown) {
        this.player = player;
        this.world = player.getWorld();
        this.pixels = pixels;
        this.playersShown.addAll(playersShown);
    }

    /**
     * Prepare (raytrace and find the surface) for spraying, then
     * call spray event and spawn spray in the first time
     * @param removeTick removal duration, negative numbers == no auto remove
     * @return success or not
     */
    public boolean init(long removeTick) {

        Location eyeLocation = player.getEyeLocation();
        RayTracer.BlockRayTraceResult ray =
                new RayTracer(eyeLocation.getDirection(), eyeLocation, CustomSprays.plugin.getConfig().getDouble("distance")).rayTraceBlock(SprayManager::isSpraySurfaceBlock);
        if (ray == null) return false;

        // 禁止在1.13以下, 在方块上下面喷漆
        if (ray.isUpOrDown() && ( NMS.getSubVer() < 13 || !CustomSprays.plugin.getConfig().getBoolean("spray_on_ground") )) return false;

        block = ray.getRelativeBlock();
        location = block.getLocation();
        blockFace = ray.blockFace;
        distance = ray.distance;
        intDirection = MapFrameFactory.blockFaceToIntDirection(ray.blockFace);
        intRotation = MapFrameFactory.getItemFrameRotate(player.getLocation(), blockFace);

        // ↓喷漆占用就取消
        if (SprayManager.hasSpray(ray.getRelativeBlock(), ray.blockFace)) return false;

        // 喷漆在禁止区域就取消
        if (!player.hasPermission("CustomSprays.nodisable") && RegionChecker.isLocInDisabledRegion(location)) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.DISABLED_REGION"));
            return false;
        }

        double cost = CustomSprays.plugin.getConfig().getDouble((this instanceof SprayBig) ? "spray_big_cost" : "spray_cost");
        if (cost != 0 && !player.hasPermission("CustomSprays.nomoney") && VaultChecker.isVaultEnabled()) {
            if (VaultChecker.costMoney(player, cost)) {
                player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.COST").replace("%cost%", cost+""));
            } else {
                player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_MONEY").replace("%cost%", cost+""));
                return false;
            }
        }

        // Call PlayerSprayEvent
        boolean canceled;
        if (Bukkit.isPrimaryThread()) {
            canceled = SprayManager.callSprayEvent(player, this);
        } else try {
            canceled = Bukkit.getScheduler().callSyncMethod(CustomSprays.plugin, () -> SprayManager.callSprayEvent(player, this)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (canceled) {
            valid = false;
            return false;
        }

        try {
            valid();
            spawn(playersShown, true, DataManager.spray_particle);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }

        SprayManager.addSpray(this);
        if (removeTick >= 0) Bukkit.getScheduler().runTaskLaterAsynchronously(CustomSprays.plugin, this::remove, removeTick);

        return true;
    }
}
