package com.soupvfx.api.circle;

import net.minecraft.network.PacketByteBuf;

public record RuneRingData(float radius, float thickness, int runeCount, float arcLength) implements MagicCircleComponent {
    @Override public void write(PacketByteBuf buf) {
        buf.writeFloat(radius); buf.writeFloat(thickness); buf.writeInt(runeCount); buf.writeFloat(arcLength);
    }
    @Override public ComponentType getType() { return ComponentType.RUNE_RING; }
}