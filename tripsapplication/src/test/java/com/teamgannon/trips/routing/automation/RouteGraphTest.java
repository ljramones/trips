package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.routing.model.SparseTransit;
import com.teamgannon.trips.transits.TransitRoute;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RouteGraph.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Graph construction</li>
 *   <li>Building graph from TransitRoute list</li>
 *   <li>Building graph from SparseTransit list</li>
 *   <li>Connectivity checking</li>
 *   <li>K-shortest path finding</li>
 *   <li>Edge weight retrieval</li>
 * </ul>
 */
class RouteGraphTest {

    private RouteGraph routeGraph;

    @BeforeEach
    void setUp() {
        routeGraph = new RouteGraph();
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private StarDisplayRecord createStarDisplayRecord(String name) {
        StarDisplayRecord record = new StarDisplayRecord();
        record.setStarName(name);
        return record;
    }

    private TransitRoute createTransitRoute(String sourceName, String targetName, double distance) {
        return TransitRoute.builder()
                .source(createStarDisplayRecord(sourceName))
                .target(createStarDisplayRecord(targetName))
                .distance(distance)
                .good(true)
                .build();
    }

    private SparseStarRecord createSparseStarRecord(String id, String name) {
        SparseStarRecord record = new SparseStarRecord();
        record.setRecordId(id);
        record.setStarName(name);
        return record;
    }

    private SparseTransit createSparseTransit(String sourceId, String sourceName,
                                               String targetId, String targetName,
                                               double distance) {
        return SparseTransit.builder()
                .source(createSparseStarRecord(sourceId, sourceName))
                .target(createSparseStarRecord(targetId, targetName))
                .distance(distance)
                .build();
    }

    // =========================================================================
    // Construction Tests
    // =========================================================================

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("New RouteGraph has empty graph")
        void newRouteGraphHasEmptyGraph() {
            assertNotNull(routeGraph.getRoutingGraph());
            assertEquals(0, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(0, routeGraph.getRoutingGraph().edgeSet().size());
        }
    }

    // =========================================================================
    // Calculate Graph For Transit Tests
    // =========================================================================

    @Nested
    @DisplayName("calculateGraphForTransit Tests")
    class CalculateGraphForTransitTests {

        @Test
        @DisplayName("Empty transit list creates no vertices or edges")
        void emptyTransitListCreatesEmptyGraph() {
            routeGraph.calculateGraphForTransit(Collections.emptyList());

            assertEquals(0, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(0, routeGraph.getRoutingGraph().edgeSet().size());
        }

        @Test
        @DisplayName("Single transit creates two vertices and one edge")
        void singleTransitCreatesTwoVerticesAndOneEdge() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37)
            );

            routeGraph.calculateGraphForTransit(transits);

            assertEquals(2, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(1, routeGraph.getRoutingGraph().edgeSet().size());
            assertTrue(routeGraph.getRoutingGraph().containsVertex("Sol"));
            assertTrue(routeGraph.getRoutingGraph().containsVertex("Alpha Centauri"));
        }

        @Test
        @DisplayName("Multiple transits create connected graph")
        void multipleTransitsCreateConnectedGraph() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37),
                    createTransitRoute("Alpha Centauri", "Barnard's Star", 3.8),
                    createTransitRoute("Barnard's Star", "Wolf 359", 5.0)
            );

            routeGraph.calculateGraphForTransit(transits);

            assertEquals(4, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(3, routeGraph.getRoutingGraph().edgeSet().size());
        }

        @Test
        @DisplayName("Connectivity inspector is initialized after calculateGraphForTransit")
        void connectivityInspectorIsInitialized() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37)
            );

            routeGraph.calculateGraphForTransit(transits);

            assertNotNull(routeGraph.getConnectivityInspector());
        }

        @Test
        @DisplayName("K-shortest paths calculator is initialized after calculateGraphForTransit")
        void kShortestPathsCalculatorIsInitialized() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37)
            );

            routeGraph.calculateGraphForTransit(transits);

            assertNotNull(routeGraph.getKShortedPaths());
        }
    }

    // =========================================================================
    // Calculate Graph For Sparse Transits Tests
    // =========================================================================

    @Nested
    @DisplayName("calculateGraphForSparseTransits Tests")
    class CalculateGraphForSparseTransitsTests {

        @Test
        @DisplayName("Empty sparse transit list returns true")
        void emptySparseTransitListReturnsTrue() {
            boolean result = routeGraph.calculateGraphForSparseTransits(Collections.emptyList());

            assertTrue(result);
            assertEquals(0, routeGraph.getRoutingGraph().vertexSet().size());
        }

        @Test
        @DisplayName("Single sparse transit creates two vertices and one edge")
        void singleSparseTransitCreatesTwoVerticesAndOneEdge() {
            List<SparseTransit> transits = List.of(
                    createSparseTransit("1", "Sol", "2", "Alpha Centauri", 4.37)
            );

            boolean result = routeGraph.calculateGraphForSparseTransits(transits);

            assertTrue(result);
            assertEquals(2, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(1, routeGraph.getRoutingGraph().edgeSet().size());
        }

        @Test
        @DisplayName("Self-loop transit is skipped")
        void selfLoopTransitIsSkipped() {
            List<SparseTransit> transits = List.of(
                    createSparseTransit("1", "Sol", "1", "Sol", 0)  // Self-loop
            );

            boolean result = routeGraph.calculateGraphForSparseTransits(transits);

            assertTrue(result);
            // Self-loop should be skipped, but vertex might still be added
            assertEquals(0, routeGraph.getRoutingGraph().edgeSet().size());
        }

        @Test
        @DisplayName("Mix of valid and self-loop transits processes valid ones")
        void mixOfValidAndSelfLoopTransitsProcessesValidOnes() {
            List<SparseTransit> transits = List.of(
                    createSparseTransit("1", "Sol", "2", "Alpha Centauri", 4.37),
                    createSparseTransit("2", "Alpha Centauri", "2", "Alpha Centauri", 0),  // Self-loop
                    createSparseTransit("2", "Alpha Centauri", "3", "Barnard's Star", 3.8)
            );

            boolean result = routeGraph.calculateGraphForSparseTransits(transits);

            assertTrue(result);
            assertEquals(3, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(2, routeGraph.getRoutingGraph().edgeSet().size());
        }
    }

    // =========================================================================
    // Connectivity Tests
    // =========================================================================

    @Nested
    @DisplayName("isConnected Tests")
    class IsConnectedTests {

        @Test
        @DisplayName("Directly connected stars are connected")
        void directlyConnectedStarsAreConnected() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37)
            );
            routeGraph.calculateGraphForTransit(transits);

            assertTrue(routeGraph.isConnected("Sol", "Alpha Centauri"));
            assertTrue(routeGraph.isConnected("Alpha Centauri", "Sol"));  // Bidirectional
        }

        @Test
        @DisplayName("Indirectly connected stars are connected")
        void indirectlyConnectedStarsAreConnected() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37),
                    createTransitRoute("Alpha Centauri", "Barnard's Star", 3.8)
            );
            routeGraph.calculateGraphForTransit(transits);

            assertTrue(routeGraph.isConnected("Sol", "Barnard's Star"));
        }

        @Test
        @DisplayName("Disconnected stars are not connected")
        void disconnectedStarsAreNotConnected() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37),
                    createTransitRoute("Proxima", "Barnard's Star", 3.8)  // Separate component
            );
            routeGraph.calculateGraphForTransit(transits);

            assertFalse(routeGraph.isConnected("Sol", "Barnard's Star"));
            assertFalse(routeGraph.isConnected("Alpha Centauri", "Proxima"));
        }
    }

    // =========================================================================
    // Find K Shortest Paths Tests
    // =========================================================================

    @Nested
    @DisplayName("findKShortestPaths Tests")
    class FindKShortestPathsTests {

        @Test
        @DisplayName("Finds direct path between connected stars")
        void findsDirectPathBetweenConnectedStars() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "AlphaCentauri", 4.37)
            );
            routeGraph.calculateGraphForTransit(transits);

            List<String> paths = routeGraph.findKShortestPaths("Sol", "AlphaCentauri", 1);

            assertFalse(paths.isEmpty());
            // JGraphT path format is like: [(Sol : AlphaCentauri)]
            String path = paths.get(0);
            assertNotNull(path);
        }

        @Test
        @DisplayName("Finds path through intermediate star")
        void findsPathThroughIntermediateStar() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "AlphaCentauri", 4.37),
                    createTransitRoute("AlphaCentauri", "BarnardsStar", 3.8)
            );
            routeGraph.calculateGraphForTransit(transits);

            List<String> paths = routeGraph.findKShortestPaths("Sol", "BarnardsStar", 1);

            assertFalse(paths.isEmpty());
        }

        @Test
        @DisplayName("Returns multiple paths when they exist")
        void returnsMultiplePathsWhenTheyExist() {
            // Create a diamond graph: Sol -> A -> Dest, Sol -> B -> Dest
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "RouteA", 2.0),
                    createTransitRoute("RouteA", "Dest", 2.0),
                    createTransitRoute("Sol", "RouteB", 3.0),
                    createTransitRoute("RouteB", "Dest", 3.0)
            );
            routeGraph.calculateGraphForTransit(transits);

            List<String> paths = routeGraph.findKShortestPaths("Sol", "Dest", 2);

            assertEquals(2, paths.size());
        }

        @Test
        @DisplayName("Throws when destination not in graph")
        void throwsWhenDestinationNotInGraph() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "AlphaCentauri", 4.37)
            );
            routeGraph.calculateGraphForTransit(transits);

            // Barnard's Star is not in the graph - JGraphT throws IllegalArgumentException
            assertThrows(IllegalArgumentException.class, () ->
                    routeGraph.findKShortestPaths("Sol", "BarnardsStar", 1));
        }
    }

    // =========================================================================
    // Find Edges Tests
    // =========================================================================

    @Nested
    @DisplayName("findEdges Tests")
    class FindEdgesTests {

        @Test
        @DisplayName("Returns edge weight for existing edge")
        void returnsEdgeWeightForExistingEdge() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37)
            );
            routeGraph.calculateGraphForTransit(transits);

            Double weight = routeGraph.findEdges("Sol", "Alpha Centauri");

            assertNotNull(weight);
            assertEquals(4.37, weight, 0.01);
        }

        @Test
        @DisplayName("Returns null for non-existing edge")
        void returnsNullForNonExistingEdge() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37)
            );
            routeGraph.calculateGraphForTransit(transits);

            Double weight = routeGraph.findEdges("Sol", "Barnard's Star");

            assertNull(weight);
        }

        @Test
        @DisplayName("Handles whitespace in from parameter")
        void handlesWhitespaceInFromParameter() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "Alpha Centauri", 4.37)
            );
            routeGraph.calculateGraphForTransit(transits);

            // Note: findEdges trims 'from' but not 'to'
            Double weight = routeGraph.findEdges(" Sol ", "Alpha Centauri");

            assertNotNull(weight);
            assertEquals(4.37, weight, 0.01);
        }
    }

    // =========================================================================
    // Edge Cases Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Duplicate edges are handled gracefully")
        void duplicateEdgesAreHandledGracefully() {
            List<TransitRoute> transits = List.of(
                    createTransitRoute("Sol", "AlphaCentauri", 4.37),
                    createTransitRoute("Sol", "AlphaCentauri", 5.0)  // Duplicate - should be skipped
            );

            // Should not throw - duplicate edges are now handled gracefully
            assertDoesNotThrow(() -> routeGraph.calculateGraphForTransit(transits));

            // Should still have only one edge (first one wins)
            assertEquals(2, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(1, routeGraph.getRoutingGraph().edgeSet().size());

            // Weight should be from first transit (4.37)
            Double weight = routeGraph.findEdges("Sol", "AlphaCentauri");
            assertEquals(4.37, weight, 0.01);
        }

        @Test
        @DisplayName("Large graph can be created")
        void largeGraphCanBeCreated() {
            List<TransitRoute> transits = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                transits.add(createTransitRoute("Star" + i, "Star" + (i + 1), i * 0.5));
            }

            routeGraph.calculateGraphForTransit(transits);

            assertEquals(101, routeGraph.getRoutingGraph().vertexSet().size());
            assertEquals(100, routeGraph.getRoutingGraph().edgeSet().size());
            assertTrue(routeGraph.isConnected("Star0", "Star100"));
        }
    }
}
