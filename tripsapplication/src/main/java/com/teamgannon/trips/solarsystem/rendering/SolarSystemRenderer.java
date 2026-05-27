package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.particlefields.RingConfiguration;
import com.teamgannon.trips.particlefields.RingFieldRenderer;
import com.teamgannon.trips.particlefields.RingType;
import com.teamgannon.trips.particlefields.SolarSystemRingAdapter;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetary.modelling.FeatureDescription;
import com.teamgannon.trips.planetary.modelling.PlanetDescription;
import com.teamgannon.trips.planetary.modelling.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProvider;
import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProviders;
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
import java.util.Random;

/**
 * Renders a solar system in 3D.
 * Creates all visual elements: stars, orbits, planets, habitable zone, scale grid.
 */
@Slf4j
public class SolarSystemRenderer {

    // Color constants and palette logic moved to SolarSystemColors in Phase 4.1.2
    // (see solarsystem/rendering/SolarSystemColors.java).

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
     * Transfer-trajectory overlay (dashed Hohmann arc).
     * Phase 4.1.4 extracted the drawing logic; this renderer keeps a
     * reference so {@link #drawTransferTrajectory} and
     * {@link #clearTransferOverlay} stay as public delegate methods.
     */
    private final TransferTrajectoryOverlay transferOverlay;

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
     * Owns the ring + belt sub-system (planetary rings, asteroid belt, Kuiper
     * belt). See {@link PlanetaryRingManager}. Phase 4.1.5 extracted the
     * Groups, the per-planet ring renderers, the ring-adapter, the visibility
     * toggles, and the public add / remove / animate API into that class.
     */
    private final PlanetaryRingManager rings;

    /**
     * Renders individual stars and planets (the two largest methods that used
     * to live here, Phase 4.1.6). The renderer's {@code render()} orchestrates
     * the system; {@code bodyRenderer} produces each Sphere + orbit + markers.
     */
    private final BodyRenderer bodyRenderer;

    /**
     * Group for system-level features (stations, gates, other point features)
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

    // Moon-orbit amplification factor moved to BodyRenderer.MOON_ORBIT_AMPLIFICATION
    // in Phase 4.1.6 — referenced here for the per-system orbit-stats pre-pass.

    /**
     * Map of 3D nodes to their 2D labels (for billboard-style label updates)
     */
    @Getter
    private final Map<Node, Label> shapeToLabel;

    private final Map<String, Node> starNodes;

    // planetRings, ringAdapter, ringRandom, showRings, showAsteroidBelt,
    // showKuiperBelt all moved into PlanetaryRingManager in Phase 4.1.5.

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
     * Handler for context menu events (optional). Phase 4.1.6 added a custom
     * setter so updates also propagate to the {@link BodyRenderer} (the new
     * owner of star/planet/orbit click wiring).
     */
    @Getter
    private SolarSystemContextMenuHandler contextMenuHandler;

    public void setContextMenuHandler(SolarSystemContextMenuHandler handler) {
        this.contextMenuHandler = handler;
        if (bodyRenderer != null) {
            bodyRenderer.setContextMenuHandler(handler);
        }
    }

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
        this.transferOverlay = new TransferTrajectoryOverlay(scaleManager);

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
        this.featuresGroup = new Group();
        this.planetNodes = new HashMap<>();
        this.planetDescriptions = new HashMap<>();
        this.orbitGroups = new HashMap<>();
        this.moonOrbitsByParent = new HashMap<>();
        this.orbitColors = new HashMap<>();
        this.shapeToLabel = new HashMap<>();
        this.starNodes = new HashMap<>();
        this.featureRenderers = new HashMap<>();
        this.featureNodes = new HashMap<>();

        // Phase 4.1.5: ring + belt state lives in PlanetaryRingManager.
        // Constructed AFTER planetNodes / planetDescriptions because the manager
        // borrows those maps for planet-position lookups.
        this.rings = new PlanetaryRingManager(scaleManager, planetNodes, planetDescriptions);

        // Phase 4.1.6: renderStar / renderPlanet live in BodyRenderer.
        // Constructed AFTER the manager + maps + groups it borrows.
        this.bodyRenderer = new BodyRenderer(
                scaleManager, orbitVisualizer, orbitSamplingProvider,
                orbitMarkerRenderer, selectionStyleManager, rings,
                planetsGroup, orbitsGroup, orbitNodeGroup, apsidesGroup,
                planetNodes, starNodes, planetDescriptions,
                orbitGroups, orbitColors, moonOrbitsByParent);
        bodyRenderer.setShowOrbits(showOrbits);
        bodyRenderer.setShowOrbitNodes(showOrbitNodes);
        bodyRenderer.setShowApsides(showApsides);

        // Order: scale grid (back), habitable zone, ecliptic, features, orbits, orbit nodes, apsides, rings, planets, labels (front)
        // Belts are rendered early so they appear behind planets
        // Rings are rendered between orbits and planets so they appear around planets
        systemGroup.getChildren().addAll(scaleGridGroup, habitableZoneGroup, eclipticPlaneGroup,
                rings.getAsteroidBeltGroup(), rings.getKuiperBeltGroup(), featuresGroup,
                orbitsGroup, orbitNodeGroup, apsidesGroup, rings.getRingsGroup(), planetsGroup, labelsGroup);
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
        if (bodyRenderer != null) bodyRenderer.setShowOrbitNodes(show);
        if (show) {
            orbitMarkerRenderer.rebuildOrbitNodeMarkers(orbitNodeGroup, planetDescriptions, orbitColors, SolarSystemColors.ORBIT_COLORS[0], show);
        } else {
            orbitNodeGroup.getChildren().clear();
            orbitNodeGroup.setVisible(false);
        }
    }

    public void setShowApsides(boolean show) {
        this.showApsides = show;
        if (bodyRenderer != null) bodyRenderer.setShowApsides(show);
        if (show) {
            orbitMarkerRenderer.rebuildApsideMarkers(apsidesGroup, planetDescriptions, orbitColors, SolarSystemColors.ORBIT_COLORS[0], show);
        } else {
            apsidesGroup.getChildren().clear();
            apsidesGroup.setVisible(false);
        }
    }

    public void setShowOrbits(boolean show) {
        this.showOrbits = show;
        if (bodyRenderer != null) bodyRenderer.setShowOrbits(show);
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

    // Ring / belt visibility toggles — Phase 4.1.5 delegate to PlanetaryRingManager.
    public void setShowRings(boolean show) { rings.setShowRings(show); }
    public boolean isShowRings() { return rings.isShowRings(); }

    public void setShowAsteroidBelt(boolean show) { rings.setShowAsteroidBelt(show); }
    public boolean isShowAsteroidBelt() { return rings.isShowAsteroidBelt(); }

    public void setShowKuiperBelt(boolean show) { rings.setShowKuiperBelt(show); }
    public boolean isShowKuiperBelt() { return rings.isShowKuiperBelt(); }

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
        double maxOrbitAU = SystemGeometryHelper.maxOrbitalDistance(description);
        double minOrbitAU = SystemGeometryHelper.minOrbitalDistance(description);
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

        // Phase 4.1.6: renderStar / renderPlanet delegated to BodyRenderer.
        // Render central star
        bodyRenderer.renderStar(description.getStarDisplayRecord(), true);

        // Render companion stars (for multi-star systems)
        for (StarDisplayRecord companion : description.getCompanionStars()) {
            bodyRenderer.renderStar(companion, false);
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
        double maxPlanetRadius = SystemGeometryHelper.maxPlanetRadius(primaryPlanets);

        // Calculate minimum moon orbit distance for each planet (for size capping)
        Map<String, Double> minMoonOrbitByParent = new HashMap<>();
        for (PlanetDescription moon : moons) {
            if (moon.getParentPlanetId() != null && moon.getSemiMajorAxis() > 0) {
                double amplifiedOrbit = moon.getSemiMajorAxis() * BodyRenderer.MOON_ORBIT_AMPLIFICATION;
                minMoonOrbitByParent.merge(moon.getParentPlanetId(), amplifiedOrbit, Math::min);
            }
        }

        // Calculate angular offsets - spread planets evenly, with extra offset for close orbits
        double[] trueAnomalies = SystemGeometryHelper.planetPhaseAngles(primaryPlanets);
        Map<String, double[]> primaryPositionsAu = new HashMap<>();
        Map<String, Color> primaryOrbitColors = new HashMap<>();
        Map<String, Double> primaryDisplayRadii = new HashMap<>();  // Track display radii for moon sizing

        for (int i = 0; i < primaryPlanets.size(); i++) {
            PlanetDescription planet = primaryPlanets.get(i);
            Color orbitColor = SolarSystemColors.ORBIT_COLORS[i % SolarSystemColors.ORBIT_COLORS.length];
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
            double displayRadius = bodyRenderer.renderPlanet(planet, orbitColor, maxPlanetRadius, trueAnomalies[i],
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
            Color orbitColor = primaryOrbitColors.getOrDefault(parent.getId(), SolarSystemColors.ORBIT_COLORS[0]);
            double trueAnomaly = random.nextDouble() * 360.0;
            // Get parent's physical radius and display radius for accurate moon sizing
            double parentPhysicalRadius = parent.getRadius() > 0 ? parent.getRadius() : 1.0;
            Double parentDisplayRadius = primaryDisplayRadii.get(parent.getId());
            bodyRenderer.renderPlanet(moon, orbitColor, maxPlanetRadius, trueAnomaly, parentPosAu, null,
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
                        String.format("%.6f", moon.getSemiMajorAxis() * BodyRenderer.MOON_ORBIT_AMPLIFICATION),
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
     * Draw a temporary transfer trajectory: a dashed half-ellipse arc between two circular orbits, in the
     * ecliptic (XZ) plane, using the renderer's radial scaling so it lines up with the planet orbits.
     *
     * @param r1Au  origin orbital radius, in AU
     * @param r2Au  destination orbital radius, in AU
     * @param color arc colour
     */
    public void drawTransferTrajectory(double r1Au, double r2Au, Color color) {
        // Attach the overlay group lazily to the system group on first use, so a
        // renderer that never plans a transfer doesn't carry an empty overlay node.
        Group overlayGroup = transferOverlay.getOverlayGroup();
        if (!systemGroup.getChildren().contains(overlayGroup)) {
            systemGroup.getChildren().add(overlayGroup);
        }
        transferOverlay.draw(r1Au, r2Au, color);
    }

    /** Remove any drawn transfer trajectory. */
    public void clearTransferOverlay() {
        transferOverlay.clear();
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
        clearTransferOverlay();

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
        rings.clearBelts();
    }

    /**
     * Clear all planetary ring systems and dispose of their resources.
     * Phase 4.1.5: delegates to {@link PlanetaryRingManager}.
     */
    public void clearRings() {
        rings.clearRings();
    }


    // ==================== Helper Methods ====================

    // Geometry helpers (calculateMaxOrbitalDistance, calculateMinOrbitalDistance,
    // calculatePlanetAngles, findMaxPlanetRadius) moved to SystemGeometryHelper
    // in Phase 4.1.3.
    //
    // Colour helpers (getStarColorFromSpectralClass, getPlanetColor + the
    // ORBIT_COLORS / *_PLANET_COLOR / MOON_* constants) moved to
    // SolarSystemColors in Phase 4.1.2.

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

        // Create the configuration (uses the shared ring adapter from PlanetaryRingManager)
        RingConfiguration config = rings.getRingAdapter().createAdaptedConfiguration(presetName, innerAU, outerAU);

        log.info("Belt '{}' config: innerScreen={}, outerScreen={}, elements={}, minSize={}, maxSize={}, thickness={}",
                feature.getName(), config.innerRadius(), config.outerRadius(), config.numElements(),
                config.minSize(), config.maxSize(), config.thickness());

        // Create and initialize the renderer (seeded for reproducible particle placement)
        RingFieldRenderer renderer = new RingFieldRenderer();
        Random sharedRandom = rings.getRingRandom();
        sharedRandom.setSeed(feature.getId().hashCode());
        renderer.initialize(config, sharedRandom);

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

        // Add to the appropriate group based on feature type
        Group asteroidBelt = rings.getAsteroidBeltGroup();
        Group kuiperBelt = rings.getKuiperBeltGroup();
        Group targetGroup = switch (feature.getFeatureType()) {
            case "ASTEROID_BELT" -> asteroidBelt;
            case "KUIPER_BELT" -> kuiperBelt;
            default -> featuresGroup;  // Other belt-type features go to general features
        };
        targetGroup.getChildren().add(renderer.getGroup());
        featureRenderers.put(feature.getId(), renderer);

        log.info("Rendered belt feature '{}' ({}) to {}: {} - {} AU",
                feature.getName(), feature.getFeatureType(),
                targetGroup == asteroidBelt ? "asteroidBeltGroup" :
                targetGroup == kuiperBelt ? "kuiperBeltGroup" : "featuresGroup",
                innerAU, outerAU);
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
