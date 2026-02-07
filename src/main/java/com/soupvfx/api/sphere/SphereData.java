package com.soupvfx.api.sphere;

import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public record SphereData(
        UUID id,
        Vec3d pos,
        float radius,
        float lineWidth,
        int subdivisions, // 0 = 20 sides, 1 = 80, 2 = 320
        int color
) {}