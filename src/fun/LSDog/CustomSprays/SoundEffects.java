package fun.LSDog.CustomSprays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffects {

    public enum Effect {
        PREPARE_SPRAY,
        SPRAY
    }

    public static void playSound(Player player, Effect effect) {
        switch (effect) {
            case PREPARE_SPRAY:
                playSound(player, Sound.BLOCK_NOTE_HAT, 0.7F, 0.5F);
                Bukkit.getScheduler().runTaskLater(CustomSprays.instant, () -> playSound(player, Sound.BLOCK_NOTE_HAT, 0.5F, 0.65F), 2);
                Bukkit.getScheduler().runTaskLater(CustomSprays.instant, () -> playSound(player, Sound.BLOCK_NOTE_HAT, 0.5F, 0.5F), 4);
                break;
            case SPRAY:
                playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1, 0.8F);
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
