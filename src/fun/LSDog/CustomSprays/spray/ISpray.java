package fun.LSDog.CustomSprays.spray;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface ISpray {

    void valid() throws Throwable;

    void spawn(Collection<? extends UUID> playersShowTo, boolean playSound, boolean spawnParticle) throws Throwable;

    void remove();

}
