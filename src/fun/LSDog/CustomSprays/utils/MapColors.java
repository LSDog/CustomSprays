package fun.LSDog.CustomSprays.utils;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.map.MapPalette;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 超级感谢 scrayos 的博客 >> https://scrayos.net/high-performance-map-displays/
 */
public class MapColors {

    private static final File fileMapColorPalette = new File(CustomSprays.instant.getDataFolder()+"/"+CustomSprays.getMcVer()+".colors");
    /* 把索引当成 24位Int颜色 对应 MC的byte颜色值 的颜色版 */
    private static byte[] colors = null;
    private static java.awt.Color[] mcColors;
    private static boolean colorPaletteAvailable = false;
    /* 存颜色板的地方 (在插件配置文件夹) */

    public static boolean isColorPaletteAvailable() {
        return colorPaletteAvailable;
    }

    @SuppressWarnings("deprecation")
    public static byte matchColor(int intColor) {

        if (!colorPaletteAvailable) {
            return MapPalette.matchColor(new Color(intColor, true));
        }

        if (((intColor >> 24) & 0xFF) < 128) return 0; // 当透明度小于 50% 时
        if (colors == null) calculateColorPalette();
        intColor = intColor & 0xFFFFFF; // ARGB -> RGB
        return colors[intColor];
    }

    public static Color[] getMcColors() {
        if (mcColors == null) {
            try {
                Field field = MapPalette.class.getDeclaredField("colors");
                field.setAccessible(true);
                mcColors = (java.awt.Color[]) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mcColors;
    }

    public static void calculateColorPalette() {

        colorPaletteAvailable = false;
        colors = new byte[16777216]; // 0b1_0000_0000

        getMcColors();

        for (int intColor = 0; intColor < colors.length; intColor++) {

            if (intColor % 1048576 == 0) CustomSprays.instant.getLogger().info("Calculating colors... "+(intColor/16777216.0*100)+"%");

            int index = 0;
            double smallestDistance = -1;

            for (int byteColor = 4; byteColor < mcColors.length; byteColor++) {
                double distance = MapColors.getColorDistance(intColor, mcColors[byteColor]);
                if (distance < smallestDistance || smallestDistance == -1) {
                    smallestDistance = distance;
                    index = byteColor;
                }
            }
            colors[intColor] = (byte) ((index < 128) ? index : -129 + (index - 127));
        }
        try(FileOutputStream out = new FileOutputStream(fileMapColorPalette)) {

            out.write(colors);
            out.flush();

            colorPaletteAvailable = true;
            CustomSprays.instant.getLogger().info("Calculation OVER... stored at "+fileMapColorPalette.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean loadColorPalette() {

        colorPaletteAvailable = false;
        colors = new byte[16777216]; // 0b1_0000_0000

        if (fileMapColorPalette.exists()) {
            try (FileInputStream in = new FileInputStream(fileMapColorPalette)) {

                byte tries = 0;
                int left = 0;
                while (left != -1) {
                    tries++;
                    left = in.read(colors, left, colors.length - left);
                    if (tries > 9) {
                        throw new IOException("Failed to read " + fileMapColorPalette.getAbsolutePath());
                    }
                }

                colorPaletteAvailable = true;

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static double getColorDistance(int argbPresetColor, Color comparisonColor) {

        // 从 argbPresetColor 中获取 RGB
        final int presetRed = (argbPresetColor >> 16) & 0xFF;
        final int presetGreen = (argbPresetColor >> 8) & 0xFF;
        final int presetBlue = argbPresetColor & 0xFF;

        // 从 comparisonColor 中获取 RGB
        final int comparisonRed = comparisonColor.getRed();
        final int comparisonGreen = comparisonColor.getGreen();
        final int comparisonBlue = comparisonColor.getBlue();

        // 计算两个红色值之间的平均值
        double redMean = (presetRed + comparisonRed) / 2D;

        // 计算三个颜色层的差异
        int diffRed = presetRed - comparisonRed;
        int diffGreen = presetGreen - comparisonGreen;
        int diffBlue = presetBlue - comparisonBlue;

        // 计算人眼感知的颜色差异的权重
        double weightR = 2D + (redMean / 256D);
        double weightG = 4D;
        double weightB = 2D + ((255 - redMean) / 256D);

        // 计算所有层之间的加权差
        return    weightR * diffRed * diffRed
                + weightG * diffGreen * diffGreen
                + weightB * diffBlue * diffBlue;
    }

}
