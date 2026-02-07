package com.soupvfx.api.circle;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class MagicCircleAPI {
    public static final Identifier SPAWN_PACKET = new Identifier("soupyvfx", "spawn_circle");
    public static final Identifier REMOVE_PACKET = new Identifier("soupyvfx", "remove_circle");
    public static final Identifier UPDATE_PACKET = new Identifier("soupyvfx", "update_circle");

    public static MagicCircle drawMagicCircle(ServerWorld world, Vec3d pos, int color, float p, float y, float r, float s, float spin, float pulse) {
        UUID id = UUID.randomUUID();
        MagicCircle circle = new MagicCircle(id, pos, color, p, y, r, s, spin, pulse);
        MagicCircleRegistry.register(id, circle);
        syncToAll(world, circle);
        return circle;
    }

    public static void updateCircle(ServerWorld world, UUID id, int color, float scale, float spin, float pulse) {
        MagicCircle circle = MagicCircleRegistry.getCircle(id);
        if (circle == null) return;

        circle.setColor(color);
        circle.setScale(scale);
        circle.setSpinSpeed(spin);
        circle.setPulseIntensity(pulse);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(id);
        buf.writeInt(color);
        buf.writeFloat(scale);
        buf.writeFloat(spin);
        buf.writeFloat(pulse);

        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, UPDATE_PACKET, buf);
        }
    }

    public static void syncToAll(ServerWorld world, MagicCircle circle) {
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            syncToPlayer(player, circle);
        }
    }

    public static void syncToPlayer(ServerPlayerEntity player, MagicCircle circle) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(circle.getId());
        buf.writeDouble(circle.getPos().x);
        buf.writeDouble(circle.getPos().y);
        buf.writeDouble(circle.getPos().z);
        buf.writeInt(circle.getColor());
        buf.writeFloat(circle.getPitch());
        buf.writeFloat(circle.getYaw());
        buf.writeFloat(circle.getRoll());
        buf.writeFloat(circle.getScale());
        buf.writeFloat(circle.getSpinSpeed());
        buf.writeFloat(circle.getPulseIntensity());

        buf.writeInt(circle.getComponents().size());
        for (MagicCircleComponent comp : circle.getComponents()) {
            buf.writeEnumConstant(comp.getType());
            comp.write(buf);
        }
        ServerPlayNetworking.send(player, SPAWN_PACKET, buf);
    }

    public static void removeCircle(ServerWorld world, UUID circleId) {
        MagicCircleRegistry.remove(circleId);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(circleId);
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, REMOVE_PACKET, buf);
        }
    }
}