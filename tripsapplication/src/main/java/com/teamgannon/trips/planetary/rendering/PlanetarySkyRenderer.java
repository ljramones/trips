package com.teamgannon.trips.planetary.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.solarsysmodelling.accrete.PlanetTypeEnum;
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
    private final Group milkyWayGroup = new Group();
    private final Group starsGroup = new Group();
    private final Group hostStarGroup = new Group();
    private final Group horizonGroup = new Group();
    private final Group siblingPlanetsGroup = new Group();

    /**
     * Random number generator for Milky Way particle placement
     */
    private java.util.Random milkyWayRandom;

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
        // Milky Way renders behind stars (added first)
        skyGroup.getChildren().addAll(groundGroup, milkyWayGroup, gridGroup, horizonGroup, starsGroup, hostStarGroup, siblingPlanetsGroup);
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

        // Render the Milky Way as a particle field (behind everything else)
        if (context.isShowMilkyWay()) {
            renderMilkyWay(context, planetPos);
        }

        // Render the host star as the "sun"
        renderHostStar(context, planetPos);

        // Render all visible stars
        renderStars(context, allStars, planetPos);

        // Render sibling planets if visible
        if (context.isShowSiblingPlanets()) {
            renderSiblingPlanets(context, planetPos);
        }

        log.info("Rendered sky dome from {} with {} visible stars, {} Milky Way particles",
                context.getPlanetName(), brightestStars.size(), milkyWayGroup.getChildren().size());

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
     * Render the Milky Way as a particle field across the sky.
     * The Milky Way appears as a diffuse band of light along the galactic plane.
     */
    private void renderMilkyWay(PlanetaryContext context, double[] planetPos) {
        // Use a seeded random for consistent particle placement
        long seed = (long) (planetPos[0] * 1000 + planetPos[1] * 100 + planetPos[2] * 10);
        milkyWayRandom = new java.util.Random(seed);

        // Number of particles depends on whether it's nighttime
        // During day, the Milky Way is not visible
        double localTime = context.getLocalTime();
        boolean isNight = localTime < 6.0 || localTime > 18.0;
        boolean isTwilight = (localTime >= 5.0 && localTime < 6.0) || (localTime > 18.0 && localTime <= 19.0);

        if (!isNight && !isTwilight) {
            log.debug("Milky Way not visible during daytime");
            return;
        }

        // Reduce particle count during twilight
        int particleCount = isNight ? 3000 : 1000;
        double baseOpacity = isNight ? 0.4 : 0.15;

        log.info("Rendering Milky Way with {} particles", particleCount);

        // The galactic plane is tilted ~63° from the celestial equator
        // Galactic center is at RA=17h45m, Dec=-29° (in Sagittarius)
        // For simplicity, we'll use a rotated coordinate system

        // The observer's position affects the apparent rotation of the Milky Way
        // For now, use a simplified model based on viewing direction

        // Precompute materials for different brightness levels (performance optimization)
        PhongMaterial[] materials = createMilkyWayMaterials(baseOpacity);

        for (int i = 0; i < particleCount; i++) {
            // Generate particle in galactic coordinates
            // Galactic longitude (l): 0-360° around the plane
            double galLon = milkyWayRandom.nextDouble() * 360.0;

            // Galactic latitude (b): concentrated near 0° with Gaussian spread
            // Use a tighter distribution near the galactic center
            double latitudeSpread = 12.0; // degrees
            double galLat = milkyWayRandom.nextGaussian() * latitudeSpread;

            // Clamp latitude to reasonable range
            galLat = Math.max(-30, Math.min(30, galLat));

            // Higher density toward galactic center (l ≈ 0° or 360°)
            // The galactic center is at l=0°
            double centerWeight = Math.cos(Math.toRadians(galLon)) * 0.5 + 0.5;
            if (milkyWayRandom.nextDouble() > centerWeight * 0.7 + 0.3) {
                // Skip some particles away from center for density variation
                if (milkyWayRandom.nextDouble() > 0.5) continue;
            }

            // Convert galactic to equatorial coordinates (simplified transformation)
            // Galactic north pole is at RA=12h51m, Dec=+27.1°
            // Galactic center is at RA=17h45m, Dec=-29°
            double[] equatorial = galacticToEquatorial(galLon, galLat);
            double ra = equatorial[0];  // Right Ascension in degrees
            double dec = equatorial[1]; // Declination in degrees

            // Convert equatorial to horizontal (alt/az) based on observer location
            // This is simplified - we rotate based on local time
            double hourAngle = (localTime / 24.0) * 360.0;
            double azimuth = ra - hourAngle + context.getViewingAzimuth();
            double altitude = dec;

            // Adjust altitude based on observer's assumed latitude (~45° N for variety)
            double observerLat = 45.0;
            altitude = dec * Math.cos(Math.toRadians(observerLat)) +
                       (90 - Math.abs(dec)) * Math.sin(Math.toRadians(observerLat)) *
                       Math.cos(Math.toRadians(azimuth));
            altitude = Math.max(-90, Math.min(90, altitude));

            // Only render particles above horizon
            if (altitude < -2) continue;

            // Position on sky dome (slightly inside to appear behind stars)
            double[] skyPos = sphericalToCartesian(SKY_DOME_RADIUS * 0.98, azimuth, altitude);

            // Particle brightness varies with galactic latitude and random variation
            double brightness = Math.exp(-Math.abs(galLat) / 8.0);  // Brighter near plane
            brightness *= (0.5 + milkyWayRandom.nextDouble() * 0.5); // Random variation
            brightness *= centerWeight;  // Brighter toward center

            // Select material based on brightness
            int matIndex = (int) (brightness * (materials.length - 1));
            matIndex = Math.max(0, Math.min(materials.length - 1, matIndex));

            // Create small particle
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

    /**
     * Create an array of materials for Milky Way particles at different brightness levels.
     */
    private PhongMaterial[] createMilkyWayMaterials(double baseOpacity) {
        int levels = 8;
        PhongMaterial[] materials = new PhongMaterial[levels];

        for (int i = 0; i < levels; i++) {
            double brightness = (double) i / (levels - 1);
            double opacity = baseOpacity * (0.3 + brightness * 0.7);

            // Milky Way has a warm, slightly yellowish color
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
     * Convert galactic coordinates to equatorial coordinates.
     * This is a simplified transformation for visualization purposes.
     *
     * @param galLon Galactic longitude in degrees (0-360)
     * @param galLat Galactic latitude in degrees (-90 to +90)
     * @return [RA, Dec] in degrees
     */
    private double[] galacticToEquatorial(double galLon, double galLat) {
        // Galactic coordinate system parameters
        // North Galactic Pole: RA = 192.85948° (12h 51m 26.28s), Dec = +27.12825°
        // Galactic Center: RA = 266.405° (17h 45m 37.2s), Dec = -28.936°

        double l = Math.toRadians(galLon);
        double b = Math.toRadians(galLat);

        // Transformation matrix parameters
        double ngpRa = Math.toRadians(192.85948);
        double ngpDec = Math.toRadians(27.12825);
        double lonNcp = Math.toRadians(122.932);  // Longitude of North Celestial Pole

        // Convert galactic to equatorial
        double sinDec = Math.sin(b) * Math.sin(ngpDec) +
                       Math.cos(b) * Math.cos(ngpDec) * Math.sin(l - lonNcp);
        double dec = Math.asin(Math.max(-1, Math.min(1, sinDec)));

        double y = Math.cos(b) * Math.cos(l - lonNcp);
        double x = Math.sin(b) * Math.cos(ngpDec) -
                  Math.cos(b) * Math.sin(ngpDec) * Math.sin(l - lonNcp);
        double ra = ngpRa + Math.atan2(y, x);

        // Normalize RA to 0-360
        double raDeg = Math.toDegrees(ra);
        while (raDeg < 0) raDeg += 360;
        while (raDeg >= 360) raDeg -= 360;

        return new double[]{raDeg, Math.toDegrees(dec)};
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
     * Planets appear as bright, non-twinkling points in the night sky.
     */
    private void renderSiblingPlanets(PlanetaryContext context, double[] planetPos) {
        SolarSystemDescription system = context.getSystem();
        if (system == null) return;

        ExoPlanet observerPlanet = context.getPlanet();
        StarDisplayRecord hostStar = context.getHostStar();
        if (hostStar == null) return;

        double localTime = context.getLocalTime();
        List<PlanetDescription> siblings = system.getPlanetDescriptionList();

        if (siblings == null || siblings.isEmpty()) {
            log.debug("No sibling planets to render");
            return;
        }

        log.info("Rendering {} sibling planets", siblings.size());

        for (PlanetDescription sibling : siblings) {
            // Skip the planet we're standing on
            if (sibling.getId() != null && observerPlanet.getId() != null &&
                    sibling.getId().equals(observerPlanet.getId().toString())) {
                continue;
            }
            if (sibling.getName() != null && observerPlanet.getName() != null &&
                    sibling.getName().equals(observerPlanet.getName())) {
                continue;
            }

            // Calculate sibling planet's orbital position
            double[] siblingPos = calculatePlanetOrbitalPosition(sibling, hostStar, localTime);

            // Calculate direction from observer to sibling
            double[] direction = calculateDirection(planetPos, siblingPos);

            // Convert direction to azimuth/altitude
            double azimuth = Math.toDegrees(Math.atan2(direction[0], direction[2]));
            double altitude = Math.toDegrees(Math.asin(direction[1]));

            // Only show planets above horizon
            if (altitude < 0) {
                log.debug("Planet {} is below horizon (alt={})", sibling.getName(), altitude);
                continue;
            }

            // Calculate apparent brightness based on distance and planet size
            double distanceLy = calculateDistance(planetPos, siblingPos);
            double distanceAU = distanceLy / AU_TO_LY;  // Convert back to AU for brightness calc
            double apparentMag = calculatePlanetApparentMagnitude(sibling, distanceAU);

            // Position on sky dome
            double[] skyPos = sphericalToCartesian(SKY_DOME_RADIUS, azimuth, altitude);

            // Get planet color based on type
            Color planetColor = getPlanetColor(sibling);

            // Create planet visual (brighter than most stars, steady light)
            Group planetVisual = createPlanetVisual(sibling.getName(), apparentMag, planetColor, skyPos);
            siblingPlanetsGroup.getChildren().add(planetVisual);

            log.info("Rendered planet {} at alt={:.1f}, az={:.1f}, mag={:.1f}",
                    sibling.getName(), altitude, azimuth, apparentMag);
        }

        // Also render companion stars in multi-star systems
        renderCompanionStars(context, planetPos);
    }

    /**
     * Render companion stars in multi-star systems.
     * These appear as additional suns in the sky.
     */
    private void renderCompanionStars(PlanetaryContext context, double[] planetPos) {
        SolarSystemDescription system = context.getSystem();
        if (system == null || !system.isMultiStarSystem()) return;

        List<StarDisplayRecord> companions = system.getCompanionStars();
        if (companions == null || companions.isEmpty()) return;

        log.info("Rendering {} companion stars", companions.size());

        for (StarDisplayRecord companion : companions) {
            double[] companionPos = {companion.getX(), companion.getY(), companion.getZ()};

            // Calculate direction from observer to companion star
            double[] direction = calculateDirection(planetPos, companionPos);

            // Convert direction to azimuth/altitude
            double azimuth = Math.toDegrees(Math.atan2(direction[0], direction[2]));
            double altitude = Math.toDegrees(Math.asin(direction[1]));

            // Show companion stars even if below horizon (they're so bright)
            // but render them dimmer at low altitudes
            double[] skyPos = sphericalToCartesian(SKY_DOME_RADIUS * 0.95, azimuth, Math.max(-5, altitude));

            // Calculate apparent magnitude based on distance
            double distanceLy = calculateDistance(planetPos, companionPos);
            double apparentMag = companion.getMagnitude() + 5.0 * Math.log10(distanceLy / companion.getDistance());

            // Create companion star visual (like a second sun)
            Color starColor = getStarColor(companion.getSpectralClass());
            double size = Math.max(8, 15 - apparentMag * 2);  // Larger than planets

            Sphere companionSphere = new Sphere(size);
            companionSphere.setCullFace(CullFace.NONE);
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(starColor);
            material.setSpecularColor(Color.WHITE);
            companionSphere.setMaterial(material);

            companionSphere.setTranslateX(skyPos[0]);
            companionSphere.setTranslateY(skyPos[1]);
            companionSphere.setTranslateZ(skyPos[2]);

            siblingPlanetsGroup.getChildren().add(companionSphere);

            // Add label for companion star
            String starName = companion.getStarName();
            if (starName != null && !starName.isEmpty()) {
                Label label = new Label(starName);
                label.setTextFill(starColor.brighter());
                label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
                shapeToLabel.put(companionSphere, label);
            }

            log.info("Rendered companion star {} at alt={:.1f}, az={:.1f}",
                    companion.getStarName(), altitude, azimuth);
        }
    }

    /**
     * Calculate a planet's position in light years based on its orbital elements.
     */
    private double[] calculatePlanetOrbitalPosition(PlanetDescription planet, StarDisplayRecord hostStar, double localTime) {
        double starX = hostStar.getX();
        double starY = hostStar.getY();
        double starZ = hostStar.getZ();

        double semiMajorAU = planet.getSemiMajorAxis();
        double eccentricity = planet.getEccentricity();
        double inclination = Math.toRadians(planet.getInclination());
        double argPeri = Math.toRadians(planet.getArgumentOfPeriapsis());
        double longAscNode = Math.toRadians(planet.getLongitudeOfAscendingNode());

        // Calculate mean anomaly based on orbital period and local time
        // This gives different planets different positions in their orbits
        double orbitalPeriod = planet.getOrbitalPeriod();
        if (orbitalPeriod <= 0) orbitalPeriod = 365.25; // Default to Earth year

        // Use local time as a fraction of the orbital period for variety
        double meanAnomaly = (localTime / 24.0) * 2 * Math.PI * (365.25 / orbitalPeriod);

        // Solve Kepler's equation: E - e*sin(E) = M
        double eccentricAnomaly = solveKeplerEquation(meanAnomaly, eccentricity);

        // Calculate true anomaly
        double trueAnomaly = 2 * Math.atan2(
                Math.sqrt(1 + eccentricity) * Math.sin(eccentricAnomaly / 2),
                Math.sqrt(1 - eccentricity) * Math.cos(eccentricAnomaly / 2)
        );

        // Calculate orbital radius at this position
        double radius = semiMajorAU * (1 - eccentricity * eccentricity) /
                       (1 + eccentricity * Math.cos(trueAnomaly));

        // Position in orbital plane
        double xOrbit = radius * Math.cos(trueAnomaly);
        double yOrbit = radius * Math.sin(trueAnomaly);

        // Apply 3D rotations: argument of periapsis, inclination, longitude of ascending node
        // R = R_z(Omega) * R_x(i) * R_z(omega) * [x, y, 0]
        double cosO = Math.cos(longAscNode);
        double sinO = Math.sin(longAscNode);
        double cosI = Math.cos(inclination);
        double sinI = Math.sin(inclination);
        double cosW = Math.cos(argPeri);
        double sinW = Math.sin(argPeri);

        // Combined rotation
        double x = (cosO * cosW - sinO * sinW * cosI) * xOrbit +
                   (-cosO * sinW - sinO * cosW * cosI) * yOrbit;
        double y = (sinO * cosW + cosO * sinW * cosI) * xOrbit +
                   (-sinO * sinW + cosO * cosW * cosI) * yOrbit;
        double z = (sinW * sinI) * xOrbit + (cosW * sinI) * yOrbit;

        // Convert AU to light years and add to star position
        return new double[]{
                starX + x * AU_TO_LY,
                starY + y * AU_TO_LY,
                starZ + z * AU_TO_LY
        };
    }

    /**
     * Solve Kepler's equation using Newton-Raphson iteration.
     */
    private double solveKeplerEquation(double meanAnomaly, double eccentricity) {
        double E = meanAnomaly;  // Initial guess
        for (int i = 0; i < 10; i++) {
            double dE = (E - eccentricity * Math.sin(E) - meanAnomaly) /
                       (1 - eccentricity * Math.cos(E));
            E -= dE;
            if (Math.abs(dE) < 1e-8) break;
        }
        return E;
    }

    /**
     * Calculate apparent magnitude of a planet based on distance and size.
     * Planets are much closer than stars so they can be very bright.
     */
    private double calculatePlanetApparentMagnitude(PlanetDescription planet, double distanceAU) {
        // Base magnitude depends on planet size and type
        double baseMag;
        if (planet.getPlanetTypeEnum() != null) {
            baseMag = switch (planet.getPlanetTypeEnum()) {
                case tGasGiant, tSubGasGiant, tSubSubGasGiant -> -2.5;  // Jupiter-like, very bright
                case tIce, tWater -> -1.5;                              // Neptune-like / icy
                case tSuperEarth, tTerrestrial -> 0.0;                  // Earth-like
                case tRock, tMartian, tAsteroids -> 2.0;                // Small rocky bodies
                case tVenusian -> -0.5;                                  // Venus is very bright
                default -> 0.5;
            };
        } else {
            // Estimate from radius (Earth radii)
            double radius = planet.getRadius();
            if (radius > 5) baseMag = -2.5;      // Gas giant
            else if (radius > 2) baseMag = -1.0; // Ice giant / super-Earth
            else if (radius > 0.5) baseMag = 0.5; // Terrestrial
            else baseMag = 2.0;                   // Small body
        }

        // Adjust for distance (inverse square law)
        // At 1 AU, we use base magnitude; further = dimmer
        double distanceFactor = 5.0 * Math.log10(Math.max(0.1, distanceAU));

        return baseMag + distanceFactor;
    }

    /**
     * Get color for a planet based on its type.
     */
    private Color getPlanetColor(PlanetDescription planet) {
        if (planet.getPlanetTypeEnum() != null) {
            return switch (planet.getPlanetTypeEnum()) {
                case tGasGiant -> Color.rgb(255, 200, 150);           // Jupiter - orange/tan
                case tSubGasGiant, tSubSubGasGiant -> Color.rgb(230, 190, 140);  // Saturn-like - pale gold
                case tIce -> Color.rgb(150, 200, 255);                // Neptune/Uranus - blue
                case tWater -> Color.rgb(100, 150, 220);              // Water world - deep blue
                case tSuperEarth -> Color.rgb(180, 200, 180);         // Greenish-blue
                case tTerrestrial -> Color.rgb(200, 180, 160);        // Earth-like - tan
                case tRock, tMartian -> Color.rgb(180, 140, 120);     // Rocky - reddish-brown
                case tVenusian -> Color.rgb(255, 230, 180);           // Venus - yellowish-white
                case tAsteroids -> Color.rgb(150, 140, 130);          // Dark gray
                default -> Color.rgb(200, 200, 200);                  // White
            };
        }

        // Fallback: estimate from temperature
        double temp = planet.getEquilibriumTemperature();
        if (temp > 500) return Color.rgb(255, 180, 120);   // Hot - orange
        if (temp > 300) return Color.rgb(200, 180, 160);   // Warm - tan
        if (temp > 200) return Color.rgb(150, 180, 200);   // Cool - blue-ish
        return Color.rgb(200, 220, 255);                    // Cold - pale blue/white
    }

    /**
     * Create a planet visual for the sky dome.
     * Planets appear as steady, bright points (unlike twinkling stars).
     */
    private Group createPlanetVisual(String name, double magnitude, Color planetColor, double[] position) {
        Group group = new Group();
        double x = position[0];
        double y = position[1];
        double z = position[2];

        // Planets are rendered larger and steadier than stars of similar magnitude
        double size = Math.max(1.5, 4.0 - magnitude * 0.5);

        // Create the planet point
        Sphere planet = new Sphere(size);
        planet.setCullFace(CullFace.NONE);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(planetColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 1, 0.3));
        planet.setMaterial(material);
        planet.setTranslateX(x);
        planet.setTranslateY(y);
        planet.setTranslateZ(z);

        // Add a subtle glow for bright planets
        if (magnitude < 0) {
            Sphere glow = new Sphere(size * 2.5);
            glow.setCullFace(CullFace.NONE);
            PhongMaterial glowMat = new PhongMaterial();
            glowMat.setDiffuseColor(planetColor.deriveColor(0, 0.5, 1.2, 0.15));
            glow.setMaterial(glowMat);
            glow.setTranslateX(x);
            glow.setTranslateY(y);
            glow.setTranslateZ(z);
            group.getChildren().add(glow);
        }

        group.getChildren().add(planet);

        // Create label for the planet
        if (name != null && !name.isEmpty()) {
            Label label = new Label(name);
            label.setTextFill(planetColor.brighter());
            label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            shapeToLabel.put(planet, label);
        }

        return group;
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
        milkyWayGroup.getChildren().clear();
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
            return "%s (mag %.1f, %.1f ly)".formatted(name, apparentMagnitude, distanceFromPlanet);
        }
    }
}
