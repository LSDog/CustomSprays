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
import java.util.ArrayList;

public class PacketHandler {


    // From 26.1 there's a new "Attack" packet for entity attack action, so we can directly listen to that instead of interact packet.
    private static final Field PacketServerboundAttack_entityId;

    private static final Field PacketPlayInUseEntity_entityId;
    private static final Field PacketPlayInUseEntity_action; // enum: ATTACK, INTERACT, ...
    // 1.17+ have a layer class of action type (in order to specify old action type or attack type)
    // use action.a() to get real action enum
    private static Method PacketPlayInUseEntity$Action_getType;

    private static final int mainVer;
    private static final int subVer;

    static {
        try {
            mainVer = NMS.getmainVer();
            subVer = NMS.getSubVer();
            Class<?> cPacketServerboundAttack = (mainVer > 1) ? NMS.getPacketClassMoj("ServerboundAttackPacket") : null;
            PacketServerboundAttack_entityId = (mainVer > 1) ? NMS.getDeclaredField(cPacketServerboundAttack, "entityId") : null;

            Class<?> cPacketPlayInUseEntity = NMS.getPacketClass("ServerboundInteractPacket", "PacketPlayInUseEntity");
            PacketPlayInUseEntity_entityId = NMS.getDeclaredField(cPacketPlayInUseEntity,
                    (mainVer > 1) ? "entityId" : (subVer > 20 || subVer == 20 && NMS.getSubRVer() >= 4) ? "b" : "a");
            PacketPlayInUseEntity_action = NMS.getDeclaredField(cPacketPlayInUseEntity,
                    (mainVer > 1) ? null : (subVer <= 16) ? "action" : (subVer <= 19 || (subVer == 20 && NMS.getSubRVer() <= 3)) ? "b" : "c");
            if (mainVer == 1 || subVer >= 17) {
                Class<?> cPacketPlayInUseEntity$EnumEntityUseAction = NMS.getPacketClass("ServerboundInteractPacket$Action", "PacketPlayInUseEntity$EnumEntityUseAction");
                PacketPlayInUseEntity$Action_getType = cPacketPlayInUseEntity$EnumEntityUseAction.getDeclaredMethod(NMS.AFTER_26_1_R1 ? "getType" : "a");
                PacketPlayInUseEntity$Action_getType.setAccessible(true);
            }
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static boolean onReceive(Object packet, Player player) {

        String packetClassName = packet.getClass().getSimpleName();
        if (mainVer > 1
                && !packetClassName.equals("ServerboundAttackPacket")) return false;
        else if (mainVer == 1
                && !packetClassName.equals("PacketPlayInUseEntity")
                && !packetClassName.equals("ServerboundInteractPacket")) return false;
        try {

            int entityId;

            if (mainVer > 1) {
                entityId = (int) NMS.getDeclaredFieldObject(packet, PacketServerboundAttack_entityId);
            } else {
                // get entity id
                entityId = (int) NMS.getDeclaredFieldObject(packet, PacketPlayInUseEntity_entityId);
                String actionName = getActionNameFromPacketPlayInUseEntity(packet);
                if (!actionName.equals("ATTACK")) return false;
            }

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
                SprayManager.playRemoveSound(player);
            });

            return true; // Cancel this packet because it's a client side entity interaction
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String getActionNameFromPacketPlayInUseEntity(Object packet) {
        try {
            if (NMS.getmainVer() > 1 || NMS.getSubVer() >= 17) {
                return ((Enum<?>) PacketPlayInUseEntity$Action_getType.invoke(
                        PacketPlayInUseEntity_action.get(packet))).name();
            } else {
                return ((Enum<?>) PacketPlayInUseEntity_action.get(packet)).name();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "ERROR";
        }

    }

}
