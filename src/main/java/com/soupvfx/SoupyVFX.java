package com.soupvfx;

import com.soupvfx.api.circle.*;
import com.soupvfx.api.pillar.PillarAPI;
import com.soupvfx.api.pillar.PillarData;
import com.soupvfx.api.pillar.PillarRegistry;
import com.soupvfx.api.sphere.SphereAPI;
import com.soupvfx.api.sphere.SphereData;
import com.soupvfx.api.sphere.SphereRegistry;
import com.soupvfx.api.trail.TrailAPI;
import com.soupvfx.api.trail.TrailData;
import com.soupvfx.api.trail.TrailRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SoupyVFX implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("[SoupyVFX] INITIALIZING MAIN MOD...");

        // Start trail when entity loads
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                TrailData data = new TrailData(500, 2000, 0.4f, 0x00FFFF);
                TrailRegistry.serverRegister(player.getUuid(), data);
                TrailAPI.startTrail(world, player.getUuid(), 500, 2000, 0.4f, 0x00FFFF);
            }
        });

        // Sync existing state to players joining the session
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joiningPlayer = handler.player;

            // 1. Sync Existing Circles
            for (MagicCircle circle : MagicCircleRegistry.getCircles()) {
                MagicCircleAPI.syncToPlayer(joiningPlayer, circle);
            }

            // 2. Sync Existing Trails
            TrailRegistry.getServerActiveTrails().forEach((uuid, data) -> {
                if (!uuid.equals(joiningPlayer.getUuid())) {
                    TrailAPI.syncToPlayer(joiningPlayer, uuid, data);
                }
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

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld != null) spawnShowcase(overworld);
        });
    }

    private void spawnShowcase(ServerWorld world) {
        float p = 90f; float s = 2.0f;
        int teal = 0x00FFFF; int gold = 0xFFD700; int magenta = 0xFF00FF; int red = 0xFF5555;

        // 1. THE PRISM
        PillarAPI.spawnPillar(world, new Vec3d(20, 100, 0), 0, 0, 5f, 1.5f, 6, 0x00FF00);

        // 2. THE SPHERE (20-Sided Icosahedron)
        // world, pos, radius, lineWidth, subdivisions, color
        SphereAPI.spawnSphere(world, new Vec3d(30, 100, 0), 2.5f, 0.08f, 0, 0xFFFFFF);

        // 3. THE MAGIC CIRCLES
        MagicCircle c1 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(0, 100, 0), teal, p, 0, 0, s, 1.0f, 0.05f);
        c1.addComponent(new HeavyRingData(1.0f, 0.7f, 0.04f, 18));

        MagicCircle c2 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(5, 100, 0), magenta, p, 0, 0, s, 0.8f, 0.1f);
        c2.addComponent(new PolygonData(1.0f, 0.06f, 8, true));

        MagicCircle c3 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(10, 100, 0), gold, p, 0, 0, s, -1.2f, 0.02f);
        c3.addComponent(new RuneRingData(1.0f, 0.03f, 6, 4));
        c3.addComponent(new StarData(1.0f, 0.03f, 6));

        MagicCircle c4 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(15, 100, 0), red, p, 0, 0, s, 0.5f, 0.2f);
        c4.addComponent(new HeavyRingData(1.0f, 0.9f, 0.02f, 32));
        c4.addComponent(new PolygonData(0.85f, 0.04f, 4, true));
        c4.addComponent(new StarData(0.8f, 0.05f, 5));
    }
}