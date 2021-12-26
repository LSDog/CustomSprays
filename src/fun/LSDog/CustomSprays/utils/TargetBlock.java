package fun.LSDog.CustomSprays.utils;

import com.sun.istack.internal.NotNull;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TargetBlock {

    private final Block block;
    private final BlockFace blockFace;

    public TargetBlock(@NotNull Block block, @NotNull BlockFace blockFace) {
        this.block = block;
        this.blockFace = blockFace;
    }

    public Block getBlock() {
        return block;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public Block getRelativeBlock() {
        return getBlock().getRelative(blockFace);
    }

    public boolean isUpOrDown() {
        return blockFace == BlockFace.UP || blockFace == BlockFace.DOWN;
    }

    @Override
    public String toString() {
        return "TargetBlock{" +
                "block=" + block +
                ", blockFace=" + blockFace +
                '}';
    }
}