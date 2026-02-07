package com.soupvfx;

import com.soupvfx.api.circle.CircleNetworking;
import com.soupvfx.api.circle.MagicCircleRenderer;
import com.soupvfx.api.pillar.PillarNetworking;
import com.soupvfx.api.pillar.PillarRenderer;
import com.soupvfx.api.trail.TrailRenderer;
import com.soupvfx.api.trail.TrailNetworking;
import net.fabricmc.api.ClientModInitializer;

public class SoupyVFXClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Init Circles
        MagicCircleRenderer.registerRenderer();
        CircleNetworking.initClient();

        // Init Trails
        TrailRenderer.register();
        TrailNetworking.initClient();

        //init Pillars
        PillarRenderer.register();
        PillarNetworking.initClient();
    }
}