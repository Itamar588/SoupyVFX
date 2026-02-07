package com.soupvfx.api.pillar;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class PillarNetworking {
    public static void initClient() {
        // Handle Spawning
        ClientPlayNetworking.registerGlobalReceiver(PillarAPI.SPAWN_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            float length = buf.readFloat();
            float radius = buf.readFloat();
            int sides = buf.readInt();
            int color = buf.readInt();

            PillarData data = new PillarData(id, pos, yaw, pitch, length, radius, sides, color);
            client.execute(() -> PillarRegistry.register(id, data));
        });

        // NEW: Handle Removal
        ClientPlayNetworking.registerGlobalReceiver(PillarAPI.REMOVE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            client.execute(() -> PillarRegistry.remove(id));
        });
    }
}