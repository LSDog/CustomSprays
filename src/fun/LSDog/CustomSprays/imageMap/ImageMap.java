package fun.LSDog.CustomSprays.imageMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * ItemFrame+map图片
 */
public class ImageMap {

    public Player player;
    public Location location;
    public Block block;
    public BlockFace blockFace;
    protected World world;
    protected byte[] pixels;
    protected Set<Player> playersShown;
    protected int intDirection;
    protected boolean valid = true;
    private int itemFrameId;

}
