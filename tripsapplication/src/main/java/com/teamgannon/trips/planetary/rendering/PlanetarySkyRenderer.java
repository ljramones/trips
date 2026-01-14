package com.teamgannon.trips.planetary.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders the sky dome with repositioned stars as seen from a planet's surface.
 * Calculates star positions relative to the planet and adjusts apparent magnitudes.
 */
@Slf4j
public class PlanetarySkyRenderer {

    /**
     * Sky dome radius in screen units
     */
    private static final double SKY_DOME_RADIUS = 500.0;

    /**
     * Conversion factor: 1 AU in light years
     */
    private static final double AU_TO_LY = 1.0 / 63241.0;

    /**
     * Maximum magnitude to display (dimmer stars filtered)
     */
    private double magnitudeLimit = 6.0;

    /**
     * Output groups
     */
    private final Group skyGroup = new Group();
    private final Group starsGroup = new Group();
    private final Group hostStarGroup = new Group();
    private final Group horizonGroup = new Group();
    private final Group siblingPlanetsGroup = new Group();

    /**
     * List of brightest stars from current view (for side pane display)
     */
    private final List<BrightStarEntry> brightestStars = new ArrayList<>();

    public PlanetarySkyRenderer() {
        skyGroup.getChildren().addAll(horizonGroup, starsGroup, hostStarGroup, siblingPlanetsGroup);
    }

    /**
     * Render the sky dome as seen from a planet's surface.
     *
     * @param context  the planetary viewing context
     * @param allStars list of all stars in the dataset
     * @return Group containing the rendered sky dome
     */
    public Group render(PlanetaryContext context, List<StarDisplayRecord> allStars) {
        clear();

        if (context == null || context.getPlanet() == null) {
            log.warn("Cannot render null planetary context");
            return skyGroup;
        }

        this.magnitudeLimit = context.getMagnitudeLimit();

        // Get planet's absolute position in light years
        double[] planetPos = context.getPlanetPositionLy();
        if (planetPos == null) {
            planetPos = calculatePlanetPosition(context);
            context.setPlanetPositionLy(planetPos);
        }

        // Render the horizon circle
        renderHorizon();

        // Render the host star as the "sun"
        renderHostStar(context, planetPos);

        // Render all visible stars
        renderStars(context, allStars, planetPos);

        // Render sibling planets if visible
        renderSiblingPlanets(context, planetPos);

        log.info("Rendered sky dome from {} with {} visible stars",
                context.getPlanetName(), brightestStars.size());

        return skyGroup;
    }

    /**
     * Calculate planet's absolute position in light years from Sol.
     */
    private double[] calculatePlanetPosition(PlanetaryContext context) {
        StarDisplayRecord hostStar = context.getHostStar();
        ExoPlanet planet = context.getPlanet();

        if (hostStar == null) {
            log.warn("Host star is null, using origin");
            return new double[]{0, 0, 0};
        }

        // Star position in light years
        double starX = hostStar.getX();
        double starY = hostStar.getY();
        double starZ = hostStar.getZ();

        // Planet's orbital position relative to star (simplified - use semi-major axis)
        // For a more accurate position, we'd need true anomaly from OrbitVisualizer
        double semiMajorAU = planet.getSemiMajorAxis() != null ? planet.getSemiMajorAxis() : 1.0;

        // Convert AU to light years (very small offset)
        double offsetLy = semiMajorAU * AU_TO_LY;

        // For now, place planet along the x-axis from star (simplified)
        return new double[]{
                starX + offsetLy,
                starY,
                starZ
        };
    }

    /**
     * Render the horizon circle.
     */
    private void renderHorizon() {
        // Create a ring at the equator of the sky dome
        int segments = 72;
        double radius = SKY_DOME_RADIUS;

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.DARKGRAY.deriveColor(0, 1, 1, 0.5));

        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double x1 = radius * Math.cos(angle1);
            double z1 = radius * Math.sin(angle1);
            double x2 = radius * Math.cos(angle2);
            double z2 = radius * Math.sin(angle2);

            // Create a thin cylinder between the two points
            Cylinder segment = createSegment(
                    new double[]{x1, 0, z1},
                    new double[]{x2, 0, z2},
                    0.5, material);
            horizonGroup.getChildren().add(segment);
        }
    }

    /**
     * Render the host star as the "sun" in the sky.
     */
    private void renderHostStar(PlanetaryContext context, double[] planetPos) {
        StarDisplayRecord hostStar = context.getHostStar();
        if (hostStar == null) return;

        // Calculate direction from planet to host star
        double[] starPos = {hostStar.getX(), hostStar.getY(), hostStar.getZ()};
        double[] direction = calculateDirection(planetPos, starPos);

        // Place sun on sky dome (opposite side from its actual direction for daytime view)
        // During "noon", sun is at zenith if time is 12:00
        double localTime = context.getLocalTime();
        double timeAngle = (localTime - 12.0) / 24.0 * 2 * Math.PI;

        // Position on sky dome based on time of day
        double altitude = Math.cos(timeAngle) * 80; // Max 80 degrees altitude
        double azimuth = context.getViewingAzimuth();

        // Convert to 3D position on dome
        double[] sunPos = sphericalToCartesian(SKY_DOME_RADIUS * 0.95, azimuth, altitude);

        // Create sun sphere (large and bright)
        Sphere sun = new Sphere(20);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.YELLOW);
        material.setSpecularColor(Color.WHITE);
        sun.setMaterial(material);

        sun.setTranslateX(sunPos[0]);
        sun.setTranslateY(sunPos[1]);
        sun.setTranslateZ(sunPos[2]);

        hostStarGroup.getChildren().add(sun);
    }

    /**
     * Render stars visible from the planet's surface.
     */
    private void renderStars(PlanetaryContext context, List<StarDisplayRecord> allStars, double[] planetPos) {
        if (allStars == null || allStars.isEmpty()) return;

        brightestStars.clear();

        for (StarDisplayRecord star : allStars) {
            // Skip the host star (rendered separately as sun)
            if (context.getHostStar() != null &&
                    star.getRecordId().equals(context.getHostStar().getRecordId())) {
                continue;
            }

            // Calculate new distance and apparent magnitude
            double[] starPos = {star.getX(), star.getY(), star.getZ()};
            double distFromPlanet = calculateDistance(planetPos, starPos);

            // Original distance from Sol (Earth)
            double distFromSol = star.getDistance();
            if (distFromSol <= 0) {
                distFromSol = calculateDistance(new double[]{0, 0, 0}, starPos);
            }

            // Calculate adjusted apparent magnitude
            double originalMag = star.getMagnitude();
            double adjustedMag = adjustMagnitude(originalMag, distFromSol, distFromPlanet);

            // Filter by magnitude limit
            if (adjustedMag > magnitudeLimit) continue;

            // Calculate direction from planet to star
            double[] direction = calculateDirection(planetPos, starPos);

            // Convert direction to azimuth/altitude
            double azimuth = Math.toDegrees(Math.atan2(direction[0], direction[2]));
            double altitude = Math.toDegrees(Math.asin(direction[1]));

            // Only show stars above horizon
            if (altitude < 0) continue;

            // Position on sky dome
            double[] skyPos = sphericalToCartesian(SKY_DOME_RADIUS, azimuth, altitude);

            // Create star sphere (size based on magnitude)
            double size = magnitudeToSize(adjustedMag);
            Sphere starSphere = new Sphere(size);

            PhongMaterial material = new PhongMaterial();
            Color starColor = getStarColor(star.getSpectralClass());
            material.setDiffuseColor(starColor);
            material.setSpecularColor(starColor.brighter());
            starSphere.setMaterial(material);

            starSphere.setTranslateX(skyPos[0]);
            starSphere.setTranslateY(skyPos[1]);
            starSphere.setTranslateZ(skyPos[2]);

            starsGroup.getChildren().add(starSphere);

            // Track brightest stars
            brightestStars.add(new BrightStarEntry(
                    star.getStarName(),
                    distFromPlanet,
                    adjustedMag,
                    azimuth,
                    altitude,
                    star
            ));
        }

        // Sort by magnitude (brightest first) and keep top 20
        brightestStars.sort(Comparator.comparingDouble(BrightStarEntry::getApparentMagnitude));
        if (brightestStars.size() > 20) {
            brightestStars.subList(20, brightestStars.size()).clear();
        }

        log.info("Rendered {} stars above horizon", starsGroup.getChildren().size());
    }

    /**
     * Render sibling planets visible in the sky.
     */
    private void renderSiblingPlanets(PlanetaryContext context, double[] planetPos) {
        SolarSystemDescription system = context.getSystem();
        if (system == null) return;

        // TODO: Implement sibling planet rendering
        // These would appear as bright points in the sky
    }

    /**
     * Calculate adjusted apparent magnitude based on new distance.
     * m_new = m_old + 5 * log10(d_new / d_old)
     */
    private double adjustMagnitude(double originalMag, double distFromSol, double distFromPlanet) {
        if (distFromSol <= 0 || distFromPlanet <= 0) return originalMag;
        return originalMag + 5.0 * Math.log10(distFromPlanet / distFromSol);
    }

    /**
     * Calculate direction vector from point A to point B (normalized).
     */
    private double[] calculateDirection(double[] from, double[] to) {
        double dx = to[0] - from[0];
        double dy = to[1] - from[1];
        double dz = to[2] - from[2];
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist == 0) return new double[]{0, 1, 0};

        return new double[]{dx / dist, dy / dist, dz / dist};
    }

    /**
     * Calculate distance between two points.
     */
    private double calculateDistance(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Convert spherical coordinates (radius, azimuth, altitude) to Cartesian (x, y, z).
     */
    private double[] sphericalToCartesian(double radius, double azimuthDeg, double altitudeDeg) {
        double azimuthRad = Math.toRadians(azimuthDeg);
        double altitudeRad = Math.toRadians(altitudeDeg);

        double cosAlt = Math.cos(altitudeRad);
        double x = radius * cosAlt * Math.sin(azimuthRad);
        double y = radius * Math.sin(altitudeRad);
        double z = radius * cosAlt * Math.cos(azimuthRad);

        return new double[]{x, y, z};
    }

    /**
     * Convert magnitude to display size.
     */
    private double magnitudeToSize(double magnitude) {
        // Brighter (lower magnitude) = larger
        // Magnitude -1 -> size 5, magnitude 6 -> size 0.5
        double size = 5.0 - (magnitude + 1) * 0.6;
        return Math.max(0.5, Math.min(5.0, size));
    }

    /**
     * Get star color based on spectral class.
     */
    private Color getStarColor(String spectralClass) {
        if (spectralClass == null || spectralClass.isEmpty()) {
            return Color.WHITE;
        }

        char type = spectralClass.charAt(0);
        return switch (type) {
            case 'O' -> Color.rgb(155, 176, 255);  // Blue
            case 'B' -> Color.rgb(170, 191, 255);  // Blue-white
            case 'A' -> Color.rgb(202, 215, 255);  // White
            case 'F' -> Color.rgb(248, 247, 255);  // Yellow-white
            case 'G' -> Color.rgb(255, 244, 234);  // Yellow
            case 'K' -> Color.rgb(255, 210, 161);  // Orange
            case 'M' -> Color.rgb(255, 204, 111);  // Red-orange
            default -> Color.WHITE;
        };
    }

    /**
     * Create a cylinder segment between two points.
     */
    private Cylinder createSegment(double[] start, double[] end, double radius, PhongMaterial material) {
        double midX = (start[0] + end[0]) / 2;
        double midY = (start[1] + end[1]) / 2;
        double midZ = (start[2] + end[2]) / 2;

        double dx = end[0] - start[0];
        double dy = end[1] - start[1];
        double dz = end[2] - start[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        Cylinder cylinder = new Cylinder(radius, length);
        cylinder.setMaterial(material);

        cylinder.setTranslateX(midX);
        cylinder.setTranslateY(midY);
        cylinder.setTranslateZ(midZ);

        if (length > 0) {
            double[] direction = {dx / length, dy / length, dz / length};
            double[] yAxis = {0, 1, 0};

            double axisX = yAxis[1] * direction[2] - yAxis[2] * direction[1];
            double axisY = yAxis[2] * direction[0] - yAxis[0] * direction[2];
            double axisZ = yAxis[0] * direction[1] - yAxis[1] * direction[0];

            double axisLength = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            if (axisLength > 0.0001) {
                double dot = yAxis[0] * direction[0] + yAxis[1] * direction[1] + yAxis[2] * direction[2];
                double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));

                Rotate rotate = new Rotate(angle, axisX / axisLength, axisY / axisLength, axisZ / axisLength);
                cylinder.getTransforms().add(rotate);
            }
        }

        return cylinder;
    }

    /**
     * Clear all rendered elements.
     */
    public void clear() {
        starsGroup.getChildren().clear();
        hostStarGroup.getChildren().clear();
        horizonGroup.getChildren().clear();
        siblingPlanetsGroup.getChildren().clear();
        brightestStars.clear();
    }

    /**
     * Get the list of brightest stars (top 20).
     */
    public List<BrightStarEntry> getBrightestStars() {
        return new ArrayList<>(brightestStars);
    }

    /**
     * Set the magnitude limit for star visibility.
     */
    public void setMagnitudeLimit(double limit) {
        this.magnitudeLimit = limit;
    }

    /**
     * Entry representing a bright star for the side pane list.
     */
    public static class BrightStarEntry {
        private final String name;
        private final double distanceFromPlanet;
        private final double apparentMagnitude;
        private final double azimuth;
        private final double altitude;
        private final StarDisplayRecord starRecord;

        public BrightStarEntry(String name, double distanceFromPlanet, double apparentMagnitude,
                               double azimuth, double altitude, StarDisplayRecord starRecord) {
            this.name = name;
            this.distanceFromPlanet = distanceFromPlanet;
            this.apparentMagnitude = apparentMagnitude;
            this.azimuth = azimuth;
            this.altitude = altitude;
            this.starRecord = starRecord;
        }

        public String getName() { return name; }
        public double getDistanceFromPlanet() { return distanceFromPlanet; }
        public double getApparentMagnitude() { return apparentMagnitude; }
        public double getAzimuth() { return azimuth; }
        public double getAltitude() { return altitude; }
        public StarDisplayRecord getStarRecord() { return starRecord; }

        @Override
        public String toString() {
            return String.format("%s (mag %.1f, %.1f ly)", name, apparentMagnitude, distanceFromPlanet);
        }
    }
}
