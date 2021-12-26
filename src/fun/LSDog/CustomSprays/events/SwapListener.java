package fun.LSDog.CustomSprays.events;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.utils.NMS;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

// helped me a lot in https://www.spigotmc.org/threads/advanced-minecraft-nms-packet-tutorial.538194/
public class SwapListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        injectPlayer(e.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    public void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                PacketDataSerializer serializer = new PacketDataSerializer((ByteBuf) packet);
                PacketPlayInBlockDig packetIn = new PacketPlayInBlockDig();
                packetIn.a(serializer);
                CustomSprays.debug(packetIn.toString());
                //if the server is sending a packet, the function "write" will be called. If you want to cancel a specific packet, just use return; Please keep in mind that using the return thing can break the intire server when using the return thing without knowing what you are doing.
                super.write(channelHandlerContext, packet, channelPromise);
            }
        };

        try {
            ChannelPipeline pipeline = getPlayerChannel(player).pipeline();
            pipeline.addLast(player.getName(), channelDuplexHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removePlayer(Player player) {
        try {
            Channel channel = getPlayerChannel(player);
            channel.eventLoop().submit(() -> {
                channel.pipeline().remove(player.getName());
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Channel getPlayerChannel(Player player) throws Exception {
        // ↓ ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        return (Channel) NMS.getMcNetworkManager().getField("channel")
                    .get(NMS.getMcPlayerConnectionClass().getField("networkManager")
                    .get(NMS.getMcPlayerConnection(player)));
    }

}
