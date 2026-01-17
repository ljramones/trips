package com.teamgannon.trips.planetary.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Maximum magnitude for star labels (stars brighter than this get labels)
     */
    private double labelMagnitudeLimit = 3.0;

    /**
     * Output groups
     */
    private final Group skyGroup = new Group();
    private final Group groundGroup = new Group();
    private final Group gridGroup = new Group();
    private final Group starsGroup = new Group();
    private final Group hostStarGroup = new Group();
    private final Group horizonGroup = new Group();
    private final Group siblingPlanetsGroup = new Group();

    /**
     * List of brightest stars from current view (for side pane display)
     */
    private final List<BrightStarEntry> brightestStars = new ArrayList<>();

    /**
     * Map from 3D star node to its 2D label (for billboard-style labels)
     */
    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * Map from 3D star node to its star data (for click-to-identify)
     */
    private final Map<Node, BrightStarEntry> shapeToStarData = new HashMap<>();

    /**
     * Callback for when a star is clicked
     */
    private java.util.function.Consumer<BrightStarEntry> onStarClicked;

    public PlanetarySkyRenderer() {
        skyGroup.getChildren().addAll(groundGroup, gridGroup, horizonGroup, starsGroup, hostStarGroup, siblingPlanetsGroup);
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
        gridGroup.setVisible(context.isShowOrientationGrid());

        // Get planet's absolute position in light years
        double[] planetPos = context.getPlanetPositionLy();
        if (planetPos == null) {
            planetPos = calculatePlanetPosition(context);
            context.setPlanetPositionLy(planetPos);
        }

        // Render ground mask to hide stars below horizon (but NOT the 3D horizon ring -
        // that's now rendered as a 2D overlay in PlanetarySpacePane to avoid z-fighting)
        if (context.isShowHorizon()) {
            renderGroundMask();
            // renderHorizon() disabled - horizon line now drawn as 2D overlay
        }

        if (context.isShowOrientationGrid()) {
            renderOrientationGrid();
        }

        // Render the host star as the "sun"
        renderHostStar(context, planetPos);

        // Render all visible stars
        renderStars(context, allStars, planetPos);

        // Render sibling planets if visible
        renderSiblingPlanets(context, planetPos);

        log.info("Rendered sky dome from {} with {} visible stars",
                context.getPlanetName(), brightestStars.size());

        // DEBUG: Add visible test sphere at origin
        Sphere testSphere = new Sphere(50);
        testSphere.setCullFace(CullFace.NONE);
        PhongMaterial testMat = new PhongMaterial(Color.MAGENTA);
        testSphere.setMaterial(testMat);
        starsGroup.getChildren().add(testSphere);
        log.info("DEBUG: Added test sphere, starsGroup has {} children", starsGroup.getChildren().size());

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
        material.setDiffuseColor(Color.DARKGRAY.deriveColor(0, 1, 1, 0.25));

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
     * Render altitude rings and azimuth spokes for orientation.
     */
    private void renderOrientationGrid() {
        // Orientation grid now rendered as a 2D overlay in PlanetarySpacePane.
    }

    /**
     * Render a dark ground mask below the horizon.
     */
    private void renderGroundMask() {
        double radius = SKY_DOME_RADIUS * 1.1;
        Cylinder ground = new Cylinder(radius, 2.0);
        ground.setCullFace(CullFace.NONE);  // Render inside-facing surfaces
        ground.setTranslateY(-1.0);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.rgb(6, 8, 12, 0.9));
        material.setSpecularColor(Color.rgb(0, 0, 0));
        ground.setMaterial(material);
        groundGroup.getChildren().add(ground);
    }

    /**
     * Render the host star as the "sun" in the sky.
     */
    private void renderHostStar(PlanetaryContext context, double[] planetPos) {
        StarDisplayRecord hostStar = context.getHostStar();
        if (hostStar == null || !context.isShowHostStar()) return;

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
        sun.setCullFace(CullFace.NONE);  // Render inside-facing surfaces (viewed from inside dome)
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
        log.info("DEBUG renderStars called with {} stars, magnitudeLimit={}",
                allStars != null ? allStars.size() : 0, magnitudeLimit);
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

            // Create star visual with magnitude-based rendering
            Color starColor = getStarColor(star.getSpectralClass());
            StarVisualResult starVisual = createStarVisual(adjustedMag, starColor, skyPos);

            starsGroup.getChildren().add(starVisual.group());
            Sphere starSphere = starVisual.coreSphere();
            Group starGroup = starVisual.group();

            // Create star entry for tracking and click identification
            BrightStarEntry starEntry = new BrightStarEntry(
                    star.getStarName(),
                    distFromPlanet,
                    adjustedMag,
                    azimuth,
                    altitude,
                    star
            );

            // Map the star group and core sphere to the star data for click-to-identify
            shapeToStarData.put(starGroup, starEntry);
            shapeToStarData.put(starSphere, starEntry);

            // Add click handler to the star group
            starGroup.setOnMouseClicked(event -> {
                if (onStarClicked != null) {
                    onStarClicked.accept(starEntry);
                }
                event.consume();
            });

            // Create label for bright stars (mag <= labelMagnitudeLimit)
            if (adjustedMag <= labelMagnitudeLimit && star.getStarName() != null && !star.getStarName().isEmpty()) {
                Label label = createStarLabel(star.getStarName(), adjustedMag);
                shapeToLabel.put(starSphere, label);
            }

            // Track brightest stars (for side pane list)
            brightestStars.add(starEntry);
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
     * Note: Y is negated so that positive altitude appears "up" on screen
     * (JavaFX 3D with our camera setup has Y increasing downward visually).
     */
    private double[] sphericalToCartesian(double radius, double azimuthDeg, double altitudeDeg) {
        double azimuthRad = Math.toRadians(azimuthDeg);
        double altitudeRad = Math.toRadians(altitudeDeg);

        double cosAlt = Math.cos(altitudeRad);
        double x = radius * cosAlt * Math.sin(azimuthRad);
        double y = -radius * Math.sin(altitudeRad);  // Negated for screen Y convention
        double z = radius * cosAlt * Math.cos(azimuthRad);

        return new double[]{x, y, z};
    }

    public double[] toSkyPoint(double radius, double azimuthDeg, double altitudeDeg) {
        return sphericalToCartesian(radius, azimuthDeg, altitudeDeg);
    }

    public double getSkyDomeRadius() {
        return SKY_DOME_RADIUS;
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
        groundGroup.getChildren().clear();
        gridGroup.getChildren().clear();
        starsGroup.getChildren().clear();
        hostStarGroup.getChildren().clear();
        horizonGroup.getChildren().clear();
        siblingPlanetsGroup.getChildren().clear();
        brightestStars.clear();
        shapeToLabel.clear();
        shapeToStarData.clear();
    }

    /**
     * Get the map of 3D star nodes to their 2D labels.
     * Used by PlanetarySpacePane for billboard-style label positioning.
     */
    public Map<Node, Label> getShapeToLabel() {
        return shapeToLabel;
    }

    /**
     * Set callback for when a star is clicked.
     */
    public void setOnStarClicked(java.util.function.Consumer<BrightStarEntry> callback) {
        this.onStarClicked = callback;
    }

    /**
     * Create a styled label for a star.
     */
    private Label createStarLabel(String name, double magnitude) {
        Label label = new Label(name);
        label.setTextFill(Color.WHITE);
        label.setVisible(true);

        // Style based on magnitude - brighter stars get larger, bolder labels
        if (magnitude <= 0.0) {
            label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        } else if (magnitude <= 1.5) {
            label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        } else {
            label.setStyle("-fx-font-size: 9px;");
        }

        return label;
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
     * Set the magnitude limit for star labels.
     * Stars brighter than this limit will have labels.
     */
    public void setLabelMagnitudeLimit(double limit) {
        this.labelMagnitudeLimit = limit;
    }

    /**
     * Get the current label magnitude limit.
     */
    public double getLabelMagnitudeLimit() {
        return labelMagnitudeLimit;
    }

    public void setOrientationGridVisible(boolean visible) {
        gridGroup.setVisible(visible);
    }

    /**
     * Result of creating a star visual - contains the group and core sphere reference.
     */
    private record StarVisualResult(Group group, Sphere coreSphere) {}

    /**
     * Create a magnitude-based star visual with tiered halos.
     * Brighter stars get larger cores and multiple glow halos.
     *
     * Magnitude tiers:
     *   mag ≤ -1.0: Exceptional (e.g., Sirius) - triple halo, large core
     *   mag ≤  0.5: Very bright - double halo
     *   mag ≤  1.5: Bright - single halo
     *   mag ≤  3.0: Visible - no halo, medium core
     *   mag >  3.0: Dim - small core only
     */
    private StarVisualResult createStarVisual(double magnitude, Color starColor, double[] position) {
        Group starGroup = new Group();
        double x = position[0];
        double y = position[1];
        double z = position[2];

        // Calculate core size based on magnitude (brighter = larger)
        double coreSize = calculateCoreSize(magnitude);

        // Create the core sphere
        Sphere core = new Sphere(coreSize);
        core.setCullFace(CullFace.NONE);
        PhongMaterial coreMaterial = new PhongMaterial();
        coreMaterial.setDiffuseColor(starColor);
        coreMaterial.setSpecularColor(Color.WHITE);
        core.setMaterial(coreMaterial);
        core.setTranslateX(x);
        core.setTranslateY(y);
        core.setTranslateZ(z);

        // Add halos based on magnitude tier
        if (magnitude <= -1.0) {
            // Exceptional brightness (Sirius-class): triple halo
            starGroup.getChildren().add(createHalo(x, y, z, coreSize * 6.0, starColor, 0.08));
            starGroup.getChildren().add(createHalo(x, y, z, coreSize * 4.0, starColor, 0.15));
            starGroup.getChildren().add(createHalo(x, y, z, coreSize * 2.5, starColor, 0.25));
        } else if (magnitude <= 0.5) {
            // Very bright: double halo
            starGroup.getChildren().add(createHalo(x, y, z, coreSize * 4.0, starColor, 0.12));
            starGroup.getChildren().add(createHalo(x, y, z, coreSize * 2.2, starColor, 0.22));
        } else if (magnitude <= 1.5) {
            // Bright: single halo
            starGroup.getChildren().add(createHalo(x, y, z, coreSize * 2.5, starColor, 0.18));
        } else if (magnitude <= 2.5) {
            // Moderately bright: subtle halo
            starGroup.getChildren().add(createHalo(x, y, z, coreSize * 1.8, starColor, 0.12));
        }
        // mag > 2.5: no halo, just the core

        starGroup.getChildren().add(core);

        return new StarVisualResult(starGroup, core);
    }

    /**
     * Create a glow halo sphere.
     */
    private Sphere createHalo(double x, double y, double z, double radius, Color tintColor, double opacity) {
        Sphere halo = new Sphere(radius);
        halo.setCullFace(CullFace.NONE);

        // Blend the star color with white for the halo (tinted glow)
        Color haloColor = tintColor.interpolate(Color.WHITE, 0.6);
        // Apply opacity to the color
        Color finalColor = Color.color(
                haloColor.getRed(),
                haloColor.getGreen(),
                haloColor.getBlue(),
                opacity
        );

        PhongMaterial haloMaterial = new PhongMaterial();
        haloMaterial.setDiffuseColor(finalColor);
        halo.setMaterial(haloMaterial);

        halo.setTranslateX(x);
        halo.setTranslateY(y);
        halo.setTranslateZ(z);

        return halo;
    }

    /**
     * Calculate core sphere size based on magnitude.
     * Uses a logarithmic scale for better visual distinction.
     */
    private double calculateCoreSize(double magnitude) {
        // Base size for mag 6 stars
        double baseSize = 0.5;

        // Each magnitude step is ~2.512x brightness difference
        // We use a gentler scaling for visual appeal
        double brightnessScale = Math.pow(2.0, (6.0 - magnitude) / 2.5);

        // Clamp the size to reasonable bounds
        double size = baseSize * Math.sqrt(brightnessScale);
        return Math.max(0.4, Math.min(3.5, size));
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
