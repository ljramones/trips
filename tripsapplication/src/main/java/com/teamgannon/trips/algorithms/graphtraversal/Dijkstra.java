package com.teamgannon.trips.algorithms.graphtraversal;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

public class Dijkstra {

    public static Graph calculateShortestPathFromSource(Graph graph, Vertex source) {

        source.setDistance(0);

        Set<Vertex> settledVertices = new HashSet<>();
        Set<Vertex> unsettledVertices = new HashSet<>();
        unsettledVertices.add(source);

        while (unsettledVertices.size() != 0) {
            Vertex currentVertex = getLowestDistanceNode(unsettledVertices);
            unsettledVertices.remove(currentVertex);
            for (Entry<Vertex, Integer> adjacencyPair : currentVertex.getAdjacentNodes().entrySet()) {
                Vertex adjacentVertex = adjacencyPair.getKey();
                Integer edgeWeigh = adjacencyPair.getValue();

                if (!settledVertices.contains(adjacentVertex)) {
                    calculateMinimumDistance(adjacentVertex, edgeWeigh, currentVertex);
                    unsettledVertices.add(adjacentVertex);
                }
            }
            settledVertices.add(currentVertex);
        }
        return graph;
    }

    private static void calculateMinimumDistance(Vertex evaluationVertex, Integer edgeWeigh, Vertex sourceVertex) {
        Integer sourceDistance = sourceVertex.getDistance();
        if (sourceDistance + edgeWeigh < evaluationVertex.getDistance()) {
            evaluationVertex.setDistance(sourceDistance + edgeWeigh);
            LinkedList<Vertex> shortestPath = new LinkedList<>(sourceVertex.getShortestPath());
            shortestPath.add(sourceVertex);
            evaluationVertex.setShortestPath(shortestPath);
        }
    }

    private static Vertex getLowestDistanceNode(Set<Vertex> unsettledVertices) {
        Vertex lowestDistanceVertex = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Vertex vertex : unsettledVertices) {
            int nodeDistance = vertex.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceVertex = vertex;
            }
        }
        return lowestDistanceVertex;
    }
}
