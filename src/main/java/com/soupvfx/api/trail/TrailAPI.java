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
    public static final Identifier UPDATE_PACKET = new Identifier("soupyvfx", "update_trail");

    public static void startTrail(ServerWorld world, UUID entityId, int maxPoints, long lifetime, float width, int color) {
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            sendInternal(player, entityId, maxPoints, lifetime, width, color, true);
        }
    }

    public static void updateTrail(ServerWorld world, UUID entityId, float width, int color) {
        TrailData data = TrailRegistry.getServerTrail(entityId);
        if (data == null) return;

        // Update Server-side source of truth
        data.setWidth(width);
        data.setColor(color);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(entityId);
        buf.writeFloat(width);
        buf.writeInt(color);

        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, UPDATE_PACKET, buf);
        }
    }

    public static void stopTrail(ServerWorld world, UUID entityId) {
        TrailRegistry.serverUnregister(entityId);
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(false);
            buf.writeUuid(entityId);
            ServerPlayNetworking.send(player, TRAIL_PACKET, buf);
        }
    }

    public static void syncToPlayer(ServerPlayerEntity player, UUID entityId, TrailData trail) {
        sendInternal(player, entityId, trail.getMaxPoints(), trail.getLifetime(), trail.getWidth(), trail.getColor(), true);
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