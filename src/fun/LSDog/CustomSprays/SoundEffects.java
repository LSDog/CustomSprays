package fun.LSDog.CustomSprays;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffects {

    public enum Effect {
        SPRAY
    }

    public static void playSound(Player player, Effect effect) {
        if (effect == Effect.SPRAY) {
            if (CustomSprays.getSubVer() == 8)
                playSound(player.getLocation(), Sound.valueOf("SILVERFISH_HIT"), 1, 0.8F);
            else playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1, 0.8F);
        }
    }

    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.stopSound(sound);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void playSound(Location location, Sound sound, float volume, float pitch) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }

}
