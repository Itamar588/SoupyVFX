package com.soupvfx.api.trail;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import java.util.UUID;

public class TrailNetworking {
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(TrailAPI.TRAIL_PACKET, (client, handler, buf, responseSender) -> {
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

        ClientPlayNetworking.registerGlobalReceiver(TrailAPI.UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            float width = buf.readFloat();
            int color = buf.readInt();
            client.execute(() -> {
                TrailData trail = TrailRegistry.getTrail(id);
                if (trail != null) {
                    trail.setWidth(width);
                    trail.setColor(color);
                }
            });
        });

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