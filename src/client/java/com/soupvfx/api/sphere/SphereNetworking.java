package com.soupvfx.api.sphere;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class SphereNetworking {
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(SphereAPI.SPAWN_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            float radius = buf.readFloat();
            float lineWidth = buf.readFloat();
            int subs = buf.readInt();
            int color = buf.readInt();
            client.execute(() -> SphereRegistry.register(id, new SphereData(id, pos, radius, lineWidth, subs, color)));
        });

        ClientPlayNetworking.registerGlobalReceiver(SphereAPI.UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            float radius = buf.readFloat();
            int color = buf.readInt();
            client.execute(() -> {
                SphereData sphere = SphereRegistry.getSphere(id);
                if (sphere != null) {
                    sphere.setRadius(radius);
                    sphere.setColor(color);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SphereAPI.REMOVE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            client.execute(() -> SphereRegistry.remove(id));
        });
    }
}