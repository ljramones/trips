package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProvider;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Renders orbital markers for solar system visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>Ascending and descending node markers</li>
 *   <li>Periapsis and apoapsis markers</li>
 * </ul>
 */
@Slf4j
public class OrbitMarkerRenderer {

    private final ScaleManager scaleManager;
    private final OrbitSamplingProvider orbitSamplingProvider;

    public OrbitMarkerRenderer(ScaleManager scaleManager, OrbitSamplingProvider orbitSamplingProvider) {
        this.scaleManager = scaleManager;
        this.orbitSamplingProvider = orbitSamplingProvider;
    }

    /**
     * Render orbit node markers (ascending/descending nodes).
     *
     * @param orbitNodeGroup the group to add markers to
     * @param planet        the planet description
     * @param orbitColor    the orbit color (for marker styling)
     * @param parentOffsetAu offset for moons (parent planet position), null for primary planets
     * @param showOrbitNodes whether orbit nodes should be shown
     */
    public void renderOrbitNodeMarkers(Group orbitNodeGroup,
                                       PlanetDescription planet,
                                       Color orbitColor,
                                       double[] parentOffsetAu,
                                       boolean showOrbitNodes) {
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

        double[] ascPosAu = orbitSamplingProvider.calculatePositionAu(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                ascendingTrueAnomaly
        );
        double[] descPosAu = orbitSamplingProvider.calculatePositionAu(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                descendingTrueAnomaly
        );
        if (parentOffsetAu != null) {
            ascPosAu[0] += parentOffsetAu[0];
            ascPosAu[1] += parentOffsetAu[1];
            ascPosAu[2] += parentOffsetAu[2];
            descPosAu[0] += parentOffsetAu[0];
            descPosAu[1] += parentOffsetAu[1];
            descPosAu[2] += parentOffsetAu[2];
        }
        double[] ascPos = scaleManager.auVectorToScreen(ascPosAu[0], ascPosAu[1], ascPosAu[2]);
        double[] descPos = scaleManager.auVectorToScreen(descPosAu[0], descPosAu[1], descPosAu[2]);

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

    /**
     * Rebuild all orbit node markers from stored planet descriptions.
     *
     * @param orbitNodeGroup     the group to add markers to
     * @param planetDescriptions map of planet names to descriptions
     * @param orbitColors        map of planet names to orbit colors
     * @param defaultColor       default orbit color
     * @param showOrbitNodes     whether orbit nodes should be shown
     */
    public void rebuildOrbitNodeMarkers(Group orbitNodeGroup,
                                        Map<String, PlanetDescription> planetDescriptions,
                                        Map<String, Color> orbitColors,
                                        Color defaultColor,
                                        boolean showOrbitNodes) {
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
            Color orbitColor = orbitColors.getOrDefault(planetName, defaultColor);
            renderOrbitNodeMarkers(orbitNodeGroup, planet, orbitColor, null, showOrbitNodes);
        }
    }

    /**
     * Render periapsis and apoapsis markers for a planet's orbit.
     * Periapsis (closest to star) is at true anomaly = 0°
     * Apoapsis (farthest from star) is at true anomaly = 180°
     *
     * @param apsidesGroup   the group to add markers to
     * @param planet         the planet description
     * @param orbitColor     the orbit color (unused, markers have fixed colors)
     * @param parentOffsetAu offset for moons (parent planet position), null for primary planets
     * @param showApsides    whether apsides should be shown
     */
    public void renderApsideMarkers(Group apsidesGroup,
                                    PlanetDescription planet,
                                    Color orbitColor,
                                    double[] parentOffsetAu,
                                    boolean showApsides) {
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
        double[] periPosAu = orbitSamplingProvider.calculatePositionAu(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                0.0  // Periapsis
        );
        double[] apoPosAu = orbitSamplingProvider.calculatePositionAu(
                semiMajorAxis,
                eccentricity,
                inclination,
                longAscNode,
                argPeriapsis,
                180.0  // Apoapsis
        );
        if (parentOffsetAu != null) {
            periPosAu[0] += parentOffsetAu[0];
            periPosAu[1] += parentOffsetAu[1];
            periPosAu[2] += parentOffsetAu[2];
            apoPosAu[0] += parentOffsetAu[0];
            apoPosAu[1] += parentOffsetAu[1];
            apoPosAu[2] += parentOffsetAu[2];
        }
        double[] periPos = scaleManager.auVectorToScreen(periPosAu[0], periPosAu[1], periPosAu[2]);
        double[] apoPos = scaleManager.auVectorToScreen(apoPosAu[0], apoPosAu[1], apoPosAu[2]);

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

    /**
     * Rebuild all apside markers from stored planet descriptions.
     *
     * @param apsidesGroup       the group to add markers to
     * @param planetDescriptions map of planet names to descriptions
     * @param orbitColors        map of planet names to orbit colors
     * @param defaultColor       default orbit color
     * @param showApsides        whether apsides should be shown
     */
    public void rebuildApsideMarkers(Group apsidesGroup,
                                     Map<String, PlanetDescription> planetDescriptions,
                                     Map<String, Color> orbitColors,
                                     Color defaultColor,
                                     boolean showApsides) {
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
            Color orbitColor = orbitColors.getOrDefault(planetName, defaultColor);
            renderApsideMarkers(apsidesGroup, planet, orbitColor, null, showApsides);
        }
    }
}
