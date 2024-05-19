package fun.LSDog.CustomSprays.listener;

import fun.LSDog.CustomSprays.CustomSprays;
import fun.LSDog.CustomSprays.util.NMS;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Packet listener for version 1.7
 * <p> Since in version 1.7, netty is under package net.minecraft.util,
 * we are going to use reflections and ams to deal with netty.
 * <p> Handle incoming packets in {@link PacketHandler}
 */
public class PacketListener7 {

    private static final Method Channel_pipeline;
    private static final Method ChannelPipeline_names;
    private static final Method ChannelPipeline_remove;
    private static final Method ChannelPipeline_addBefore;

    private static final Constructor<?> listenerChannelHandler;

    static {
        try {
            Class<?> cChannelPipeline = Class.forName("net.minecraft.util.io.netty.channel.ChannelPipeline");
            Class<?> cChannelHandler = Class.forName("net.minecraft.util.io.netty.channel.ChannelHandler");
            Channel_pipeline = Class.forName("net.minecraft.util.io.netty.channel.AbstractChannel").getMethod("pipeline");
            ChannelPipeline_names = cChannelPipeline.getMethod("names");
            ChannelPipeline_remove = cChannelPipeline.getMethod("remove", String.class);
            ChannelPipeline_addBefore = cChannelPipeline.getMethod("addBefore", String.class, String.class, cChannelHandler);
            listenerChannelHandler = generateChannelHandlerClass().getConstructors()[0];
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String HANDLER_NAME = "customsprays_handler";

    @SuppressWarnings({"unchecked"})
    public static void addPlayer(Player player) {
        try {
            Object channel = NMS.getMcPlayerNettyChannel(player);
            Object pipeline = Channel_pipeline.invoke(channel);
            List<String> handlerNames = (List<String>) ChannelPipeline_names.invoke(pipeline);
            if (channel == null) return;
            if (handlerNames.contains(HANDLER_NAME)) ChannelPipeline_remove.invoke(pipeline, HANDLER_NAME);
            ChannelPipeline_addBefore.invoke(
                    pipeline,
                    "packet_handler",
                    HANDLER_NAME,
                    listenerChannelHandler.newInstance(player)
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void removePlayer(Player player) {
        try {
            Object channel = NMS.getMcPlayerNettyChannel(player);
            Object pipeline = Channel_pipeline.invoke(channel);
            List<String> handlerNames = (List<String>) ChannelPipeline_names.invoke(pipeline);
            if (channel != null && handlerNames.contains(HANDLER_NAME)) {
                ChannelPipeline_remove.invoke(pipeline, HANDLER_NAME);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Black magic (ASM) to generate a class to adapt version 1.7.10,
     * generate a packet handler (a subclass of ChannelHandlerContext) to receive packets.
     *
     * @see PacketListener.ChannelHandler
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static Class<?> generateChannelHandlerClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                "fun/LSDog/CustomSprays/listener/PacketListener7$PacketHandler",
                null,
                "net/minecraft/util/io/netty/channel/ChannelDuplexHandler",
                new String[]{"net/minecraft/util/io/netty/channel/ChannelOutboundHandler"}
        );

        // private final Player player
        cw.visitField(Opcodes.ACC_PRIVATE,
                "player", "Lorg/bukkit/entity/Player;", null, null);

        // <init> - Constructor method
        MethodVisitor mvInit = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "(Lorg/bukkit/entity/Player;)V",
                null,null
        );
        mvInit.visitCode();
        // this.player = player
        mvInit.visitVarInsn(Opcodes.ALOAD, 0); // this
        mvInit.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "net/minecraft/util/io/netty/channel/ChannelDuplexHandler", "<init>", "()V", false);
        mvInit.visitVarInsn(Opcodes.ALOAD, 0); // this
        mvInit.visitVarInsn(Opcodes.ALOAD, 1); // player
        mvInit.visitFieldInsn(Opcodes.PUTFIELD,
                "fun/LSDog/CustomSprays/listener/PacketListener7$PacketHandler", "player", "Lorg/bukkit/entity/Player;");
        // return
        mvInit.visitInsn(Opcodes.RETURN);
        mvInit.visitMaxs(2, 1);
        mvInit.visitEnd();

        // channelRead - Override channelRead method in ChannelOutboundHandler
        MethodVisitor mvRead = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "channelRead",
                "(Lnet/minecraft/util/io/netty/channel/ChannelHandlerContext;Ljava/lang/Object;)V",
                null,
                null
        );
        mvRead.visitAnnotation("java/lang/Override;", true);
        mvRead.visitCode();
        Label labelReturn = new Label();
        mvRead.visitVarInsn(Opcodes.ALOAD, 2); // packet
        mvRead.visitVarInsn(Opcodes.ALOAD, 0); // this
        mvRead.visitFieldInsn(Opcodes.GETFIELD,
                "fun/LSDog/CustomSprays/listener/PacketListener7$PacketHandler", "player", "Lorg/bukkit/entity/Player;");

        mvRead.visitMethodInsn(Opcodes.INVOKESTATIC,
                "fun/LSDog/CustomSprays/listener/PacketHandler", "onReceive", "(Ljava/lang/Object;Lorg/bukkit/entity/Player;)Z", false);
        mvRead.visitVarInsn(Opcodes.ISTORE, 3); // boolean cancel
        mvRead.visitVarInsn(Opcodes.ILOAD, 3);
        mvRead.visitJumpInsn(Opcodes.IFNE, labelReturn);
        mvRead.visitVarInsn(Opcodes.ALOAD, 0); // this
        mvRead.visitVarInsn(Opcodes.ALOAD, 1); // ctx
        mvRead.visitVarInsn(Opcodes.ALOAD, 2); // packet
        mvRead.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "net/minecraft/util/io/netty/channel/ChannelDuplexHandler", "channelRead", "(Lnet/minecraft/util/io/netty/channel/ChannelHandlerContext;Ljava/lang/Object;)V", true);
        // return (labelReturn)
        mvRead.visitLabel(labelReturn);
        mvRead.visitInsn(Opcodes.RETURN);
        mvRead.visitMaxs(3, 1);
        mvRead.visitEnd();

        DynamicClassLoader classLoader = new DynamicClassLoader(CustomSprays.class.getClassLoader());
        return classLoader.defineClass("fun.LSDog.CustomSprays.listener.PacketListener7$PacketHandler", cw.toByteArray());
    }

     @SuppressWarnings("SameParameterValue")
     private static class DynamicClassLoader extends ClassLoader {
        DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }
        Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

}
