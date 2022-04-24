package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.Data.DataManager;
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
        return CustomSprays.instant;
    }

    /**
     * Get the manager of sprays
     * @see fun.LSDog.CustomSprays.manager.SpraysManager
     */
    public static class SpraysManager extends fun.LSDog.CustomSprays.manager.SpraysManager {
    }

    /**
     * Get the ImageUtil
     * @see fun.LSDog.CustomSprays.utils.ImageUtil
     */
    public static class ImageUtil extends fun.LSDog.CustomSprays.utils.ImageUtil {
    }

    /**
     * Let player spray his/her image
     */
    public static void spray(Player player, boolean isBigSpray) {
        CustomSprays.spray(player, isBigSpray);
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
            setPlayerImage(player, ImageUtil.getPxMapBytes(ImageUtil.resizeImage(image, 384, 384)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a player's image by using byte color array <br>
     * You can use ImageUtil.resizeImage() and resizeImage() to get colorArray
     * @param colorArray <b>Size must be 384*384</b>
     * @see fun.LSDog.CustomSprays.utils.ImageUtil
     */
    public static void setPlayerImage(Player player, byte[] colorArray) {
        DataManager.saveImageBytes(player, colorArray);
    }


}