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

    public static void spawnPillar(ServerWorld world, Vec3d pos, float yaw, float pitch, float length, float radius, int sides, int color) {
        UUID id = UUID.randomUUID();
        PillarData data = new PillarData(id, pos, yaw, pitch, length, radius, sides, color);

        PillarRegistry.register(id, data);

        // Broadcast to everyone currently online
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            syncToPlayer(player, data);
        }
    }

    // NEW: For syncing specific players (Join) or broadcasting spawns
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

    // NEW: The Removal method
    public static void removePillar(ServerWorld world, UUID pillarId) {
        PillarRegistry.remove(pillarId); // Server cleanup

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(pillarId);

        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, REMOVE_PACKET, buf);
        }
    }
}