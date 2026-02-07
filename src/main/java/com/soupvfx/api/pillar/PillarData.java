package com.soupvfx.api.pillar;

import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public record PillarData(
    UUID id, 
    Vec3d pos, 
    float yaw, 
    float pitch, 
    float length, 
    float radius, 
    int sides, 
    int color
) {}