package fun.LSDog.CustomSprays.map;

import org.bukkit.map.MapPalette;

import java.awt.*;
import java.util.Arrays;

public class MapImageByteCanvas {

    private final byte[] buffer = new byte[128 * 128];

    private final Image image;

    public MapImageByteCanvas(Image image) {
        this.image = MapPalette.resizeImage(image);
        drawImage();
    }

    public byte[] getMapImageBuffer() {
        byte[] buf = new byte[16384];
        Arrays.fill(buf, (byte) 0);
        for (int i = 0; i < this.buffer.length; ++i) {
            byte color = this.buffer[i];
            if (color >= 0 || color <= -49) buf[i] = color;
        }
        return buf;
    }

    @SuppressWarnings("deprecation")
    private void drawImage() {
        byte[] bytes = MapPalette.imageToBytes(image);
        for (int x2 = 0; x2 < image.getWidth(null); ++x2) {
            for (int y2 = 0; y2 < image.getHeight(null); ++y2) {
                setPixel(x2, y2, bytes[y2 * image.getWidth(null) + x2]);
            }
        }
    }

    private void setPixel(int x, int y, byte color) {
        if (x < 0 || y < 0 || x >= 128 || y >= 128)
            return;
        if (buffer[y * 128 + x] != color) {
            buffer[y * 128 + x] = color;
        }
    }

}
