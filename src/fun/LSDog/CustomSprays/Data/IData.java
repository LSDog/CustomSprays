package fun.LSDog.CustomSprays.Data;

import org.bukkit.entity.Player;

public interface IData {

    int saveImageBytes(Player player, byte[] imgBytes);

    byte[] getImageBytes(Player player);

    void setCopyAllowed(Player player, boolean flag);

    boolean getCopyAllowed(Player player);

}
