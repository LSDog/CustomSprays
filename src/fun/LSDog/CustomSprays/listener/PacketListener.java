package fun.LSDog.CustomSprays.listener;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.util.NMS;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.entity.Player;

/**
 * Packet listener using player's netty channel.
 * <p> Handle incoming packets in {@link PacketHandler}
 */
public class PacketListener {

    private static final String HANDLER_NAME = "customsprays_handler";

    public static void addPlayer(Player player) {
        try {
            CustomSprays.debug("[PacketListener] Injecting netty channel of "+player.getName());

            Channel channel = (Channel) NMS.getMcPlayerNettyChannel(player);
            if (channel == null) return;
            if (channel.pipeline().names().contains(HANDLER_NAME)) {
                channel.pipeline().remove(HANDLER_NAME);
            }

            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new ChannelHandler(player));

            CustomSprays.debug("[PacketListener] Injected netty channel of "+player.getName());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void removePlayer(Player player) {
        try {

            Channel channel = (Channel) NMS.getMcPlayerNettyChannel(player);
            if (channel != null && channel.pipeline().names().contains(HANDLER_NAME)) {
                channel.pipeline().remove(HANDLER_NAME);
            }

            CustomSprays.debug("[PacketListener] Removed netty channel injection of " + player.getName());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handle packet and call {@link PacketHandler#onReceive(Object, Player)} when receiving a packet
     */
    static class ChannelHandler extends ChannelDuplexHandler {

        private final Player player;
        private ChannelHandler(Player player) {
            this.player = player;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
            boolean cancel = PacketHandler.onReceive(packet, player);
            if (!cancel) super.channelRead(ctx, packet);
        }

    }

}
