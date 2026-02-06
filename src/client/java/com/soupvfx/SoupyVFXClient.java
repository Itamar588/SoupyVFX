package com.soupvfx;

import com.soupvfx.api.*;
import com.soupvfx.api.circle.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class SoupyVFXClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Initialize the renderer
        MagicCircleRenderer.registerRenderer();

        // Register the networking receiver for multiplayer sync
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

            // Create the base circle object
            MagicCircle circle = new MagicCircle(id, pos, color, pitch, yaw, roll, scale, spin, pulse);

            // Read the components sent by the server
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

            // Sync with the main client thread to update the registry
            client.execute(() -> MagicCircleRegistry.register(id, circle));
        });
    }
}