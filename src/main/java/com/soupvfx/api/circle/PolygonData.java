package com.soupvfx.api.circle;

import net.minecraft.network.PacketByteBuf;

public record PolygonData(float radius, float thickness, int sides, boolean drawLattice) implements MagicCircleComponent {
    @Override public void write(PacketByteBuf buf) {
        buf.writeFloat(radius); buf.writeFloat(thickness); buf.writeInt(sides); buf.writeBoolean(drawLattice);
    }
    @Override public ComponentType getType() { return ComponentType.POLYGON; }
}