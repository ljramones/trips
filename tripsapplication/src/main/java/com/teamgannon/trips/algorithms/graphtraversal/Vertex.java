package com.teamgannon.trips.algorithms.graphtraversal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Vertex {

    private String name;

    private LinkedList<Vertex> shortestPath = new LinkedList<>();

    private Integer distance = Integer.MAX_VALUE;

    private Map<Vertex, Integer> adjacentNodes = new HashMap<>();

    public Vertex(String name) {
        this.name = name;
    }

    public void addDestination(Vertex destination, int distance) {
        adjacentNodes.put(destination, distance);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Vertex, Integer> getAdjacentNodes() {
        return adjacentNodes;
    }

    public void setAdjacentNodes(Map<Vertex, Integer> adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public List<Vertex> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(LinkedList<Vertex> shortestPath) {
        this.shortestPath = shortestPath;
    }

}
