package com.soupvfx.api.pillar;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.Collection;

public class PillarRenderer {
    public static void register() {
        WorldRenderEvents.LAST.register(context -> {
            renderPillars(context.matrixStack(), context.camera().getPos());
        });
    }

    private static void renderPillars(MatrixStack matrices, Vec3d cameraPos) {
        Collection<PillarData> pillars = PillarRegistry.getPillars();
        if (pillars == null || pillars.isEmpty()) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // FIXED: Enable blending and setup proper depth states to prevent state leakage
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();

        for (PillarData p : pillars) {
            matrices.push();
            Vec3d relPos = p.pos().subtract(cameraPos);
            matrices.translate(relPos.x, relPos.y, relPos.z);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p.yaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p.pitch()));

            float r = (p.color() >> 16 & 0xFF) / 255.0f;
            float g = (p.color() >> 8 & 0xFF) / 255.0f;
            float bl = (p.color() & 0xFF) / 255.0f;

            Matrix4f m = matrices.peek().getPositionMatrix();

            // 1. DRAW TUBE
            buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= p.sides(); i++) {
                float angle = (float) (i * 2 * Math.PI / p.sides());
                float dx = (float) Math.cos(angle) * p.radius();
                float dz = (float) Math.sin(angle) * p.radius();

                buffer.vertex(m, dx, 0, dz).color(r, g, bl, 1.0f).next();
                buffer.vertex(m, dx, p.length(), dz).color(r, g, bl, 1.0f).next();
            }
            tessellator.draw();

            drawCap(m, buffer, p, r, g, bl, 0);
            drawCap(m, buffer, p, r, g, bl, p.length());

            matrices.pop();
        }

        // FIXED: Strict cleanup to restore vanilla rendering state
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void drawCap(Matrix4f m, BufferBuilder b, PillarData p, float r, float g, float bl, float height) {
        b.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        b.vertex(m, 0, height, 0).color(r, g, bl, 1.0f).next();
        for (int i = 0; i <= p.sides(); i++) {
            float angle = (float) (i * 2 * Math.PI / p.sides());
            float dx = (float) Math.cos(angle) * p.radius();
            float dz = (float) Math.sin(angle) * p.radius();
            b.vertex(m, dx, height, dz).color(r, g, bl, 1.0f).next();
        }
        Tessellator.getInstance().draw();
    }
}