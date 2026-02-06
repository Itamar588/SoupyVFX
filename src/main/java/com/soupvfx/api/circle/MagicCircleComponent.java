package com.soupvfx.api.circle;

import net.minecraft.network.PacketByteBuf;

public interface MagicCircleComponent {
    void write(PacketByteBuf buf);
    ComponentType getType();

    enum ComponentType {
        HEAVY_RING, POLYGON, STAR, RUNE_RING;
    }
}