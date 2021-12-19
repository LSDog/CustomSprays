package fun.LSDog.CustomSprays.map;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("deprecation")
public class MapGetter {

    public static ItemStack getMap(MapView mapView) {

        short id = mapView.getId();
        ItemStack map = new ItemStack(Material.MAP);
        map.setDurability(id);

        if (CustomSprays.getSubVer() >= 13) {
            MapMeta mapMeta = (MapMeta) map.getItemMeta();
            try {
                Method setMapIdMethod = mapMeta.getClass().getMethod("setMapId", int.class);
                setMapIdMethod.setAccessible(true);
                setMapIdMethod.invoke(mapMeta, id);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            map.setItemMeta(mapMeta);
        }

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
