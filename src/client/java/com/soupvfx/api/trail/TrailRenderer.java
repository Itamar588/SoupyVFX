package com.soupvfx.api.trail;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.List;

public class TrailRenderer {

    public static void register() {
        // Use LAST to ensure we are above other world geometry
        WorldRenderEvents.LAST.register(context -> {
            renderTrails(context.matrixStack(), context.camera().getPos());
        });
    }

    private static void renderTrails(MatrixStack matrices, Vec3d cameraPos) {
        var trails = TrailRegistry.getActiveEntityTrails();
        if (trails.isEmpty()) return;

        // MASTER CLEANUP: Automatically remove trails from memory once they have faded out completely
        trails.entrySet().removeIf(entry -> entry.getValue().isExpired());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        for (TrailData trail : trails.values()) {
            List<TrailPoint> points = trail.getPoints();
            if (points.size() < 2) continue;

            // Start the buffer for POSITION_COLOR
            buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            int color = trail.getColor();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            for (int i = 0; i < points.size(); i++) {
                TrailPoint current = points.get(i);
                Vec3d pos = current.position().subtract(cameraPos);

                // 1. Calculate direction of the segment
                Vec3d dir;
                if (i < points.size() - 1) {
                    dir = points.get(i + 1).position().subtract(current.position()).normalize();
                } else {
                    dir = current.position().subtract(points.get(i - 1).position()).normalize();
                }

                // 2. Calculate camera-facing side vector (Billboarding)
                Vec3d toCamera = current.position().subtract(cameraPos).normalize();
                Vec3d side = dir.crossProduct(toCamera);

                // Safety fallback: if looking straight at the trail, use an 'up' vector
                if (side.lengthSquared() < 0.001) {
                    side = new Vec3d(0, 1, 0);
                } else {
                    side = side.normalize();
                }

                // 3. Width and Alpha logic
                float lifePct = 1.0f - ((float) i / points.size());
                float halfWidth = (trail.getWidth() * lifePct) / 2f;
                float alpha = lifePct * 0.7f;

                // Vertex 1: Left side
                buffer.vertex(matrix, (float)(pos.x - side.x * halfWidth), (float)(pos.y - side.y * halfWidth), (float)(pos.z - side.z * halfWidth))
                        .color(r, g, b, alpha)
                        .next();

                // Vertex 2: Right side
                buffer.vertex(matrix, (float)(pos.x + side.x * halfWidth), (float)(pos.y + side.y * halfWidth), (float)(pos.z + side.z * halfWidth))
                        .color(r, g, b, alpha)
                        .next();
            }
            tessellator.draw();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }
}