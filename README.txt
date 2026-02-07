SoupyVFX API documentation
SoupyVFX is a math-driven, atomic geometry engine for the Fabric Mod Loader (Minecraft 1.20.1).
It is designed to provide high-performance, procedural 3D primitives that are synchronized across
the server-client boundary without the use of textures or sprites.


Installation
To implement SoupyVFX in your development environment, add the JitPack repository and the following dependency configurations to your build.gradle file.

Gradle Configuration
Gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // API for development
    modImplementation "com.github.Itamar588:SoupyVFX:1.0.4"

    // Shadowing the library into the final JAR
    include "com.github.Itamar588:SoupyVFX:1.0.4"
}


Pillars (3D Prisms)
Pillars are volumetric regular polygons extruded along a central axis.

Spawning a Pillar:


// Parameters: ServerWorld, Vec3d, Yaw, Pitch, Length, Radius, Sides, Color
UUID pillarId = PillarAPI.spawnPillar(world, new Vec3d(0, 100, 0), 0f, 0f, 5f, 1.2f, 6, 0x00FF00);
Updating Traits:


// Updates length, radius, and color in real-time
PillarAPI.updatePillar(world, pillarId, 8f, 1.5f, 0xFF0000);
Removal:


PillarAPI.removePillar(world, pillarId);
Spheres (Icospheres)
Procedural 3D wireframe spheres generated via recursive icosahedron subdivision.

Spawning a Sphere:


// Parameters: ServerWorld, Vec3d, Radius, LineWidth, Subdivisions (0-2), Color
UUID sphereId = SphereAPI.spawnSphere(world, pos, 2.5f, 0.08f, 1, 0xFFFFFF);
Updating Traits:


PillarAPI.updateSphere(world, sphereId, 3.0f, 0x00FFFF);
Removal:


SphereAPI.removeSphere(world, sphereId);
Magic Circles (2D Runes)
Planar, component-based geometry. Circles are highly customizable through the addition of various geometric sub-components.

Spawning a Circle:


// Parameters: ServerWorld, Vec3d, Color, Pitch, Yaw, Roll, Scale, RotationSpeed, FadeSpeed
MagicCircle circle = MagicCircleAPI.drawMagicCircle(world, pos, 0xFFD700, 90f, 0f, 0f, 2f, 1f, 0.05f);

// Adding sub-components
circle.addComponent(new HeavyRingData(1.0f, 0.8f, 0.05f, 24));
circle.addComponent(new PolygonData(0.8f, 0.04f, 6, false));
circle.addComponent(new StarData(0.7f, 0.02f, 5));
Modification: Update base properties via the MagicCircle instance; state is synchronized to clients automatically.

Removal:


MagicCircleAPI.removeCircle(world, circle.getId());
Trails (Entity Ribbons)
Dynamic, camera-facing ribbons that track the movement history of any entity.

Assigning a Trail:


// Parameters: ServerWorld, EntityUUID, MaxPoints, LifetimeMS, Width, Color
TrailAPI.startTrail(world, targetEntity.getUuid(), 100, 1500, 0.5f, 0xFF00FF);
Updating Traits:


// Update width and color dynamically
TrailAPI.updateTrail(world, targetEntity.getUuid(), 0.8f, 0x00FF00);
Removing a Trail:


// The trail stops generating new points and decays naturally
TrailAPI.stopTrail(world, targetEntity.getUuid());



Technical Specifications
Server Authority: All API calls must originate from the server. The engine manages packet broadcasting and client-side interpolation.

Memory Management: The engine includes an automated cleanup cycle. Trails attached to entities that are killed or unloaded will decay naturally and unregister from memory upon the expiration of the final point.

Color Formatting: All colors are handled as standard hexadecimal integers (e.g., 0xFFFFFF).

Coordinate System: Standard Minecraft Vec3d coordinates and float-based rotation (Yaw/Pitch/Roll) are used for all primitives.
