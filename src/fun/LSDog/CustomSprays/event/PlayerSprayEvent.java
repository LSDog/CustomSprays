package fun.LSDog.CustomSprays.event;

import fun.LSDog.CustomSprays.spray.Spray;
import fun.LSDog.CustomSprays.spray.SprayBig;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Trigger when a player wants to spray (after available checks, before sending packets)
 */
public class PlayerSprayEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The {@link Spray}, not generate (send packet) yet
     * @see #isBigSpray()
     */
    private final Spray spray;
    private boolean canceled = false;

    public PlayerSprayEvent(Player player, Spray spray) {
        super(player);
        this.spray = spray;
    }

    public Spray getSpray() {
        return spray;
    }

    /**
     * Check if it is a big spray
     * @return true if big spray
     */
    public boolean isBigSpray() {
        return spray instanceof SprayBig;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    /**
     * Cancel spraying. If cancelled, the spray will remove itself.
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
