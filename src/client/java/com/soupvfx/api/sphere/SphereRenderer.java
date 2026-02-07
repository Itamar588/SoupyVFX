package com.soupvfx.api.sphere;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class SphereRenderer {
    public static void register() {
        WorldRenderEvents.LAST.register(context -> render(context.matrixStack(), context.camera().getPos()));
    }

    private static void render(MatrixStack matrices, Vec3d cameraPos) {
        Collection<SphereData> spheres = SphereRegistry.getSpheres();
        if (spheres.isEmpty()) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableCull();

        for (SphereData s : spheres) {
            matrices.push();
            Vec3d relPos = s.pos().subtract(cameraPos);
            matrices.translate(relPos.x, relPos.y, relPos.z);
            Matrix4f m = matrices.peek().getPositionMatrix();

            int c = s.color();
            float r = (c >> 16 & 255) / 255f, g = (c >> 8 & 255) / 255f, b = (c & 255) / 255f;

            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            
            // Get procedural edges
            List<Vec3d[]> edges = getIcoEdges(s.subdivisions());
            
            for (Vec3d[] edge : edges) {
                Vec3d v1 = edge[0].multiply(s.radius());
                Vec3d v2 = edge[1].multiply(s.radius());

                // Calculate ribbon orientation (Always face camera)
                Vec3d lineDir = v2.subtract(v1).normalize();
                Vec3d toCam = v1.add(s.pos()).subtract(cameraPos).normalize();
                Vec3d side = lineDir.crossProduct(toCam).normalize().multiply(s.lineWidth() / 2f);

                // Draw the segment
                drawSegment(buffer, m, v1, v2, side, r, g, b, 1f);
            }
            
            tessellator.draw();
            matrices.pop();
        }
    }

    private static void drawSegment(BufferBuilder b, Matrix4f m, Vec3d v1, Vec3d v2, Vec3d side, float r, float g, float bl, float a) {
        b.vertex(m, (float)(v1.x + side.x), (float)(v1.y + side.y), (float)(v1.z + side.z)).color(r, g, bl, a).next();
        b.vertex(m, (float)(v1.x - side.x), (float)(v1.y - side.y), (float)(v1.z - side.z)).color(r, g, bl, a).next();
        b.vertex(m, (float)(v2.x - side.x), (float)(v2.y - side.y), (float)(v2.z - side.z)).color(r, g, bl, a).next();
        b.vertex(m, (float)(v2.x + side.x), (float)(v2.y + side.y), (float)(v2.z + side.z)).color(r, g, bl, a).next();
    }

    private static List<Vec3d[]> getIcoEdges(int subs) {
        // Base 12 vertices of Icosahedron
        float phi = (1.0f + (float)Math.sqrt(5.0f)) / 2.0f;
        List<Vec3d> verts = new ArrayList<>();
        // Generates the (0, ±1, ±phi) pattern...
        for(float i : new float[]{-1, 1}) for(float j : new float[]{-phi, phi}) {
            verts.add(new Vec3d(0, i, j).normalize());
            verts.add(new Vec3d(i, j, 0).normalize());
            verts.add(new Vec3d(j, 0, i).normalize());
        }

        List<Vec3d[]> edges = new ArrayList<>();
        // Simple distance-based edge detection for base icosahedron
        for (int i = 0; i < verts.size(); i++) {
            for (int j = i + 1; j < verts.size(); j++) {
                if (verts.get(i).distanceTo(verts.get(j)) < 1.1f) {
                    edges.add(new Vec3d[]{verts.get(i), verts.get(j)});
                }
            }
        }
        return edges; 
        // Note: For real subdivisions, you'd split triangles here. 
        // This base handles the D20 wireframe.
    }
}