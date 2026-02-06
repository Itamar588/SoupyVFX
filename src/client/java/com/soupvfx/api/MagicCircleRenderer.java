package com.soupvfx.api;

import com.mojang.blaze3d.systems.RenderSystem;
import com.soupvfx.api.circle.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class MagicCircleRenderer {
    private static final MagicCircleRenderer INSTANCE = new MagicCircleRenderer();

    public static void registerRenderer() {
        WorldRenderEvents.LAST.register(context -> INSTANCE.onRender(context.matrixStack(), context.camera().getPos()));
    }

    private void onRender(MatrixStack matrices, Vec3d cameraPos) {
        var circles = MagicCircleRegistry.getCircles();
        if (circles.isEmpty()) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (MagicCircle circle : circles) {
            circle.tick();

            matrices.push();
            Vec3d relPos = circle.getPos().subtract(cameraPos);
            matrices.translate(relPos.x, relPos.y, relPos.z);

            // Apply Orientation
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(circle.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(circle.getPitch()));
            // Apply Spin
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(circle.getRoll()));

            float s = circle.getVisualScale();
            matrices.scale(s, s, s);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.disableCull();

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float r = ((circle.getColor() >> 16) & 0xFF) / 255f;
            float g = ((circle.getColor() >> 8) & 0xFF) / 255f;
            float b = (circle.getColor() & 0xFF) / 255f;

            for (MagicCircleComponent comp : circle.getComponents()) {
                if (comp instanceof HeavyRingData d) {
                    buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    drawRibbon(matrix, buffer, d.outerRadius(), d.thickness(), 64, r, g, b, 1f);
                    drawRibbon(matrix, buffer, d.innerRadius(), d.thickness(), 64, r, g, b, 1f);
                    for (int i = 0; i <= d.ridgeCount(); i++) {
                        float a = (float)(i * 2 * Math.PI / d.ridgeCount());
                        drawRidge(matrix, buffer, a, d.innerRadius(), d.outerRadius(), d.thickness(), r, g, b);
                    }
                    tessellator.draw();
                } else if (comp instanceof PolygonData d) {
                    buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                    drawRibbon(matrix, buffer, d.radius(), d.thickness(), d.sides(), r, g, b, 1f);
                    tessellator.draw();
                    if (d.drawLattice()) drawLattice(matrix, buffer, tessellator, d.radius(), d.sides(), d.thickness() * 0.5f, r, g, b, 0.6f);
                } else if (comp instanceof StarData d) {
                    drawLattice(matrix, buffer, tessellator, d.radius(), d.points(), d.thickness(), r, g, b, 1f);
                } else if (comp instanceof RuneRingData d) {
                    drawRunes(matrix, buffer, tessellator, d, r, g, b);
                }
            }
            RenderSystem.enableCull();
            matrices.pop();
        }
    }

    private void drawLattice(Matrix4f m, BufferBuilder b, Tessellator t, float rad, int sides, float thick, float r, float g, float bl, float alpha) {
        Vec3d[] v = new Vec3d[sides];
        for (int i = 0; i < sides; i++) {
            float a = (float)(i * 2 * Math.PI / sides);
            v[i] = new Vec3d(Math.cos(a) * rad, 0, Math.sin(a) * rad);
        }
        int step = (sides > 4) ? 2 : 1;
        for (int i = 0; i < sides; i++) {
            for (int j = i + step; j < sides; j++) {
                if (Math.abs(i - j) == 1 || Math.abs(i - j) == (sides - 1)) continue;
                b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                double dx = v[j].x - v[i].x, dz = v[j].z - v[i].z, len = Math.sqrt(dx*dx + dz*dz);
                float px = (float)(-dz/len * thick/2), pz = (float)(dx/len * thick/2);
                b.vertex(m, (float)v[i].x-px, 0, (float)v[i].z-pz).color(r, g, bl, alpha).next();
                b.vertex(m, (float)v[i].x+px, 0, (float)v[i].z+pz).color(r, g, bl, alpha).next();
                b.vertex(m, (float)v[j].x-px, 0, (float)v[j].z-pz).color(r, g, bl, alpha).next();
                b.vertex(m, (float)v[j].x+px, 0, (float)v[j].z+pz).color(r, g, bl, alpha).next();
                t.draw();
            }
        }
    }

    private void drawRunes(Matrix4f m, BufferBuilder b, Tessellator t, RuneRingData d, float r, float g, float bl) {
        for (int i = 0; i < d.runeCount(); i++) {
            b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            float arc = (float)(i * 2 * Math.PI / d.runeCount()), h = (float)(Math.PI/d.runeCount() * d.arcLength());
            for (float a = arc-h; a <= arc+h; a+=0.1f) {
                float cos = (float)Math.cos(a), sin = (float)Math.sin(a);
                b.vertex(m, cos*(d.radius()-d.thickness()/2), 0, sin*(d.radius()-d.thickness()/2)).color(r, g, bl, 1f).next();
                b.vertex(m, cos*(d.radius()+d.thickness()/2), 0, sin*(d.radius()+d.thickness()/2)).color(r, g, bl, 1f).next();
            }
            t.draw();
        }
    }

    private void drawRibbon(Matrix4f m, BufferBuilder b, float rad, float thick, int seg, float r, float g, float bl, float a) {
        for (int i = 0; i <= seg; i++) {
            float ang = (float)(i * 2 * Math.PI / seg), cos = (float)Math.cos(ang), sin = (float)Math.sin(ang);
            b.vertex(m, cos*(rad-thick/2), 0, sin*(rad-thick/2)).color(r, g, bl, a).next();
            b.vertex(m, cos*(rad+thick/2), 0, sin*(rad+thick/2)).color(r, g, bl, a).next();
        }
    }

    private void drawRidge(Matrix4f m, BufferBuilder b, float a, float iR, float oR, float t, float r, float g, float bl) {
        float dx = (float)Math.cos(a), dz = (float)Math.sin(a), px = -dz*(t/2), pz = dx*(t/2);
        b.vertex(m, dx*iR-px, 0, dz*iR-pz).color(r, g, bl, 1f).next();
        b.vertex(m, dx*iR+px, 0, dz*iR+pz).color(r, g, bl, 1f).next();
        b.vertex(m, dx*oR-px, 0, dz*oR-pz).color(r, g, bl, 1f).next();
        b.vertex(m, dx*oR+px, 0, dz*oR+pz).color(r, g, bl, 1f).next();
    }
}