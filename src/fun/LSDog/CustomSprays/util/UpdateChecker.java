package fun.LSDog.CustomSprays.util;

import fun.LSDog.CustomSprays.CustomSprays;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {


    public static String checkGithub() {

        try {
            URL url = new URL("https://api.github.com/repos/LSDogX/CustomSprays/releases/latest");
            return getVersionFromUrlApi(url);
        } catch (Exception e) {
            CustomSprays.log("failed to check version...");
            CustomSprays.log(e);
        }
        return null;
    }

    public static String checkGitee() {

        try {
            URL url = new URL("https://gitee.com/api/v5/repos/PixelMC/CustomSprays/releases/latest");
            return getVersionFromUrlApi(url);
        } catch (Exception e) {
            CustomSprays.log("failed to check version...");
            CustomSprays.log(e);
        }
        return null;
    }


    private static String getVersionFromUrlApi(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setConnectTimeout(10000);
        conn.connect();
        try (InputStream in = conn.getInputStream()) {
            String string = inputStreamToString(in);
            JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
            CustomSprays.log("get newest version: " + jsonObject.get("tag_name") + " (you are using "+CustomSprays.plugin.getDescription().getVersion()+")");
            return (String) jsonObject.get("tag_name");
        }
    }


    public static String inputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

}
