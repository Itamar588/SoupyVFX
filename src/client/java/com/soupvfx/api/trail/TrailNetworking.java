package com.soupvfx.api.trail;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class TrailNetworking {
    // MUST MATCH TrailAPI exactly
    public static final Identifier TRAIL_PACKET = new Identifier("soupyvfx", "trail_sync");

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(TRAIL_PACKET, (client, handler, buf, responseSender) -> {
            boolean start = buf.readBoolean();
            UUID id = buf.readUuid();
            if (start) {
                int max = buf.readInt();
                long life = buf.readLong();
                float w = buf.readFloat();
                int c = buf.readInt();
                client.execute(() -> TrailRegistry.register(id, new TrailData(max, life, w, c)));
            } else {
                client.execute(() -> TrailRegistry.unregister(id));
            }
        });

        // Keep this TICK logic, but REMOVE the addPoint call from the Renderer
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;
            TrailRegistry.getActiveEntityTrails().forEach((uuid, trail) -> {
                Entity target = null;
                for (Entity e : client.world.getEntities()) {
                    if (e.getUuid().equals(uuid)) { target = e; break; }
                }
                if (target != null && !target.isRemoved()) {
                    trail.addPoint(target.getLerpedPos(client.getTickDelta()).add(0, target.getHeight() / 2f, 0));
                    trail.tick();
                } else {
                    trail.setInactive();
                }
            });
        });
    }
}