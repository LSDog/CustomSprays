package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.Data.DataManager;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImageDownloader implements Closeable {

    private static int downloadCount = 0;

    private final String destUrl;

    private InputStream in;
    private BufferedImage image;

    public int size;

    public ImageDownloader(String destUrl) throws TooManyDownloadException {
        if (downloadCount >= DataManager.downloadLimit) {
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
            int byteSize = conn.getContentLength();
            if (byteSize == 0) return 4;
            size = byteSize/1024;
            if (size >= CustomSprays.instant.getConfig().getDouble("file_size_limit")+1) return 3;
            else if (conn.getContentLength() == 0) return 4;
        } catch (SSLHandshakeException e) {
            return 2;
        } catch (IllegalArgumentException|IOException e) {
            return 1;
        }
        return 0;
    }

    public BufferedImage getBufferedImage() {
        try {
            URL url = new URL(destUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0;WindowsNT 5.0)");
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.connect();
            in = conn.getInputStream();
            image = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void close() {
        if (downloadCount > 0) downloadCount--;
        // 关闭 InputStream
        if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
    }

}