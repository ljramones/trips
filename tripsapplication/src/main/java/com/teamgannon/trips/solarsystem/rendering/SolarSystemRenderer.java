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
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
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

    private static final double DEEMPHASIZED_OPACITY = 0.6;
    private static final double ORBIT_DEEMPHASIZED_OPACITY = 1.0;
    private static final double LABEL_DEEMPHASIZED_OPACITY = 0.7;
    private static final double PLANET_SELECTED_SCALE = 1.25;
    private static final double STAR_SELECTED_SCALE = 1.15;

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
     * Group for ecliptic reference plane/grid
     */
    @Getter
    private final Group eclipticPlaneGroup;

    /**
     * Group for orbit node markers (ascending/descending)
     */
    @Getter
    private final Group orbitNodeGroup;

    /**
     * Group for apside markers (periapsis/apoapsis)
     */
    @Getter
    private final Group apsidesGroup;

    /**
     * Group for habitable zone ring
     */
    @Getter
    private final Group habitableZoneGroup;

    /**
     * Group for scale grid circles
     */
    @Getter
    private final Group scaleGridGroup;

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
    private final Map<String, Color> orbitColors;

    /**
     * Map of 3D nodes to their 2D labels (for billboard-style label updates)
     */
    @Getter
    private final Map<Node, Label> shapeToLabel;

    private final Map<Node, Double> baseScales;
    private final Map<Node, Double> baseOpacities;
    private final Map<String, Node> starNodes;
    private final Map<Node, double[]> orbitSegmentScales;
    private final Map<PhongMaterial, Color[]> orbitSegmentMaterials;
    private final Glow selectedBodyGlow = new Glow(0.6);
    private Node selectedBody;
    private Group selectedOrbit;
    private boolean showEclipticPlane = false;
    private boolean showOrbitNodes = false;
    private boolean showApsides = false;
    private boolean showOrbits = true;
    private boolean showHabitableZone = true;
    private boolean showScaleGrid = true;
    private ScaleMode scaleMode = ScaleMode.AUTO;

    public enum ScaleMode {
        AUTO,
        LINEAR,
        LOGARITHMIC
    }

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
        this.scaleGridGroup = new Group();
        this.habitableZoneGroup = new Group();
        this.orbitsGroup = new Group();
        this.planetsGroup = new Group();
        this.labelsGroup = new Group();
        this.eclipticPlaneGroup = new Group();
        this.orbitNodeGroup = new Group();
        this.apsidesGroup = new Group();
        this.planetNodes = new HashMap<>();
        this.planetDescriptions = new HashMap<>();
        this.orbitGroups = new HashMap<>();
        this.orbitColors = new HashMap<>();
        this.shapeToLabel = new HashMap<>();
        this.baseScales = new HashMap<>();
        this.baseOpacities = new HashMap<>();
        this.starNodes = new HashMap<>();
        this.orbitSegmentScales = new HashMap<>();
        this.orbitSegmentMaterials = new HashMap<>();

        // Order: scale grid (back), habitable zone, ecliptic, orbits, orbit nodes, apsides, planets, labels (front)
        systemGroup.getChildren().addAll(scaleGridGroup, habitableZoneGroup, eclipticPlaneGroup,
                orbitsGroup, orbitNodeGroup, apsidesGroup, planetsGroup, labelsGroup);
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

    public void selectPlanet(PlanetDescription planet) {
        if (planet == null) {
            clearSelection();
            return;
        }
        Node planetNode = planetNodes.get(planet.getName());
        Group orbitGroup = orbitGroups.get(planet.getName());
        applySelection(planetNode, orbitGroup);
    }

    public void selectStar(StarDisplayRecord star) {
        if (star == null) {
            clearSelection();
            return;
        }
        Node starNode = starNodes.get(star.getStarName());
        applySelection(starNode, null);
    }

    public void clearSelection() {
        applySelection(null, null);
    }

    public void setShowEclipticPlane(boolean show) {
        this.showEclipticPlane = show;
        if (show) {
            renderEclipticReference();
        } else {
            eclipticPlaneGroup.getChildren().clear();
            eclipticPlaneGroup.setVisible(false);
        }
    }

    public void setShowOrbitNodes(boolean show) {
        this.showOrbitNodes = show;
        if (show) {
            rebuildOrbitNodeMarkers();
        } else {
            orbitNodeGroup.getChildren().clear();
            orbitNodeGroup.setVisible(false);
        }
    }

    public void setShowApsides(boolean show) {
        this.showApsides = show;
        if (show) {
            rebuildApsideMarkers();
        } else {
            apsidesGroup.getChildren().clear();
            apsidesGroup.setVisible(false);
        }
    }

    public void setShowOrbits(boolean show) {
        this.showOrbits = show;
        orbitsGroup.setVisible(show);
    }

    public void setShowHabitableZone(boolean show) {
        this.showHabitableZone = show;
        habitableZoneGroup.setVisible(show);
    }

    public void setShowScaleGrid(boolean show) {
        this.showScaleGrid = show;
        scaleGridGroup.setVisible(show);
    }

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode == null ? ScaleMode.AUTO : scaleMode;
    }

    public void setZoomLevel(double zoomLevel) {
        scaleManager.setZoomLevel(zoomLevel);
    }

    public void setUseRelativePlanetSizes(boolean useRelative) {
        scaleManager.setUseRelativeScale(useRelative);
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
        if (scaleMode == ScaleMode.AUTO) {
            if (minOrbitAU > 0 && maxOrbitAU / minOrbitAU > 20) {
                useLogScale = true;
                log.info("Auto-enabled log scale: orbit ratio = {}", maxOrbitAU / minOrbitAU);
            }
        } else if (scaleMode == ScaleMode.LOGARITHMIC) {
            useLogScale = true;
        }
        scaleManager.setUseLogScale(useLogScale);

        // Recalculate base scale for the system
        if (maxOrbitAU > 0) {
            scaleManager.setBaseScale(350.0 / maxOrbitAU);
        }

        log.info("Rendering solar system with max orbit {} AU, min orbit {} AU, scale factor {}, logScale={}, mode={}",
                maxOrbitAU, minOrbitAU, scaleManager.getBaseScale(), useLogScale, scaleMode);

        // Render scale grid first (behind everything)
        renderScaleGrid();

        renderEclipticReference();

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
        scaleGridGroup.getChildren().clear();
        habitableZoneGroup.getChildren().clear();
        eclipticPlaneGroup.getChildren().clear();
        orbitsGroup.getChildren().clear();
        orbitNodeGroup.getChildren().clear();
        apsidesGroup.getChildren().clear();
        planetsGroup.getChildren().clear();
        labelsGroup.getChildren().clear();
        planetNodes.clear();
        planetDescriptions.clear();
        orbitGroups.clear();
        orbitColors.clear();
        shapeToLabel.clear();
        baseScales.clear();
        baseOpacities.clear();
        starNodes.clear();
        orbitSegmentScales.clear();
        orbitSegmentMaterials.clear();
        selectedBody = null;
        selectedOrbit = null;
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
            if (e.getButton() == MouseButton.PRIMARY && contextMenuHandler != null) {
                contextMenuHandler.onStarSelected(starSphere, star);
                e.consume();
                return;
            }
            if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                contextMenuHandler.onStarContextMenu(starSphere, star, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        registerSelectableNode(starSphere);
        starNodes.put(star.getStarName(), starSphere);
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
        orbitColors.put(planet.getName(), orbitColor);

        // Add context menu handler to orbit
        addOrbitContextMenuHandler(orbitPath, planet);

        orbitsGroup.getChildren().add(orbitPath);
        registerOrbitSegments(orbitPath);
        registerSelectableNode(orbitPath);
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
            if (e.getButton() == MouseButton.PRIMARY && contextMenuHandler != null) {
                contextMenuHandler.onPlanetSelected(planetSphere, planet);
                e.consume();
                return;
            }
            if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                contextMenuHandler.onPlanetContextMenu(planetSphere, planet, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        registerSelectableNode(planetSphere);
        planetsGroup.getChildren().add(planetSphere);

        // Store references for animation
        planetNodes.put(planet.getName(), planetSphere);
        planetDescriptions.put(planet.getName(), planet);

        renderOrbitNodeMarkers(planet, orbitColor);
        renderApsideMarkers(planet, orbitColor);
    }

    /**
     * Add context menu handler to all segments in an orbit group.
     */
    private void addOrbitContextMenuHandler(Group orbitGroup, PlanetDescription planet) {
        // Add handler to the group itself
        orbitGroup.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && contextMenuHandler != null) {
                contextMenuHandler.onOrbitSelected(orbitGroup, planet);
                e.consume();
                return;
            }
            if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                contextMenuHandler.onOrbitContextMenu(orbitGroup, planet, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        // Also add to each child for better hit detection
        for (Node child : orbitGroup.getChildren()) {
            child.setUserData(planet);
            child.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.PRIMARY && contextMenuHandler != null) {
                    contextMenuHandler.onOrbitSelected(child, planet);
                    e.consume();
                    return;
                }
                if (e.getButton() == MouseButton.SECONDARY && contextMenuHandler != null) {
                    contextMenuHandler.onOrbitContextMenu(child, planet, e.getScreenX(), e.getScreenY());
                    e.consume();
                }
            });
        }
    }

    private void registerSelectableNode(Node node) {
        if (node == null) {
            return;
        }
        baseScales.put(node, node.getScaleX());
        baseOpacities.put(node, node.getOpacity());
    }

    private void applySelection(Node selectedBody, Group selectedOrbit) {
        this.selectedBody = selectedBody;
        this.selectedOrbit = selectedOrbit;
        boolean hasSelection = selectedBody != null || selectedOrbit != null;

        for (Map.Entry<String, Sphere> entry : planetNodes.entrySet()) {
            Node planet = entry.getValue();
            if (planet == null) {
                continue;
            }
            if (planet == selectedBody) {
                applySelectedBodyStyle(planet, PLANET_SELECTED_SCALE);
            } else if (hasSelection) {
                applyDeemphasizedStyle(planet, DEEMPHASIZED_OPACITY);
            } else {
                restoreBaseStyle(planet);
            }
        }

        for (Map.Entry<String, Node> entry : starNodes.entrySet()) {
            Node star = entry.getValue();
            if (star == null) {
                continue;
            }
            if (star == selectedBody) {
                applySelectedBodyStyle(star, STAR_SELECTED_SCALE);
            } else if (hasSelection) {
                applyDeemphasizedStyle(star, 0.6);
            } else {
                restoreBaseStyle(star);
            }
        }

        for (Map.Entry<String, Group> entry : orbitGroups.entrySet()) {
            Group orbit = entry.getValue();
            if (orbit == null) {
                continue;
            }
            if (orbit == selectedOrbit) {
                applySelectedOrbitStyle(orbit);
            } else {
                restoreOrbitStyle(orbit);
            }
        }

        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Label label = entry.getValue();
            if (label == null) {
                continue;
            }
            if (!hasSelection) {
                label.setOpacity(1.0);
            } else if (entry.getKey() == selectedBody) {
                label.setOpacity(1.0);
            } else {
                label.setOpacity(LABEL_DEEMPHASIZED_OPACITY);
            }
        }
    }

    private void applySelectedBodyStyle(Node node, double scaleMultiplier) {
        restoreBaseScale(node);
        node.setScaleX(baseScales.getOrDefault(node, 1.0) * scaleMultiplier);
        node.setScaleY(baseScales.getOrDefault(node, 1.0) * scaleMultiplier);
        node.setScaleZ(baseScales.getOrDefault(node, 1.0) * scaleMultiplier);
        node.setOpacity(1.0);
        node.setEffect(selectedBodyGlow);
    }

    private void applySelectedOrbitStyle(Group orbit) {
        restoreOrbitStyle(orbit);
        orbit.setOpacity(1.0);
        for (Node child : orbit.getChildren()) {
            double[] baseScale = orbitSegmentScales.get(child);
            if (baseScale != null) {
                child.setScaleX(baseScale[0] * 1.6);
                child.setScaleY(baseScale[1]);
                child.setScaleZ(baseScale[2] * 1.6);
            }
            brightenOrbitMaterial(child);
        }
    }

    private void restoreOrbitStyle(Group orbit) {
        restoreBaseStyle(orbit);
        orbit.setOpacity(ORBIT_DEEMPHASIZED_OPACITY);
        for (Node child : orbit.getChildren()) {
            double[] baseScale = orbitSegmentScales.get(child);
            if (baseScale != null) {
                child.setScaleX(baseScale[0]);
                child.setScaleY(baseScale[1]);
                child.setScaleZ(baseScale[2]);
            }
            restoreOrbitMaterial(child);
        }
    }

    private void applyDeemphasizedStyle(Node node, double opacity) {
        restoreBaseScale(node);
        node.setOpacity(opacity);
        node.setEffect(null);
    }

    private void restoreBaseStyle(Node node) {
        restoreBaseScale(node);
        node.setOpacity(baseOpacities.getOrDefault(node, 1.0));
        node.setEffect(null);
    }

    private void restoreBaseScale(Node node) {
        double baseScale = baseScales.getOrDefault(node, 1.0);
        node.setScaleX(baseScale);
        node.setScaleY(baseScale);
        node.setScaleZ(baseScale);
    }

    private void registerOrbitSegments(Group orbitGroup) {
        for (Node child : orbitGroup.getChildren()) {
            orbitSegmentScales.putIfAbsent(child, new double[]{
                    child.getScaleX(),
                    child.getScaleY(),
                    child.getScaleZ()
            });
            if (child instanceof Shape3D shape) {
                if (shape.getMaterial() instanceof PhongMaterial material) {
                    orbitSegmentMaterials.putIfAbsent(material, new Color[]{
                            material.getDiffuseColor(),
                            material.getSpecularColor()
                    });
                }
            }
        }
    }

    private void brightenOrbitMaterial(Node node) {
        if (!(node instanceof Shape3D shape)) {
            return;
        }
        if (!(shape.getMaterial() instanceof PhongMaterial material)) {
            return;
        }
        Color[] baseColors = orbitSegmentMaterials.get(material);
        if (baseColors == null) {
            return;
        }
        material.setDiffuseColor(baseColors[0].brighter().brighter());
        material.setSpecularColor(baseColors[1].brighter());
    }

    private void restoreOrbitMaterial(Node node) {
        if (!(node instanceof Shape3D shape)) {
            return;
        }
        if (!(shape.getMaterial() instanceof PhongMaterial material)) {
            return;
        }
        Color[] baseColors = orbitSegmentMaterials.get(material);
        if (baseColors == null) {
            return;
        }
        material.setDiffuseColor(baseColors[0]);
        material.setSpecularColor(baseColors[1]);
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

        habitableZoneGroup.getChildren().add(hzGroup);

        log.info("Rendered habitable zone: {} - {} AU", innerAU, outerAU);
    }

    private void renderEclipticReference() {
        eclipticPlaneGroup.getChildren().clear();
        if (!showEclipticPlane) {
            return;
        }

        PhongMaterial gridMaterial = new PhongMaterial();
        gridMaterial.setDiffuseColor(Color.rgb(60, 60, 60, 0.25));
        gridMaterial.setSpecularColor(Color.rgb(80, 80, 80, 0.2));

        double[] gridValues = scaleManager.getScaleGridAuValues();
        for (double au : gridValues) {
            Group circle = createCircle(scaleManager.auToScreen(au), gridMaterial);
            eclipticPlaneGroup.getChildren().add(circle);
        }

        double maxRadius = scaleManager.auToScreen(scaleManager.getMaxOrbitalDistanceAU());
        double gridStep = maxRadius / 6;
        int steps = 12;
        for (int i = -steps; i <= steps; i++) {
            double offset = i * gridStep;
            Cylinder lineX = createRingSegmentXZ(-maxRadius, offset, maxRadius, offset, 0.15, gridMaterial);
            Cylinder lineZ = createRingSegmentXZ(offset, -maxRadius, offset, maxRadius, 0.15, gridMaterial);
            eclipticPlaneGroup.getChildren().addAll(lineX, lineZ);
        }

        eclipticPlaneGroup.setVisible(showEclipticPlane);
    }

    private void renderOrbitNodeMarkers(PlanetDescription planet, Color orbitColor) {
        if (!showOrbitNodes) {
            return;
        }
        double semiMajorAxis = planet.getSemiMajorAxis();
        if (semiMajorAxis <= 0) {
            return;
        }

        double eccentricity = Math.max(0, Math.min(0.99, planet.getEccentricity()));
        double inclination = planet.getInclination();
        double argPeriapsis = planet.getArgumentOfPeriapsis();
        double longAscNode = planet.getLongitudeOfAscendingNode();

        double ascendingTrueAnomaly = -argPeriapsis;
        double descendingTrueAnomaly = 180 - argPeriapsis;

        double[] ascPos = orbitVisualizer.calculateOrbitalPosition(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                ascendingTrueAnomaly
        );
        double[] descPos = orbitVisualizer.calculateOrbitalPosition(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                descendingTrueAnomaly
        );

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(orbitColor.brighter());
        material.setSpecularColor(orbitColor.brighter());

        Sphere ascending = new Sphere(1.4);
        ascending.setMaterial(material);
        ascending.setTranslateX(ascPos[0]);
        ascending.setTranslateY(ascPos[1]);
        ascending.setTranslateZ(ascPos[2]);

        Sphere descending = new Sphere(1.2);
        descending.setMaterial(material);
        descending.setTranslateX(descPos[0]);
        descending.setTranslateY(descPos[1]);
        descending.setTranslateZ(descPos[2]);

        orbitNodeGroup.getChildren().addAll(ascending, descending);
        orbitNodeGroup.setVisible(showOrbitNodes);
    }

    private void rebuildOrbitNodeMarkers() {
        orbitNodeGroup.getChildren().clear();
        if (!showOrbitNodes) {
            return;
        }
        for (Map.Entry<String, PlanetDescription> entry : planetDescriptions.entrySet()) {
            String planetName = entry.getKey();
            PlanetDescription planet = entry.getValue();
            if (planet == null) {
                continue;
            }
            Color orbitColor = orbitColors.getOrDefault(planetName, ORBIT_COLORS[0]);
            renderOrbitNodeMarkers(planet, orbitColor);
        }
    }

    /**
     * Render periapsis and apoapsis markers for a planet's orbit.
     * Periapsis (closest to star) is at true anomaly = 0°
     * Apoapsis (farthest from star) is at true anomaly = 180°
     */
    private void renderApsideMarkers(PlanetDescription planet, Color orbitColor) {
        if (!showApsides) {
            return;
        }
        double semiMajorAxis = planet.getSemiMajorAxis();
        if (semiMajorAxis <= 0) {
            return;
        }

        double eccentricity = Math.max(0, Math.min(0.99, planet.getEccentricity()));

        // Skip nearly circular orbits where apsides are meaningless
        if (eccentricity < 0.01) {
            return;
        }

        double inclination = planet.getInclination();
        double argPeriapsis = planet.getArgumentOfPeriapsis();
        double longAscNode = planet.getLongitudeOfAscendingNode();

        // Periapsis at true anomaly = 0, Apoapsis at true anomaly = 180
        double[] periPos = orbitVisualizer.calculateOrbitalPosition(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                0.0  // Periapsis
        );
        double[] apoPos = orbitVisualizer.calculateOrbitalPosition(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                180.0  // Apoapsis
        );

        // Use distinct colors: warm for periapsis (close/hot), cool for apoapsis (far/cold)
        PhongMaterial periMaterial = new PhongMaterial();
        periMaterial.setDiffuseColor(Color.ORANGERED);
        periMaterial.setSpecularColor(Color.ORANGE);

        PhongMaterial apoMaterial = new PhongMaterial();
        apoMaterial.setDiffuseColor(Color.CORNFLOWERBLUE);
        apoMaterial.setSpecularColor(Color.LIGHTBLUE);

        // Use boxes instead of spheres to distinguish from orbit node markers
        // Size scales with orbit distance for visibility
        double markerSize = Math.max(4.0, Math.min(8.0, semiMajorAxis * 2));

        Box periMarker = new Box(markerSize, markerSize, markerSize);
        periMarker.setMaterial(periMaterial);
        periMarker.setTranslateX(periPos[0]);
        periMarker.setTranslateY(periPos[1]);
        periMarker.setTranslateZ(periPos[2]);
        // Rotate 45 degrees to look like a diamond
        periMarker.setRotate(45);

        Box apoMarker = new Box(markerSize, markerSize, markerSize);
        apoMarker.setMaterial(apoMaterial);
        apoMarker.setTranslateX(apoPos[0]);
        apoMarker.setTranslateY(apoPos[1]);
        apoMarker.setTranslateZ(apoPos[2]);
        apoMarker.setRotate(45);

        log.debug("Created apside markers for {} at peri=({},{},{}) apo=({},{},{}) size={}",
                planet.getName(), periPos[0], periPos[1], periPos[2],
                apoPos[0], apoPos[1], apoPos[2], markerSize);

        apsidesGroup.getChildren().addAll(periMarker, apoMarker);
        apsidesGroup.setVisible(showApsides);
    }

    private void rebuildApsideMarkers() {
        apsidesGroup.getChildren().clear();
        if (!showApsides) {
            return;
        }
        for (Map.Entry<String, PlanetDescription> entry : planetDescriptions.entrySet()) {
            String planetName = entry.getKey();
            PlanetDescription planet = entry.getValue();
            if (planet == null) {
                continue;
            }
            Color orbitColor = orbitColors.getOrDefault(planetName, ORBIT_COLORS[0]);
            renderApsideMarkers(planet, orbitColor);
        }
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
            scaleGridGroup.getChildren().add(circle);
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
