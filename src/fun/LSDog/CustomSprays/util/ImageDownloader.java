package fun.LSDog.CustomSprays.util;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.data.DataManager;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class ImageDownloader implements Closeable {

    private static int downloadCount = 0;

    private String destUrl;
    private boolean triedNoHttps = false;
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

    private static void setSimulationProp(URLConnection conn) {
        String url = conn.getURL().toString();
        String host = url.substring(url.indexOf("://") + 3);
        if (!host.contains("/")) return;
        host = host.substring(0, url.indexOf("/"));
        conn.setRequestProperty("HOST", host);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Microsoft Edge\";v=\"101\"");
        conn.setRequestProperty("sec-ch-ua-mobile", "?0");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.64 Safari/537.36 Edg/101.0.1210.47");
        conn.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
        conn.setRequestProperty("Accept", "image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        conn.setRequestProperty("Sec-Fetch-Site", "cross-site");
        conn.setRequestProperty("Sec-Fetch-Mode", "no-cors");
        conn.setRequestProperty("Sec-Fetch-Dest", "image");
        if (url.contains("i.pximg.net"))
            conn.setRequestProperty("Referer", "https://www.pixiv.net/");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Accept-Language", "en;q=0.9");
    }

    @Override
    public void close() {
        if (downloadCount > 0) downloadCount--;
        // 关闭 InputStream
        if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
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
            setSimulationProp(conn);
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            if (conn.getResponseCode() == 403) return 4;
            int byteSize = conn.getContentLength();
            String contentType = conn.getContentType();
            if ((byteSize == 0) || (contentType != null && !contentType.startsWith("image"))) return 4;
            size = byteSize/1024;
            if (size >= CustomSprays.instance.getConfig().getDouble("file_size_limit") + 1) {
                return 3;
            } else if (conn.getContentLength() == 0){
                return 4;
            }
        } catch (SSLHandshakeException e) {
            if (triedNoHttps) return 2;
            triedNoHttps = true;
            destUrl = destUrl.replaceFirst("(?i)https", "http");
            return checkImage();
        } catch (IllegalArgumentException | IOException e) {
            return 1;
        }
        return 0;
    }

    public BufferedImage getBufferedImage() {
        try {
            URL url = new URL(destUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            setSimulationProp(conn);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.connect();
            in = conn.getInputStream();
            image = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}