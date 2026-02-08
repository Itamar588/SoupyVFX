package com.soupvfx.api.circle;

import com.mojang.blaze3d.systems.RenderSystem;
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

        // FIX: Setup clean state for transparency sorting
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false); // Prevents circles from creating "box" artifacts in glass
        RenderSystem.disableCull();

        for (MagicCircle circle : circles) {
            circle.tick();

            matrices.push();
            Vec3d relPos = circle.getPos().subtract(cameraPos);
            matrices.translate(relPos.x, relPos.y, relPos.z);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(circle.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(circle.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(circle.getRoll()));

            float s = circle.getVisualScale();
            matrices.scale(s, s, s);

            float r = (circle.getColor() >> 16 & 0xFF) / 255.0f;
            float g = (circle.getColor() >> 8 & 0xFF) / 255.0f;
            float bl = (circle.getColor() & 0xFF) / 255.0f;

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            for (MagicCircleComponent comp : circle.getComponents()) {
                renderComponent(matrix, tessellator, buffer, comp, r, g, bl);
            }

            matrices.pop();
        }

        // FIX: Mandatory cleanup to restore Vanilla rendering
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private void renderComponent(Matrix4f m, Tessellator t, BufferBuilder b, MagicCircleComponent comp, float r, float g, float bl) {
        if (comp instanceof HeavyRingData d) {
            b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            drawRibbon(m, b, d.outerRadius(), d.thickness(), d.ridgeCount(), r, g, bl, 1.0f);
            t.draw();
        } else if (comp instanceof PolygonData d) {
            b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            drawRibbon(m, b, d.radius(), d.thickness(), d.sides(), r, g, bl, 1f);
            t.draw();
        } else if (comp instanceof StarData d) {
            b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            // I used 0.5f as a default inner scale for stars
            float inner = d.radius() * 0.5f;
            for (int i = 0; i <= d.points() * 2; i++) {
                float ang = (float)(i * Math.PI / d.points()), rad = (i % 2 == 0) ? d.radius() : inner;
                float cos = (float)Math.cos(ang), sin = (float)Math.sin(ang);
                b.vertex(m, cos * (rad - d.thickness()), 0, sin * (rad - d.thickness())).color(r, g, bl, 1f).next();
                b.vertex(m, cos * (rad + d.thickness()), 0, sin * (rad + d.thickness())).color(r, g, bl, 1f).next();
            }
            t.draw();
        } else if (comp instanceof RuneRingData d) {
            b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            float arc = (float)(2 * Math.PI / d.runeCount()), h = (float)(Math.PI/d.runeCount() * d.arcLength());
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
}