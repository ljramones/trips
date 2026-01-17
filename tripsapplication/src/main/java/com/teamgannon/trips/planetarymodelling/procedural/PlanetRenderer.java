package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
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

    private final List<Polygon> polygons;
    private final int[] heights;
    private final double scale;

    public PlanetRenderer(List<Polygon> polygons, int[] heights, double scale) {
        this.polygons = polygons;
        this.heights = heights;
        this.scale = scale;
    }

    public PlanetRenderer(List<Polygon> polygons, int[] heights) {
        this(polygons, heights, 1.0);
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
}
