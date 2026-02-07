package com.soupvfx.api.pillar;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class PillarNetworking {
    public static void initClient() {
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

        ClientPlayNetworking.registerGlobalReceiver(PillarAPI.UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            float length = buf.readFloat();
            float radius = buf.readFloat();
            int color = buf.readInt();
            client.execute(() -> {
                PillarData pillar = PillarRegistry.getPillar(id);
                if (pillar != null) {
                    pillar.setLength(length);
                    pillar.setRadius(radius);
                    pillar.setColor(color);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PillarAPI.REMOVE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            client.execute(() -> PillarRegistry.remove(id));
        });
    }
}