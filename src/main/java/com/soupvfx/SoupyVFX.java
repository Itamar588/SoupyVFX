package com.soupvfx;

import com.soupvfx.api.circle.*;
import com.soupvfx.api.pillar.*;
import com.soupvfx.api.sphere.*;
import com.soupvfx.api.trail.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SoupyVFX implements ModInitializer {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInitialize() {
        System.out.println("[SoupyVFX] INITIALIZING ULTIMATE SHOWCASE...");

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joiningPlayer = handler.player;
            MagicCircleRegistry.getCircles().forEach(c -> MagicCircleAPI.syncToPlayer(joiningPlayer, c));
            TrailRegistry.getServerActiveTrails().forEach((uuid, data) -> TrailAPI.syncToPlayer(joiningPlayer, uuid, data));
            PillarRegistry.getPillars().forEach(p -> PillarAPI.syncToPlayer(joiningPlayer, p));
            SphereRegistry.getSpheres().forEach(s -> SphereAPI.syncToPlayer(joiningPlayer, s));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld != null) spawnShowcase(overworld);
        });
    }

    private void spawnShowcase(ServerWorld world) {
        Vec3d center = new Vec3d(0, 100, 0);

        float faceUpPitch = 0f;
        int teal = 0x00FFFF, magenta = 0xFF00FF, gold = 0xFFD700, white = 0xFFFFFF;

        // 1. THE CENTRAL PULSING SPHERE
        UUID coreId = SphereAPI.spawnSphere(world, center.add(0, 2, 0), 2.0f, 0.1f, 1, white);

        // 2. THE FOUNDATION (Flat Circles & Color-Shifting Pillars)
        // North
        MagicCircleAPI.drawMagicCircle(world, center.add(5, 0, 0), teal, faceUpPitch, 0, 0, 2f, 1f, 0.05f)
                .addComponent(new HeavyRingData(1.0f, 0.7f, 0.04f, 18));
        UUID p1 = PillarAPI.spawnPillar(world, center.add(5, 0, 0), 0, 0, 6f, 1.2f, 6, teal);

        // South
        MagicCircleAPI.drawMagicCircle(world, center.add(-5, 0, 0), magenta, faceUpPitch, 0, 0, 2f, -1f, 0.05f)
                .addComponent(new PolygonData(1.0f, 0.06f, 8, true));
        UUID p2 = PillarAPI.spawnPillar(world, center.add(-5, 0, 0), 0, 0, 6f, 1.2f, 6, magenta);

        // East
        MagicCircle cEast = MagicCircleAPI.drawMagicCircle(world, center.add(0, 0, 5), gold, faceUpPitch, 0, 0, 2f, 0.5f, 0.1f);
        cEast.addComponent(new StarData(1.0f, 0.03f, 6));
        UUID p3 = PillarAPI.spawnPillar(world, center.add(0, 0, 5), 0, 0, 6f, 1.2f, 6, gold);

        // 3. THE KINETIC ENTITY (Pig with Rainbow Trail)
        PigEntity testSubject = EntityType.PIG.create(world);
        if (testSubject != null) {
            testSubject.setNoGravity(true);
            testSubject.setInvulnerable(true);
            testSubject.refreshPositionAndAngles(0, 102, 0, 0, 0);
            world.spawnEntity(testSubject);

            TrailData rainbowTrail = new TrailData(100, 1500, 0.5f, white);
            TrailRegistry.serverRegister(testSubject.getUuid(), rainbowTrail);
            TrailAPI.startTrail(world, testSubject.getUuid(), 100, 1500, 0.5f, white);

            // 4. ANIMATION ENGINE
            final float[] time = {0};
            SCHEDULER.scheduleAtFixedRate(() -> {
                world.getServer().execute(() -> {
                    time[0] += 0.05f;

                    // Move Pig
                    double posX = Math.cos(time[0] * 2) * 4;
                    double posZ = Math.sin(time[0] * 2) * 4;
                    testSubject.refreshPositionAndAngles(posX, 103 + Math.sin(time[0]), posZ, 0, 0);

                    // Rainbow Calculations
                    int rainbow = Color.HSBtoRGB(time[0] * 0.2f % 1.0f, 0.8f, 1.0f) & 0xFFFFFF;

                    // Update Trail
                    TrailAPI.updateTrail(world, testSubject.getUuid(), 0.5f, rainbow);

                    // Update Sphere (Pulse + Color)
                    float sphereRadius = 1.5f + (float)Math.sin(time[0] * 3) * 0.5f;
                    SphereAPI.updateSphere(world, coreId, sphereRadius, rainbow);

                    // Update Pillars (COLOR SHIFT ONLY)
                    PillarAPI.updatePillar(world, p1, 6f, 1.2f, rainbow + 10);
                    PillarAPI.updatePillar(world, p2, 6f, 1.2f, rainbow + 10);
                    PillarAPI.updatePillar(world, p3, 6f, 1.2f, rainbow + 10);
                });
            }, 0, 50, TimeUnit.MILLISECONDS);
        }
    }
}