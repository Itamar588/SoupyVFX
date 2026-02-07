package com.soupvfx;

import com.soupvfx.api.circle.*;
import com.soupvfx.api.pillar.*;
import com.soupvfx.api.sphere.*;
import com.soupvfx.api.trail.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class SoupyVFX implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("[SoupyVFX] INITIALIZING VFX ENGINE...");

        // Sync all existing server-side VFX to players when they join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joiningPlayer = handler.player;

            // 1. Sync Existing Circles
            for (MagicCircle circle : MagicCircleRegistry.getCircles()) {
                MagicCircleAPI.syncToPlayer(joiningPlayer, circle);
            }

            // 2. Sync Existing Trails
            TrailRegistry.getServerActiveTrails().forEach((uuid, data) -> {
                TrailAPI.syncToPlayer(joiningPlayer, uuid, data);
            });

            // 3. Sync Existing Pillars
            for (PillarData pillar : PillarRegistry.getPillars()) {
                PillarAPI.syncToPlayer(joiningPlayer, pillar);
            }

            // 4. Sync Existing Spheres
            for (SphereData sphere : SphereRegistry.getSpheres()) {
                SphereAPI.syncToPlayer(joiningPlayer, sphere);
            }
        });
    }
}