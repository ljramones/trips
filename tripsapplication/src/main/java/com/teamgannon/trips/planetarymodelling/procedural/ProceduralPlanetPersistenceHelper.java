package com.teamgannon.trips.planetarymodelling.procedural;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.image.WritableImage;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Slf4j
public final class ProceduralPlanetPersistenceHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String GENERATOR_VERSION = "2026-01-18";

    private ProceduralPlanetPersistenceHelper() {}

    public static void populateProceduralMetadata(
            ExoPlanet exoPlanet,
            PlanetConfig config,
            long seed,
            PlanetGenerator.GeneratedPlanet generated,
            String sourceLabel) {
        if (exoPlanet == null || config == null || generated == null) {
            return;
        }

        exoPlanet.setProceduralSeed(seed);
        exoPlanet.setProceduralGeneratorVersion(GENERATOR_VERSION);
        exoPlanet.setProceduralSource(sourceLabel);
        exoPlanet.setProceduralAccreteSnapshot(createAccreteSnapshot(exoPlanet, seed));
        exoPlanet.setProceduralOverrides(createOverridesSnapshot(config));
        exoPlanet.setProceduralGeneratedAt(OffsetDateTime.now().toString());
        exoPlanet.setProceduralPreview(createPreview(generated, 256));
    }

    public static PlanetConfig buildConfigFromSnapshots(ExoPlanet exoPlanet) {
        if (exoPlanet == null) {
            return null;
        }
        long seed = exoPlanet.getProceduralSeed() != null
            ? exoPlanet.getProceduralSeed()
            : deriveSeed(exoPlanet);

        SnapshotInput snapshot = SnapshotInput.from(exoPlanet, exoPlanet.getProceduralAccreteSnapshot());
        PlanetConfig base = buildConfigFromPhysicals(snapshot, seed);
        return applyOverrides(base, exoPlanet.getProceduralOverrides());
    }

    public static PlanetConfig buildConfigFromExoPlanet(ExoPlanet exoPlanet, long seed) {
        SnapshotInput snapshot = SnapshotInput.from(exoPlanet, null);
        return buildConfigFromPhysicals(snapshot, seed);
    }

    public static String createAccreteSnapshot(ExoPlanet exoPlanet, long seed) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("seed", seed);
        snapshot.put("mass", exoPlanet.getMass());
        snapshot.put("radius", exoPlanet.getRadius());
        snapshot.put("surfaceGravity", exoPlanet.getSurfaceGravity());
        snapshot.put("hydrosphere", exoPlanet.getHydrosphere());
        snapshot.put("surfaceTemperature", exoPlanet.getSurfaceTemperature());
        snapshot.put("tempCalculated", exoPlanet.getTempCalculated());
        snapshot.put("axialTilt", exoPlanet.getAxialTilt());
        snapshot.put("dayLength", exoPlanet.getDayLength());
        snapshot.put("gasGiant", exoPlanet.getGasGiant());
        snapshot.put("starAge", exoPlanet.getStarAge());
        return toJson(snapshot);
    }

    public static String createOverridesSnapshot(PlanetConfig config) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("size", deriveSize(config.n()).name());
        snapshot.put("plateCount", config.plateCount());
        snapshot.put("waterFraction", config.waterFraction());
        snapshot.put("oceanicPlateRatio", config.oceanicPlateRatio());
        snapshot.put("heightScaleMultiplier", config.heightScaleMultiplier());
        snapshot.put("riftDepthMultiplier", config.riftDepthMultiplier());
        snapshot.put("hotspotProbability", config.hotspotProbability());
        snapshot.put("enableActiveTectonics", config.enableActiveTectonics());
        snapshot.put("erosionIterations", config.erosionIterations());
        snapshot.put("rainfallScale", config.rainfallScale());
        snapshot.put("enableRivers", config.enableRivers());
        snapshot.put("useContinuousHeights", config.useContinuousHeights());
        snapshot.put("continuousReliefMin", config.continuousReliefMin());
        snapshot.put("continuousReliefMax", config.continuousReliefMax());
        snapshot.put("rainfallThreshold", config.rainfallThreshold());
        snapshot.put("riverSourceThreshold", config.riverSourceThreshold());
        snapshot.put("riverSourceElevationMin", config.riverSourceElevationMin());
        snapshot.put("erosionCap", config.erosionCap());
        snapshot.put("depositionFactor", config.depositionFactor());
        snapshot.put("riverCarveDepth", config.riverCarveDepth());
        snapshot.put("climateModel", config.climateModel().name());
        snapshot.put("axialTiltDegrees", config.axialTiltDegrees());
        snapshot.put("seasonalOffsetDegrees", config.seasonalOffsetDegrees());
        snapshot.put("seasonalSamples", config.seasonalSamples());
        return toJson(snapshot);
    }

    public static byte[] createPreview(PlanetGenerator.GeneratedPlanet planet, int size) {
        if (planet == null) {
            return null;
        }

        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            final byte[][] result = new byte[1][];
            Platform.runLater(() -> {
                result[0] = createPreviewOnFxThread(planet, size);
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return result[0];
        }

        return createPreviewOnFxThread(planet, size);
    }

    private static byte[] createPreviewOnFxThread(PlanetGenerator.GeneratedPlanet planet, int size) {
        try {
            TriangleMesh mesh = JavaFxPlanetMeshConverter.convert(
                planet.polygons(), planet.heights(), 1.0);
            MeshView meshView = new MeshView(mesh);
            meshView.setCullFace(CullFace.BACK);
            meshView.setMaterial(JavaFxPlanetMeshConverter.createTerrainMaterial(
                planet.polygons(), planet.heights()));
            meshView.getTransforms().addAll(
                new Rotate(25, Rotate.X_AXIS),
                new Rotate(25, Rotate.Y_AXIS)
            );

            Group root = new Group(meshView);
            SubScene subScene = new SubScene(root, size, size, true, SceneAntialiasing.BALANCED);
            subScene.setFill(Color.rgb(10, 10, 25));

            PerspectiveCamera camera = new PerspectiveCamera(true);
            camera.setNearClip(0.01);
            camera.setFarClip(100);
            camera.setTranslateZ(-3.0);
            subScene.setCamera(camera);

            AmbientLight ambientLight = new AmbientLight(Color.rgb(80, 80, 90));
            PointLight sun = new PointLight(Color.rgb(255, 250, 240));
            sun.setTranslateX(4);
            sun.setTranslateY(-3);
            sun.setTranslateZ(-6);
            root.getChildren().addAll(ambientLight, sun);

            WritableImage image = subScene.snapshot(null, null);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output);
            return output.toByteArray();
        } catch (Exception e) {
            log.warn("Failed to generate procedural preview image", e);
            return null;
        }
    }

    private static long deriveSeed(ExoPlanet exoPlanet) {
        return exoPlanet.getId() != null ? exoPlanet.getId().hashCode() : System.nanoTime();
    }

    private static PlanetConfig buildConfigFromPhysicals(SnapshotInput input, long seed) {
        final double earthRadiusKm = 6371.0;

        double radiusKm = (input.radius != null && input.radius > 0)
            ? input.radius * earthRadiusKm
            : earthRadiusKm;

        double waterFraction = input.hydrosphere != null
            ? input.hydrosphere
            : 0.66;

        if (waterFraction > 1.0) {
            waterFraction /= 100.0;
        }
        waterFraction = Math.min(1.0, Math.max(0.0, waterFraction));

        double mass = (input.mass != null && input.mass > 0) ? input.mass : 1.0;
        double gravity = (input.surfaceGravity != null && input.surfaceGravity > 0)
            ? input.surfaceGravity : 1.0;

        int plateCount;
        if (mass < 0.3) {
            plateCount = 5;
        } else if (mass < 0.7) {
            plateCount = 8;
        } else if (mass < 1.5) {
            plateCount = 12;
        } else if (mass < 3.0) {
            plateCount = 16;
        } else {
            plateCount = 10;
        }

        double oceanicRatio = 0.5 + (waterFraction * 0.35);
        oceanicRatio = Math.min(0.85, Math.max(0.3, oceanicRatio));

        double heightMultiplier = 1.0 / Math.sqrt(gravity);
        heightMultiplier = Math.min(2.0, Math.max(0.5, heightMultiplier));
        if (waterFraction > 0.5) {
            heightMultiplier *= 0.9;
        }

        double riftMultiplier = 1.0 / Math.sqrt(gravity);
        if (waterFraction > 0.3) {
            riftMultiplier *= 1.0 + (waterFraction * 0.3);
        }
        riftMultiplier = Math.min(2.0, Math.max(0.5, riftMultiplier));

        double hotspotProb = 0.12;
        if (mass > 1.5) {
            hotspotProb *= 1.3;
        } else if (mass < 0.5) {
            hotspotProb *= 0.6;
        }
        hotspotProb = Math.min(0.4, Math.max(0.02, hotspotProb));

        boolean activeTectonics = mass >= 0.2 && waterFraction >= 0.05;

        Double surfTemp = input.surfaceTemperature != null
            ? input.surfaceTemperature
            : input.tempCalculated;
        if (surfTemp != null) {
            if (surfTemp > 700 && waterFraction < 0.01) {
                activeTectonics = false;
            }
            if (surfTemp < 150) {
                activeTectonics = false;
            }
        }

        PlanetConfig.Builder builder = PlanetConfig.builder()
            .seed(seed)
            .fromAccreteRadius(radiusKm)
            .waterFraction(waterFraction)
            .plateCount(plateCount)
            .oceanicPlateRatio(oceanicRatio)
            .heightScaleMultiplier(heightMultiplier)
            .riftDepthMultiplier(riftMultiplier)
            .hotspotProbability(hotspotProb)
            .enableActiveTectonics(activeTectonics);

        if (input.gasGiant != null && input.gasGiant) {
            builder.enableActiveTectonics(false)
                .plateCount(1)
                .heightScaleMultiplier(0.2)
                .riftDepthMultiplier(0.2)
                .hotspotProbability(0.0);
        }

        return builder.build();
    }

    private static PlanetConfig applyOverrides(PlanetConfig base, String overridesJson) {
        if (base == null || overridesJson == null || overridesJson.isBlank()) {
            return base;
        }

        Map<String, Object> overrides = parseMap(overridesJson);
        if (overrides == null || overrides.isEmpty()) {
            return base;
        }

        PlanetConfig.Builder builder = base.toBuilder();

        String sizeValue = getString(overrides, "size");
        if (sizeValue != null) {
            try {
                builder.size(PlanetConfig.Size.valueOf(sizeValue));
            } catch (IllegalArgumentException ignored) {
                // ignore unknown sizes
            }
        }

        Integer plateCount = getInt(overrides, "plateCount");
        if (plateCount != null) {
            builder.plateCount(plateCount);
        }
        Double waterFraction = getDouble(overrides, "waterFraction");
        if (waterFraction != null) {
            builder.waterFraction(waterFraction);
        }
        Double oceanicRatio = getDouble(overrides, "oceanicPlateRatio");
        if (oceanicRatio != null) {
            builder.oceanicPlateRatio(oceanicRatio);
        }
        Double heightScale = getDouble(overrides, "heightScaleMultiplier");
        if (heightScale != null) {
            builder.heightScaleMultiplier(heightScale);
        }
        Double riftDepth = getDouble(overrides, "riftDepthMultiplier");
        if (riftDepth != null) {
            builder.riftDepthMultiplier(riftDepth);
        }
        Double hotspot = getDouble(overrides, "hotspotProbability");
        if (hotspot != null) {
            builder.hotspotProbability(hotspot);
        }
        Boolean activeTectonics = getBoolean(overrides, "enableActiveTectonics");
        if (activeTectonics != null) {
            builder.enableActiveTectonics(activeTectonics);
        }

        Integer erosionIterations = getInt(overrides, "erosionIterations");
        if (erosionIterations != null) {
            builder.erosionIterations(erosionIterations);
        }
        Double rainfallScale = getDouble(overrides, "rainfallScale");
        if (rainfallScale != null) {
            builder.rainfallScale(rainfallScale);
        }
        Boolean enableRivers = getBoolean(overrides, "enableRivers");
        if (enableRivers != null) {
            builder.enableRivers(enableRivers);
        }

        Boolean useContinuous = getBoolean(overrides, "useContinuousHeights");
        if (useContinuous != null) {
            builder.useContinuousHeights(useContinuous);
        }
        Double reliefMin = getDouble(overrides, "continuousReliefMin");
        if (reliefMin != null) {
            builder.continuousReliefMin(reliefMin);
        }
        Double reliefMax = getDouble(overrides, "continuousReliefMax");
        if (reliefMax != null) {
            builder.continuousReliefMax(reliefMax);
        }

        Double rainfallThreshold = getDouble(overrides, "rainfallThreshold");
        if (rainfallThreshold != null) {
            builder.rainfallThreshold(rainfallThreshold);
        }
        Double riverSourceThreshold = getDouble(overrides, "riverSourceThreshold");
        if (riverSourceThreshold != null) {
            builder.riverSourceThreshold(riverSourceThreshold);
        }
        Double riverSourceElevationMin = getDouble(overrides, "riverSourceElevationMin");
        if (riverSourceElevationMin != null) {
            builder.riverSourceElevationMin(riverSourceElevationMin);
        }
        Double erosionCap = getDouble(overrides, "erosionCap");
        if (erosionCap != null) {
            builder.erosionCap(erosionCap);
        }
        Double depositionFactor = getDouble(overrides, "depositionFactor");
        if (depositionFactor != null) {
            builder.depositionFactor(depositionFactor);
        }
        Double riverCarveDepth = getDouble(overrides, "riverCarveDepth");
        if (riverCarveDepth != null) {
            builder.riverCarveDepth(riverCarveDepth);
        }

        String climateModel = getString(overrides, "climateModel");
        if (climateModel != null) {
            try {
                builder.climateModel(ClimateCalculator.ClimateModel.valueOf(climateModel));
            } catch (IllegalArgumentException ignored) {
                // ignore invalid values
            }
        }
        Double axialTilt = getDouble(overrides, "axialTiltDegrees");
        if (axialTilt != null) {
            builder.axialTiltDegrees(axialTilt);
        }
        Double seasonalOffset = getDouble(overrides, "seasonalOffsetDegrees");
        if (seasonalOffset != null) {
            builder.seasonalOffsetDegrees(seasonalOffset);
        }
        Integer seasonalSamples = getInt(overrides, "seasonalSamples");
        if (seasonalSamples != null) {
            builder.seasonalSamples(seasonalSamples);
        }

        return builder.build();
    }

    private static PlanetConfig.Size deriveSize(int n) {
        for (PlanetConfig.Size size : PlanetConfig.Size.values()) {
            if (size.n == n) {
                return size;
            }
        }
        PlanetConfig.Size closest = PlanetConfig.Size.STANDARD;
        int minDiff = Integer.MAX_VALUE;
        for (PlanetConfig.Size size : PlanetConfig.Size.values()) {
            int diff = Math.abs(size.n - n);
            if (diff < minDiff) {
                minDiff = diff;
                closest = size;
            }
        }
        return closest;
    }

    private static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize procedural metadata", e);
            return null;
        }
    }

    private static Map<String, Object> parseMap(String json) {
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse procedural metadata JSON", e);
            return null;
        }
    }

    private static Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Integer getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.parseBoolean(text);
        }
        return null;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private record SnapshotInput(
        Double mass,
        Double radius,
        Double surfaceGravity,
        Double hydrosphere,
        Double surfaceTemperature,
        Double tempCalculated,
        Boolean gasGiant
    ) {
        static SnapshotInput from(ExoPlanet exoPlanet, String json) {
            if (json == null || json.isBlank()) {
                return fromExo(exoPlanet);
            }
            Map<String, Object> map = parseMap(json);
            if (map == null || map.isEmpty()) {
                return fromExo(exoPlanet);
            }
            return new SnapshotInput(
                getDouble(map, "mass"),
                getDouble(map, "radius"),
                getDouble(map, "surfaceGravity"),
                getDouble(map, "hydrosphere"),
                getDouble(map, "surfaceTemperature"),
                getDouble(map, "tempCalculated"),
                getBoolean(map, "gasGiant")
            );
        }

        private static SnapshotInput fromExo(ExoPlanet exoPlanet) {
            if (exoPlanet == null) {
                return new SnapshotInput(null, null, null, null, null, null, null);
            }
            return new SnapshotInput(
                exoPlanet.getMass(),
                exoPlanet.getRadius(),
                exoPlanet.getSurfaceGravity(),
                exoPlanet.getHydrosphere(),
                exoPlanet.getSurfaceTemperature(),
                exoPlanet.getTempCalculated(),
                exoPlanet.getGasGiant()
            );
        }
    }
}
