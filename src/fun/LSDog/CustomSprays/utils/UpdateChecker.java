package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UpdateChecker {


    public static String check() {

        try {
            URL url = new URL("https://gitee.com/api/v5/repos/PixelMC/CustomSprays/releases/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.connect();
            try (InputStream in = conn.getInputStream()) {
                byte[] bytes = new byte[in.available()];
                int read = in.read(bytes);
                if (read == -1) return null;
                JSONObject jsonObject = (JSONObject) JSONValue.parse(new String(bytes, StandardCharsets.UTF_8));
                return (String) jsonObject.get("tag_name");
            }
        } catch (Exception e) {
            CustomSprays.log("failed to check version...");
        }
        return null;
    }


}
