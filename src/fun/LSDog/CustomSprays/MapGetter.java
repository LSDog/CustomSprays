package fun.LSDog.CustomSprays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.Collections;

public class MapGetter {

    public static MapView getMapView(BufferedImage image) {

        MapRenderer renderer = new MapRenderer() {
            boolean rendered = false;
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                if (rendered) return;
                mapCanvas.drawImage(0, 0, MapPalette.resizeImage(image));
                rendered = true;
            }
        };

        MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(renderer);

        return mapView;
    }

    public static ItemStack getMap(MapView mapView) {

        ItemStack map = new ItemStack(Material.MAP);

        map.setDurability(mapView.getId());
        MapMeta meta = (MapMeta) map.getItemMeta();
        meta.setScaling(false);
        meta.setLore(Collections.emptyList());
        map.setItemMeta(meta);

        return map;
    }


}
