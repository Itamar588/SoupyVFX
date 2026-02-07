package com.soupvfx.api.pillar;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PillarRegistry {
    private static final Map<UUID, PillarData> PILLARS = new ConcurrentHashMap<>();

    public static void register(UUID id, PillarData data) {
        PILLARS.put(id, data);
    }

    public static void remove(UUID id) {
        PILLARS.remove(id);
    }

    public static Collection<PillarData> getPillars() {
        return PILLARS.values();
    }
}