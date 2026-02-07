package com.soupvfx.api.circle;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class CircleNetworking {
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(MagicCircleAPI.SPAWN_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            int color = buf.readInt();
            float pitch = buf.readFloat();
            float yaw = buf.readFloat();
            float roll = buf.readFloat();
            float scale = buf.readFloat();
            float spin = buf.readFloat();
            float pulse = buf.readFloat();

            MagicCircle circle = new MagicCircle(id, pos, color, pitch, yaw, roll, scale, spin, pulse);
            int compCount = buf.readInt();
            for (int i = 0; i < compCount; i++) {
                MagicCircleComponent.ComponentType type = buf.readEnumConstant(MagicCircleComponent.ComponentType.class);
                switch (type) {
                    case HEAVY_RING -> circle.addComponent(new HeavyRingData(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readInt()));
                    case POLYGON -> circle.addComponent(new PolygonData(buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readBoolean()));
                    case STAR -> circle.addComponent(new StarData(buf.readFloat(), buf.readFloat(), buf.readInt()));
                    case RUNE_RING -> circle.addComponent(new RuneRingData(buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readFloat()));
                }
            }
            client.execute(() -> MagicCircleRegistry.register(id, circle));
        });

        ClientPlayNetworking.registerGlobalReceiver(MagicCircleAPI.UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            int color = buf.readInt();
            float scale = buf.readFloat();
            float spin = buf.readFloat();
            float pulse = buf.readFloat();

            client.execute(() -> {
                MagicCircle c = MagicCircleRegistry.getCircle(id);
                if (c != null) {
                    c.setColor(color);
                    c.setScale(scale);
                    c.setSpinSpeed(spin);
                    c.setPulseIntensity(pulse);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MagicCircleAPI.REMOVE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            client.execute(() -> MagicCircleRegistry.remove(id));
        });
    }
}