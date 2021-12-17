package fun.LSDog.CustomSprays;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

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
        return ImageIO.read( new Base64InputStream(new ByteArrayInputStream(base64.getBytes(StandardCharsets.UTF_8))) );
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
            conn.setConnectTimeout(10000);
            conn.getInputStream();
            size = conn.getContentLength()/1024;
            if (size >= CustomSprays.instant.getConfig().getInt("file_size_limit")+1) return 3;
            else if (conn.getContentLength() == 0) return 4;
        } catch (SSLHandshakeException e) {
            return 2;
        } catch (IOException e) {
            return 1;
        }
        return 0;
    }

    public void getBufferedImage() {
        try {
            URL url = new URL(destUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            in = conn.getInputStream();
            image = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String Get128pxImageBase64() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Base64OutputStream b64 = new Base64OutputStream(out);
        ImageIO.write(get128pxImage(), "png", b64);
        return out.toString("UTF-8");
    }

    public String GetImageBase64() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Base64OutputStream b64 = new Base64OutputStream(out);
        ImageIO.write(image, "png", b64);
        return out.toString("UTF-8");
    }

    private BufferedImage get128pxImage() {
        BufferedImage bufferedImage = new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);
        bufferedImage.createGraphics().drawImage(image, 0, 0, 128, 128, null);
        bufferedImage.getGraphics().dispose();
        return bufferedImage;
    }

    public void saveToFile(File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        // 关闭 InputStream
        if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}