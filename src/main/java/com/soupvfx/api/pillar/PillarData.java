package com.soupvfx.api.pillar;

import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class PillarData {
    private final UUID id;
    private final Vec3d pos;
    private final float yaw;
    private final float pitch;
    private final int sides;
    private float length;
    private float radius;
    private int color;

    public PillarData(UUID id, Vec3d pos, float yaw, float pitch, float length, float radius, int sides, int color) {
        this.id = id;
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
        this.length = length;
        this.radius = radius;
        this.sides = sides;
        this.color = color;
    }

    // Getters
    public UUID id() { return id; }
    public Vec3d pos() { return pos; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public float length() { return length; }
    public float radius() { return radius; }
    public int sides() { return sides; }
    public int color() { return color; }

    // Setters for Real-Time Updates
    public void setLength(float length) { this.length = length; }
    public void setRadius(float radius) { this.radius = radius; }
    public void setColor(int color) { this.color = color; }
}