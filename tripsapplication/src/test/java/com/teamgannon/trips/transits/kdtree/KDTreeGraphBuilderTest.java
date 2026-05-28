package com.teamgannon.trips.transits.kdtree;

import com.teamgannon.trips.routing.model.SparseStarRecord;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KDTreeGraphBuilderTest {

    @Test
    void parallelBuilderMatchesSequentialBuilderWithoutDuplicateEdges() {
        List<SparseStarRecord> stars = createLineStars(600);
        KDTreeGraphBuilder sequentialBuilder = new KDTreeGraphBuilder(false);
        KDTreeGraphBuilder parallelBuilder = new KDTreeGraphBuilder(true);

        Graph<String, DefaultEdge> sequential = sequentialBuilder.buildGraphFromSparse(stars, 0.5, 1.1);
        Graph<String, DefaultEdge> parallel = parallelBuilder.buildGraphFromSparse(stars, 0.5, 1.1);

        assertEquals(sequential.vertexSet(), parallel.vertexSet());
        assertEquals(sequential.edgeSet().size(), parallel.edgeSet().size());
        assertEquals(599, parallel.edgeSet().size());

        for (int i = 0; i < 599; i++) {
            assertTrue(parallel.containsEdge("star-" + i, "star-" + (i + 1)));
            assertEquals(1.0, parallel.getEdgeWeight(parallel.getEdge("star-" + i, "star-" + (i + 1))), 0.0001);
        }
    }

    @Test
    void builderCreatesOnlyOneEdgePerMatchingStarPair() {
        List<SparseStarRecord> stars = List.of(
                createStar("star-0", 0.0, 0.0, 0.0),
                createStar("star-1", 1.0, 0.0, 0.0),
                createStar("star-2", 0.0, 1.0, 0.0),
                createStar("star-3", 0.0, 0.0, 1.0)
        );

        Graph<String, DefaultEdge> graph = new KDTreeGraphBuilder(false).buildGraphFromSparse(stars, 0.0, 2.0);

        assertEquals(Set.of("star-0", "star-1", "star-2", "star-3"), graph.vertexSet());
        assertEquals(6, graph.edgeSet().size());
    }

    private List<SparseStarRecord> createLineStars(int count) {
        List<SparseStarRecord> stars = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            stars.add(createStar("star-" + i, i, 0.0, 0.0));
        }
        return stars;
    }

    private SparseStarRecord createStar(String name, double x, double y, double z) {
        SparseStarRecord record = new SparseStarRecord();
        record.setRecordId(name);
        record.setStarName(name);
        record.setActualCoordinates(new double[]{x, y, z});
        return record;
    }
}
