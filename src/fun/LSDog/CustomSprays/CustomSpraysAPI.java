package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.data.DataManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * The API of CustomSprays
 */
@SuppressWarnings("unused")
public class CustomSpraysAPI {

    /**
     * Get the plugin it self
     * @see fun.LSDog.CustomSprays.CustomSprays
     */
    public static Plugin getPlugin() {
        return CustomSprays.plugin;
    }

    /**
     * Let player spray his/her image
     */
    public static void spray(Player player, boolean isBigSpray) {
        fun.LSDog.CustomSprays.spray.SprayManager.spray(player, isBigSpray);
    }

    /**
     * Set a player's image by using byte color array <br>
     * You can use ImageUtil.resizeImage() and resizeImage() to get colorArray
     * @param colorArray <b>Size must be 384*384</b>
     * @see fun.LSDog.CustomSprays.util.ImageUtil
     */
    public static void setPlayerImage(Player player, byte[] colorArray) {
        DataManager.saveImageBytes(player, colorArray);
    }

    /**
     * Get the manager of sprays
     * @see fun.LSDog.CustomSprays.spray.SprayManager
     */
    public static class SprayManager extends fun.LSDog.CustomSprays.spray.SprayManager {
    }

    /**
     * Get a player's byte color array image
     */
    public static byte[] getPlayerByteColorArray(Player player) {
        return DataManager.get384pxImageBytes(player);
    }

    /**
     * Set a player's image
     */
    public static void setPlayerImage(Player player, BufferedImage image) {
        try {
            setPlayerImage(player, ImageUtil.getMcColorBytes(ImageUtil.resizeImage(image, 384, 384)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the ImageUtil
     * @see fun.LSDog.CustomSprays.util.ImageUtil
     */
    public static class ImageUtil extends fun.LSDog.CustomSprays.util.ImageUtil {
    }


}