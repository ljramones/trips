package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Composite;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders planet mesh using Jzy3d.
 */
public class PlanetRenderer {

    private static final Color[] HEIGHT_COLORS = {
        new Color(0.0f, 0.0f, 0.4f),   // -4: deep ocean
        new Color(0.0f, 0.0f, 0.5f),   // -3: ocean
        new Color(0.2f, 0.3f, 0.9f),   // -2: shallow ocean
        new Color(0.4f, 0.6f, 1.0f),   // -1: coastal
        new Color(0.8f, 0.8f, 0.6f),   //  0: lowlands
        new Color(0.65f, 0.8f, 0.4f),  //  1: plains
        new Color(0.65f, 0.6f, 0.4f),  //  2: hills
        new Color(0.4f, 0.2f, 0.0f),   //  3: mountains
        new Color(0.2f, 0.0f, 0.0f)    //  4: high mountains
    };

    // River colors - gradient from source (light blue) to mouth (darker blue)
    private static final Color RIVER_SOURCE_COLOR = new Color(0.3f, 0.7f, 1.0f);
    private static final Color RIVER_MOUTH_COLOR = new Color(0.1f, 0.3f, 0.8f);

    // Frozen river colors - gradient from source to frozen terminus (white/ice blue)
    private static final Color FROZEN_RIVER_SOURCE_COLOR = new Color(0.6f, 0.85f, 1.0f);
    private static final Color FROZEN_RIVER_TERMINUS_COLOR = new Color(0.9f, 0.95f, 1.0f);

    // Offset multiplier to raise rivers slightly above terrain surface
    private static final double RIVER_HEIGHT_OFFSET = 1.02;

    // Rainfall heat-map colors (dry to wet gradient)
    private static final Color RAINFALL_DRY = new Color(0.8f, 0.6f, 0.3f);    // Sandy brown (arid)
    private static final Color RAINFALL_LOW = new Color(0.9f, 0.85f, 0.5f);   // Yellow (semi-arid)
    private static final Color RAINFALL_MED = new Color(0.5f, 0.8f, 0.4f);    // Green (temperate)
    private static final Color RAINFALL_HIGH = new Color(0.2f, 0.6f, 0.9f);   // Blue (wet)
    private static final Color RAINFALL_EXTREME = new Color(0.1f, 0.3f, 0.7f); // Deep blue (tropical)

    private final List<Polygon> polygons;
    private final int[] heights;
    private final double[] preciseHeights;  // Optional high-precision heights
    private final double scale;

    public PlanetRenderer(List<Polygon> polygons, int[] heights, double scale) {
        this.polygons = polygons;
        this.heights = heights;
        this.preciseHeights = null;
        this.scale = scale;
    }

    public PlanetRenderer(List<Polygon> polygons, int[] heights) {
        this(polygons, heights, 1.0);
    }

    /**
     * Constructor with precise heights for smooth terrain rendering.
     */
    public PlanetRenderer(List<Polygon> polygons, int[] heights, double[] preciseHeights, double scale) {
        this.polygons = polygons;
        this.heights = heights;
        this.preciseHeights = preciseHeights;
        this.scale = scale;
    }

    public Shape createShape() {
        List<org.jzy3d.plot3d.primitives.Polygon> faces = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon poly = polygons.get(i);
            int height = heights[i];
            Color color = getColorForHeight(height);

            org.jzy3d.plot3d.primitives.Polygon face = createFace(poly, color);
            faces.add(face);
        }

        Shape shape = new Shape(faces);
        shape.setWireframeDisplayed(false);
        shape.setFaceDisplayed(true);
        return shape;
    }

    /**
     * Creates a shape using precise heights for smooth color gradients.
     * Falls back to integer heights if precise heights unavailable.
     */
    public Shape createSmoothShape() {
        if (preciseHeights == null || preciseHeights.length != polygons.size()) {
            return createShape();  // Fallback to integer heights
        }

        List<org.jzy3d.plot3d.primitives.Polygon> faces = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon poly = polygons.get(i);
            Color color = getColorForPreciseHeight(preciseHeights[i]);

            org.jzy3d.plot3d.primitives.Polygon face = createFace(poly, color);
            faces.add(face);
        }

        Shape shape = new Shape(faces);
        shape.setWireframeDisplayed(false);
        shape.setFaceDisplayed(true);
        return shape;
    }

    private org.jzy3d.plot3d.primitives.Polygon createFace(Polygon poly, Color color) {
        org.jzy3d.plot3d.primitives.Polygon face = new org.jzy3d.plot3d.primitives.Polygon();

        for (Vector3D vertex : poly.vertices()) {
            Coord3d coord = toCoord3d(vertex);
            face.add(new Point(coord, color));
        }

        face.setColor(color);
        face.setWireframeColor(new Color(color.r * 0.7f, color.g * 0.7f, color.b * 0.7f));
        return face;
    }

    private Coord3d toCoord3d(Vector3D v) {
        return new Coord3d(
            (float) (v.getX() * scale),
            (float) (v.getY() * scale),
            (float) (v.getZ() * scale)
        );
    }

    public static Color getColorForHeight(int height) {
        int index = height + 4;
        index = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, index));
        return HEIGHT_COLORS[index];
    }

    /**
     * Returns a smoothly interpolated color for a precise height value.
     * Blends between adjacent height colors based on fractional part.
     */
    public static Color getColorForPreciseHeight(double height) {
        // Map height from [-4, 4] to [0, 8]
        double normalized = height + 4.0;

        // Clamp to valid range
        normalized = Math.max(0, Math.min(8, normalized));

        // Get integer index and fractional part
        int lowerIndex = (int) Math.floor(normalized);
        int upperIndex = lowerIndex + 1;
        float fraction = (float) (normalized - lowerIndex);

        // Clamp indices
        lowerIndex = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, lowerIndex));
        upperIndex = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, upperIndex));

        // Interpolate between adjacent colors
        Color lowerColor = HEIGHT_COLORS[lowerIndex];
        Color upperColor = HEIGHT_COLORS[upperIndex];

        return interpolateColorStatic(lowerColor, upperColor, fraction);
    }

    public Shape createWireframeShape() {
        List<org.jzy3d.plot3d.primitives.Polygon> faces = new ArrayList<>();

        for (Polygon poly : polygons) {
            org.jzy3d.plot3d.primitives.Polygon face = new org.jzy3d.plot3d.primitives.Polygon();
            Color color = poly.isPentagon() ? Color.RED : Color.WHITE;

            for (Vector3D vertex : poly.vertices()) {
                face.add(new Point(toCoord3d(vertex), color));
            }

            face.setFaceDisplayed(false);
            face.setWireframeDisplayed(true);
            face.setWireframeColor(color);
            faces.add(face);
        }

        return new Shape(faces);
    }

    /**
     * Creates a composite shape containing both the planet surface and rivers.
     * Rivers are rendered as blue line strips slightly above the terrain.
     *
     * @param rivers List of river paths, where each path is a list of polygon indices
     * @return Composite shape with planet surface and rivers
     */
    public Composite createShapeWithRivers(List<List<Integer>> rivers) {
        return createShapeWithRivers(rivers, null);
    }

    /**
     * Creates a composite shape containing both the planet surface and rivers,
     * with frozen river visualization support.
     *
     * @param rivers List of river paths
     * @param frozenTerminus Array indicating which rivers end frozen (or null)
     * @return Composite shape with planet surface and rivers
     */
    public Composite createShapeWithRivers(List<List<Integer>> rivers, boolean[] frozenTerminus) {
        Composite composite = new Composite();

        // Add planet surface
        composite.add(createShape());

        // Add rivers
        if (rivers != null && !rivers.isEmpty()) {
            composite.add(createRiverShape(rivers, frozenTerminus));
        }

        return composite;
    }

    /**
     * Creates a shape containing only the river lines.
     * Each river is rendered as a LineStrip connecting polygon centers.
     *
     * @param rivers List of river paths, where each path is a list of polygon indices
     * @return Composite containing all river line strips
     */
    public Composite createRiverShape(List<List<Integer>> rivers) {
        return createRiverShape(rivers, null);
    }

    /**
     * Creates a shape containing river lines with frozen terminus support.
     * Frozen rivers (ending in polar zones) are rendered with ice-blue gradient.
     *
     * @param rivers List of river paths
     * @param frozenTerminus Array indicating which rivers end frozen (or null)
     * @return Composite containing all river line strips
     */
    public Composite createRiverShape(List<List<Integer>> rivers, boolean[] frozenTerminus) {
        Composite riverComposite = new Composite();

        for (int i = 0; i < rivers.size(); i++) {
            List<Integer> river = rivers.get(i);
            if (river.size() < 2) continue;

            boolean isFrozen = frozenTerminus != null && i < frozenTerminus.length && frozenTerminus[i];
            LineStrip line = createRiverLine(river, isFrozen);
            if (line != null) {
                riverComposite.add(line);
            }
        }

        return riverComposite;
    }

    /**
     * Creates a single river as a LineStrip.
     * Color gradients from light blue at source to darker blue at mouth.
     */
    private LineStrip createRiverLine(List<Integer> riverPath) {
        return createRiverLine(riverPath, false);
    }

    /**
     * Creates a single river as a LineStrip with frozen terminus support.
     * Frozen rivers use ice-blue gradient, flowing rivers use water-blue gradient.
     */
    private LineStrip createRiverLine(List<Integer> riverPath, boolean isFrozen) {
        LineStrip line = new LineStrip();
        line.setWireframeWidth(2.0f);

        int pathLength = riverPath.size();

        // Select colors based on frozen status
        Color sourceColor = isFrozen ? FROZEN_RIVER_SOURCE_COLOR : RIVER_SOURCE_COLOR;
        Color terminusColor = isFrozen ? FROZEN_RIVER_TERMINUS_COLOR : RIVER_MOUTH_COLOR;

        for (int i = 0; i < pathLength; i++) {
            int polyIdx = riverPath.get(i);
            if (polyIdx < 0 || polyIdx >= polygons.size()) continue;

            Vector3D center = polygons.get(polyIdx).center();

            // Raise river slightly above terrain to prevent z-fighting
            Vector3D raised = center.scalarMultiply(RIVER_HEIGHT_OFFSET);
            Coord3d coord = toCoord3d(raised);

            // Interpolate color from source to terminus
            float t = (float) i / Math.max(1, pathLength - 1);
            Color color = interpolateColor(sourceColor, terminusColor, t);

            line.add(new Point(coord, color));
        }

        return line;
    }

    /**
     * Linear interpolation between two colors.
     */
    private Color interpolateColor(Color c1, Color c2, float t) {
        return new Color(
            c1.r + (c2.r - c1.r) * t,
            c1.g + (c2.g - c1.g) * t,
            c1.b + (c2.b - c1.b) * t
        );
    }

    /**
     * Creates a heat-map visualization of rainfall distribution.
     * Colors range from brown (dry) through yellow/green to blue (wet).
     *
     * @param rainfall Array of rainfall values per polygon
     * @return Shape colored by rainfall intensity
     */
    public Shape createRainfallHeatMap(double[] rainfall) {
        if (rainfall == null || rainfall.length != polygons.size()) {
            return createShape();  // Fallback to regular render
        }

        // Find min/max for normalization
        double minRain = Double.MAX_VALUE;
        double maxRain = Double.MIN_VALUE;
        for (double r : rainfall) {
            if (r < minRain) minRain = r;
            if (r > maxRain) maxRain = r;
        }

        double range = maxRain - minRain;
        if (range < 0.001) range = 1.0;  // Avoid division by zero

        List<org.jzy3d.plot3d.primitives.Polygon> faces = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon poly = polygons.get(i);
            double normalized = (rainfall[i] - minRain) / range;
            Color color = getColorForRainfall(normalized);

            org.jzy3d.plot3d.primitives.Polygon face = createFace(poly, color);
            faces.add(face);
        }

        Shape shape = new Shape(faces);
        shape.setWireframeDisplayed(false);
        shape.setFaceDisplayed(true);
        return shape;
    }

    /**
     * Returns a color for a normalized rainfall value (0.0 = dry, 1.0 = wet).
     * Uses a 5-stop gradient: dry → low → medium → high → extreme.
     */
    public static Color getColorForRainfall(double normalizedRainfall) {
        float t = (float) Math.max(0, Math.min(1, normalizedRainfall));

        if (t < 0.25f) {
            // Dry to low (0.0 - 0.25)
            return interpolateColorStatic(RAINFALL_DRY, RAINFALL_LOW, t / 0.25f);
        } else if (t < 0.5f) {
            // Low to medium (0.25 - 0.5)
            return interpolateColorStatic(RAINFALL_LOW, RAINFALL_MED, (t - 0.25f) / 0.25f);
        } else if (t < 0.75f) {
            // Medium to high (0.5 - 0.75)
            return interpolateColorStatic(RAINFALL_MED, RAINFALL_HIGH, (t - 0.5f) / 0.25f);
        } else {
            // High to extreme (0.75 - 1.0)
            return interpolateColorStatic(RAINFALL_HIGH, RAINFALL_EXTREME, (t - 0.75f) / 0.25f);
        }
    }

    /**
     * Static version of color interpolation for use in static context.
     */
    private static Color interpolateColorStatic(Color c1, Color c2, float t) {
        return new Color(
            c1.r + (c2.r - c1.r) * t,
            c1.g + (c2.g - c1.g) * t,
            c1.b + (c2.b - c1.b) * t
        );
    }

    /**
     * Creates a debug visualization showing both terrain and rainfall.
     * Terrain is shown with reduced saturation, rainfall as color overlay.
     *
     * @param rainfall Array of rainfall values per polygon
     * @param blendFactor How much rainfall color to blend (0.0 = terrain only, 1.0 = rainfall only)
     * @return Shape with blended terrain/rainfall colors
     */
    public Shape createTerrainWithRainfallOverlay(double[] rainfall, double blendFactor) {
        if (rainfall == null || rainfall.length != polygons.size()) {
            return createShape();
        }

        // Find min/max for normalization
        double minRain = Double.MAX_VALUE;
        double maxRain = Double.MIN_VALUE;
        for (double r : rainfall) {
            if (r < minRain) minRain = r;
            if (r > maxRain) maxRain = r;
        }

        double range = maxRain - minRain;
        if (range < 0.001) range = 1.0;

        float blend = (float) Math.max(0, Math.min(1, blendFactor));

        List<org.jzy3d.plot3d.primitives.Polygon> faces = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon poly = polygons.get(i);

            // Get terrain color
            Color terrainColor = getColorForHeight(heights[i]);

            // Get rainfall color
            double normalized = (rainfall[i] - minRain) / range;
            Color rainfallColor = getColorForRainfall(normalized);

            // Blend colors
            Color blended = interpolateColorStatic(terrainColor, rainfallColor, blend);

            org.jzy3d.plot3d.primitives.Polygon face = createFace(poly, blended);
            faces.add(face);
        }

        Shape shape = new Shape(faces);
        shape.setWireframeDisplayed(false);
        shape.setFaceDisplayed(true);
        return shape;
    }
}
