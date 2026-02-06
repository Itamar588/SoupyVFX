package com.soupvfx.api.trail;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TrailRenderer {
    public static void register() {
        WorldRenderEvents.LAST.register(context -> {
            render(context.matrixStack(), context.camera().getPos());
        });
    }

    private static void render(MatrixStack matrices, Vec3d cameraPos) {
        var trails = TrailRegistry.getTrails();
        if (trails.isEmpty()) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableCull();

        for (TrailData trail : trails) {
            trail.tick();
            var points = trail.getPoints();
            if (points.size() < 2) continue;

            buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            int color = trail.getColor();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            for (int i = 0; i < points.size(); i++) {
                TrailPoint p = points.get(i);
                Vec3d pos = p.position().subtract(cameraPos);

                // Trail Taper: Newer points are wider, older points are thinner
                float agePct = 1.0f - ((float) i / points.size());
                float currentWidth = trail.getWidth() * agePct;

                // Simple Billboarding Math:
                // We expand the vertex "up" and "down" relative to the camera
                // For a toolkit, this provides a "plasma ribbon" look.
                buffer.vertex(matrix, (float)pos.x, (float)pos.y - (currentWidth / 2), (float)pos.z)
                        .color(r, g, b, agePct).next();
                buffer.vertex(matrix, (float)pos.x, (float)pos.y + (currentWidth / 2), (float)pos.z)
                        .color(r, g, b, agePct).next();
            }
            tessellator.draw();
        }
        RenderSystem.enableCull();
    }
}