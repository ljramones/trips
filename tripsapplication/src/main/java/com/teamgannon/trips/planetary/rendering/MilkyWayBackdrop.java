package com.teamgannon.trips.planetary.rendering;

import com.teamgannon.trips.planetary.PlanetaryContext;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Renders a procedurally seeded Milky Way starfield band onto its own scene
 * graph {@link Group}, projected onto the inside of the sky dome.
 * <p>
 * Extracted from {@code PlanetarySkyRenderer} in Phase 4.7 of the
 * codebase-review remediation. The Milky Way subsystem is fully
 * self-contained: it owns its render group, its seeded {@link Random},
 * the brightness-tier materials palette, and the galactic-to-equatorial
 * coordinate transform (used nowhere else in the codebase).
 *
 * <h2>Lifecycle</h2>
 * Per-frame the host renderer calls {@link #clear()} then
 * {@link #render(PlanetaryContext, double[])} with the observer's planet
 * position. The seed is derived from the planet position so the same
 * planet renders the same Milky Way pattern across frames / restarts.
 *
 * <h2>Time-of-day</h2>
 * Particles render at full count at night and at reduced count + opacity
 * during dawn/dusk twilight; daytime is a no-op.
 */
@Slf4j
public class MilkyWayBackdrop {

    private final Group milkyWayGroup = new Group();
    private final double skyDomeRadius;
    private Random milkyWayRandom;

    public MilkyWayBackdrop(double skyDomeRadius) {
        this.skyDomeRadius = skyDomeRadius;
    }

    /** Scene-graph node to add to the sky parent group. */
    public Group getGroup() {
        return milkyWayGroup;
    }

    /** Remove all rendered particles. Called before each re-render. */
    public void clear() {
        milkyWayGroup.getChildren().clear();
    }

    /**
     * Procedurally render the Milky Way band as seen from {@code planetPos}.
     * No-op during daylight; reduced particle count + opacity during twilight.
     */
    public void render(PlanetaryContext context, double[] planetPos) {
        // Seed by planet position so a given observer sees a stable pattern.
        long seed = (long) (planetPos[0] * 1000 + planetPos[1] * 100 + planetPos[2] * 10);
        milkyWayRandom = new Random(seed);

        double localTime = context.getLocalTime();
        boolean isNight = localTime < 6.0 || localTime > 18.0;
        boolean isTwilight = (localTime >= 5.0 && localTime < 6.0) || (localTime > 18.0 && localTime <= 19.0);

        if (!isNight && !isTwilight) {
            log.debug("Milky Way not visible during daytime");
            return;
        }

        int particleCount = isNight ? 3000 : 1000;
        double baseOpacity = isNight ? 0.4 : 0.15;

        log.info("Rendering Milky Way with {} particles", particleCount);

        PhongMaterial[] materials = createMaterials(baseOpacity);

        for (int i = 0; i < particleCount; i++) {
            // Galactic longitude (0-360°) sampled uniformly around the plane.
            double galLon = milkyWayRandom.nextDouble() * 360.0;

            // Galactic latitude: Gaussian-distributed about the plane and clamped.
            double latitudeSpread = 12.0;
            double galLat = milkyWayRandom.nextGaussian() * latitudeSpread;
            galLat = Math.max(-30, Math.min(30, galLat));

            // Density bias toward the galactic centre (l ≈ 0°).
            double centerWeight = Math.cos(Math.toRadians(galLon)) * 0.5 + 0.5;
            if (milkyWayRandom.nextDouble() > centerWeight * 0.7 + 0.3) {
                if (milkyWayRandom.nextDouble() > 0.5) continue;
            }

            double[] equatorial = galacticToEquatorial(galLon, galLat);
            double ra = equatorial[0];
            double dec = equatorial[1];

            // Equatorial → horizontal via simplified rotation by hour-angle + a fixed observer latitude.
            double hourAngle = (localTime / 24.0) * 360.0;
            double azimuth = ra - hourAngle + context.getViewingAzimuth();
            double altitude = dec;

            double observerLat = 45.0;
            altitude = dec * Math.cos(Math.toRadians(observerLat)) +
                       (90 - Math.abs(dec)) * Math.sin(Math.toRadians(observerLat)) *
                       Math.cos(Math.toRadians(azimuth));
            altitude = Math.max(-90, Math.min(90, altitude));

            if (altitude < -2) continue;

            // Slightly inside the dome so the band reads as behind the stars.
            double[] skyPos = sphericalToCartesian(skyDomeRadius * 0.98, azimuth, altitude);

            double brightness = Math.exp(-Math.abs(galLat) / 8.0);
            brightness *= (0.5 + milkyWayRandom.nextDouble() * 0.5);
            brightness *= centerWeight;

            int matIndex = (int) (brightness * (materials.length - 1));
            matIndex = Math.max(0, Math.min(materials.length - 1, matIndex));

            double size = 0.3 + brightness * 0.4 + milkyWayRandom.nextDouble() * 0.2;
            Sphere particle = new Sphere(size);
            particle.setCullFace(CullFace.NONE);
            particle.setMaterial(materials[matIndex]);
            particle.setTranslateX(skyPos[0]);
            particle.setTranslateY(skyPos[1]);
            particle.setTranslateZ(skyPos[2]);

            milkyWayGroup.getChildren().add(particle);
        }

        log.info("Rendered {} Milky Way particles", milkyWayGroup.getChildren().size());
    }

    /** Pre-compute an 8-tier warm-tint material palette indexed by brightness. */
    private static PhongMaterial[] createMaterials(double baseOpacity) {
        int levels = 8;
        PhongMaterial[] materials = new PhongMaterial[levels];

        for (int i = 0; i < levels; i++) {
            double brightness = (double) i / (levels - 1);
            double opacity = baseOpacity * (0.3 + brightness * 0.7);

            int r = (int) (200 + brightness * 55);
            int g = (int) (190 + brightness * 50);
            int b = (int) (170 + brightness * 40);

            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(Color.rgb(r, g, b, opacity));
            materials[i] = mat;
        }

        return materials;
    }

    /**
     * Convert galactic (l, b) to equatorial (RA, Dec) in degrees. Simplified
     * single-rotation transform — good enough for a visual band, not for
     * astrometry. NGP at RA=192.85948°, Dec=+27.12825°.
     */
    static double[] galacticToEquatorial(double galLon, double galLat) {
        double l = Math.toRadians(galLon);
        double b = Math.toRadians(galLat);

        double ngpRa = Math.toRadians(192.85948);
        double ngpDec = Math.toRadians(27.12825);
        double lonNcp = Math.toRadians(122.932);

        double sinDec = Math.sin(b) * Math.sin(ngpDec) +
                       Math.cos(b) * Math.cos(ngpDec) * Math.sin(l - lonNcp);
        double dec = Math.asin(Math.max(-1, Math.min(1, sinDec)));

        double y = Math.cos(b) * Math.cos(l - lonNcp);
        double x = Math.sin(b) * Math.cos(ngpDec) -
                  Math.cos(b) * Math.sin(ngpDec) * Math.sin(l - lonNcp);
        double ra = ngpRa + Math.atan2(y, x);

        double raDeg = Math.toDegrees(ra);
        while (raDeg < 0) raDeg += 360;
        while (raDeg >= 360) raDeg -= 360;

        return new double[]{raDeg, Math.toDegrees(dec)};
    }

    /**
     * Spherical (radius, azimuth°, altitude°) → Cartesian (x, y, z).
     * Y is negated so positive altitude reads as "up" in the JavaFX
     * screen-Y-down convention used by the sky dome.
     */
    private static double[] sphericalToCartesian(double radius, double azimuthDeg, double altitudeDeg) {
        double azimuthRad = Math.toRadians(azimuthDeg);
        double altitudeRad = Math.toRadians(altitudeDeg);

        double cosAlt = Math.cos(altitudeRad);
        double x = radius * cosAlt * Math.sin(azimuthRad);
        double y = -radius * Math.sin(altitudeRad);
        double z = radius * cosAlt * Math.cos(azimuthRad);

        return new double[]{x, y, z};
    }
}
