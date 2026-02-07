package com.soupvfx.api.trail;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import java.util.UUID;

public class TrailAPI {
    public static final Identifier TRAIL_PACKET = new Identifier("soupyvfx", "trail_sync");

    public static void startTrail(ServerWorld world, UUID entityId, int maxPoints, long lifetime, float width, int color) {
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            sendInternal(player, entityId, maxPoints, lifetime, width, color, true);
        }
    }

    // NEW: Stop method for mod devs to call
    public static void stopTrail(ServerWorld world, UUID entityId) {
        // Remove from Server Registry so newcomers don't sync it
        TrailRegistry.serverUnregister(entityId);

        // Tell all clients to stop rendering
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(false); // START = false
            buf.writeUuid(entityId);
            ServerPlayNetworking.send(player, TRAIL_PACKET, buf);
        }
    }

    public static void syncToPlayer(ServerPlayerEntity player, UUID entityId, TrailData trail) {
        sendInternal(player, entityId, 500, 2000, trail.getWidth(), trail.getColor(), true);
    }

    private static void sendInternal(ServerPlayerEntity player, UUID entityId, int maxPoints, long lifetime, float width, int color, boolean start) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(start);
        buf.writeUuid(entityId);
        if (start) {
            buf.writeInt(maxPoints);
            buf.writeLong(lifetime);
            buf.writeFloat(width);
            buf.writeInt(color);
        }
        ServerPlayNetworking.send(player, TRAIL_PACKET, buf);
    }
}