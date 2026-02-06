package com.soupvfx.api.circle;

import net.minecraft.network.PacketByteBuf;

public record StarData(float radius, float thickness, int points) implements MagicCircleComponent {
    @Override public void write(PacketByteBuf buf) {
        buf.writeFloat(radius); buf.writeFloat(thickness); buf.writeInt(points);
    }
    @Override public ComponentType getType() { return ComponentType.STAR; }
}