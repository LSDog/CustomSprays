package fun.LSDog.CustomSprays.utils;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;
import fun.LSDog.CustomSprays.CustomSprays;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class ImageGetter implements Closeable {

    /**
     * base64字符串转化成BufferedImage
     */
    public static BufferedImage getBufferedImage(String base64) throws IOException {
        return ImageIO.read( new BASE64DecoderStream(new ByteArrayInputStream(base64.getBytes(StandardCharsets.UTF_8))) );
    }

    private final String destUrl;

    private InputStream in;
    private BufferedImage image;

    public int size;

    public ImageGetter(String destUrl) {
        this.destUrl = destUrl;
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
            conn.setConnectTimeout(10000);
            conn.connect();
            if (conn.getResponseCode() == 403) return 4;
            conn.getInputStream();
            size = conn.getContentLength()/1024;
            if (size == 0) return 4;
            if (size >= CustomSprays.instant.getConfig().getInt("file_size_limit")+1) return 3;
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
            conn.setConnectTimeout(10000);
            conn.connect();
            in = conn.getInputStream();
            image = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String Get128pxImageBase64() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BASE64EncoderStream b64 = new BASE64EncoderStream(out);
        ImageIO.write(get128pxImage(), "png", b64);
        return out.toString("UTF-8");
    }

    private BufferedImage get128pxImage() {
        BufferedImage bufferedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.createGraphics().drawImage(image, 0, 0, 128, 128, null);
        bufferedImage.getGraphics().dispose();
        return bufferedImage;
    }



    @Override
    public void close() {
        // 关闭 InputStream
        if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}