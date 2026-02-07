package com.soupvfx.api.trail;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrailRegistry {
    private static final Map<UUID, TrailData> ENTITY_TRAILS = new ConcurrentHashMap<>();
    private static final Map<UUID, TrailData> SERVER_ACTIVE_TRAILS = new ConcurrentHashMap<>();

    public static void register(UUID id, TrailData trail) { ENTITY_TRAILS.put(id, trail); }

    public static void unregister(UUID id) {
        TrailData trail = ENTITY_TRAILS.get(id);
        if(trail != null) trail.setInactive();
    }

    // NEW: Server-side cleanup
    public static void serverUnregister(UUID id) {
        SERVER_ACTIVE_TRAILS.remove(id);
    }

    public static Map<UUID, TrailData> getActiveEntityTrails() { return ENTITY_TRAILS; }
    public static void serverRegister(UUID id, TrailData trail) { SERVER_ACTIVE_TRAILS.put(id, trail); }
    public static Map<UUID, TrailData> getServerActiveTrails() { return SERVER_ACTIVE_TRAILS; }
}