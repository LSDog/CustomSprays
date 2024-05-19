package fun.LSDog.CustomSprays.spray;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface ISpray {

    void valid() throws Throwable;

    void spawn(Collection<? extends Player> playersShowTo, boolean playSound, boolean spawnParticle) throws Throwable;

    void remove();

}
