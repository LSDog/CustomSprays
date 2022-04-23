package fun.LSDog.CustomSprays;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffects {

    public static void spray(Player player) {
        String sound = CustomSprays.instant.getConfig().getString("spray_sound");
        if (sound == null || "default".equals(sound)) {
            if (CustomSprays.getSubVer() == 8) player.getWorld().playSound(player.getLocation(), Sound.valueOf("SILVERFISH_HIT"), 1, 0.8F);
            else player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_HURT, 1, 0.8F);
        } else {
            String[] strings = sound.split("-");
            if (strings.length == 3) {
                player.getWorld().playSound(player.getLocation(), strings[0], Float.parseFloat(strings[1]), Float.parseFloat(strings[2]));
            }
        }
    }

}
