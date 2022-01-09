package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.map.MapPalette;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImageGetter implements Closeable {

    private static int downloadCount = 0;

    private final String destUrl;

    private InputStream in;
    private BufferedImage image;

    public int size;

    public ImageGetter(String destUrl) throws TooManyDownloadException {
        if (downloadCount >= CustomSprays.instant.getConfig().getInt("download_limit")) {
            throw new TooManyDownloadException();
        }
        downloadCount++;
        this.destUrl = destUrl;
    }

    public static class TooManyDownloadException extends Exception {
    }

    /**
     * 检测url指向的图片大小是否合规
     * 0 -> ok; 1 -> connect failed; 2 -> https connect failed; 3 -> file too big; 4 -> cannot get size;
     */
    public byte checkImage() {
        try {
            // 通过请求获取文件大小
            URL url = new URL(destUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0;WindowsNT 5.0)");
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            conn.connect();
            if (conn.getResponseCode() == 403) return 4;
            conn.getInputStream();
            size = conn.getContentLength()/1024;
            if (size == 0) return 4;
            if (size >= CustomSprays.instant.getConfig().getDouble("file_size_limit")+1) return 3;
            else if (conn.getContentLength() == 0) return 4;
        } catch (SSLHandshakeException e) {
            return 2;
        } catch (IllegalArgumentException|IOException e) {
            return 1;
        }
        return 0;
    }

    public void getBufferedImage() {
        try {
            URL url = new URL(destUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0;WindowsNT 5.0)");
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            conn.connect();
            in = conn.getInputStream();
            image = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public byte[] get384pxMapBytes() throws IOException {
        int[] pixels = new int[384*384];
        get384pxImage().getRGB(0, 0, 384, 384, pixels, 0, 384);
        byte[] result = new byte[384*384];
        for(int i = 0; i < pixels.length; ++i) {
            result[i] = MapPalette.matchColor(new Color(pixels[i], true));
        }
        return result;
    }

    private BufferedImage get384pxImage() {
        BufferedImage bufferedImage = new BufferedImage(384, 384, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.createGraphics().drawImage(image, 0, 0, 384, 384, null);
        bufferedImage.getGraphics().dispose();
        return bufferedImage;
    }

    @Override
    public void close() {
        if (downloadCount > 0) downloadCount--;
        // 关闭 InputStream
        if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
    }




    @SuppressWarnings("deprecation")
    public static Image getImageFromPixels(int w, int h, byte[] pixels) {

        int[] ints = new int[w*h];
        for (int i = 0; i < ints.length; i++) {
            try {
                ints[i] = MapPalette.getColor(pixels[i]).getRGB();
            } catch (IndexOutOfBoundsException e) {
                ints[i] = MapPalette.getColor(MapPalette.matchColor(new Color(pixels[i], true))).getRGB();
            }
        }
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, w, h, ints, 0, 384);

        return image;
    }
}