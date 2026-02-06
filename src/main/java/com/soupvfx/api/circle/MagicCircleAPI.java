package com.soupvfx.api.circle;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class MagicCircleAPI {
    public static final Identifier SPAWN_PACKET = new Identifier("soupyvfx", "spawn_circle");

    public static MagicCircle drawMagicCircle(ServerWorld world, Vec3d pos, int color, float p, float y, float r, float s, float spin, float pulse) {
        UUID id = UUID.randomUUID();
        MagicCircle circle = new MagicCircle(id, pos, color, p, y, r, s, spin, pulse);
        MagicCircleRegistry.register(id, circle);
        return circle;
    }

    public static void syncToAll(ServerWorld world, MagicCircle circle) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(circle.getId());
        buf.writeDouble(circle.getPos().x);
        buf.writeDouble(circle.getPos().y);
        buf.writeDouble(circle.getPos().z);
        buf.writeInt(circle.getColor());
        buf.writeFloat(circle.getPitch());
        buf.writeFloat(circle.getYaw());
        buf.writeFloat(circle.getRoll());
        buf.writeFloat(circle.getVisualScale()); // write base scale
        buf.writeFloat(0.5f); // dummy spin for this example
        buf.writeFloat(0.1f); // dummy pulse

        // Write Components
        buf.writeInt(circle.getComponents().size());
        for (MagicCircleComponent comp : circle.getComponents()) {
            buf.writeEnumConstant(comp.getType());
            comp.write(buf);
        }

        for (var player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, SPAWN_PACKET, buf);
        }
    }
}