package com.soupvfx.api.sphere;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class SphereAPI {
    public static final Identifier SPAWN_PACKET = new Identifier("soupyvfx", "spawn_sphere");
    public static final Identifier REMOVE_PACKET = new Identifier("soupyvfx", "remove_sphere");
    public static final Identifier UPDATE_PACKET = new Identifier("soupyvfx", "update_sphere");

    public static UUID spawnSphere(ServerWorld world, Vec3d pos, float radius, float lineWidth, int subdivisions, int color) {
        UUID id = UUID.randomUUID();
        SphereData data = new SphereData(id, pos, radius, lineWidth, subdivisions, color);
        SphereRegistry.register(id, data);

        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            syncToPlayer(player, data);
        }
        return id;
    }

    public static void updateSphere(ServerWorld world, UUID id, float radius, int color) {
        SphereData sphere = SphereRegistry.getSphere(id);
        if (sphere == null) return;

        sphere.setRadius(radius);
        sphere.setColor(color);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(id);
        buf.writeFloat(radius);
        buf.writeInt(color);

        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, UPDATE_PACKET, buf);
        }
    }

    public static void removeSphere(ServerWorld world, UUID id) {
        SphereRegistry.remove(id);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(id);
        for (ServerPlayerEntity player : PlayerLookup.world(world)) {
            ServerPlayNetworking.send(player, REMOVE_PACKET, buf);
        }
    }

    public static void syncToPlayer(ServerPlayerEntity player, SphereData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(data.id());
        buf.writeDouble(data.pos().x);
        buf.writeDouble(data.pos().y);
        buf.writeDouble(data.pos().z);
        buf.writeFloat(data.radius());
        buf.writeFloat(data.lineWidth());
        buf.writeInt(data.subdivisions());
        buf.writeInt(data.color());
        ServerPlayNetworking.send(player, SPAWN_PACKET, buf);
    }
}