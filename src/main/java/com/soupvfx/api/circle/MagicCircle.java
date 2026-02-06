package com.soupvfx.api.circle;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MagicCircle {
    private final UUID id;
    private Vec3d pos;
    private float pitch, yaw, roll, scale, spinSpeed, pulseIntensity;
    private int color;
    private float animationTimer = 0;
    private final List<MagicCircleComponent> components = new ArrayList<>();

    public MagicCircle(UUID id, Vec3d pos, int color, float pitch, float yaw, float roll, float scale, float spinSpeed, float pulseIntensity) {
        this.id = id; this.pos = pos; this.color = color;
        this.pitch = pitch; this.yaw = yaw; this.roll = roll;
        this.scale = scale; this.spinSpeed = spinSpeed; this.pulseIntensity = pulseIntensity;
    }

    public void tick() {
        this.roll += this.spinSpeed;
        if (this.roll >= 360) this.roll -= 360;
        this.animationTimer += 0.05f;
    }

    public float getVisualScale() {
        return this.scale + ((float) Math.sin(animationTimer) * pulseIntensity);
    }

    public void addComponent(MagicCircleComponent component) { this.components.add(component); }
    public List<MagicCircleComponent> getComponents() { return components; }
    public UUID getId() { return id; }
    public Vec3d getPos() { return pos; }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
    public float getRoll() { return roll; }
    public int getColor() { return color; }
}