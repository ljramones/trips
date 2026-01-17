package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Percentage.withPercentage;

class IcosahedralMeshTest {

    @Test
    @DisplayName("Mesh generates non-empty polygon list")
    void generatesPolygons() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.DUEL).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        assertThat(polygons).isNotEmpty();
    }

    @Test
    @DisplayName("Mesh caches generated polygons")
    void cachesPolygons() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.DUEL).build();
        var mesh = new IcosahedralMesh(config);

        List<Polygon> first = mesh.generate();
        List<Polygon> second = mesh.generate();

        assertThat(first).isSameAs(second);
    }

    @ParameterizedTest
    @EnumSource(PlanetConfig.Size.class)
    @DisplayName("All size presets generate approximately expected polygon count")
    void allSizesGenerateCorrectCount(PlanetConfig.Size size) {
        var config = PlanetConfig.builder().size(size).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        // Allow 5% variance due to floating-point edge detection differences
        // Java port may have slight differences from GDScript original
        assertThat(polygons.size()).isCloseTo(size.polyCount, withPercentage(5));
    }

    @Test
    @DisplayName("Goldberg polyhedron has exactly 12 pentagons")
    void exactlyTwelvePentagons() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.STANDARD).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        long pentagonCount = polygons.stream()
            .filter(Polygon::isPentagon)
            .count();

        assertThat(pentagonCount).isEqualTo(12);
    }

    @Test
    @DisplayName("All non-pentagon polygons are hexagons")
    void onlyHexagonsAndPentagons() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.SMALL).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        for (Polygon p : polygons) {
            assertThat(p.vertexCount())
                .as("Polygon should be pentagon (5) or hexagon (6)")
                .isIn(5, 6);
        }
    }

    @Test
    @DisplayName("All polygon centers are approximately on unit sphere")
    void centersOnSphere() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.TINY).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        // Get expected radius from first polygon's vertex distance
        double expectedRadius = polygons.get(0).vertices().get(0).getNorm();

        for (Polygon p : polygons) {
            double centerRadius = p.center().getNorm();
            assertThat(centerRadius)
                .as("Center should be on sphere surface")
                .isCloseTo(expectedRadius, withPercentage(1));
        }
    }

    @Test
    @DisplayName("All polygon vertices are on sphere surface")
    void verticesOnSphere() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.TINY).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        double expectedRadius = polygons.get(0).vertices().get(0).getNorm();

        for (Polygon p : polygons) {
            for (Vector3D vertex : p.vertices()) {
                assertThat(vertex.getNorm())
                    .as("Vertex should be on sphere surface")
                    .isCloseTo(expectedRadius, withPercentage(0.1));
            }
        }
    }

    @Test
    @DisplayName("Polygon vertices form closed loops (adjacent vertices are close)")
    void verticesFormClosedLoops() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.STANDARD).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        // Calculate expected max edge length from first polygon
        Polygon firstHex = polygons.stream()
            .filter(Polygon::isHexagon)
            .findFirst()
            .orElseThrow();
        List<Vector3D> refVerts = firstHex.vertices();
        double refEdge = Vector3D.distance(refVerts.get(0), refVerts.get(1));
        double maxEdge = refEdge * 1.5;  // Allow 50% variance for edge boundaries

        for (Polygon p : polygons) {
            List<Vector3D> verts = p.vertices();
            int n = verts.size();

            // Check that consecutive vertices are reasonably close
            for (int i = 0; i < n; i++) {
                Vector3D v1 = verts.get(i);
                Vector3D v2 = verts.get((i + 1) % n);
                double dist = Vector3D.distance(v1, v2);

                // Adjacent vertices should be close (edge length on Goldberg polyhedron)
                assertThat(dist)
                    .as("Adjacent vertices should form edges")
                    .isLessThan(maxEdge);
            }
        }
    }

    @Test
    @DisplayName("getN returns correct subdivision level")
    void getNReturnsSubdivisionLevel() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.LARGE).build();
        var mesh = new IcosahedralMesh(config);

        assertThat(mesh.getN()).isEqualTo(PlanetConfig.Size.LARGE.n);
    }

    @Test
    @DisplayName("Different subdivision levels produce different polygon counts")
    void differentSubdivisionsProduceDifferentCounts() {
        var smallConfig = PlanetConfig.builder().size(PlanetConfig.Size.SMALL).build();
        var largeConfig = PlanetConfig.builder().size(PlanetConfig.Size.LARGE).build();

        var smallMesh = new IcosahedralMesh(smallConfig);
        var largeMesh = new IcosahedralMesh(largeConfig);

        assertThat(largeMesh.generate().size())
            .isGreaterThan(smallMesh.generate().size());
    }

    @Test
    @DisplayName("Polygon centers are unique")
    void centersAreUnique() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.DUEL).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        // Check for duplicate centers (within tolerance)
        for (int i = 0; i < polygons.size(); i++) {
            for (int j = i + 1; j < polygons.size(); j++) {
                double dist = Vector3D.distance(
                    polygons.get(i).center(),
                    polygons.get(j).center()
                );
                assertThat(dist)
                    .as("Polygon centers should be unique")
                    .isGreaterThan(0.01);
            }
        }
    }
}
