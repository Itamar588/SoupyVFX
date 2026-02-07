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
        WorldRenderEvents.LAST.register(context -> {
            renderTrails(context.matrixStack(), context.camera().getPos());
        });
    }

    private static void renderTrails(MatrixStack matrices, Vec3d cameraPos) {
        var trails = TrailRegistry.getActiveEntityTrails();
        if (trails.isEmpty()) return;

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

            buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            int color = trail.getColor();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            for (int i = 0; i < points.size(); i++) {
                TrailPoint current = points.get(i);
                Vec3d pos = current.position().subtract(cameraPos);

                // 1. Calculate the movement direction (tangent)
                Vec3d dir;
                if (i < points.size() - 1) {
                    dir = points.get(i + 1).position().subtract(current.position()).normalize();
                } else {
                    dir = current.position().subtract(points.get(i - 1).position()).normalize();
                }

                // 2. Calculate the vector to the camera
                Vec3d toCamera = current.position().subtract(cameraPos).normalize();

                // 3. FIXED BILLBOARDING: Calculate the side vector
                Vec3d side = dir.crossProduct(toCamera);

                // 4. SAFETY FALLBACK: If moving toward camera, cross product is 0.
                // We must use an arbitrary UP vector to prevent the ribbon from disappearing.
                if (side.lengthSquared() < 0.0001) {
                    side = dir.crossProduct(new Vec3d(0, 1, 0));
                    if (side.lengthSquared() < 0.0001) {
                        side = dir.crossProduct(new Vec3d(1, 0, 0));
                    }
                }
                side = side.normalize();

                float lifePct = 1.0f - ((float) i / points.size());
                float halfWidth = (trail.getWidth() * lifePct) / 2f;
                float alpha = lifePct * 0.7f;

                // Expand vertices perpendicular to the look-vector and movement-vector
                buffer.vertex(matrix, (float)(pos.x - side.x * halfWidth), (float)(pos.y - side.y * halfWidth), (float)(pos.z - side.z * halfWidth))
                        .color(r, g, b, alpha).next();
                buffer.vertex(matrix, (float)(pos.x + side.x * halfWidth), (float)(pos.y + side.y * halfWidth), (float)(pos.z + side.z * halfWidth))
                        .color(r, g, b, alpha).next();
            }
            tessellator.draw();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }
}