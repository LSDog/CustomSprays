package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.manager.SprayManager;
import fun.LSDog.CustomSprays.map.MapImageByteCanvas;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.utils.Data;
import fun.LSDog.CustomSprays.utils.NMS;
import fun.LSDog.CustomSprays.utils.SprayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Spray {

    private final Player player;
    private final World world;

    private int itemFrameId;

    public Spray(Player player) {
        this.player = player;
        this.world = player.getWorld();
    }

    public void create() {
        try {
            Map.Entry<Block, BlockFace> voidBlock = SprayUtils.getTargetBlock(player);
            if (voidBlock == null) return;
            if (voidBlock.getValue() == BlockFace.UP || voidBlock.getValue() == BlockFace.DOWN ) {
                // TODO 1.13 以上版本 支持差别
                return;
            }
            itemFrameId = spawnItemFrameWithMap (
                    voidBlock.getKey().getRelative(voidBlock.getValue()).getLocation(),
                    voidBlock.getValue(),
                    Data.getImage(player),
                    Bukkit.getOnlinePlayers()
            );
            SprayManager.addSpray(player, this);
            autoRemove();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int spawnItemFrameWithMap(Location location, BlockFace blockFace, Image image, Collection<? extends Player> players) throws Exception {

        short mapViewId = MapViewId.getId(); /*这个不>=0就没效果(就算注册了id)...气死偶嘞！！！！*/

        Object mcMap = NMS.getMcItemStack(new ItemStack(Material.MAP, 1, mapViewId));

        Object itemFrame = NMS.getMcClass("EntityItemFrame")
                .getConstructor(NMS.getMcWorldClass(), NMS.getMcBlockPositionClass(), NMS.getMcEnumDirectionClass())
                .newInstance(
                        NMS.getMcWorld(world),
                        NMS.getMcBlockPosition(location),
                        SprayUtils.blockFaceToEnumDirection(blockFace)
                );
        //set silent
        switch (CustomSprays.getSubVer()) {
            case 8: NMS.getMcEntityClass().getMethod("b", boolean.class).invoke(itemFrame, true); break;
            case 9: NMS.getMcEntityClass().getMethod("c", boolean.class).invoke(itemFrame, true); break;
            default: itemFrame.getClass().getMethod("setSilent", boolean.class).invoke(itemFrame, true);
        }
        // set item
        itemFrame.getClass().getMethod("setItem", NMS.getMcItemStackClass()).invoke(itemFrame, mcMap);
        // set location
        itemFrame.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class).invoke(itemFrame, location.getX(), location.getY(), location.getZ(), SprayUtils.getYawFromPositiveBlockFace(blockFace), 0);
        // get spawn packet
        Object spawnPacket = NMS.getPacketClass("PacketPlayOutSpawnEntity")
                .getConstructor(NMS.getMcEntityClass(), int.class)
                .newInstance(itemFrame, 71); // 71, 谁知道呢
        // get id
        itemFrameId = (int) itemFrame.getClass().getMethod("getId").invoke(itemFrame);


        Object dataWatcher = itemFrame.getClass().getMethod("getDataWatcher").invoke(itemFrame);
        // set dataWatcher
        switch (CustomSprays.getSubVer()) {
            case 8:
                dataWatcher = NMS.getMcDataWatcherClass().getConstructor(NMS.getMcEntityClass()).newInstance(itemFrame);
                NMS.getMcDataWatcherClass()
                        .getMethod("a", int.class, Object.class)
                        .invoke(dataWatcher, 8, mcMap);
                NMS.getMcDataWatcherClass().getMethod("update", int.class).invoke(dataWatcher, 8);
                break;
            case 9:
            case 10: // 1_9~1_10-> "dataWatcher.set(DataWatcherRegistry.f.a(5), mcMap);"
                dataWatcher.getClass()
                        .getMethod("set", NMS.getMcDataWatcherObjectClass(), Object.class)
                        .invoke(
                                dataWatcher,
                                NMS.getMcDataWatcherSerializerClass().getMethod("a", int.class)
                                        .invoke(NMS.getField(NMS.getMcDataWatcherRegistryClass(),null,"f"), 6), com.google.common.base.Optional.of(mcMap)
                        );
                break;
            default: // 1_10~1_12-> "dataWatcher.set(DataWatcherRegistry.f.a(6), mcMap);"
                dataWatcher.getClass()
                        .getMethod("set", NMS.getMcDataWatcherObjectClass(), Object.class)
                        .invoke(
                                dataWatcher,
                                NMS.getMcDataWatcherSerializerClass().getMethod("a", int.class)
                                        .invoke(NMS.getField(NMS.getMcDataWatcherRegistryClass(),null,"f"), 6), mcMap
                        );
                break;
        }


        Object dataPacket = NMS.getPacketClass("PacketPlayOutEntityMetadata")
                .getConstructor(int.class, dataWatcher.getClass(), boolean.class)
                .newInstance(itemFrameId, dataWatcher, false);

        Object mapPacket;
        if (CustomSprays.getSubVer() == 8) {
            mapPacket = NMS.getPacketClass("PacketPlayOutMap")
                    .getConstructor(int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)
                    .newInstance(mapViewId, (byte) 3, new ArrayList<>(), new MapImageByteCanvas(image).getMapImageBuffer(), 0, 0, 128, 128);
        } else {
            mapPacket = NMS.getPacketClass("PacketPlayOutMap")
                    .getConstructor(int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class)
                    .newInstance(mapViewId, (byte) 3, true, new ArrayList<>(), new MapImageByteCanvas(image).getMapImageBuffer(), 0, 0, 128, 128);
        }

        for (Player p : players) {
            NMS.sendPacket(p, spawnPacket);  // spawns a itemFrame with map
            NMS.sendPacket(p, dataPacket);  // add dataWatcher for itemFrame
            NMS.sendPacket(p, mapPacket);  // refresh mapView (draw image)
        }
        SoundEffects.playSound(player, SoundEffects.Effect.SPRAY);

        return itemFrameId;
    }

    public void autoRemove() {
        Bukkit.getScheduler().runTaskLater(CustomSprays.instant, () -> SprayManager.removeSpray(player, this), CustomSprays.instant.getConfig().getInt("destroy")*20L);
    }

    public void destroy() {
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                NMS.sendPacket(p, NMS.getPacketClass("PacketPlayOutEntityDestroy").getConstructor(int[].class).newInstance( new Object[]{new int[]{itemFrameId}} ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
