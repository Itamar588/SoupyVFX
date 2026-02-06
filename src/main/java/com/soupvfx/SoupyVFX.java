package com.soupvfx;

import com.soupvfx.api.circle.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SoupyVFX implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("[SoupyVFX] Initializing Server Logic...");

        // FIX: This replaces START_SERVER_WORLD. It triggers when the server is fully ready.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // FIX: This replaces getRegistryKey(). We ask the server for the Overworld directly.
            ServerWorld overworld = server.getWorld(World.OVERWORLD);

            if (overworld != null) {
                spawnShowcase(overworld);
            }
        });
    }

    private void spawnShowcase(ServerWorld world) {
        System.out.println("[SoupyVFX] Generating Multiplayer-Ready Showcase...");

        float p = 90f; // Pitch (Standing up)
        float s = 2.0f; // Scale
        int teal = 0x00FFFF;
        int gold = 0xFFD700;
        int magenta = 0xFF00FF;
        int red = 0xFF5555;

        // 1. THE HEAVY RING (Teal)
        MagicCircle c1 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(0, 100, 0), teal, p, 0, 0, s, 1.0f, 0.05f);
        c1.addComponent(new HeavyRingData(1.0f, 0.7f, 0.04f, 18));
        MagicCircleAPI.syncToAll(world, c1);

        // 2. THE LATTICE OCTAGON (Magenta)
        MagicCircle c2 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(5, 100, 0), magenta, p, 0, 0, s, 0.8f, 0.1f);
        c2.addComponent(new PolygonData(1.0f, 0.06f, 8, true));
        MagicCircleAPI.syncToAll(world, c2);

        // 3. THE GOLD CIRCLE (Gold)
        MagicCircle c3 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(10, 100, 0), gold, p, 0, 0, s, -1.2f, 0.02f);
        c3.addComponent(new PolygonData(1.0f, 0.03f, 6, false)); // Outline
        c3.addComponent(new StarData(1.0f, 0.03f, 6));           // Star
        c3.addComponent(new RuneRingData(0.6f, 0.12f, 16, 0.7f)); // Rune blocks
        MagicCircleAPI.syncToAll(world, c3);

        // 4. THE ULTIMATE ARRAY (Red)
        MagicCircle c4 = MagicCircleAPI.drawMagicCircle(world, new Vec3d(15, 100, 0), red, p, 0, 0, s, 0.5f, 0.15f);
        c4.addComponent(new HeavyRingData(1.0f, 0.9f, 0.02f, 32));
        c4.addComponent(new PolygonData(0.85f, 0.04f, 4, true));
        c4.addComponent(new StarData(0.6f, 0.02f, 5));
        MagicCircleAPI.syncToAll(world, c4);

        System.out.println("[SoupyVFX] Showcase circles registered and synced to clients!");
    }
}