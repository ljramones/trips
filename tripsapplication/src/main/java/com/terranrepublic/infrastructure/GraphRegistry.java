package com.terranrepublic.infrastructure;

import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable lookup and validation registry for transport graph references.
 */
public record GraphRegistry(
        Map<String, TransportNode> nodesById,
        Map<String, Conduit> conduitsById
) {

    public GraphRegistry {
        nodesById = copyNodes(nodesById == null ? List.of() : nodesById.values());
        conduitsById = copyConduits(conduitsById == null ? List.of() : conduitsById.values());
    }

    public static GraphRegistry of(Collection<TransportNode> nodes, Collection<Conduit> conduits) {
        return new GraphRegistry(copyNodes(nodes), copyConduits(conduits));
    }

    public static GraphRegistry ofNodes(Collection<TransportNode> nodes) {
        return of(nodes, List.of());
    }

    public List<String> validate() {
        ArrayList<String> errors = new ArrayList<>();
        for (TransportNode node : nodesById.values()) {
            for (String connectedId : node.connectedNodeIds()) {
                if (!nodesById.containsKey(connectedId)) {
                    errors.add("TransportNode " + node.id() + " has dangling connectedNodeId " + connectedId);
                }
            }
        }
        for (Conduit conduit : conduitsById.values()) {
            if (!nodesById.containsKey(conduit.fromNodeId())) {
                errors.add("Conduit " + conduit.id() + " has dangling fromNodeId " + conduit.fromNodeId());
            }
            if (!nodesById.containsKey(conduit.toNodeId())) {
                errors.add("Conduit " + conduit.id() + " has dangling toNodeId " + conduit.toNodeId());
            }
        }
        return List.copyOf(errors);
    }

    public void requireValid() {
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join("; ", errors));
        }
    }

    private static Map<String, TransportNode> copyNodes(Collection<TransportNode> nodes) {
        LinkedHashMap<String, TransportNode> copy = new LinkedHashMap<>();
        if (nodes != null) {
            for (TransportNode node : nodes) {
                if (node == null) {
                    throw new IllegalArgumentException("GraphRegistry cannot contain null nodes");
                }
                if (copy.put(node.id(), node) != null) {
                    throw new IllegalArgumentException("Duplicate TransportNode id: " + node.id());
                }
            }
        }
        return Map.copyOf(copy);
    }

    private static Map<String, Conduit> copyConduits(Collection<Conduit> conduits) {
        LinkedHashMap<String, Conduit> copy = new LinkedHashMap<>();
        if (conduits != null) {
            for (Conduit conduit : conduits) {
                if (conduit == null) {
                    throw new IllegalArgumentException("GraphRegistry cannot contain null conduits");
                }
                if (copy.put(conduit.id(), conduit) != null) {
                    throw new IllegalArgumentException("Duplicate Conduit id: " + conduit.id());
                }
            }
        }
        return Map.copyOf(copy);
    }
}
