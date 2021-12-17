package fun.LSDog.CustomSprays.map;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.utils.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class MapGetter {

    public static short getFakeMapView() throws Exception {

        MapView mapView = (MapView) NMS.getMcWorldMapClass().getField("mapView").get(getFakeWorldMap());
        mapView.getRenderers().forEach(mapView::removeRenderer);

        CustomSprays.debug("view ID = " + mapView.getId());
        return mapView.getId();
    }

    private static Object getFakeWorldMap() throws Exception {

        Object itemStack = NMS.getMcItemStackClass()
                .getConstructor(NMS.getMcItemClass(), int.class, int.class, boolean.class)
                .newInstance(NMS.getMcItemsClass().getField("MAP").get(null), 1, -1, false);
        Object world = NMS.getMcWorld(Bukkit.getWorlds().get(0));

        //itemStack.setData(world.b("map"));
        NMS.getMcItemStackClass()
                .getMethod("setData", int.class)
                .invoke(itemStack,
                        (int) NMS.getMcWorldClass()
                                .getMethod("b", String.class)
                                .invoke(world, "map")
                );
        String s = "map_" + itemStack.getClass().getMethod("getData").invoke(itemStack);

        CustomSprays.debug("itemStack ID = " + s);
        return NMS.getMcWorldMapClass().getConstructor(String.class).newInstance(s);
    }


    public static ItemStack getMap(MapView mapView) {

        ItemStack map = new ItemStack(Material.MAP, 1, mapView.getId());

        return map;
    }

    public static MapView getMapView(BufferedImage image) {

        MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(getImageMapRenderer(image));

        return mapView;
    }

    private static MapRenderer getImageMapRenderer(BufferedImage image) {

        return new MapRenderer() {
            boolean rendered = false;
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player p) {
                if (rendered) return;
                mapCanvas.drawImage(0, 0, image);
                rendered = true;
            }
        };
    }

}
