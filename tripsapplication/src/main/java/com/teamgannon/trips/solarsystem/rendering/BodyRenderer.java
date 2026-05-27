package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetary.modelling.PlanetDescription;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuHandler;
import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProvider;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders a single star or planet into the solar-system scene graph.
 * <p>
 * Extracted from {@code SolarSystemRenderer} in Phase 4.1.6 of the
 * codebase-review remediation — the last and largest of the renderer's
 * decompositions ({@code renderPlanet} alone was ~245 lines).
 * <p>
 * Holds references to the renderer's collaborator helpers (scale, orbits,
 * markers, selection, rings) plus the shared groups + maps that the renderer
 * owns. Visibility flags ({@code showOrbits} / {@code showOrbitNodes} /
 * {@code showApsides}) and the {@link SolarSystemContextMenuHandler} are
 * mutable settings the renderer pushes here via setters; everything else is
 * passed once at construction.
 */
@Slf4j
public final class BodyRenderer {

    /**
     * Amplification factor for moon orbits so they're visible alongside planet
     * orbits — moons are typically tiny (0.001-0.01 AU) compared to a planet's
     * SMA. 15× makes Callisto's orbit (~0.0126 AU) appear at ~0.19 AU.
     */
    public static final double MOON_ORBIT_AMPLIFICATION = 15.0;

    // ----- stateless / shared helpers -----
    private final ScaleManager scaleManager;
    private final OrbitVisualizer orbitVisualizer;
    private final OrbitSamplingProvider orbitSamplingProvider;
    private final OrbitMarkerRenderer orbitMarkerRenderer;
    private final SelectionStyleManager selectionStyleManager;
    private final PlanetaryRingManager rings;

    // ----- shared scene-graph groups (owned by renderer; rendered into here) -----
    private final Group planetsGroup;
    private final Group orbitsGroup;
    private final Group orbitNodeGroup;
    private final Group apsidesGroup;

    // ----- shared maps (owned by renderer; written here) -----
    private final Map<String, Sphere> planetNodes;
    private final Map<String, Node> starNodes;
    private final Map<String, PlanetDescription> planetDescriptions;
    private final Map<String, Group> orbitGroups;
    private final Map<String, Color> orbitColors;
    private final Map<String, List<Group>> moonOrbitsByParent;

    // ----- mutable settings (pushed by renderer before/during render passes) -----
    private SolarSystemContextMenuHandler contextMenuHandler;
    private boolean showOrbits = true;
    private boolean showOrbitNodes = false;
    private boolean showApsides = false;

    public BodyRenderer(ScaleManager scaleManager,
                        OrbitVisualizer orbitVisualizer,
                        OrbitSamplingProvider orbitSamplingProvider,
                        OrbitMarkerRenderer orbitMarkerRenderer,
                        SelectionStyleManager selectionStyleManager,
                        PlanetaryRingManager rings,
                        Group planetsGroup,
                        Group orbitsGroup,
                        Group orbitNodeGroup,
                        Group apsidesGroup,
                        Map<String, Sphere> planetNodes,
                        Map<String, Node> starNodes,
                        Map<String, PlanetDescription> planetDescriptions,
                        Map<String, Group> orbitGroups,
                        Map<String, Color> orbitColors,
                        Map<String, List<Group>> moonOrbitsByParent) {
        this.scaleManager = scaleManager;
        this.orbitVisualizer = orbitVisualizer;
        this.orbitSamplingProvider = orbitSamplingProvider;
        this.orbitMarkerRenderer = orbitMarkerRenderer;
        this.selectionStyleManager = selectionStyleManager;
        this.rings = rings;
        this.planetsGroup = planetsGroup;
        this.orbitsGroup = orbitsGroup;
        this.orbitNodeGroup = orbitNodeGroup;
        this.apsidesGroup = apsidesGroup;
        this.planetNodes = planetNodes;
        this.starNodes = starNodes;
        this.planetDescriptions = planetDescriptions;
        this.orbitGroups = orbitGroups;
        this.orbitColors = orbitColors;
        this.moonOrbitsByParent = moonOrbitsByParent;
    }

    // ----- mutable settings -----

    public void setContextMenuHandler(SolarSystemContextMenuHandler handler) {
        this.contextMenuHandler = handler;
    }

    public void setShowOrbits(boolean showOrbits) {
        this.showOrbits = showOrbits;
    }

    public void setShowOrbitNodes(boolean showOrbitNodes) {
        this.showOrbitNodes = showOrbitNodes;
    }

    public void setShowApsides(boolean showApsides) {
        this.showApsides = showApsides;
    }

    // ----- star rendering -----

    /**
     * Render the central or a companion star.
     */
    public void renderStar(StarDisplayRecord star, boolean isPrimary) {
        if (star == null) return;

        double radius = scaleManager.getStarRadius();
        if (!isPrimary) {
            radius *= 0.7; // Companion stars are visually slightly smaller.
        }

        Sphere starSphere = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();

        Color starColor = star.getStarColor();
        if (starColor == null) {
            starColor = SolarSystemColors.starColor(star.getSpectralClass());
        }
        material.setDiffuseColor(starColor);
        material.setSpecularColor(Color.WHITE);
        material.setSelfIlluminationMap(null);
        starSphere.setMaterial(material);

        if (isPrimary) {
            // Primary star at origin
            starSphere.setTranslateX(0);
            starSphere.setTranslateY(0);
            starSphere.setTranslateZ(0);
        } else {
            // Companion star: simple horizontal offset for now (binary-orbit math TBD).
            starSphere.setTranslateX(scaleManager.auToScreen(0.5));
            starSphere.setTranslateY(0);
            starSphere.setTranslateZ(0);
        }

        Tooltip.install(starSphere, new Tooltip("%s\nSpectral: %s\nDistance: %.2f ly".formatted(
                star.getStarName(), star.getSpectralClass(), star.getDistance())));

        starSphere.setUserData(star);
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

    // ----- planet rendering -----

    /**
     * Render a planet (or moon) with its orbit + orbit markers.
     *
     * @param planet               the planet to render
     * @param orbitColor           orbit colour (ignored for moons — they always get the unified silver)
     * @param maxPlanetRadius      max planet radius in the system (for relative sizing)
     * @param trueAnomaly          initial position on the orbit
     * @param parentOffsetAu       AU offset of the parent planet (moons only; null for primaries)
     * @param minMoonOrbitAu       minimum moon orbit distance in AU (already amplified), used to cap the parent's size
     * @param parentPhysicalRadius parent's physical radius in Earth radii (moons only)
     * @param parentDisplayRadius  parent's display radius in screen units (moons only)
     * @return the display radius used for this body (so callers can size moons relative to it)
     */
    public double renderPlanet(PlanetDescription planet,
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

        // Detect moons by either the flag or a non-blank parentPlanetId, for robustness.
        boolean isMoonBody = planet.isMoon()
                || (planet.getParentPlanetId() != null && !planet.getParentPlanetId().isBlank());

        if (isMoonBody) {
            log.info("Rendering MOON: {} (isMoon={}, parentId={}, sma={} AU)",
                    planet.getName(), planet.isMoon(), planet.getParentPlanetId(), semiMajorAxis);
        }

        // Moons get their orbit amplified so it's visible alongside the planet orbits.
        double orbitSmaForRendering = isMoonBody ? semiMajorAxis * MOON_ORBIT_AMPLIFICATION : semiMajorAxis;
        if (isMoonBody) {
            log.info("  Amplified orbit SMA: {} AU -> {} AU", semiMajorAxis, orbitSmaForRendering);
        }

        // Orbit path
        Group orbitPath = isMoonBody
                ? orbitVisualizer.createSolidOrbitPath(orbitSmaForRendering, eccentricity, inclination, longAscNode, argPeriapsis, SolarSystemColors.MOON_ORBIT_COLOR)
                : orbitVisualizer.createOrbitPath(orbitSmaForRendering, eccentricity, inclination, longAscNode, argPeriapsis, orbitColor);

        if (parentOffsetAu != null) {
            double[] offset = scaleManager.auVectorToScreen(parentOffsetAu[0], parentOffsetAu[1], parentOffsetAu[2]);
            orbitPath.setTranslateX(offset[0]);
            orbitPath.setTranslateY(offset[1]);
            orbitPath.setTranslateZ(offset[2]);
        }

        orbitPath.setUserData(planet);
        orbitGroups.put(planet.getName(), orbitPath);
        orbitColors.put(planet.getName(), orbitColor);

        // Track moon orbits by parent for dynamic visibility (zoom-driven).
        if (isMoonBody && planet.getParentPlanetId() != null) {
            moonOrbitsByParent
                    .computeIfAbsent(planet.getParentPlanetId(), k -> new ArrayList<>())
                    .add(orbitPath);
            orbitPath.setVisible(showOrbits);
        }

        addOrbitContextMenuHandler(orbitPath, planet);
        orbitsGroup.getChildren().add(orbitPath);
        selectionStyleManager.registerOrbitSegments(orbitPath);
        selectionStyleManager.registerSelectableNode(orbitPath);

        // Use the amplified SMA for moon position so they sit on their visible orbit.
        double positionSma = isMoonBody ? orbitSmaForRendering : semiMajorAxis;
        if (isMoonBody) {
            log.info("  Moon {} position using SMA: {} AU (amplified={})",
                    planet.getName(), positionSma, positionSma == orbitSmaForRendering);
        }
        double[] localPosAu = orbitSamplingProvider.calculatePositionAu(
                positionSma, eccentricity, inclination, longAscNode, argPeriapsis, trueAnomaly);

        // For moons under log scaling: screen(a + b) != screen(a) + screen(b), so add offsets
        // in SCREEN space (matching how the orbit path is positioned post-creation).
        double[] position;
        if (parentOffsetAu != null && isMoonBody) {
            double[] localScreen = scaleManager.auVectorToScreen(localPosAu[0], localPosAu[1], localPosAu[2]);
            double[] parentScreen = scaleManager.auVectorToScreen(parentOffsetAu[0], parentOffsetAu[1], parentOffsetAu[2]);
            position = new double[]{
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
            // Non-moon with parent offset — shouldn't really happen, handle gracefully.
            localPosAu[0] += parentOffsetAu[0];
            localPosAu[1] += parentOffsetAu[1];
            localPosAu[2] += parentOffsetAu[2];
            position = scaleManager.auVectorToScreen(localPosAu[0], localPosAu[1], localPosAu[2]);
        } else {
            // Primary planet — no parent offset.
            position = scaleManager.auVectorToScreen(localPosAu[0], localPosAu[1], localPosAu[2]);
        }

        // Sphere sizing: moons use the actual physical ratio to their parent;
        // primaries use the system-wide relative-size scaling with an optional
        // cap when they have moons (so the planet doesn't engulf its nearest moon).
        double planetRadius;
        if (isMoonBody && parentPhysicalRadius != null && parentDisplayRadius != null) {
            planetRadius = scaleManager.calculateMoonDisplayRadius(
                    planet.getRadius() > 0 ? planet.getRadius() : 0.1,
                    parentPhysicalRadius,
                    parentDisplayRadius);
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
            planetRadius = scaleManager.calculatePlanetDisplayRadius(
                    planet.getRadius() > 0 ? planet.getRadius() : 1.0,
                    maxPlanetRadius > 0 ? maxPlanetRadius : 1.0);

            if (minMoonOrbitAu != null && minMoonOrbitAu > 0) {
                double minMoonOrbitScreen = scaleManager.auToScreen(minMoonOrbitAu);
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
        material.setDiffuseColor(SolarSystemColors.planetColor(planet));
        material.setSpecularColor(Color.WHITE);
        planetSphere.setMaterial(material);

        // Glow boost for tiny moons so they remain visible.
        if (isMoonBody && planetRadius < 1.0) {
            double glowIntensity = Math.min(0.8, 0.3 + (1.0 - planetRadius) * 0.5);
            planetSphere.setEffect(new Glow(glowIntensity));
        }

        planetSphere.setTranslateX(position[0]);
        planetSphere.setTranslateY(position[1]);
        planetSphere.setTranslateZ(position[2]);

        Tooltip.install(planetSphere, new Tooltip(String.format(
                "%s\nSemi-major axis: %.3f AU\nPeriod: %.1f days\nRadius: %.2f Earth",
                planet.getName(),
                planet.getSemiMajorAxis(),
                planet.getOrbitalPeriod(),
                planet.getRadius())));

        planetSphere.setUserData(planet);
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

        planetNodes.put(planet.getName(), planetSphere);
        planetDescriptions.put(planet.getName(), planet);

        orbitMarkerRenderer.renderOrbitNodeMarkers(orbitNodeGroup, planet, orbitColor, parentOffsetAu, showOrbitNodes);
        orbitMarkerRenderer.renderApsideMarkers(apsidesGroup, planet, orbitColor, parentOffsetAu, showApsides);

        // Rings (per-planet) — Phase 4.1.5 delegated.
        if (planet.isHasRings() && !isMoonBody) {
            rings.renderPlanetRing(planet, position, planetRadius);
        }

        return planetRadius;
    }

    // ----- internal -----

    /**
     * Attach primary-click and secondary-click context-menu wiring to an orbit
     * group, and additionally to each child of the group for better hit
     * detection (the orbit is rendered as a strip of cylinder segments, and
     * users click on a specific segment).
     */
    private void addOrbitContextMenuHandler(Group orbitGroup, PlanetDescription planet) {
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
        // Each segment is also clickable.
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
}
