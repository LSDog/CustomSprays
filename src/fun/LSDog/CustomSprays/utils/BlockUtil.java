package fun.LSDog.CustomSprays.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockUtil {


    public static boolean isSpraySurfaceBlock(Block block) {
        if (block.isEmpty() || block.isLiquid()) return false;
        Material type = block.getType();
        switch (type) {
            case SIGN:
            case WALL_SIGN:
            case STRING:
                return false;
        }
        String name = type.name();
        switch (name) {
            case "LIGHT":
            case "BUBBLE_COLUMN":
            case "CONDUIT":
            case "KELP":
            case "KELP_PLANT":
            case "SEAGRASS":
            case "TALL_SEAGRASS":
            case "SEA_PICKLE":
            case "TURTLE_EGG":
            case "BAMBOO":
            case "BELL":
            case "LANTERN":
            case "SWEET_BERRIES":
            case "SOUL_LANTERN":
            case "SOUL_FIRE":
            case "SOUL_WALL_TORCH":
            case "WARPED_FUNGUS":
            case "CRIMSON_FUNGUS":
            case "WEEPING_VINES":
            case "WEEPING_VINES_PLANT":
            case "TWISTING_VINES":
            case "TWISTING_VINES_PLANT":
            case "NETHER_SPROUTS":
            case "CRIMSON_ROOTS":
            case "WARPED_ROOTS":
            case "AMETHYST_BUD":
            case "AMETHYST_CLUSTER":
            case "LIGHTNING_ROD":
            case "POINTED_DRIPSTONE":
            case "GLOW_LICHEN":
            case "FLOWERING_AZALEA":
            case "MOSS_CARPET":
            case "CAVE_VINES":
            case "CAVE_VINES_PLANT":
            case "GLOW_BERRIES":
            case "BIG_DRIPLEAF":
            case "BIG_DRIPLEAF_STEM":
            case "SMALL_DRIPLEAF":
            case "HANGING_ROOTS":
            case "SPORE_BLOSSOM":
                return false;
        }
        if (
                (name.contains("CORAL") || name.contains("AMETHYST")) && !name.contains("BLOCK")
                || name.contains("CARPET")
                || name.contains("CANDLE")
                || name.contains("SIGN")
        ) {
            return false;
        }
        return !type.isTransparent();
    }


}
