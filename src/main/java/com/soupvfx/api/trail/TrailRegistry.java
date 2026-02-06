package com.soupvfx.api.trail;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TrailRegistry {
    private static final Set<TrailData> TRAILS = ConcurrentHashMap.newKeySet();

    public static void register(TrailData trail) { TRAILS.add(trail); }
    public static void unregister(TrailData trail) { TRAILS.remove(trail); }
    public static Collection<TrailData> getTrails() { return TRAILS; }
}