package fun.LSDog.CustomSprays.Data;

import org.bukkit.entity.Player;

public interface IData {

    void saveImageString(Player player, String imageString);

    String getImageString(Player player);

}
