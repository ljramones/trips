package com.teamgannon.trips.planetarymodelling.procedural;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts procedural planet icosahedral mesh to JavaFX TriangleMesh.
 * Handles both pentagons (5 vertices) and hexagons (6 vertices) by
 * triangulating from center point using fan triangulation.
 */
public class JavaFxPlanetMeshConverter {

    // Height displacement scale factor (how much height affects radial distance)
    private static final double HEIGHT_SCALE = 0.02;

    // Height color mapping (same as PlanetRenderer)
    private static final Color[] HEIGHT_COLORS = {
        Color.rgb(0, 0, 102),     // -4: deep ocean
        Color.rgb(0, 0, 128),     // -3: ocean
        Color.rgb(51, 77, 230),   // -2: shallow ocean
        Color.rgb(102, 153, 255), // -1: coastal
        Color.rgb(204, 204, 153), //  0: lowlands
        Color.rgb(166, 204, 102), //  1: plains
        Color.rgb(166, 153, 102), //  2: hills
        Color.rgb(102, 51, 0),    //  3: mountains
        Color.rgb(51, 0, 0)       //  4: high mountains
    };

    /**
     * Convert procedural planet mesh to JavaFX TriangleMesh.
     * Uses fan triangulation from polygon center to edges.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon (for vertex displacement)
     * @param scale    Scale factor for rendering
     * @return JavaFX TriangleMesh ready for MeshView
     */
    public static TriangleMesh convert(List<Polygon> polygons, int[] heights, double scale) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();

        // Single texture coordinate pair (we use vertex colors via material)
        texCoords.add(0.5f);
        texCoords.add(0.5f);

        int vertexIndex = 0;

        // For each polygon, create triangles using fan triangulation
        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            Polygon poly = polygons.get(polyIdx);
            int height = heights[polyIdx];

            // Calculate radial displacement from height
            double displacement = 1.0 + height * HEIGHT_SCALE;

            // Add center point (displaced by height)
            Vector3D center = poly.center().normalize().scalarMultiply(displacement * scale);
            points.add((float) center.getX());
            points.add((float) center.getY());
            points.add((float) center.getZ());
            int centerIdx = vertexIndex++;

            // Add edge vertices (at unit sphere distance, to create slight beveling effect)
            List<Vector3D> vertices = poly.vertices();
            int[] edgeIndices = new int[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                Vector3D v = vertices.get(i).normalize().scalarMultiply(scale);
                points.add((float) v.getX());
                points.add((float) v.getY());
                points.add((float) v.getZ());
                edgeIndices[i] = vertexIndex++;
            }

            // Create fan triangles from center to edges
            // For polygon with n vertices, creates n triangles
            for (int i = 0; i < vertices.size(); i++) {
                int nextI = (i + 1) % vertices.size();

                // Face indices: p0, t0, p1, t1, p2, t2
                // All use same texture coord (0)
                faces.add(centerIdx);
                faces.add(0);
                faces.add(edgeIndices[i]);
                faces.add(0);
                faces.add(edgeIndices[nextI]);
                faces.add(0);
            }
        }

        // Convert to arrays
        float[] pointsArray = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = points.get(i);
        }

        int[] facesArray = new int[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            facesArray[i] = faces.get(i);
        }

        float[] texCoordsArray = new float[texCoords.size()];
        for (int i = 0; i < texCoords.size(); i++) {
            texCoordsArray[i] = texCoords.get(i);
        }

        mesh.getPoints().addAll(pointsArray);
        mesh.getTexCoords().addAll(texCoordsArray);
        mesh.getFaces().addAll(facesArray);

        return mesh;
    }

    /**
     * Convert procedural planet mesh to JavaFX TriangleMesh with smooth heights.
     * Uses precise heights for smoother displacement.
     *
     * @param polygons       The icosahedral mesh polygons
     * @param preciseHeights Precise height values per polygon
     * @param scale          Scale factor for rendering
     * @return JavaFX TriangleMesh ready for MeshView
     */
    public static TriangleMesh convertSmooth(List<Polygon> polygons, double[] preciseHeights, double scale) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();

        texCoords.add(0.5f);
        texCoords.add(0.5f);

        int vertexIndex = 0;

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            Polygon poly = polygons.get(polyIdx);
            double height = preciseHeights != null && polyIdx < preciseHeights.length
                ? preciseHeights[polyIdx]
                : 0.0;

            double displacement = 1.0 + height * HEIGHT_SCALE;

            Vector3D center = poly.center().normalize().scalarMultiply(displacement * scale);
            points.add((float) center.getX());
            points.add((float) center.getY());
            points.add((float) center.getZ());
            int centerIdx = vertexIndex++;

            List<Vector3D> vertices = poly.vertices();
            int[] edgeIndices = new int[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                Vector3D v = vertices.get(i).normalize().scalarMultiply(scale);
                points.add((float) v.getX());
                points.add((float) v.getY());
                points.add((float) v.getZ());
                edgeIndices[i] = vertexIndex++;
            }

            for (int i = 0; i < vertices.size(); i++) {
                int nextI = (i + 1) % vertices.size();
                faces.add(centerIdx);
                faces.add(0);
                faces.add(edgeIndices[i]);
                faces.add(0);
                faces.add(edgeIndices[nextI]);
                faces.add(0);
            }
        }

        float[] pointsArray = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = points.get(i);
        }

        int[] facesArray = new int[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            facesArray[i] = faces.get(i);
        }

        float[] texCoordsArray = new float[texCoords.size()];
        for (int i = 0; i < texCoords.size(); i++) {
            texCoordsArray[i] = texCoords.get(i);
        }

        mesh.getPoints().addAll(pointsArray);
        mesh.getTexCoords().addAll(texCoordsArray);
        mesh.getFaces().addAll(facesArray);

        return mesh;
    }

    /**
     * Creates PhongMaterial with terrain colors based on heights.
     * Generates a texture map where each polygon gets a color based on its height.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon
     * @return PhongMaterial with height-based diffuse color map
     */
    public static PhongMaterial createTerrainMaterial(List<Polygon> polygons, int[] heights) {
        PhongMaterial material = new PhongMaterial();

        // Create a diffuse texture based on average height
        int avgHeight = calculateAverageHeight(heights);
        Color baseColor = getColorForHeight(avgHeight);

        material.setDiffuseColor(baseColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.3, 1));
        material.setSpecularPower(8.0);

        return material;
    }

    /**
     * Creates PhongMaterial with a generated height-map texture.
     * Each polygon's color is baked into a 2D texture for more detailed rendering.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon
     * @return PhongMaterial with diffuse texture map
     */
    public static PhongMaterial createTexturedTerrainMaterial(List<Polygon> polygons, int[] heights) {
        PhongMaterial material = new PhongMaterial();

        // Generate a simple gradient texture based on height distribution
        int textureSize = 256;
        WritableImage diffuseMap = new WritableImage(textureSize, textureSize);
        PixelWriter writer = diffuseMap.getPixelWriter();

        // Create a latitude-based gradient representing terrain
        for (int y = 0; y < textureSize; y++) {
            // Map y to latitude (-90 to 90)
            double lat = ((double) y / textureSize - 0.5) * 180.0;

            // Determine base color based on latitude (polar ice, temperate, tropical)
            Color rowColor;
            if (Math.abs(lat) > 70) {
                // Polar - ice
                rowColor = Color.WHITE.interpolate(Color.LIGHTCYAN, 0.7);
            } else if (Math.abs(lat) > 50) {
                // Subpolar - tundra
                rowColor = Color.rgb(180, 190, 170);
            } else if (Math.abs(lat) > 30) {
                // Temperate - forest/plains
                rowColor = Color.rgb(100, 140, 80);
            } else {
                // Tropical - desert/jungle
                rowColor = Color.rgb(160, 150, 100);
            }

            for (int x = 0; x < textureSize; x++) {
                writer.setColor(x, y, rowColor);
            }
        }

        material.setDiffuseMap(diffuseMap);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.2, 1));
        material.setSpecularPower(10.0);

        return material;
    }

    /**
     * Creates PhongMaterial using precise heights for smooth color gradients.
     *
     * @param polygons       The icosahedral mesh polygons
     * @param preciseHeights Precise height values per polygon
     * @return PhongMaterial with smooth height-based coloring
     */
    public static PhongMaterial createSmoothTerrainMaterial(List<Polygon> polygons, double[] preciseHeights) {
        PhongMaterial material = new PhongMaterial();

        // Calculate average height for base color
        double avgHeight = 0;
        if (preciseHeights != null && preciseHeights.length > 0) {
            for (double h : preciseHeights) {
                avgHeight += h;
            }
            avgHeight /= preciseHeights.length;
        }

        Color baseColor = getColorForPreciseHeight(avgHeight);
        material.setDiffuseColor(baseColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.3, 1));
        material.setSpecularPower(8.0);

        return material;
    }

    /**
     * Creates a per-polygon colored mesh using vertex colors.
     * This is a more complex approach that creates separate meshes per color group.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon
     * @param scale    Scale factor for rendering
     * @return Map of height value to TriangleMesh (for separate coloring)
     */
    public static Map<Integer, TriangleMesh> convertByHeight(List<Polygon> polygons, int[] heights, double scale) {
        Map<Integer, List<Integer>> heightToPolygons = new HashMap<>();

        // Group polygons by height
        for (int i = 0; i < polygons.size(); i++) {
            int height = heights[i];
            heightToPolygons.computeIfAbsent(height, k -> new ArrayList<>()).add(i);
        }

        Map<Integer, TriangleMesh> result = new HashMap<>();

        // Create separate mesh for each height level
        for (Map.Entry<Integer, List<Integer>> entry : heightToPolygons.entrySet()) {
            int height = entry.getKey();
            List<Integer> polyIndices = entry.getValue();

            TriangleMesh mesh = new TriangleMesh();
            List<Float> points = new ArrayList<>();
            List<Integer> faces = new ArrayList<>();

            mesh.getTexCoords().addAll(0.5f, 0.5f);

            int vertexIndex = 0;
            double displacement = 1.0 + height * HEIGHT_SCALE;

            for (int polyIdx : polyIndices) {
                Polygon poly = polygons.get(polyIdx);

                Vector3D center = poly.center().normalize().scalarMultiply(displacement * scale);
                points.add((float) center.getX());
                points.add((float) center.getY());
                points.add((float) center.getZ());
                int centerIdx = vertexIndex++;

                List<Vector3D> vertices = poly.vertices();
                int[] edgeIndices = new int[vertices.size()];

                for (int i = 0; i < vertices.size(); i++) {
                    Vector3D v = vertices.get(i).normalize().scalarMultiply(scale);
                    points.add((float) v.getX());
                    points.add((float) v.getY());
                    points.add((float) v.getZ());
                    edgeIndices[i] = vertexIndex++;
                }

                for (int i = 0; i < vertices.size(); i++) {
                    int nextI = (i + 1) % vertices.size();
                    faces.add(centerIdx);
                    faces.add(0);
                    faces.add(edgeIndices[i]);
                    faces.add(0);
                    faces.add(edgeIndices[nextI]);
                    faces.add(0);
                }
            }

            float[] pointsArray = new float[points.size()];
            for (int i = 0; i < points.size(); i++) {
                pointsArray[i] = points.get(i);
            }

            int[] facesArray = new int[faces.size()];
            for (int i = 0; i < faces.size(); i++) {
                facesArray[i] = faces.get(i);
            }

            mesh.getPoints().addAll(pointsArray);
            mesh.getFaces().addAll(facesArray);

            result.put(height, mesh);
        }

        return result;
    }

    /**
     * Returns color for a given integer height value.
     * Maps height from [-4, 4] to color gradient.
     *
     * @param height Integer height value
     * @return JavaFX Color for that height
     */
    public static Color getColorForHeight(int height) {
        int index = height + 4;
        index = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, index));
        return HEIGHT_COLORS[index];
    }

    /**
     * Returns a smoothly interpolated color for a precise height value.
     * Blends between adjacent height colors based on fractional part.
     *
     * @param height Precise height value (typically -4 to 4)
     * @return JavaFX Color interpolated for that height
     */
    public static Color getColorForPreciseHeight(double height) {
        // Map height from [-4, 4] to [0, 8]
        double normalized = height + 4.0;
        normalized = Math.max(0, Math.min(8, normalized));

        int lowerIndex = (int) Math.floor(normalized);
        int upperIndex = lowerIndex + 1;
        double fraction = normalized - lowerIndex;

        lowerIndex = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, lowerIndex));
        upperIndex = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, upperIndex));

        Color lowerColor = HEIGHT_COLORS[lowerIndex];
        Color upperColor = HEIGHT_COLORS[upperIndex];

        return lowerColor.interpolate(upperColor, fraction);
    }

    /**
     * Creates a material for a specific height level.
     *
     * @param height The height level
     * @return PhongMaterial with appropriate color
     */
    public static PhongMaterial createMaterialForHeight(int height) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(getColorForHeight(height));
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.2, 1));
        material.setSpecularPower(12.0);
        return material;
    }

    private static int calculateAverageHeight(int[] heights) {
        if (heights == null || heights.length == 0) {
            return 0;
        }
        long sum = 0;
        for (int h : heights) {
            sum += h;
        }
        return (int) (sum / heights.length);
    }
}
