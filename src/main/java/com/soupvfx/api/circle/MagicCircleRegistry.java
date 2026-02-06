package com.soupvfx.api.circle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MagicCircleRegistry {
    private static final Map<UUID, MagicCircle> CIRCLES = new ConcurrentHashMap<>();
    public static void register(UUID id, MagicCircle circle) { CIRCLES.put(id, circle); }
    public static Collection<MagicCircle> getCircles() { return CIRCLES.values(); }
}