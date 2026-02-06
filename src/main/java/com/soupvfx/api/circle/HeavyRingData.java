package com.soupvfx.api.circle;

import net.minecraft.network.PacketByteBuf;

public record HeavyRingData(float outerRadius, float innerRadius, float thickness, int ridgeCount) implements MagicCircleComponent {

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(outerRadius);
        buf.writeFloat(innerRadius);
        buf.writeFloat(thickness);
        buf.writeInt(ridgeCount);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.HEAVY_RING;
    }
}