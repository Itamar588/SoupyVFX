package com.soupvfx.api.sphere;

import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class SphereData {
    private final UUID id;
    private final Vec3d pos;
    private final float lineWidth;
    private final int subdivisions;
    private float radius;
    private int color;

    public SphereData(UUID id, Vec3d pos, float radius, float lineWidth, int subdivisions, int color) {
        this.id = id;
        this.pos = pos;
        this.radius = radius;
        this.lineWidth = lineWidth;
        this.subdivisions = subdivisions;
        this.color = color;
    }

    public UUID id() { return id; }
    public Vec3d pos() { return pos; }
    public float lineWidth() { return lineWidth; }
    public int subdivisions() { return subdivisions; }

    public float radius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public int color() { return color; }
    public void setColor(int color) { this.color = color; }
}