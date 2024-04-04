package fun.LSDog.CustomSprays.util;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageUtil {

    /**
     * Resize image
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.createGraphics().drawImage(image, 0, 0, width, height, null);
        bufferedImage.getGraphics().dispose();
        return bufferedImage;
    }

    /**
     * Get the color byte array that map use
     */
    public static byte[] getMcColorBytes(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        byte[] result = new byte[width * height];
        for(int i = 0; i < pixels.length; ++i) {
            result[i] = MapColors.matchColor(pixels[i]);
        }
        return result;
    }

}
