package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuHandler;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders a solar system in 3D.
 * Creates all visual elements: stars, orbits, planets, habitable zone, scale grid.
 */
@Slf4j
public class SolarSystemRenderer {

    /**
     * Default orbit colors by index
     */
    private static final Color[] ORBIT_COLORS = {
            Color.rgb(100, 149, 237, 0.7),  // Cornflower blue
            Color.rgb(144, 238, 144, 0.7),  // Light green
            Color.rgb(255, 182, 193, 0.7),  // Light pink
            Color.rgb(255, 218, 185, 0.7),  // Peach
            Color.rgb(221, 160, 221, 0.7),  // Plum
            Color.rgb(176, 224, 230, 0.7),  // Powder blue
            Color.rgb(240, 230, 140, 0.7),  // Khaki
            Color.rgb(152, 251, 152, 0.7),  // Pale green
    };

    /**
     * Planet colors based on type/temperature
     */
    private static final Color HOT_PLANET_COLOR = Color.rgb(255, 100, 50);
    private static final Color TEMPERATE_PLANET_COLOR = Color.rgb(100, 180, 100);
    private static final Color COLD_PLANET_COLOR = Color.rgb(150, 200, 255);
    private static final Color GAS_GIANT_COLOR = Color.rgb(230, 180, 120);

    /**
     * Moon color - silver/gray to distinguish from planets
     */
    private static final Color MOON_COLOR = Color.rgb(192, 192, 200);

    @Getter
    private final ScaleManager scaleManager;

    @Getter
    private final OrbitVisualizer orbitVisualizer;

    /**
     * Group containing all rendered elements
     */
    @Getter
    private final Group systemGroup;

    /**
     * Group for orbit paths
     */
    @Getter
    private final Group orbitsGroup;

    /**
     * Group for planet spheres (updated during animation)
     */
    @Getter
    private final Group planetsGroup;

    /**
     * Group for labels
     */
    @Getter
    private final Group labelsGroup;

    /**
     * Map of planet name to its sphere node (for animation updates)
     */
    @Getter
    private final Map<String, Sphere> planetNodes;

    /**
     * Map of planet name to its PlanetDescription (for position calculations)
     */
    @Getter
    private final Map<String, PlanetDescription> planetDescriptions;

    /**
     * Map of planet name to its orbit Group (for context menu on orbits)
     */
    @Getter
    private final Map<String, Group> orbitGroups;

    /**
     * Map of 3D nodes to their 2D labels (for billboard-style label updates)
     */
    @Getter
    private final Map<Node, Label> shapeToLabel;

    /**
     * Handler for context menu events (optional)
     */
    @Getter
    @Setter
    private SolarSystemContextMenuHandler contextMenuHandler;

    /**
     * Reference to the current star being rendered (for context menu)
     */
    private StarDisplayRecord currentStar;

    /**
     * Label font for planet/star names
     */
    private Font labelFont = Font.font("Verdana", 12);

    /**
     * Label color for planet/star names
     */
    private Color labelColor = Color.WHEAT;

    public SolarSystemRenderer() {
        this.scaleManager = new ScaleManager();
        this.orbitVisualizer = new OrbitVisualizer(scaleManager);
        this.systemGroup = new Group();
        this.orbitsGroup = new Group();
        this.planetsGroup = new Group();
        this.labelsGroup = new Group();
        this.planetNodes = new HashMap<>();
        this.planetDescriptions = new HashMap<>();
        this.orbitGroups = new HashMap<>();
        this.shapeToLabel = new HashMap<>();

        systemGroup.getChildren().addAll(orbitsGroup, planetsGroup, labelsGroup);
    }

    /**
     * Set label appearance
     *
     * @param font  the font for labels
     * @param color the color for labels
     */
    public void setLabelStyle(Font font, Color color) {
        this.labelFont = font;
        this.labelColor = color;
    }

    /**
     * Create a label for a named object.
     * The label is NOT added to any group here - it should be added to a 2D overlay by the caller.
     *
     * @param name the name to display
     * @return the created label
     */
    public Label createLabel(String name) {
        Label label = new Label(name);
        label.setFont(labelFont);
        label.setTextFill(labelColor);
        return label;
    }

    /**
     * Register a 3D node with its corresponding 2D label.
     * This allows the pane to update label positions when the view rotates.
     *
     * @param node  the 3D node (sphere, etc.)
     * @param label the 2D label
     */
    public void registerLabel(Node node, Label label) {
        shapeToLabel.put(node, label);
    }

    /**
     * Render a complete solar system
     *
     * @param description the solar system description
     * @return Group containing all rendered elements
     */
    public Group render(SolarSystemDescription description) {
        clear();

        if (description == null) {
            log.warn("Cannot render null solar system description");
            return systemGroup;
        }

        // Determine scale based on outermost orbit
        double maxOrbitAU = calculateMaxOrbitalDistance(description);
        double minOrbitAU = calculateMinOrbitalDistance(description);
        scaleManager.setMaxOrbitalDistanceAU(Math.max(maxOrbitAU, 1.0));

        // Auto-enable log scale if orbit ratio is large (helps spread out tightly-packed inner planets)
        boolean useLogScale = false;
        if (minOrbitAU > 0 && maxOrbitAU / minOrbitAU > 20) {
            useLogScale = true;
            scaleManager.setUseLogScale(true);
            log.info("Auto-enabled log scale: orbit ratio = {}", maxOrbitAU / minOrbitAU);
        } else {
            scaleManager.setUseLogScale(false);
        }

        // Recalculate base scale for the system
        if (maxOrbitAU > 0) {
            scaleManager.setBaseScale(350.0 / maxOrbitAU);
        }

        log.info("Rendering solar system with max orbit {} AU, min orbit {} AU, scale factor {}, logScale={}",
                maxOrbitAU, minOrbitAU, scaleManager.getBaseScale(), useLogScale);

        // Render scale grid first (behind everything)
        renderScaleGrid();

        // Render habitable zone
        renderHabitableZone(description.getHabitableZoneInnerAU(),
                description.getHabitableZoneOuterAU());

        // Store current star reference for context menus
        this.currentStar = description.getStarDisplayRecord();

        // Render central star
        renderStar(description.getStarDisplayRecord(), true);

        // Render companion stars (for multi-star systems)
        for (StarDisplayRecord companion : description.getCompanionStars()) {
            renderStar(companion, false);
        }

        // Render planets - sort by semi-major axis for better angular distribution
        List<PlanetDescription> planets = new ArrayList<>(description.getPlanetDescriptionList());
        planets.sort((a, b) -> Double.compare(a.getSemiMajorAxis(), b.getSemiMajorAxis()));
        double maxPlanetRadius = findMaxPlanetRadius(planets);

        // Calculate angular offsets - spread planets evenly, with extra offset for close orbits
        double[] trueAnomalies = calculatePlanetAngles(planets);

        for (int i = 0; i < planets.size(); i++) {
            PlanetDescription planet = planets.get(i);
            Color orbitColor = ORBIT_COLORS[i % ORBIT_COLORS.length];
            renderPlanet(planet, orbitColor, maxPlanetRadius, trueAnomalies[i]);
        }

        log.info("Rendered {} planets", planets.size());

        return systemGroup;
    }

    /**
     * Clear all rendered elements
     */
    public void clear() {
        orbitsGroup.getChildren().clear();
        planetsGroup.getChildren().clear();
        labelsGroup.getChildren().clear();
        planetNodes.clear();
        planetDescriptions.clear();
        orbitGroups.clear();
        shapeToLabel.clear();
        currentStar = null;
    }

    /**
     * Render the central or companion star
     */
    private void renderStar(StarDisplayRecord star, boolean isPrimary) {
        if (star == null) return;

        double radius = scaleManager.getStarRadius();
        if (!isPrimary) {
            radius *= 0.7; // Companion stars slightly smaller visually
        }

        Sphere starSphere = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();

        Color starColor = star.getStarColor();
        if (starColor == null) {
            starColor = getStarColorFromSpectralClass(star.getSpectralClass());
        }

        material.setDiffuseColor(starColor);
        material.setSpecularColor(Color.WHITE);
        // Add some glow effect
        material.setSelfIlluminationMap(null);

        starSphere.setMaterial(material);

        if (isPrimary) {
            // Primary star at origin
            starSphere.setTranslateX(0);
            starSphere.setTranslateY(0);
            starSphere.setTranslateZ(0);
        } else {
            // Position companion star (simplified - just offset for now)
            // In reality, binary stars have their own orbital parameters
            starSphere.setTranslateX(scaleManager.auToScreen(0.5));
            starSphere.setTranslateY(0);
            starSphere.setTranslateZ(0);
        }

        // Add tooltip with star info
        String tooltipText = String.format("%s\nSpectral: %s\nDistance: %.2f ly",
                star.getStarName(),
                star.getSpectralClass(),
                star.getDistance());
        Tooltip.install(starSphere, new Tooltip(tooltipText));

        // Store star reference for context menu
        starSphere.setUserData(star);

        // Add context menu handler
        starSphere.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                contextMenuHandler.onStarContextMenu(starSphere, star, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        planetsGroup.getChildren().add(starSphere);
    }

    /**
     * Render a planet with its orbit
     */
    private void renderPlanet(PlanetDescription planet,
                               Color orbitColor,
                               double maxPlanetRadius,
                               double trueAnomaly) {

        double semiMajorAxis = planet.getSemiMajorAxis();
        if (semiMajorAxis <= 0) {
            log.warn("Planet {} has no semi-major axis, skipping", planet.getName());
            return;
        }

        double eccentricity = Math.max(0, Math.min(0.99, planet.getEccentricity()));
        double inclination = planet.getInclination();
        double argPeriapsis = planet.getArgumentOfPeriapsis();
        double longAscNode = planet.getLongitudeOfAscendingNode();

        // Create orbit path
        Group orbitPath = orbitVisualizer.createOrbitPath(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                orbitColor
        );

        // Store planet reference in orbit for context menu
        orbitPath.setUserData(planet);
        orbitGroups.put(planet.getName(), orbitPath);

        // Add context menu handler to orbit
        addOrbitContextMenuHandler(orbitPath, planet);

        orbitsGroup.getChildren().add(orbitPath);
        double[] position = orbitVisualizer.calculateOrbitalPosition(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                trueAnomaly
        );

        // Create planet sphere
        double planetRadius = scaleManager.calculatePlanetDisplayRadius(
                planet.getRadius() > 0 ? planet.getRadius() : 1.0,
                maxPlanetRadius > 0 ? maxPlanetRadius : 1.0
        );

        Sphere planetSphere = new Sphere(planetRadius);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(getPlanetColor(planet));
        material.setSpecularColor(Color.WHITE);
        planetSphere.setMaterial(material);

        planetSphere.setTranslateX(position[0]);
        planetSphere.setTranslateY(position[1]);
        planetSphere.setTranslateZ(position[2]);

        // Add tooltip
        String tooltipText = String.format("%s\nSemi-major axis: %.3f AU\nPeriod: %.1f days\nRadius: %.2f Earth",
                planet.getName(),
                planet.getSemiMajorAxis(),
                planet.getOrbitalPeriod(),
                planet.getRadius());
        Tooltip.install(planetSphere, new Tooltip(tooltipText));

        // Store planet reference for context menu
        planetSphere.setUserData(planet);

        // Add context menu handler to planet sphere
        planetSphere.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                contextMenuHandler.onPlanetContextMenu(planetSphere, planet, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        planetsGroup.getChildren().add(planetSphere);

        // Store references for animation
        planetNodes.put(planet.getName(), planetSphere);
        planetDescriptions.put(planet.getName(), planet);
    }

    /**
     * Add context menu handler to all segments in an orbit group.
     */
    private void addOrbitContextMenuHandler(Group orbitGroup, PlanetDescription planet) {
        // Add handler to the group itself
        orbitGroup.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                contextMenuHandler.onOrbitContextMenu(orbitGroup, planet, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        // Also add to each child for better hit detection
        for (Node child : orbitGroup.getChildren()) {
            child.setUserData(planet);
            child.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                    contextMenuHandler.onOrbitContextMenu(child, planet, e.getScreenX(), e.getScreenY());
                    e.consume();
                }
            });
        }
    }

    /**
     * Render the habitable zone as a translucent ring
     */
    private void renderHabitableZone(double innerAU, double outerAU) {
        if (innerAU <= 0 || outerAU <= 0 || outerAU <= innerAU) {
            return;
        }

        double innerRadius = scaleManager.auToScreen(innerAU);
        double outerRadius = scaleManager.auToScreen(outerAU);
        double width = outerRadius - innerRadius;
        double centerRadius = (innerRadius + outerRadius) / 2;

        // Create a torus for the habitable zone
        // Torus parameters: major radius (center of tube) and minor radius (tube thickness)
        int torusDivisions = 64;

        // Create ring using cylinders as a simple approximation
        Group hzGroup = new Group();
        PhongMaterial hzMaterial = new PhongMaterial();
        hzMaterial.setDiffuseColor(Color.rgb(0, 255, 0, 0.15));

        int segments = 72;
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            // Inner edge in XZ plane (Y is up)
            double x1Inner = innerRadius * Math.cos(angle1);
            double z1Inner = innerRadius * Math.sin(angle1);
            double x2Inner = innerRadius * Math.cos(angle2);
            double z2Inner = innerRadius * Math.sin(angle2);

            // Outer edge in XZ plane
            double x1Outer = outerRadius * Math.cos(angle1);
            double z1Outer = outerRadius * Math.sin(angle1);
            double x2Outer = outerRadius * Math.cos(angle2);
            double z2Outer = outerRadius * Math.sin(angle2);

            // Create thin cylinders for inner and outer edges
            Cylinder innerSeg = createRingSegmentXZ(x1Inner, z1Inner, x2Inner, z2Inner, 0.3, hzMaterial);
            Cylinder outerSeg = createRingSegmentXZ(x1Outer, z1Outer, x2Outer, z2Outer, 0.3, hzMaterial);

            hzGroup.getChildren().addAll(innerSeg, outerSeg);
        }

        // Add radial connectors every 30 degrees
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            double xInner = innerRadius * Math.cos(angle);
            double zInner = innerRadius * Math.sin(angle);
            double xOuter = outerRadius * Math.cos(angle);
            double zOuter = outerRadius * Math.sin(angle);

            Cylinder radial = createRingSegmentXZ(xInner, zInner, xOuter, zOuter, 0.2, hzMaterial);
            hzGroup.getChildren().add(radial);
        }

        orbitsGroup.getChildren().add(hzGroup);

        log.info("Rendered habitable zone: {} - {} AU", innerAU, outerAU);
    }

    /**
     * Render scale grid circles at standard AU intervals
     */
    private void renderScaleGrid() {
        double[] gridValues = scaleManager.getScaleGridAuValues();

        PhongMaterial gridMaterial = new PhongMaterial();
        gridMaterial.setDiffuseColor(Color.rgb(80, 80, 80, 0.5));

        for (double au : gridValues) {
            Group circle = createCircle(scaleManager.auToScreen(au), gridMaterial);
            orbitsGroup.getChildren().add(circle);
        }
    }

    /**
     * Create a circle in the XZ plane (Y is up)
     */
    private Group createCircle(double radius, PhongMaterial material) {
        Group circleGroup = new Group();
        int segments = 72;

        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double x1 = radius * Math.cos(angle1);
            double z1 = radius * Math.sin(angle1);
            double x2 = radius * Math.cos(angle2);
            double z2 = radius * Math.sin(angle2);

            Cylinder segment = createRingSegmentXZ(x1, z1, x2, z2, 0.2, material);
            circleGroup.getChildren().add(segment);
        }

        return circleGroup;
    }

    /**
     * Create a cylinder segment between two points in the XZ plane (Y is up)
     */
    private Cylinder createRingSegmentXZ(double x1, double z1, double x2, double z2,
                                          double radius, PhongMaterial material) {
        double midX = (x1 + x2) / 2;
        double midZ = (z1 + z2) / 2;

        double dx = x2 - x1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dz * dz);

        Cylinder cylinder = new Cylinder(radius, length);
        cylinder.setMaterial(material);

        cylinder.setTranslateX(midX);
        cylinder.setTranslateY(0);  // In XZ plane
        cylinder.setTranslateZ(midZ);

        // Rotate to align with segment direction in XZ plane
        // Default cylinder is along Y axis, we need to lay it flat and rotate
        double angle = Math.toDegrees(Math.atan2(dx, dz));

        // First lay the cylinder flat (rotate 90° around X to put it in XZ plane)
        cylinder.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        // Then rotate around Y to align with the segment direction
        cylinder.getTransforms().add(new Rotate(-angle, Rotate.Y_AXIS));

        return cylinder;
    }

    // ==================== Helper Methods ====================

    private double calculateMaxOrbitalDistance(SolarSystemDescription description) {
        double max = 0;
        for (PlanetDescription planet : description.getPlanetDescriptionList()) {
            if (planet.getSemiMajorAxis() > max) {
                max = planet.getSemiMajorAxis();
            }
        }
        // Also consider habitable zone outer edge
        if (description.getHabitableZoneOuterAU() > max) {
            max = description.getHabitableZoneOuterAU();
        }
        return max;
    }

    private double calculateMinOrbitalDistance(SolarSystemDescription description) {
        double min = Double.MAX_VALUE;
        for (PlanetDescription planet : description.getPlanetDescriptionList()) {
            double sma = planet.getSemiMajorAxis();
            if (sma > 0 && sma < min) {
                min = sma;
            }
        }
        return min == Double.MAX_VALUE ? 0 : min;
    }

    /**
     * Calculate angular positions for planets to minimize visual overlap.
     * Planets with similar orbital distances get opposite positions.
     *
     * @param planets list of planets sorted by semi-major axis
     * @return array of true anomaly angles in degrees
     */
    private double[] calculatePlanetAngles(List<PlanetDescription> planets) {
        int n = planets.size();
        if (n == 0) return new double[0];

        double[] angles = new double[n];

        // Base angle spread - evenly distribute around the orbit
        double baseSpread = 360.0 / Math.max(n, 1);

        for (int i = 0; i < n; i++) {
            double baseAngle = i * baseSpread;

            // Check if this planet's orbit is close to the previous one
            // If so, offset by 180° to put them on opposite sides
            if (i > 0) {
                double prevSma = planets.get(i - 1).getSemiMajorAxis();
                double currSma = planets.get(i).getSemiMajorAxis();

                // If orbits are within 15% of each other, offset by 180°
                if (prevSma > 0 && Math.abs(currSma - prevSma) / prevSma < 0.15) {
                    baseAngle = angles[i - 1] + 180;
                }
            }

            angles[i] = baseAngle % 360;
        }

        return angles;
    }

    private double findMaxPlanetRadius(List<PlanetDescription> planets) {
        double max = 0;
        for (PlanetDescription planet : planets) {
            if (planet.getRadius() > max) {
                max = planet.getRadius();
            }
        }
        return max > 0 ? max : 1.0;
    }

    private Color getStarColorFromSpectralClass(String spectralClass) {
        if (spectralClass == null || spectralClass.isEmpty()) {
            return Color.YELLOW;
        }
        char type = spectralClass.charAt(0);
        return switch (type) {
            case 'O' -> Color.rgb(155, 176, 255);
            case 'B' -> Color.rgb(170, 191, 255);
            case 'A' -> Color.rgb(202, 215, 255);
            case 'F' -> Color.rgb(248, 247, 255);
            case 'G' -> Color.rgb(255, 244, 234);
            case 'K' -> Color.rgb(255, 210, 161);
            case 'M' -> Color.rgb(255, 180, 120);
            default -> Color.YELLOW;
        };
    }

    private Color getPlanetColor(PlanetDescription planet) {
        // Use distinct color for moons
        if (planet.isMoon()) {
            return MOON_COLOR;
        }

        // Color based on equilibrium temperature if available
        double temp = planet.getEquilibriumTemperature();
        if (temp > 0) {
            if (temp > 500) return HOT_PLANET_COLOR;
            if (temp > 200) return TEMPERATE_PLANET_COLOR;
            return COLD_PLANET_COLOR;
        }

        // Fallback: color based on mass (gas giants are more massive)
        if (planet.getMass() > 50) { // > 50 Earth masses
            return GAS_GIANT_COLOR;
        }

        return TEMPERATE_PLANET_COLOR;
    }

}
