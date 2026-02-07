package com.soupvfx.api.trail;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrailRegistry {
    // Client-side: Active rendering trails
    private static final Map<UUID, TrailData> ENTITY_TRAILS = new ConcurrentHashMap<>();

    // Server-side: Source of truth for active trails
    private static final Map<UUID, TrailData> SERVER_ACTIVE_TRAILS = new ConcurrentHashMap<>();

    // Client Methods
    public static void register(UUID id, TrailData trail) { ENTITY_TRAILS.put(id, trail); }
    public static TrailData getTrail(UUID id) { return ENTITY_TRAILS.get(id); }
    public static void unregister(UUID id) {
        TrailData trail = ENTITY_TRAILS.get(id);
        if(trail != null) trail.setInactive();
    }
    public static Map<UUID, TrailData> getActiveEntityTrails() { return ENTITY_TRAILS; }

    // Server Methods
    public static void serverRegister(UUID id, TrailData trail) { SERVER_ACTIVE_TRAILS.put(id, trail); }
    public static void serverUnregister(UUID id) { SERVER_ACTIVE_TRAILS.remove(id); }
    public static TrailData getServerTrail(UUID id) { return SERVER_ACTIVE_TRAILS.get(id); }
    public static Map<UUID, TrailData> getServerActiveTrails() { return SERVER_ACTIVE_TRAILS; }
}