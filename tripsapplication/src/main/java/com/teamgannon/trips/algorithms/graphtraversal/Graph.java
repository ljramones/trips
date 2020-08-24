package com.teamgannon.trips.algorithms.graphtraversal;

import java.util.HashSet;
import java.util.Set;

public class Graph {

    private Set<Vertex> vertices = new HashSet<>();

    public void addNode(Vertex vertexA) {
        vertices.add(vertexA);
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(Set<Vertex> vertices) {
        this.vertices = vertices;
    }
}
