package com.soupvfx.api.trail;

import net.minecraft.util.math.Vec3d;
import java.util.LinkedList;
import java.util.List;

public class TrailData {
    private final LinkedList<TrailPoint> points = new LinkedList<>();
    private final int maxPoints;
    private final long lifetimeMs;
    private final float width;
    private final int color;
    private boolean active = true;

    public TrailData(int maxPoints, long lifetimeMs, float width, int color) {
        this.maxPoints = maxPoints;
        this.lifetimeMs = lifetimeMs;
        this.width = width;
        this.color = color;
    }

    public void addPoint(Vec3d pos) {
        if (!active) return;
        points.addFirst(new TrailPoint(pos, System.currentTimeMillis()));
        if (points.size() > maxPoints) points.removeLast();
    }

    public void tick() {
        long now = System.currentTimeMillis();
        points.removeIf(p -> (now - p.timestamp()) > lifetimeMs);
    }

    public void setInactive() { this.active = false; }
    public boolean isExpired() { return !active && points.isEmpty(); }
    public List<TrailPoint> getPoints() { return points; }
    public float getWidth() { return width; }
    public int getColor() { return color; }
}