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
            URL url = new URL("https://api.github.com/repos/LSDog/CustomSprays/releases/latest");
            return getVersionFromUrlApi(url);
        } catch (Exception e) {
            CustomSprays.log("failed to check version: " + e);
        }
        return null;
    }

    public static String checkGitee() {

        try {
            URL url = new URL("https://gitee.com/api/v5/repos/PixelMC/CustomSprays/releases/latest");
            return getVersionFromUrlApi(url);
        } catch (Exception e) {
            CustomSprays.log("failed to check version: " + e);
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
            CustomSprays.log("Found the newest version: " + jsonObject.get("tag_name") + " (you are using "+CustomSprays.plugin.getDescription().getVersion()+")");
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

    /**
     * 比较两个版本号的大小
     * @return int < 0 → ver1 < ver2, int = 0 → ver1 = ver2, int > 0 → ver1 > ver2
     */
    public static int compareVersions(String ver1, String ver2) {
        String[] ver1strs = ver1.split("\\.");
        String[] ver2strs = ver2.split("\\.");
        // 统一版本数字段数（向最小位(最右)对齐）
        int lengthOff = ver1strs.length - ver2strs.length;
        if (lengthOff > 0) ver2strs = backPushArray(ver2strs, lengthOff);
        else if (lengthOff < 0) ver1strs = backPushArray(ver1strs, -lengthOff);
        int diff;
        for (int i = 0; i < ver1strs.length; i++) {
            diff = toInt(ver1strs[i]) - toInt(ver2strs[i]);
            if (diff != 0) return diff;
        }
        return 0;
    }

    private static String[] backPushArray(String[] array, int pos) {
        String[] newArray = new String[array.length+pos];
        System.arraycopy(array, 0, newArray, pos, array.length);
        return newArray;
    }

    private static int toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
