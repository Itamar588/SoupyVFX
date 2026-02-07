package com.soupvfx.api.pillar;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class PillarAPI {
    public static final Identifier SPAWN_PACKET = new Identifier("soupyvfx", "spawn_pillar");
    public static final Identifier REMOVE_PACKET = new Identifier("soupyvfx", "remove_pillar");
    public static final Identifier UPDATE_PACKET = new Identifier("soupyvfx", "update_pillar");

    public static UUID spawnPillar(ServerWorld world, Vec3d pos, float yaw, float pitch, float length, float radius, int sides, int color) {
        UUID id = UUID.randomUUID();
        PillarData data = new PillarData(id, pos, yaw, pitch, length, radius, sides, color);
        PillarRegistry.register(id, data);

        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            syncToPlayer(player, data);
        }
        return id;
    }

    public static void updatePillar(ServerWorld world, UUID id, float length, float radius, int color) {
        PillarData pillar = PillarRegistry.getPillar(id);
        if (pillar == null) return;

        pillar.setLength(length);
        pillar.setRadius(radius);
        pillar.setColor(color);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(id);
        buf.writeFloat(length);
        buf.writeFloat(radius);
        buf.writeInt(color);

        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, UPDATE_PACKET, buf);
        }
    }

    public static void syncToPlayer(ServerPlayerEntity player, PillarData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(data.id());
        buf.writeDouble(data.pos().x);
        buf.writeDouble(data.pos().y);
        buf.writeDouble(data.pos().z);
        buf.writeFloat(data.yaw());
        buf.writeFloat(data.pitch());
        buf.writeFloat(data.length());
        buf.writeFloat(data.radius());
        buf.writeInt(data.sides());
        buf.writeInt(data.color());
        ServerPlayNetworking.send(player, SPAWN_PACKET, buf);
    }

    public static void removePillar(ServerWorld world, UUID pillarId) {
        PillarRegistry.remove(pillarId);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(pillarId);
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, REMOVE_PACKET, buf);
        }
    }
}