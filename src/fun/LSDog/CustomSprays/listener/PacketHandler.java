package fun.LSDog.CustomSprays.listener;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.spray.SprayBase;
import fun.LSDog.CustomSprays.spray.SprayManager;
import fun.LSDog.CustomSprays.util.CoolDown;
import fun.LSDog.CustomSprays.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PacketHandler {


    private static final Field PacketPlayInUseEntity_entityId;
    private static final Field PacketPlayInUseEntity_action; // enum: ATTACK, INTERACT, ...
    // 1.17+ have a layer class of action type (in order to specify old action type or attack type)
    // use action.a() to get real action enum
    private static Method PacketPlayInUseEntity$Action_getType;

    static {
        try {
            int subVer = NMS.getSubVer();
            Class<?> cPacketPlayInUseEntity = NMS.getPacketClass("PacketPlayInUseEntity");
            PacketPlayInUseEntity_entityId = NMS.getDeclaredField(cPacketPlayInUseEntity, "a");
            PacketPlayInUseEntity_action = NMS.getDeclaredField(cPacketPlayInUseEntity, subVer <= 16 ? "action" : "b");
            if (subVer >= 17) {
                Class<?> cPacketPlayInUseEntity$EnumEntityUseAction = NMS.getPacketClass("PacketPlayInUseEntity$EnumEntityUseAction");
                PacketPlayInUseEntity$Action_getType = cPacketPlayInUseEntity$EnumEntityUseAction.getDeclaredMethod("a");
                PacketPlayInUseEntity$Action_getType.setAccessible(true);
            }
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean onReceive(Object packet, Player player) {

        String packetClassName = packet.getClass().getSimpleName();

        if (packetClassName.equals("PacketPlayInUseEntity")) {

            // get entity id
            int entityId = (int) NMS.getDeclaredFieldObject(packet, PacketPlayInUseEntity_entityId);
            String actionName = getActionNameFromPacketPlayInUseEntity(packet);
            if (!actionName.equals("ATTACK")) return false;
            // get spray and check it
            SprayBase spray = SprayManager.getSpray(entityId);
            if (spray == null) return false;
            Player owner = spray.player;
            if (player != owner && !player.hasPermission("CustomSprays.delete")) return false;
            // try to remove it
            Bukkit.getScheduler().runTask(CustomSprays.plugin, () -> {
                spray.remove();
                if (player.hasPermission("CustomSprays.delete")) {
                    player.sendMessage(CustomSprays.prefix + "§7[" + spray.player.getName() + "§7]");
                }
                if (CoolDown.getSprayCd(player) > 1000) CoolDown.setSprayCd(player, 1000);
                SprayBase.playRemoveSound(player);
            });

            return true; // Cancel this packet because it's a client side entity interaction
        }

        return false;
    }

    private static String getActionNameFromPacketPlayInUseEntity(Object packet) {
        try {
            if (NMS.getSubVer() <= 16) {
                return ((Enum<?>) PacketPlayInUseEntity_action.get(packet)).name();
            } else {
                return ((Enum<?>) PacketPlayInUseEntity$Action_getType.invoke(
                        PacketPlayInUseEntity_action.get(packet))).name();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "ERROR";
        }

    }

}
