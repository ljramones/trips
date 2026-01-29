package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.particlefields.RingConfiguration;
import com.teamgannon.trips.particlefields.RingFieldRenderer;
import com.teamgannon.trips.particlefields.RingType;
import com.teamgannon.trips.particlefields.SolarSystemRingAdapter;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.FeatureDescription;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProvider;
import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProviders;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    /**
     * Moon orbit color - consistent silver/gray for all moon orbits.
     * Using a unified color makes moon orbits clearly distinguishable from planet orbits.
     */
    private static final Color MOON_ORBIT_COLOR = Color.rgb(180, 180, 200, 0.8);

    @Getter
    private final ScaleManager scaleManager;

    @Getter
    private final OrbitVisualizer orbitVisualizer;
    private final OrbitSamplingProvider orbitSamplingProvider;

    // Extracted helper classes
    private final SelectionStyleManager selectionStyleManager;
    private final GridAndZoneRenderer gridAndZoneRenderer;
    private final OrbitMarkerRenderer orbitMarkerRenderer;

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
     * Group for planetary ring systems (e.g., Saturn's rings)
     */
    @Getter
    private final Group ringsGroup;

    /**
     * Group for system-level features (asteroid belts, stations, gates, etc.)
     */
    @Getter
    private final Group featuresGroup;

    /**
     * Map of feature ID to its RingFieldRenderer (for belt-type features)
     */
    private final Map<String, RingFieldRenderer> featureRenderers;

    /**
     * Map of feature ID to its 3D node (for point-type features)
     */
    private final Map<String, Node> featureNodes;

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
     * Map of parent planet name to list of moon orbit Groups.
     * Used for showing/hiding moon orbits based on zoom level.
     */
    private final Map<String, List<Group>> moonOrbitsByParent;

    /**
     * Amplification factor for moon orbits to make them visible.
     * Moon orbits are typically tiny (0.001-0.01 AU) compared to planet orbits.
     * 15x makes Callisto's orbit (~0.0126 AU) appear at ~0.19 AU - visible but
     * not overlapping with neighboring planet orbits.
     */
    private static final double MOON_ORBIT_AMPLIFICATION = 15.0;

    /**
     * Map of 3D nodes to their 2D labels (for billboard-style label updates)
     */
    @Getter
    private final Map<Node, Label> shapeToLabel;

    private final Map<String, Node> starNodes;

    /**
     * Map of planet ID/name to its RingFieldRenderer (for animated ring systems)
     */
    private final Map<String, RingFieldRenderer> planetRings;

    /**
     * Adapter for converting AU-based ring configurations to screen units
     */
    private SolarSystemRingAdapter ringAdapter;

    /**
     * Random generator for reproducible ring particle placement
     */
    private final Random ringRandom = new Random(42);

    /**
     * Whether to show planetary rings
     */
    private boolean showRings = true;

    /**
     * Whether to show asteroid belts and Kuiper belt
     */
    private boolean showAsteroidBelts = true;

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
        this.orbitSamplingProvider = OrbitSamplingProviders.defaultKepler();
        this.orbitVisualizer = new OrbitVisualizer(scaleManager, orbitSamplingProvider);

        // Initialize helper classes
        this.selectionStyleManager = new SelectionStyleManager();
        this.gridAndZoneRenderer = new GridAndZoneRenderer(scaleManager);
        this.orbitMarkerRenderer = new OrbitMarkerRenderer(scaleManager, orbitSamplingProvider);

        this.systemGroup = new Group();
        this.scaleGridGroup = new Group();
        this.habitableZoneGroup = new Group();
        this.orbitsGroup = new Group();
        this.planetsGroup = new Group();
        this.labelsGroup = new Group();
        this.eclipticPlaneGroup = new Group();
        this.orbitNodeGroup = new Group();
        this.apsidesGroup = new Group();
        this.ringsGroup = new Group();
        this.featuresGroup = new Group();
        this.planetNodes = new HashMap<>();
        this.planetDescriptions = new HashMap<>();
        this.orbitGroups = new HashMap<>();
        this.moonOrbitsByParent = new HashMap<>();
        this.orbitColors = new HashMap<>();
        this.shapeToLabel = new HashMap<>();
        this.starNodes = new HashMap<>();
        this.planetRings = new HashMap<>();
        this.featureRenderers = new HashMap<>();
        this.featureNodes = new HashMap<>();

        // Create ring adapter using our scale manager
        this.ringAdapter = new SolarSystemRingAdapter(scaleManager);

        // Order: scale grid (back), habitable zone, ecliptic, features, orbits, orbit nodes, apsides, rings, planets, labels (front)
        // Features (asteroid belts) are rendered early so they appear behind planets
        // Rings are rendered between orbits and planets so they appear around planets
        systemGroup.getChildren().addAll(scaleGridGroup, habitableZoneGroup, eclipticPlaneGroup,
                featuresGroup, orbitsGroup, orbitNodeGroup, apsidesGroup, ringsGroup, planetsGroup, labelsGroup);
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
        selectionStyleManager.applySelection(planetNode, orbitGroup, planetNodes, starNodes, orbitGroups, shapeToLabel);
    }

    public void selectStar(StarDisplayRecord star) {
        if (star == null) {
            clearSelection();
            return;
        }
        Node starNode = starNodes.get(star.getStarName());
        selectionStyleManager.applySelection(starNode, null, planetNodes, starNodes, orbitGroups, shapeToLabel);
    }

    public void clearSelection() {
        selectionStyleManager.applySelection(null, null, planetNodes, starNodes, orbitGroups, shapeToLabel);
        selectionStyleManager.clearSelection();
    }

    public void setShowEclipticPlane(boolean show) {
        this.showEclipticPlane = show;
        if (show) {
            gridAndZoneRenderer.renderEclipticReference(eclipticPlaneGroup, show);
        } else {
            eclipticPlaneGroup.getChildren().clear();
            eclipticPlaneGroup.setVisible(false);
        }
    }

    public void setShowOrbitNodes(boolean show) {
        this.showOrbitNodes = show;
        if (show) {
            orbitMarkerRenderer.rebuildOrbitNodeMarkers(orbitNodeGroup, planetDescriptions, orbitColors, ORBIT_COLORS[0], show);
        } else {
            orbitNodeGroup.getChildren().clear();
            orbitNodeGroup.setVisible(false);
        }
    }

    public void setShowApsides(boolean show) {
        this.showApsides = show;
        if (show) {
            orbitMarkerRenderer.rebuildApsideMarkers(apsidesGroup, planetDescriptions, orbitColors, ORBIT_COLORS[0], show);
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

    /**
     * Set whether to show planetary ring systems.
     *
     * @param show true to show rings, false to hide
     */
    public void setShowRings(boolean show) {
        this.showRings = show;
        ringsGroup.setVisible(show);
    }

    /**
     * Check if rings are currently visible.
     *
     * @return true if rings are visible
     */
    public boolean isShowRings() {
        return showRings;
    }

    /**
     * Set whether to show asteroid belts and Kuiper belt.
     *
     * @param show true to show belts, false to hide
     */
    public void setShowAsteroidBelts(boolean show) {
        this.showAsteroidBelts = show;
        featuresGroup.setVisible(show);
    }

    /**
     * Check if asteroid belts are currently visible.
     *
     * @return true if belts are visible
     */
    public boolean isShowAsteroidBelts() {
        return showAsteroidBelts;
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

        // Determine scale based on outermost primary planet orbit
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
        gridAndZoneRenderer.renderScaleGrid(scaleGridGroup);

        gridAndZoneRenderer.renderEclipticReference(eclipticPlaneGroup, showEclipticPlane);

        // Render habitable zone
        gridAndZoneRenderer.renderHabitableZone(habitableZoneGroup,
                description.getHabitableZoneInnerAU(),
                description.getHabitableZoneOuterAU());

        // Store current star reference for context menus
        this.currentStar = description.getStarDisplayRecord();

        // Render central star
        renderStar(description.getStarDisplayRecord(), true);

        // Render companion stars (for multi-star systems)
        for (StarDisplayRecord companion : description.getCompanionStars()) {
            renderStar(companion, false);
        }

        // Render planets - sort primaries by semi-major axis for better angular distribution
        List<PlanetDescription> allBodies = new ArrayList<>(description.getPlanetDescriptionList());
        List<PlanetDescription> primaryPlanets = new ArrayList<>();
        List<PlanetDescription> moons = new ArrayList<>();
        Map<String, PlanetDescription> planetsById = new HashMap<>();
        for (PlanetDescription planet : allBodies) {
            if (planet == null) {
                continue;
            }
            planetsById.put(planet.getId(), planet);
            // Check both isMoon flag AND parentPlanetId to handle Boolean/boolean getter issues
            boolean isMoonBody = planet.isMoon() ||
                    (planet.getParentPlanetId() != null && !planet.getParentPlanetId().isBlank());
            if (isMoonBody) {
                moons.add(planet);
                log.debug("Categorized {} as MOON (isMoon={}, parentId={})",
                        planet.getName(), planet.isMoon(), planet.getParentPlanetId());
            } else {
                primaryPlanets.add(planet);
            }
        }
        primaryPlanets.sort((a, b) -> Double.compare(a.getSemiMajorAxis(), b.getSemiMajorAxis()));
        double maxPlanetRadius = findMaxPlanetRadius(primaryPlanets);

        // Calculate minimum moon orbit distance for each planet (for size capping)
        Map<String, Double> minMoonOrbitByParent = new HashMap<>();
        for (PlanetDescription moon : moons) {
            if (moon.getParentPlanetId() != null && moon.getSemiMajorAxis() > 0) {
                double amplifiedOrbit = moon.getSemiMajorAxis() * MOON_ORBIT_AMPLIFICATION;
                minMoonOrbitByParent.merge(moon.getParentPlanetId(), amplifiedOrbit, Math::min);
            }
        }

        // Calculate angular offsets - spread planets evenly, with extra offset for close orbits
        double[] trueAnomalies = calculatePlanetAngles(primaryPlanets);
        Map<String, double[]> primaryPositionsAu = new HashMap<>();
        Map<String, Color> primaryOrbitColors = new HashMap<>();
        Map<String, Double> primaryDisplayRadii = new HashMap<>();  // Track display radii for moon sizing

        for (int i = 0; i < primaryPlanets.size(); i++) {
            PlanetDescription planet = primaryPlanets.get(i);
            Color orbitColor = ORBIT_COLORS[i % ORBIT_COLORS.length];
            primaryOrbitColors.put(planet.getId(), orbitColor);
            double[] positionAu = orbitSamplingProvider.calculatePositionAu(
                    planet.getSemiMajorAxis(),
                    Math.max(0, Math.min(0.99, planet.getEccentricity())),
                    planet.getInclination(),
                    planet.getLongitudeOfAscendingNode(),
                    planet.getArgumentOfPeriapsis(),
                    trueAnomalies[i]
            );
            primaryPositionsAu.put(planet.getId(), positionAu);
            Double minMoonOrbit = minMoonOrbitByParent.get(planet.getId());
            // Render planet and track its display radius for moon sizing
            double displayRadius = renderPlanet(planet, orbitColor, maxPlanetRadius, trueAnomalies[i],
                    null, minMoonOrbit, null, null);
            primaryDisplayRadii.put(planet.getId(), displayRadius);
        }

        java.util.Random random = new java.util.Random(42);
        for (PlanetDescription moon : moons) {
            PlanetDescription parent = moon.getParentPlanetId() != null
                    ? planetsById.get(moon.getParentPlanetId())
                    : null;
            if (parent == null) {
                log.warn("Skipping moon {}: parent not found", moon.getName());
                continue;
            }
            double[] parentPosAu = primaryPositionsAu.get(parent.getId());
            if (parentPosAu == null) {
                log.warn("Skipping moon {}: parent position not found", moon.getName());
                continue;
            }
            Color orbitColor = primaryOrbitColors.getOrDefault(parent.getId(), ORBIT_COLORS[0]);
            double trueAnomaly = random.nextDouble() * 360.0;
            // Get parent's physical radius and display radius for accurate moon sizing
            double parentPhysicalRadius = parent.getRadius() > 0 ? parent.getRadius() : 1.0;
            Double parentDisplayRadius = primaryDisplayRadii.get(parent.getId());
            renderPlanet(moon, orbitColor, maxPlanetRadius, trueAnomaly, parentPosAu, null,
                    parentPhysicalRadius, parentDisplayRadius);
        }

        log.info("Rendered {} planets and {} moons", primaryPlanets.size(), moons.size());

        // Log moon details for debugging
        if (!moons.isEmpty()) {
            log.info("Moon details:");
            for (PlanetDescription moon : moons) {
                PlanetDescription parent = planetsById.get(moon.getParentPlanetId());
                String parentName = parent != null ? parent.getName() : "unknown";
                log.info("  - {} orbiting {}: sma={} AU (amplified: {} AU), radius={} Earth radii",
                        moon.getName(),
                        parentName,
                        String.format("%.6f", moon.getSemiMajorAxis()),
                        String.format("%.6f", moon.getSemiMajorAxis() * MOON_ORBIT_AMPLIFICATION),
                        String.format("%.4f", moon.getRadius()));
            }
        } else {
            log.warn("No moons found in planet list. If expecting moons, check that moon data is loaded.");
        }

        // Render system features (asteroid belts, stations, gates, etc.)
        if (description.hasFeatures()) {
            renderFeatures(description.getFeatures());
        }

        return systemGroup;
    }

    /**
     * Update planet positions during animation.
     * Receives positions in AU and updates the 3D sphere positions.
     *
     * @param positionsAu map of planet name to position in AU {x, y, z}
     */
    public void updatePlanetPositions(Map<String, double[]> positionsAu) {
        int updated = 0;
        int notFound = 0;
        for (Map.Entry<String, double[]> entry : positionsAu.entrySet()) {
            String planetName = entry.getKey();
            double[] posAu = entry.getValue();

            Sphere sphere = planetNodes.get(planetName);
            if (sphere != null && posAu != null && posAu.length >= 3) {
                double[] screen = scaleManager.auVectorToScreen(posAu[0], posAu[1], posAu[2]);
                sphere.setTranslateX(screen[0]);
                sphere.setTranslateY(screen[1]);
                sphere.setTranslateZ(screen[2]);
                updated++;
            } else {
                notFound++;
                if (notFound == 1) {
                    log.warn("Planet sphere not found: '{}', available keys: {}", planetName, planetNodes.keySet());
                }
            }
        }
        if (updateLogCount++ % 60 == 0 && updated > 0) {
            log.trace("Updated {} planet positions", updated);
        }
    }

    private int updateLogCount = 0;

    /**
     * Update moon orbit visibility based on camera zoom level.
     * Moon orbits become visible when zoomed in close enough.
     *
     * @param cameraZ the camera's Z position (negative = closer)
     */
    public void updateMoonOrbitVisibility(double cameraZ) {
        // Show moon orbits when camera is closer than this threshold
        // At -1600 (default), don't show. At -400 or closer, show.
        double visibilityThreshold = -600.0;
        boolean shouldShow = cameraZ > visibilityThreshold;

        for (List<Group> moonOrbits : moonOrbitsByParent.values()) {
            for (Group orbit : moonOrbits) {
                orbit.setVisible(shouldShow && showOrbits);
            }
        }

        if (shouldShow) {
            log.debug("Moon orbits visible (camera Z: {})", cameraZ);
        }
    }

    /**
     * Show moon orbits for a specific parent planet.
     *
     * @param parentPlanetId the parent planet's ID
     * @param visible        whether to show or hide
     */
    public void setMoonOrbitsVisible(String parentPlanetId, boolean visible) {
        List<Group> moonOrbits = moonOrbitsByParent.get(parentPlanetId);
        if (moonOrbits != null) {
            for (Group orbit : moonOrbits) {
                orbit.setVisible(visible && showOrbits);
            }
        }
    }

    /**
     * Show all moon orbits.
     */
    public void showAllMoonOrbits() {
        for (List<Group> moonOrbits : moonOrbitsByParent.values()) {
            for (Group orbit : moonOrbits) {
                orbit.setVisible(showOrbits);
            }
        }
    }

    /**
     * Hide all moon orbits.
     */
    public void hideAllMoonOrbits() {
        for (List<Group> moonOrbits : moonOrbitsByParent.values()) {
            for (Group orbit : moonOrbits) {
                orbit.setVisible(false);
            }
        }
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

        // Dispose ring renderers to free resources
        clearRings();

        // Dispose feature renderers
        clearFeatures();

        planetNodes.clear();
        planetDescriptions.clear();
        orbitGroups.clear();
        orbitColors.clear();
        moonOrbitsByParent.clear();
        shapeToLabel.clear();
        starNodes.clear();
        selectionStyleManager.clear();
        currentStar = null;
    }

    /**
     * Clear all system features and dispose of their resources.
     */
    public void clearFeatures() {
        for (RingFieldRenderer renderer : featureRenderers.values()) {
            renderer.dispose();
        }
        featureRenderers.clear();
        featureNodes.clear();
        featuresGroup.getChildren().clear();
    }

    /**
     * Clear all planetary ring systems and dispose of their resources.
     */
    public void clearRings() {
        for (RingFieldRenderer renderer : planetRings.values()) {
            renderer.dispose();
        }
        planetRings.clear();
        ringsGroup.getChildren().clear();
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
        String tooltipText = "%s\nSpectral: %s\nDistance: %.2f ly".formatted(
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

        selectionStyleManager.registerSelectableNode(starSphere);
        starNodes.put(star.getStarName(), starSphere);
        planetsGroup.getChildren().add(starSphere);
    }

    /**
     * Render a planet with its orbit
     */
    /**
     * Render a planet with its orbit.
     *
     * @param planet              the planet to render
     * @param orbitColor          color for the orbit path
     * @param maxPlanetRadius     max planet radius in the system (for relative sizing)
     * @param trueAnomaly         initial position on orbit
     * @param parentOffsetAu      offset for moons (parent planet position), null for primary planets
     * @param minMoonOrbitAu      minimum moon orbit distance in AU (amplified), for capping planet size
     * @param parentPhysicalRadius parent planet's physical radius in Earth radii (for moon sizing)
     * @param parentDisplayRadius  parent planet's display radius in screen units (for moon sizing)
     * @return the display radius used for this planet (for tracking parent sizes)
     */
    private double renderPlanet(PlanetDescription planet,
                               Color orbitColor,
                               double maxPlanetRadius,
                               double trueAnomaly,
                               double[] parentOffsetAu,
                               Double minMoonOrbitAu,
                               Double parentPhysicalRadius,
                               Double parentDisplayRadius) {

        double semiMajorAxis = planet.getSemiMajorAxis();
        if (semiMajorAxis <= 0) {
            log.warn("Planet {} has no semi-major axis, skipping", planet.getName());
            return 0.0;
        }

        double eccentricity = Math.max(0, Math.min(0.99, planet.getEccentricity()));
        double inclination = planet.getInclination();
        double argPeriapsis = planet.getArgumentOfPeriapsis();
        double longAscNode = planet.getLongitudeOfAscendingNode();

        // Detect if this is a moon - check both the flag AND parentPlanetId
        // This is more robust than relying solely on the isMoon flag
        boolean isMoonBody = planet.isMoon() ||
                (planet.getParentPlanetId() != null && !planet.getParentPlanetId().isBlank());

        if (isMoonBody) {
            log.info("Rendering MOON: {} (isMoon={}, parentId={}, sma={} AU)",
                    planet.getName(), planet.isMoon(), planet.getParentPlanetId(), semiMajorAxis);
        }

        // For moons, amplify the orbit size so it's visible
        double orbitSmaForRendering = semiMajorAxis;
        if (isMoonBody) {
            orbitSmaForRendering = semiMajorAxis * MOON_ORBIT_AMPLIFICATION;
            log.info("  Amplified orbit SMA: {} AU -> {} AU", semiMajorAxis, orbitSmaForRendering);
        }

        // Create orbit path - use solid orbits with unified color for moons
        Group orbitPath;
        if (isMoonBody) {
            // Moon orbits: solid lines, consistent silver color, easier to see
            orbitPath = orbitVisualizer.createSolidOrbitPath(
                    orbitSmaForRendering,
                    eccentricity,
                    inclination,
                    longAscNode,
                    argPeriapsis,
                    MOON_ORBIT_COLOR
            );
        } else {
            // Planet orbits: dashed lines with varied colors
            orbitPath = orbitVisualizer.createOrbitPath(
                    orbitSmaForRendering,
                    eccentricity,
                    inclination,
                    longAscNode,
                    argPeriapsis,
                    orbitColor
            );
        }
        if (parentOffsetAu != null) {
            double[] offset = scaleManager.auVectorToScreen(
                    parentOffsetAu[0], parentOffsetAu[1], parentOffsetAu[2]);
            orbitPath.setTranslateX(offset[0]);
            orbitPath.setTranslateY(offset[1]);
            orbitPath.setTranslateZ(offset[2]);
        }

        // Store planet reference in orbit for context menu
        orbitPath.setUserData(planet);
        orbitGroups.put(planet.getName(), orbitPath);
        orbitColors.put(planet.getName(), orbitColor);

        // Track moon orbits by parent for dynamic visibility
        if (isMoonBody && planet.getParentPlanetId() != null) {
            moonOrbitsByParent
                    .computeIfAbsent(planet.getParentPlanetId(), k -> new ArrayList<>())
                    .add(orbitPath);
            // Moon orbits are now visible by default (solid silver circles)
            // They can be hidden via the showOrbits toggle if desired
            orbitPath.setVisible(showOrbits);
        }

        // Add context menu handler to orbit
        addOrbitContextMenuHandler(orbitPath, planet);

        orbitsGroup.getChildren().add(orbitPath);
        selectionStyleManager.registerOrbitSegments(orbitPath);
        selectionStyleManager.registerSelectableNode(orbitPath);

        // Calculate position - use amplified SMA for moons so they appear on their visible orbit
        double positionSma = isMoonBody ? orbitSmaForRendering : semiMajorAxis;
        if (isMoonBody) {
            log.info("  Moon {} position using SMA: {} AU (amplified={})",
                    planet.getName(), positionSma, positionSma == orbitSmaForRendering);
        }
        double[] localPosAu = orbitSamplingProvider.calculatePositionAu(
                positionSma,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                trueAnomaly
        );

        // Calculate screen position
        // IMPORTANT: For moons with log scaling, we must add offsets in SCREEN space, not AU space.
        // This matches how the orbit path is positioned (translated after creation).
        // With log scaling: screen(a + b) ≠ screen(a) + screen(b), so we must:
        // 1. Convert local position to screen space
        // 2. Convert parent position to screen space
        // 3. Add them in screen space
        double[] position;
        if (parentOffsetAu != null && isMoonBody) {
            // Moon positioning: add offset in screen space to match orbit positioning
            double[] localScreen = scaleManager.auVectorToScreen(localPosAu[0], localPosAu[1], localPosAu[2]);
            double[] parentScreen = scaleManager.auVectorToScreen(parentOffsetAu[0], parentOffsetAu[1], parentOffsetAu[2]);
            position = new double[] {
                    localScreen[0] + parentScreen[0],
                    localScreen[1] + parentScreen[1],
                    localScreen[2] + parentScreen[2]
            };
            log.info("  Moon {} screen position: local=[{},{},{}] + parent=[{},{},{}] = [{},{},{}]",
                    planet.getName(),
                    "%.1f".formatted(localScreen[0]), "%.1f".formatted(localScreen[1]), "%.1f".formatted(localScreen[2]),
                    "%.1f".formatted(parentScreen[0]), "%.1f".formatted(parentScreen[1]), "%.1f".formatted(parentScreen[2]),
                    "%.1f".formatted(position[0]), "%.1f".formatted(position[1]), "%.1f".formatted(position[2]));
        } else if (parentOffsetAu != null) {
            // Non-moon with parent offset (shouldn't happen, but handle gracefully)
            localPosAu[0] += parentOffsetAu[0];
            localPosAu[1] += parentOffsetAu[1];
            localPosAu[2] += parentOffsetAu[2];
            position = scaleManager.auVectorToScreen(localPosAu[0], localPosAu[1], localPosAu[2]);
        } else {
            // Primary planet - no parent offset
            position = scaleManager.auVectorToScreen(localPosAu[0], localPosAu[1], localPosAu[2]);
        }

        // Create planet sphere - use different sizing for moons vs primary planets
        double planetRadius;
        if (isMoonBody && parentPhysicalRadius != null && parentDisplayRadius != null) {
            // Moon sizing: use actual physical ratio relative to parent planet
            // This ensures moons are realistically tiny compared to their parent
            planetRadius = scaleManager.calculateMoonDisplayRadius(
                    planet.getRadius() > 0 ? planet.getRadius() : 0.1,
                    parentPhysicalRadius,
                    parentDisplayRadius
            );
            double moonPhysical = planet.getRadius() > 0 ? planet.getRadius() : 0.1;
            double ratio = moonPhysical / parentPhysicalRadius;
            log.info("Moon {} sizing: moonR={} Earth, parentR={} Earth, ratio={} (1/{}), parentDisplay={}, moonDisplay={}",
                    planet.getName(),
                    "%.3f".formatted(moonPhysical),
                    "%.2f".formatted(parentPhysicalRadius),
                    "%.4f".formatted(ratio),
                    "%.0f".formatted(1.0 / ratio),
                    "%.2f".formatted(parentDisplayRadius),
                    "%.3f".formatted(planetRadius));
        } else {
            // Primary planet sizing
            planetRadius = scaleManager.calculatePlanetDisplayRadius(
                    planet.getRadius() > 0 ? planet.getRadius() : 1.0,
                    maxPlanetRadius > 0 ? maxPlanetRadius : 1.0
            );

            // Cap planet size if it has moons - ensure planet doesn't overlap with closest moon orbit
            if (minMoonOrbitAu != null && minMoonOrbitAu > 0) {
                // Convert closest moon orbit (in AU) to screen units
                double minMoonOrbitScreen = scaleManager.auToScreen(minMoonOrbitAu);
                // Cap planet radius to 30% of closest moon orbit
                double maxAllowedRadius = minMoonOrbitScreen * 0.3;
                if (planetRadius > maxAllowedRadius) {
                    log.debug("Capping {} radius from {} to {} (moon orbit constraint)",
                            planet.getName(), planetRadius, maxAllowedRadius);
                    planetRadius = maxAllowedRadius;
                }
            }
        }

        Sphere planetSphere = new Sphere(planetRadius);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(getPlanetColor(planet));
        material.setSpecularColor(Color.WHITE);
        planetSphere.setMaterial(material);

        // Add subtle glow to moons so they remain visible when tiny
        // The glow intensity increases for smaller moons to compensate for their size
        if (isMoonBody && planetRadius < 1.0) {
            double glowIntensity = Math.min(0.8, 0.3 + (1.0 - planetRadius) * 0.5);
            planetSphere.setEffect(new Glow(glowIntensity));
        }

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

        selectionStyleManager.registerSelectableNode(planetSphere);
        planetsGroup.getChildren().add(planetSphere);

        // Store references for animation
        planetNodes.put(planet.getName(), planetSphere);
        planetDescriptions.put(planet.getName(), planet);

        orbitMarkerRenderer.renderOrbitNodeMarkers(orbitNodeGroup, planet, orbitColor, parentOffsetAu, showOrbitNodes);
        orbitMarkerRenderer.renderApsideMarkers(apsidesGroup, planet, orbitColor, parentOffsetAu, showApsides);

        // Render ring system if planet has one
        if (planet.isHasRings() && !isMoonBody) {
            renderPlanetRing(planet, position, planetRadius);
        }

        return planetRadius;
    }

    /**
     * Render a ring system for a planet.
     *
     * @param planet        the planet with ring data
     * @param position      the planet's screen position [x, y, z]
     * @param displayRadius the planet's display radius in screen units
     */
    private void renderPlanetRing(PlanetDescription planet, double[] position, double displayRadius) {
        String ringType = planet.getRingType();
        double innerAU = planet.getRingInnerRadiusAU();
        double outerAU = planet.getRingOuterRadiusAU();

        // Calculate ring radii as multiples of the planet's physical radius
        // Ring radii in AU are stored relative to planet center
        // Planet radius in AU = planet.getRadius() (Jupiter radii) * 4.778e-4 (AU per Jupiter radius)
        // Note: Jupiter radius = 71,492 km; 1 AU = 149,597,870.7 km
        double planetRadiusAU = planet.getRadius() * 4.778e-4;

        // Default ring ratios if not specified
        double innerRatio = 1.5;  // Default: ring starts at 1.5x planet radius
        double outerRatio = 2.5;  // Default: ring ends at 2.5x planet radius

        if (innerAU > 0 && outerAU > 0 && outerAU > innerAU && planetRadiusAU > 0) {
            // Calculate the actual ratios from the stored AU values
            innerRatio = innerAU / planetRadiusAU;
            outerRatio = outerAU / planetRadiusAU;
        }

        // Scale rings relative to the planet's DISPLAY radius, not physical size
        // This ensures rings are visible around the rendered planet sphere
        double innerScreen = displayRadius * innerRatio;
        double outerScreen = displayRadius * outerRatio;

        // Determine preset colors based on ring type
        Color primaryColor = Color.rgb(230, 220, 200);  // Default: icy tan
        Color secondaryColor = Color.rgb(180, 170, 160);
        int numElements = 8000;

        switch (ringType != null ? ringType.toUpperCase() : "SATURN") {
            case "URANUS" -> {
                primaryColor = Color.rgb(80, 80, 90);    // Dark gray
                secondaryColor = Color.rgb(50, 50, 60);
                numElements = 5000;
            }
            case "NEPTUNE" -> {
                primaryColor = Color.rgb(60, 60, 75);    // Very dark blue-gray
                secondaryColor = Color.rgb(40, 40, 60);
                numElements = 4000;
            }
            case "CUSTOM" -> {
                // Jupiter's faint ring
                primaryColor = Color.rgb(74, 74, 74);    // Dark gray
                secondaryColor = Color.rgb(58, 58, 58);
                numElements = 3000;
            }
        }

        // Create ring configuration in screen units (not AU)
        double ringWidth = outerScreen - innerScreen;
        double minSize = Math.max(0.3, ringWidth * 0.01);
        double maxSize = Math.max(0.8, ringWidth * 0.03);

        RingConfiguration config = RingConfiguration.builder()
                .type(RingType.PLANETARY_RING)
                .innerRadius(innerScreen)
                .outerRadius(outerScreen)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(ringWidth * 0.02)  // Very thin
                .maxInclinationDeg(0.5)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.004)
                .centralBodyRadius(displayRadius)
                .primaryColor(primaryColor)
                .secondaryColor(secondaryColor)
                .name(planet.getName() + " Ring")
                .build();

        log.info("Planet '{}' ring config: displayRadius={}, innerScreen={}, outerScreen={}, ratio={}x-{}x, elements={}",
                planet.getName(), String.format("%.1f", displayRadius), String.format("%.1f", innerScreen),
                String.format("%.1f", outerScreen), String.format("%.2f", innerRatio),
                String.format("%.2f", outerRatio), config.numElements());

        // Create and initialize the renderer
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(planet.getName().hashCode());
        renderer.initialize(config, ringRandom);

        // Debug: sample element positions
        var elements = renderer.getElements();
        if (!elements.isEmpty()) {
            var sample = elements.get(0);
            log.info("Planet '{}' ring sample: pos=({}, {}, {}), sma={}, size={}",
                    planet.getName(), sample.getX(), sample.getY(), sample.getZ(),
                    sample.getSemiMajorAxis(), sample.getSize());
        }
        log.info("Planet '{}' ring renderer group children: {}", planet.getName(), renderer.getGroup().getChildren().size());

        // Position at planet location
        renderer.setPosition(position[0], position[1], position[2]);

        // Apply ring inclination if specified
        if (planet.getRingInclination() != 0) {
            // The ring inclination would be applied via rotation transforms
            // For now, the default ring lies in the XZ plane
            log.debug("Ring inclination {} for {} (rotation not yet implemented)",
                    planet.getRingInclination(), planet.getName());
        }

        // Add to scene
        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put(planet.getName(), renderer);

        log.info("Rendered ring for planet '{}': {} - {} screen units, ratio {}x-{}x (type: {})",
                planet.getName(), String.format("%.1f", innerScreen), String.format("%.1f", outerScreen),
                String.format("%.2f", innerRatio), String.format("%.2f", outerRatio), ringType);
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

    // ==================== Helper Methods ====================

    private double calculateMaxOrbitalDistance(SolarSystemDescription description) {
        double max = 0;
        for (PlanetDescription planet : description.getPlanetDescriptionList()) {
            if (planet.isMoon()) {
                continue;
            }
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
            if (planet.isMoon()) {
                continue;
            }
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

    // ==================== Planetary Ring Methods ====================

    /**
     * Add a planetary ring system to a planet.
     * The ring will be positioned at the planet's current location.
     *
     * @param planetName   the name of the planet (must already be rendered)
     * @param innerRadiusAU inner ring radius in AU (from planet center)
     * @param outerRadiusAU outer ring radius in AU (from planet center)
     * @param ringName     display name for the ring
     * @return true if ring was successfully added
     */
    public boolean addPlanetaryRing(String planetName, double innerRadiusAU, double outerRadiusAU, String ringName) {
        Sphere planetSphere = planetNodes.get(planetName);
        if (planetSphere == null) {
            log.warn("Cannot add ring to planet '{}': planet not found", planetName);
            return false;
        }

        // Create ring configuration using the adapter
        RingConfiguration config = ringAdapter.createPlanetaryRing(innerRadiusAU, outerRadiusAU, ringName);

        // Create and initialize the renderer
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(planetName.hashCode());  // Reproducible but unique per planet
        renderer.initialize(config, ringRandom);

        // Position the ring at the planet's location
        renderer.setPosition(
                planetSphere.getTranslateX(),
                planetSphere.getTranslateY(),
                planetSphere.getTranslateZ()
        );

        // Add to the rings group
        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put(planetName, renderer);

        log.info("Added planetary ring '{}' to planet '{}': {} - {} AU",
                ringName, planetName, innerRadiusAU, outerRadiusAU);

        return true;
    }

    /**
     * Add a ring system using a preset configuration.
     *
     * @param planetName   the name of the planet
     * @param presetName   name of the ring preset (e.g., "Saturn Ring", "Uranus Ring")
     * @param innerRadiusAU inner radius in AU
     * @param outerRadiusAU outer radius in AU
     * @return true if ring was successfully added
     */
    public boolean addRingFromPreset(String planetName, String presetName, double innerRadiusAU, double outerRadiusAU) {
        Sphere planetSphere = planetNodes.get(planetName);
        if (planetSphere == null) {
            log.warn("Cannot add ring to planet '{}': planet not found", planetName);
            return false;
        }

        // Create ring configuration from preset, adapted to our scale
        RingConfiguration config = ringAdapter.createAdaptedConfiguration(presetName, innerRadiusAU, outerRadiusAU);

        // Create and initialize the renderer
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(planetName.hashCode());
        renderer.initialize(config, ringRandom);

        // Position at planet
        renderer.setPosition(
                planetSphere.getTranslateX(),
                planetSphere.getTranslateY(),
                planetSphere.getTranslateZ()
        );

        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put(planetName, renderer);

        log.info("Added '{}' ring preset to planet '{}'", presetName, planetName);

        return true;
    }

    /**
     * Add an asteroid belt around the central star.
     *
     * @param innerRadiusAU inner belt radius in AU
     * @param outerRadiusAU outer belt radius in AU
     * @param name          display name for the belt
     * @return true if belt was successfully added
     */
    public boolean addAsteroidBelt(double innerRadiusAU, double outerRadiusAU, String name) {
        // Create asteroid belt configuration
        RingConfiguration config = ringAdapter.createAsteroidBelt(innerRadiusAU, outerRadiusAU, name);

        // Create and initialize the renderer
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(name.hashCode());
        renderer.initialize(config, ringRandom);

        // Position at origin (around the star)
        renderer.setPosition(0, 0, 0);

        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put("__belt__" + name, renderer);

        log.info("Added asteroid belt '{}': {} - {} AU", name, innerRadiusAU, outerRadiusAU);

        return true;
    }

    /**
     * Add a debris disk around the central star.
     *
     * @param innerRadiusAU inner disk radius in AU
     * @param outerRadiusAU outer disk radius in AU
     * @param name          display name for the disk
     * @return true if disk was successfully added
     */
    public boolean addDebrisDisk(double innerRadiusAU, double outerRadiusAU, String name) {
        RingConfiguration config = ringAdapter.createDebrisDisk(innerRadiusAU, outerRadiusAU, name);

        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(name.hashCode());
        renderer.initialize(config, ringRandom);

        renderer.setPosition(0, 0, 0);

        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put("__disk__" + name, renderer);

        log.info("Added debris disk '{}': {} - {} AU", name, innerRadiusAU, outerRadiusAU);

        return true;
    }

    /**
     * Remove a planetary ring from a planet.
     *
     * @param planetName the name of the planet
     * @return true if a ring was removed
     */
    public boolean removeRing(String planetName) {
        RingFieldRenderer renderer = planetRings.remove(planetName);
        if (renderer != null) {
            ringsGroup.getChildren().remove(renderer.getGroup());
            renderer.dispose();
            log.info("Removed ring from planet '{}'", planetName);
            return true;
        }
        return false;
    }

    /**
     * Check if a planet has a ring system.
     *
     * @param planetName the planet name
     * @return true if the planet has a ring
     */
    public boolean hasRing(String planetName) {
        return planetRings.containsKey(planetName);
    }

    /**
     * Get the ring renderer for a planet.
     *
     * @param planetName the planet name
     * @return the ring renderer, or null if no ring exists
     */
    public RingFieldRenderer getRingRenderer(String planetName) {
        return planetRings.get(planetName);
    }

    /**
     * Update all ring animations.
     * Call this from the animation loop to animate ring particles.
     *
     * @param timeScale time scale factor (1.0 = normal speed)
     */
    public void updateRings(double timeScale) {
        if (!showRings) {
            return;
        }

        for (RingFieldRenderer renderer : planetRings.values()) {
            renderer.update(timeScale);
        }
    }

    /**
     * Refresh ring meshes after updates.
     * Call this periodically (e.g., every 5 frames) to update visual appearance.
     */
    public void refreshRingMeshes() {
        if (!showRings) {
            return;
        }

        for (RingFieldRenderer renderer : planetRings.values()) {
            renderer.refreshMeshes();
        }
    }

    /**
     * Update ring positions to follow their parent planets.
     * Call this after updatePlanetPositions() if planets are being animated.
     */
    public void updateRingPositions() {
        for (Map.Entry<String, RingFieldRenderer> entry : planetRings.entrySet()) {
            String planetName = entry.getKey();

            // Skip belts/disks (they orbit the star at origin)
            if (planetName.startsWith("__")) {
                continue;
            }

            Sphere planetSphere = planetNodes.get(planetName);
            if (planetSphere != null) {
                RingFieldRenderer renderer = entry.getValue();
                renderer.setPosition(
                        planetSphere.getTranslateX(),
                        planetSphere.getTranslateY(),
                        planetSphere.getTranslateZ()
                );
            }
        }
    }

    /**
     * Automatically add rings to gas giant planets based on mass.
     * Planets with mass > 50 Earth masses are considered gas giants.
     * Ring sizes are scaled based on planet radius.
     *
     * @param massThreshold minimum mass in Earth masses to be considered a gas giant
     */
    public void addRingsToGasGiants(double massThreshold) {
        for (Map.Entry<String, PlanetDescription> entry : planetDescriptions.entrySet()) {
            String planetName = entry.getKey();
            PlanetDescription planet = entry.getValue();

            // Skip moons
            if (planet.isMoon()) {
                continue;
            }

            // Check if it's a gas giant
            if (planet.getMass() >= massThreshold) {
                // Skip if already has a ring
                if (hasRing(planetName)) {
                    continue;
                }

                // Calculate ring radii based on planet radius
                // Rings typically extend from ~1.2 to ~2.5 planet radii
                double planetRadiusAU = planet.getRadius() * 4.2635e-5;  // Earth radii to AU
                double innerRadiusAU = planetRadiusAU * 1.5;
                double outerRadiusAU = planetRadiusAU * 2.5;

                // Scale ring size based on planet mass (larger planets get larger rings)
                double massScale = Math.sqrt(planet.getMass() / 300.0);  // Normalized to Jupiter mass
                innerRadiusAU *= Math.max(0.5, massScale);
                outerRadiusAU *= Math.max(0.5, massScale);

                // Use Saturn-style ring
                addRingFromPreset(planetName, "Saturn Ring", innerRadiusAU, outerRadiusAU);

                log.info("Auto-added ring to gas giant '{}' (mass={} Earth masses)",
                        planetName, planet.getMass());
            }
        }
    }

    /**
     * Get the number of active ring systems.
     *
     * @return number of ring systems
     */
    public int getRingCount() {
        return planetRings.size();
    }

    // ==================== System Feature Rendering ====================

    /**
     * Render all system features (asteroid belts, stations, gates, etc.)
     *
     * @param features list of feature descriptions to render
     */
    private void renderFeatures(List<FeatureDescription> features) {
        int beltCount = 0;
        int pointCount = 0;

        for (FeatureDescription feature : features) {
            if (feature.isBeltType()) {
                renderBeltFeature(feature);
                beltCount++;
            } else if (feature.isPointType()) {
                renderPointFeature(feature);
                pointCount++;
            }
        }

        log.info("Rendered {} belt features and {} point features", beltCount, pointCount);
    }

    /**
     * Render a belt-type feature (asteroid belt, debris disk, etc.)
     */
    private void renderBeltFeature(FeatureDescription feature) {
        double innerAU = feature.getInnerRadiusAU();
        double outerAU = feature.getOuterRadiusAU();

        if (innerAU <= 0 || outerAU <= 0 || outerAU <= innerAU) {
            log.warn("Invalid belt dimensions for feature '{}': inner={}, outer={}",
                    feature.getName(), innerAU, outerAU);
            return;
        }

        // Determine the preset based on feature type
        String presetName = switch (feature.getFeatureType()) {
            case "ASTEROID_BELT" -> "Main Asteroid Belt";
            case "KUIPER_BELT" -> "Kuiper Belt";
            case "DEBRIS_DISK" -> "Protoplanetary Disk";
            case "OORT_CLOUD" -> "Emission Nebula";  // Use nebula for spherical distribution
            case "ZODIACAL_DUST" -> "Protoplanetary Disk";
            case "DYSON_SWARM" -> "Saturn Ring";  // Thin dense particles
            case "DEFENSE_PERIMETER" -> "Main Asteroid Belt";  // Sparse distribution
            case "SENSOR_NETWORK" -> "Main Asteroid Belt";
            default -> "Main Asteroid Belt";
        };

        // Create the configuration
        RingConfiguration config = ringAdapter.createAdaptedConfiguration(presetName, innerAU, outerAU);

        log.info("Belt '{}' config: innerScreen={}, outerScreen={}, elements={}, minSize={}, maxSize={}, thickness={}",
                feature.getName(), config.innerRadius(), config.outerRadius(), config.numElements(),
                config.minSize(), config.maxSize(), config.thickness());

        // Create and initialize the renderer
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(feature.getId().hashCode());
        renderer.initialize(config, ringRandom);

        // Debug: sample element positions
        var elements = renderer.getElements();
        if (!elements.isEmpty()) {
            var sample = elements.get(0);
            log.info("Belt '{}' sample element: pos=({}, {}, {}), sma={}, size={}",
                    feature.getName(), sample.getX(), sample.getY(), sample.getZ(),
                    sample.getSemiMajorAxis(), sample.getSize());
        }
        log.info("Belt '{}' renderer group children: {}", feature.getName(), renderer.getGroup().getChildren().size());

        // Position at origin (around the star)
        renderer.setPosition(0, 0, 0);

        // Add to the features group
        featuresGroup.getChildren().add(renderer.getGroup());
        featureRenderers.put(feature.getId(), renderer);

        log.info("Rendered belt feature '{}' ({}): {} - {} AU",
                feature.getName(), feature.getFeatureType(), innerAU, outerAU);
    }

    /**
     * Render a point-type feature (station, gate, etc.)
     */
    private void renderPointFeature(FeatureDescription feature) {
        double orbitalRadius = feature.getOrbitalRadiusAU();
        double orbitalAngle = feature.getOrbitalAngleDeg();
        double orbitalHeight = feature.getOrbitalHeightAU();

        if (orbitalRadius <= 0) {
            log.warn("Invalid orbital radius for feature '{}': {}", feature.getName(), orbitalRadius);
            return;
        }

        // Calculate position in AU
        double angleRad = Math.toRadians(orbitalAngle);
        double xAU = orbitalRadius * Math.cos(angleRad);
        double zAU = orbitalRadius * Math.sin(angleRad);
        double yAU = orbitalHeight;

        // Convert to screen coordinates
        double[] screen = scaleManager.auVectorToScreen(xAU, yAU, zAU);

        // Determine appearance based on feature type
        double size = getFeatureSize(feature);
        Color color = getFeatureColor(feature);

        // Create the visual representation
        Sphere featureSphere = new Sphere(size);
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        featureSphere.setMaterial(material);

        featureSphere.setTranslateX(screen[0]);
        featureSphere.setTranslateY(screen[1]);
        featureSphere.setTranslateZ(screen[2]);

        // Add glow for artificial structures
        if (feature.isArtificial()) {
            featureSphere.setEffect(new Glow(0.4));
        }

        // Add tooltip
        String tooltipText = buildFeatureTooltip(feature);
        Tooltip.install(featureSphere, new Tooltip(tooltipText));

        // Store reference
        featureSphere.setUserData(feature);
        featureNodes.put(feature.getId(), featureSphere);
        featuresGroup.getChildren().add(featureSphere);

        log.info("Rendered point feature '{}' ({}) at {} AU",
                feature.getName(), feature.getFeatureType(), orbitalRadius);
    }

    private double getFeatureSize(FeatureDescription feature) {
        // Base size for features (relative to scale)
        double baseSize = scaleManager.auToScreen(0.02);  // About 0.02 AU in size

        return switch (feature.getFeatureType()) {
            case "JUMP_GATE" -> baseSize * 2.0;       // Large and prominent
            case "ORBITAL_HABITAT" -> baseSize * 1.5;
            case "SHIPYARD" -> baseSize * 1.8;
            case "RESEARCH_STATION" -> baseSize * 1.0;
            case "MINING_OPERATION" -> baseSize * 1.2;
            case "TROJAN_CLUSTER" -> baseSize * 2.5;  // Represents cluster of objects
            default -> baseSize;
        };
    }

    private Color getFeatureColor(FeatureDescription feature) {
        // Try to use the feature's custom color if specified
        if (feature.getPrimaryColor() != null && !feature.getPrimaryColor().isEmpty()) {
            try {
                return Color.web(feature.getPrimaryColor());
            } catch (Exception e) {
                // Fall through to default
            }
        }

        // Default colors based on feature type
        return switch (feature.getFeatureType()) {
            case "JUMP_GATE" -> Color.CYAN;
            case "ORBITAL_HABITAT" -> Color.LIGHTGREEN;
            case "SHIPYARD" -> Color.ORANGE;
            case "RESEARCH_STATION" -> Color.LIGHTYELLOW;
            case "MINING_OPERATION" -> Color.BROWN;
            case "TROJAN_CLUSTER" -> Color.GRAY;
            case "DEFENSE_PERIMETER" -> Color.RED;
            default -> Color.WHITE;
        };
    }

    private String buildFeatureTooltip(FeatureDescription feature) {
        StringBuilder sb = new StringBuilder();
        sb.append(feature.getName());
        sb.append("\nType: ").append(feature.getFeatureType().replace("_", " "));

        if (feature.getControllingPolity() != null && !feature.getControllingPolity().isEmpty()) {
            sb.append("\nPolity: ").append(feature.getControllingPolity());
        }

        if (feature.getPopulation() > 0) {
            sb.append("\nPopulation: ").append(String.format("%,d", feature.getPopulation()));
        }

        if (feature.getPurpose() != null && !feature.getPurpose().isEmpty()) {
            sb.append("\nPurpose: ").append(feature.getPurpose());
        }

        if (feature.getStatus() != null && !feature.getStatus().isEmpty()) {
            sb.append("\nStatus: ").append(feature.getStatus());
        }

        if (feature.isNavigationHazard()) {
            sb.append("\n⚠ Navigation Hazard");
            if (feature.getHazardType() != null) {
                sb.append(" (").append(feature.getHazardType()).append(")");
            }
        }

        if (feature.getTransitDestinations() != null && !feature.getTransitDestinations().isEmpty()) {
            sb.append("\nDestinations: ").append(feature.getTransitDestinations());
        }

        return sb.toString();
    }

    /**
     * Update all feature animations.
     *
     * @param timeScale time scale factor
     */
    public void updateFeatures(double timeScale) {
        for (RingFieldRenderer renderer : featureRenderers.values()) {
            renderer.update(timeScale);
        }
    }

    /**
     * Refresh feature meshes.
     */
    public void refreshFeatureMeshes() {
        for (RingFieldRenderer renderer : featureRenderers.values()) {
            renderer.refreshMeshes();
        }
    }

    /**
     * Get the number of rendered features.
     */
    public int getFeatureCount() {
        return featureRenderers.size() + featureNodes.size();
    }

}
