package com.soupvfx.api.sphere;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SphereRegistry {
    private static final Map<UUID, SphereData> SPHERES = new ConcurrentHashMap<>();
    public static void register(UUID id, SphereData data) { SPHERES.put(id, data); }
    public static void remove(UUID id) { SPHERES.remove(id); }
    public static SphereData getSphere(UUID id) { return SPHERES.get(id); }
    public static Collection<SphereData> getSpheres() { return SPHERES.values(); }
}