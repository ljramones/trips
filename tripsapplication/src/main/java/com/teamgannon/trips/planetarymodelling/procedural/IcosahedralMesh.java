package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a Goldberg polyhedron (subdivided icosahedron) of hexagons and pentagons.
 */
public class IcosahedralMesh {

    /**
     * Epsilon tolerance for floating-point vertex comparisons.
     * Set to 1e-10 which is much smaller than the vertex spacing on even
     * the finest mesh (n=32 has ~10242 polygons, vertex spacing ~0.01).
     */
    private static final double EPSILON = 1e-10;

    private final int n;
    private final Vector3D[] icoVertices;
    private List<Polygon> polygons;

    /**
     * Compares two Vector3D points for equality within epsilon tolerance.
     * Direct .equals() on floating-point vectors can fail due to rounding errors.
     */
    private static boolean vectorsEqual(Vector3D v1, Vector3D v2) {
        return v1.distance(v2) < EPSILON;
    }

    public IcosahedralMesh(PlanetConfig config) {
        this.n = config.n();
        this.icoVertices = createTiltedIcosahedron();
    }

    private Vector3D[] createTiltedIcosahedron() {
        double a = 1.0;
        double b = (Math.sqrt(5) - 1) / 2;
        double side = a + b;

        Vector3D[] ico = {
            new Vector3D(0, side / 2, 0.5),
            new Vector3D(-0.5, 0, side / 2),
            new Vector3D(0.5, 0, side / 2),
            new Vector3D(side / 2, 0.5, 0),
            new Vector3D(0, side / 2, -0.5),
            new Vector3D(-side / 2, 0.5, 0),
            new Vector3D(0, -side / 2, -0.5),
            new Vector3D(0, -side / 2, 0.5),
            new Vector3D(side / 2, -0.5, 0),
            new Vector3D(0.5, 0, -side / 2),
            new Vector3D(-0.5, 0, -side / 2),
            new Vector3D(-side / 2, -0.5, 0)
        };

        double tilt = -Math.atan(1.0 / side);
        Rotation rotation = new Rotation(Vector3D.PLUS_I, tilt, null);

        Vector3D[] tilted = new Vector3D[12];
        for (int i = 0; i < 12; i++) {
            tilted[i] = rotation.applyTo(ico[i]);
        }
        return tilted;
    }

    private List<Vector3D> pointLine(Vector3D v1, Vector3D v2, int segments) {
        List<Vector3D> points = new ArrayList<>();
        if (segments == 0) {
            points.add(v1);
            return points;
        }
        Vector3D step = v2.subtract(v1).scalarMultiply(1.0 / segments);
        for (int i = 0; i <= segments; i++) {
            points.add(v1.add(step.scalarMultiply(i)));
        }
        return points;
    }

    private List<Vector3D> threeLines(Vector3D v1, Vector3D tip, Vector3D v2, int segments) {
        List<Vector3D> points = new ArrayList<>();
        if (segments == 0) {
            points.add(tip);
            return points;
        }
        Vector3D baseVec = v2.subtract(v1).scalarMultiply(1.0 / (2.0 * segments));
        Vector3D midpoint = v1.add(v2.subtract(v1).scalarMultiply(0.5));
        Vector3D planeVec = tip.subtract(midpoint).scalarMultiply(1.0 / (segments * 3.0));

        for (int i = 0; i < 3; i++) {
            Vector3D start = v1.add(baseVec.scalarMultiply(i)).add(planeVec.scalarMultiply(i));
            Vector3D end = v2.subtract(baseVec.scalarMultiply(i)).add(planeVec.scalarMultiply(i));
            points.addAll(pointLine(start, end, segments - i));
        }
        return points;
    }

    private List<Vector3D> hexGrid(Vector3D v1, Vector3D tip, Vector3D v2, int segments) {
        List<Vector3D> points = new ArrayList<>();
        Vector3D baseVec = v2.subtract(v1).scalarMultiply(1.0 / (2.0 * segments));
        Vector3D midpoint = v1.add(v2.subtract(v1).scalarMultiply(0.5));
        Vector3D planeVec = tip.subtract(midpoint).scalarMultiply(1.0 / segments);

        for (int i = 0; i <= segments; i++) {
            Vector3D start = v1.add(baseVec.scalarMultiply(i)).add(planeVec.scalarMultiply(i));
            Vector3D end = v2.subtract(baseVec.scalarMultiply(i)).add(planeVec.scalarMultiply(i));
            points.addAll(threeLines(start, tip, end, segments - i));
        }
        return points;
    }

    private Polygon orderHex(int x, int y, List<Vector3D> grid, int segments) {
        int center = (int) (3 * y * (segments - ((y - 1) / 2.0)) + x);
        int offset = segments - y;

        Vector3D centerPt = grid.get(center);
        List<Vector3D> verts = List.of(
            grid.get(center + offset * 2),
            grid.get(center + offset + 1),
            grid.get(center - offset),
            grid.get(center - offset * 2 - 1),
            grid.get(center - offset - 1),
            grid.get(center + offset)
        );
        return new Polygon(centerPt, verts);
    }

    private List<Polygon> orderInnerHex(List<Vector3D> grid, int segments) {
        List<Polygon> hexes = new ArrayList<>();
        int row = 0;
        for (int y = 1; y < segments - 1; y++) {
            for (int x = 1; x < segments - 1 - row; x++) {
                hexes.add(orderHex(x, y, grid, segments));
            }
            row++;
        }
        return hexes;
    }

    private List<List<Vector3D>> gridedFaces() {
        Vector3D[] v = icoVertices;
        List<List<Vector3D>> faces = new ArrayList<>();

        faces.add(hexGrid(v[1], v[0], v[2], n));
        faces.add(hexGrid(v[2], v[0], v[3], n));
        faces.add(hexGrid(v[3], v[0], v[4], n));
        faces.add(hexGrid(v[4], v[0], v[5], n));
        faces.add(hexGrid(v[5], v[0], v[1], n));
        faces.add(hexGrid(v[8], v[6], v[7], n));
        faces.add(hexGrid(v[9], v[6], v[8], n));
        faces.add(hexGrid(v[10], v[6], v[9], n));
        faces.add(hexGrid(v[11], v[6], v[10], n));
        faces.add(hexGrid(v[7], v[6], v[11], n));
        faces.add(hexGrid(v[3], v[8], v[2], n));
        faces.add(hexGrid(v[4], v[9], v[3], n));
        faces.add(hexGrid(v[5], v[10], v[4], n));
        faces.add(hexGrid(v[1], v[11], v[5], n));
        faces.add(hexGrid(v[2], v[7], v[1], n));
        faces.add(hexGrid(v[7], v[2], v[8], n));
        faces.add(hexGrid(v[8], v[3], v[9], n));
        faces.add(hexGrid(v[9], v[4], v[10], n));
        faces.add(hexGrid(v[10], v[5], v[11], n));
        faces.add(hexGrid(v[11], v[1], v[7], n));

        return faces;
    }

    private List<Polygon> pentagons(List<List<Vector3D>> faces) {
        Vector3D[] v = icoVertices;
        int tip = faces.get(0).size() - 2;
        int b1 = n + 1;
        int b2 = 2 * n;

        List<Polygon> pentas = new ArrayList<>();

        pentas.add(new Polygon(v[0], List.of(
            faces.get(4).get(tip), faces.get(3).get(tip), faces.get(2).get(tip),
            faces.get(1).get(tip), faces.get(0).get(tip)
        )));
        pentas.add(new Polygon(v[1], List.of(
            faces.get(4).get(b2), faces.get(0).get(b1), faces.get(14).get(b2),
            faces.get(19).get(tip), faces.get(13).get(b1)
        )));
        pentas.add(new Polygon(v[2], List.of(
            faces.get(0).get(b2), faces.get(1).get(b1), faces.get(10).get(b2),
            faces.get(15).get(tip), faces.get(14).get(b1)
        )));
        pentas.add(new Polygon(v[3], List.of(
            faces.get(1).get(b2), faces.get(2).get(b1), faces.get(11).get(b2),
            faces.get(16).get(tip), faces.get(10).get(b1)
        )));
        pentas.add(new Polygon(v[4], List.of(
            faces.get(2).get(b2), faces.get(3).get(b1), faces.get(12).get(b2),
            faces.get(17).get(tip), faces.get(11).get(b1)
        )));
        pentas.add(new Polygon(v[5], List.of(
            faces.get(3).get(b2), faces.get(4).get(b1), faces.get(13).get(b2),
            faces.get(18).get(tip), faces.get(12).get(b1)
        )));
        pentas.add(new Polygon(v[6], List.of(
            faces.get(5).get(tip), faces.get(6).get(tip), faces.get(7).get(tip),
            faces.get(8).get(tip), faces.get(9).get(tip)
        )));
        pentas.add(new Polygon(v[7], List.of(
            faces.get(14).get(tip), faces.get(15).get(b1), faces.get(5).get(b2),
            faces.get(9).get(b1), faces.get(19).get(b2)
        )));
        pentas.add(new Polygon(v[8], List.of(
            faces.get(10).get(tip), faces.get(16).get(b1), faces.get(6).get(b2),
            faces.get(5).get(b1), faces.get(15).get(b2)
        )));
        pentas.add(new Polygon(v[9], List.of(
            faces.get(11).get(tip), faces.get(17).get(b1), faces.get(7).get(b2),
            faces.get(6).get(b1), faces.get(16).get(b2)
        )));
        pentas.add(new Polygon(v[10], List.of(
            faces.get(12).get(tip), faces.get(18).get(b1), faces.get(8).get(b2),
            faces.get(7).get(b1), faces.get(17).get(b2)
        )));
        pentas.add(new Polygon(v[11], List.of(
            faces.get(13).get(tip), faces.get(19).get(b1), faces.get(9).get(b2),
            faces.get(8).get(b1), faces.get(18).get(b2)
        )));

        return pentas;
    }

    private Polygon hexOnLineTips(int y, List<Vector3D> leftFace, List<Vector3D> rightFace) {
        int rightCenter = (int) (3 * y * (n - ((y - 1) / 2.0)));
        int leftCenter = rightCenter + (n - y);

        Vector3D center = rightFace.get(rightCenter);
        List<Vector3D> verts = List.of(
            rightFace.get(rightCenter + n - y + 1),
            rightFace.get(rightCenter - (n - y)),
            rightFace.get(rightCenter - (n - y) * 2 - 1),
            leftFace.get(leftCenter - (n - y) * 2 - 1),
            leftFace.get(leftCenter - (n - y + 1)),
            leftFace.get(leftCenter + (n - y))
        );
        return new Polygon(center, verts);
    }

    private Polygon hexOnLineBottoms(int x, List<Vector3D> topFace, List<Vector3D> bottomFace) {
        int topCenter = x;
        int bottomCenter = n - x;

        Vector3D center = topFace.get(topCenter);
        List<Vector3D> verts = List.of(
            topFace.get(topCenter + n),
            topFace.get(topCenter + n * 2),
            topFace.get(topCenter + n + 1),
            bottomFace.get(bottomCenter + n),
            bottomFace.get(bottomCenter + n * 2),
            bottomFace.get(bottomCenter + n + 1)
        );
        return new Polygon(center, verts);
    }

    private Polygon hexOnLineSidesClockwise(int y, List<Vector3D> leftFace, List<Vector3D> rightFace) {
        int leftCenter = (int) (3 * (n - y) * (n - ((n - y - 1) / 2.0)));
        int rightCenter = (int) (3 * y * (n - ((y - 1) / 2.0)));

        Vector3D center = rightFace.get(rightCenter);
        List<Vector3D> verts = List.of(
            rightFace.get(rightCenter + n - y + 1),
            rightFace.get(rightCenter - (n - y)),
            rightFace.get(rightCenter - (n - y) * 2 - 1),
            leftFace.get(leftCenter + y + 1),
            leftFace.get(leftCenter - y),
            leftFace.get(leftCenter - y * 2 - 1)
        );
        return new Polygon(center, verts);
    }

    private Polygon hexOnLineSidesAnticlockwise(int y, List<Vector3D> leftFace, List<Vector3D> rightFace) {
        int leftCenter = (int) (3 * y * (n - ((y - 1) / 2.0)) + (n - y));
        int rightCenter = (int) (3 * (n - y) * (n - (((n - y) - 1) / 2.0)) + y);

        Vector3D center = rightFace.get(rightCenter);
        List<Vector3D> verts = List.of(
            rightFace.get(rightCenter - y * 2 - 1),
            rightFace.get(rightCenter - y - 1),
            rightFace.get(rightCenter + y),
            leftFace.get(leftCenter - (n - y) * 2 - 1),
            leftFace.get(leftCenter - (n - y) - 1),
            leftFace.get(leftCenter + (n - y))
        );
        return new Polygon(center, verts);
    }

    private List<Polygon> lineHexagons(List<Vector3D> face1, List<Vector3D> face2) {
        List<Polygon> hexes = new ArrayList<>();
        int lastIdx = face1.size() - 1;

        // Use epsilon-tolerant comparison for floating-point vertices
        if (vectorsEqual(face1.get(lastIdx), face2.get(lastIdx))) {
            for (int y = 1; y < n; y++) {
                hexes.add(hexOnLineTips(y, face1, face2));
            }
        } else if (vectorsEqual(face1.get(0), face2.get(n))) {
            for (int x = 1; x < n; x++) {
                hexes.add(hexOnLineBottoms(x, face1, face2));
            }
        } else if (vectorsEqual(face1.get(0), face2.get(lastIdx))) {
            for (int y = 1; y < n; y++) {
                hexes.add(hexOnLineSidesClockwise(y, face1, face2));
            }
        } else if (vectorsEqual(face1.get(lastIdx), face2.get(n))) {
            for (int y = 1; y < n; y++) {
                hexes.add(hexOnLineSidesAnticlockwise(y, face1, face2));
            }
        }
        return hexes;
    }

    private List<Polygon> edges(List<List<Vector3D>> faces) {
        List<Polygon> edgeHexes = new ArrayList<>();

        edgeHexes.addAll(lineHexagons(faces.get(0), faces.get(1)));
        edgeHexes.addAll(lineHexagons(faces.get(1), faces.get(2)));
        edgeHexes.addAll(lineHexagons(faces.get(2), faces.get(3)));
        edgeHexes.addAll(lineHexagons(faces.get(3), faces.get(4)));
        edgeHexes.addAll(lineHexagons(faces.get(4), faces.get(0)));
        edgeHexes.addAll(lineHexagons(faces.get(0), faces.get(14)));
        edgeHexes.addAll(lineHexagons(faces.get(1), faces.get(10)));
        edgeHexes.addAll(lineHexagons(faces.get(2), faces.get(11)));
        edgeHexes.addAll(lineHexagons(faces.get(3), faces.get(12)));
        edgeHexes.addAll(lineHexagons(faces.get(4), faces.get(13)));
        edgeHexes.addAll(lineHexagons(faces.get(9), faces.get(8)));
        edgeHexes.addAll(lineHexagons(faces.get(8), faces.get(7)));
        edgeHexes.addAll(lineHexagons(faces.get(7), faces.get(6)));
        edgeHexes.addAll(lineHexagons(faces.get(6), faces.get(5)));
        edgeHexes.addAll(lineHexagons(faces.get(5), faces.get(9)));
        edgeHexes.addAll(lineHexagons(faces.get(9), faces.get(19)));
        edgeHexes.addAll(lineHexagons(faces.get(8), faces.get(18)));
        edgeHexes.addAll(lineHexagons(faces.get(7), faces.get(17)));
        edgeHexes.addAll(lineHexagons(faces.get(6), faces.get(16)));
        edgeHexes.addAll(lineHexagons(faces.get(5), faces.get(15)));
        edgeHexes.addAll(lineHexagons(faces.get(19), faces.get(13)));
        edgeHexes.addAll(lineHexagons(faces.get(18), faces.get(12)));
        edgeHexes.addAll(lineHexagons(faces.get(17), faces.get(11)));
        edgeHexes.addAll(lineHexagons(faces.get(16), faces.get(10)));
        edgeHexes.addAll(lineHexagons(faces.get(15), faces.get(14)));
        edgeHexes.addAll(lineHexagons(faces.get(14), faces.get(19)));
        edgeHexes.addAll(lineHexagons(faces.get(10), faces.get(15)));
        edgeHexes.addAll(lineHexagons(faces.get(11), faces.get(16)));
        edgeHexes.addAll(lineHexagons(faces.get(12), faces.get(17)));
        edgeHexes.addAll(lineHexagons(faces.get(13), faces.get(18)));

        return edgeHexes;
    }

    private List<Polygon> projectToSphere(List<Polygon> polys) {
        if (polys.isEmpty()) return polys;
        Polygon first = polys.get(0);
        double theta = Vector3D.angle(first.vertices().get(0), first.vertices().get(1));
        double r = Math.sqrt(1.0 / (2.0 * (1.0 - Math.cos(theta))));

        List<Polygon> projected = new ArrayList<>();
        for (Polygon p : polys) {
            Vector3D projCenter = p.center().normalize().scalarMultiply(r);
            List<Vector3D> projVerts = p.vertices().stream()
                .map(v -> v.normalize().scalarMultiply(r))
                .toList();
            projected.add(new Polygon(projCenter, projVerts));
        }
        return projected;
    }

    private List<Polygon> adjustCenters(List<Polygon> polys) {
        List<Polygon> adjusted = new ArrayList<>();
        for (Polygon p : polys) {
            Vector3D refVert = p.vertices().get(0);
            double dot = refVert.dotProduct(p.center());
            double c = dot / p.center().getNormSq();
            Vector3D newCenter = p.center().scalarMultiply(c);
            adjusted.add(new Polygon(newCenter, p.vertices()));
        }
        return adjusted;
    }

    public List<Polygon> generate() {
        if (polygons != null) {
            return polygons;
        }

        List<List<Vector3D>> faces = gridedFaces();

        List<Polygon> allPolys = new ArrayList<>();
        allPolys.addAll(pentagons(faces));

        for (List<Vector3D> face : faces) {
            allPolys.addAll(orderInnerHex(face, n));
        }

        allPolys.addAll(edges(faces));

        allPolys = projectToSphere(allPolys);
        allPolys = adjustCenters(allPolys);

        this.polygons = allPolys;
        return polygons;
    }

    public int getN() {
        return n;
    }
}
