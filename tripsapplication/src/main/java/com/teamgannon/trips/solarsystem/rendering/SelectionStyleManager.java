package com.teamgannon.trips.solarsystem.rendering;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages selection styling for solar system elements.
 * <p>
 * Handles visual highlighting of selected bodies (planets, stars) and orbits,
 * including scale changes, opacity adjustments, and glow effects.
 */
@Slf4j
public class SelectionStyleManager {

    private static final double DEEMPHASIZED_OPACITY = 0.6;
    private static final double ORBIT_DEEMPHASIZED_OPACITY = 1.0;
    private static final double LABEL_DEEMPHASIZED_OPACITY = 0.7;
    private static final double PLANET_SELECTED_SCALE = 1.25;
    private static final double STAR_SELECTED_SCALE = 1.15;

    private final Glow selectedBodyGlow = new Glow(0.6);

    @Getter
    private final Map<Node, Double> baseScales = new HashMap<>();

    @Getter
    private final Map<Node, Double> baseOpacities = new HashMap<>();

    private final Map<Node, double[]> orbitSegmentScales = new HashMap<>();
    private final Map<PhongMaterial, Color[]> orbitSegmentMaterials = new HashMap<>();

    private Node selectedBody;
    private Group selectedOrbit;

    /**
     * Register a node as selectable, storing its base scale and opacity.
     */
    public void registerSelectableNode(Node node) {
        if (node == null) {
            return;
        }
        baseScales.put(node, node.getScaleX());
        baseOpacities.put(node, node.getOpacity());
    }

    /**
     * Register orbit segments for scale/material tracking.
     */
    public void registerOrbitSegments(Group orbitGroup) {
        for (Node child : orbitGroup.getChildren()) {
            orbitSegmentScales.putIfAbsent(child, new double[]{
                    child.getScaleX(),
                    child.getScaleY(),
                    child.getScaleZ()
            });
            if (child instanceof Shape3D shape) {
                if (shape.getMaterial() instanceof PhongMaterial material) {
                    orbitSegmentMaterials.putIfAbsent(material, new Color[]{
                            material.getDiffuseColor(),
                            material.getSpecularColor()
                    });
                }
            }
        }
    }

    /**
     * Apply selection styling to nodes based on current selection.
     *
     * @param selectedBody  the selected body (planet or star), or null
     * @param selectedOrbit the selected orbit, or null
     * @param planetNodes   map of planet names to sphere nodes
     * @param starNodes     map of star names to their nodes
     * @param orbitGroups   map of planet names to orbit groups
     * @param shapeToLabel  map of 3D nodes to their labels
     */
    public void applySelection(Node selectedBody,
                               Group selectedOrbit,
                               Map<String, Sphere> planetNodes,
                               Map<String, Node> starNodes,
                               Map<String, Group> orbitGroups,
                               Map<Node, Label> shapeToLabel) {
        this.selectedBody = selectedBody;
        this.selectedOrbit = selectedOrbit;
        boolean hasSelection = selectedBody != null || selectedOrbit != null;

        // Style planets
        for (Map.Entry<String, Sphere> entry : planetNodes.entrySet()) {
            Node planet = entry.getValue();
            if (planet == null) {
                continue;
            }
            if (planet == selectedBody) {
                applySelectedBodyStyle(planet, PLANET_SELECTED_SCALE);
            } else if (hasSelection) {
                applyDeemphasizedStyle(planet, DEEMPHASIZED_OPACITY);
            } else {
                restoreBaseStyle(planet);
            }
        }

        // Style stars
        for (Map.Entry<String, Node> entry : starNodes.entrySet()) {
            Node star = entry.getValue();
            if (star == null) {
                continue;
            }
            if (star == selectedBody) {
                applySelectedBodyStyle(star, STAR_SELECTED_SCALE);
            } else if (hasSelection) {
                applyDeemphasizedStyle(star, 0.6);
            } else {
                restoreBaseStyle(star);
            }
        }

        // Style orbits
        for (Map.Entry<String, Group> entry : orbitGroups.entrySet()) {
            Group orbit = entry.getValue();
            if (orbit == null) {
                continue;
            }
            if (orbit == selectedOrbit) {
                applySelectedOrbitStyle(orbit);
            } else {
                restoreOrbitStyle(orbit);
            }
        }

        // Style labels
        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Label label = entry.getValue();
            if (label == null) {
                continue;
            }
            if (!hasSelection) {
                label.setOpacity(1.0);
            } else if (entry.getKey() == selectedBody) {
                label.setOpacity(1.0);
            } else {
                label.setOpacity(LABEL_DEEMPHASIZED_OPACITY);
            }
        }
    }

    /**
     * Clear selection state.
     */
    public void clearSelection() {
        this.selectedBody = null;
        this.selectedOrbit = null;
    }

    /**
     * Clear all cached style data.
     */
    public void clear() {
        baseScales.clear();
        baseOpacities.clear();
        orbitSegmentScales.clear();
        orbitSegmentMaterials.clear();
        selectedBody = null;
        selectedOrbit = null;
    }

    private void applySelectedBodyStyle(Node node, double scaleMultiplier) {
        restoreBaseScale(node);
        node.setScaleX(baseScales.getOrDefault(node, 1.0) * scaleMultiplier);
        node.setScaleY(baseScales.getOrDefault(node, 1.0) * scaleMultiplier);
        node.setScaleZ(baseScales.getOrDefault(node, 1.0) * scaleMultiplier);
        node.setOpacity(1.0);
        node.setEffect(selectedBodyGlow);
    }

    private void applySelectedOrbitStyle(Group orbit) {
        restoreOrbitStyle(orbit);
        orbit.setOpacity(1.0);
        OrbitMeshSelection meshSelection = getOrbitMeshSelection(orbit);
        if (meshSelection != null) {
            meshSelection.highlightMesh().setVisible(true);
            brightenOrbitMaterial(meshSelection.baseMesh());
            return;
        }
        for (Node child : orbit.getChildren()) {
            double[] baseScale = orbitSegmentScales.get(child);
            if (baseScale != null) {
                child.setScaleX(baseScale[0] * 1.6);
                child.setScaleY(baseScale[1]);
                child.setScaleZ(baseScale[2] * 1.6);
            }
            brightenOrbitMaterial(child);
        }
    }

    private void restoreOrbitStyle(Group orbit) {
        restoreBaseStyle(orbit);
        orbit.setOpacity(ORBIT_DEEMPHASIZED_OPACITY);
        OrbitMeshSelection meshSelection = getOrbitMeshSelection(orbit);
        if (meshSelection != null) {
            meshSelection.highlightMesh().setVisible(false);
            restoreOrbitMaterial(meshSelection.baseMesh());
            return;
        }
        for (Node child : orbit.getChildren()) {
            double[] baseScale = orbitSegmentScales.get(child);
            if (baseScale != null) {
                child.setScaleX(baseScale[0]);
                child.setScaleY(baseScale[1]);
                child.setScaleZ(baseScale[2]);
            }
            restoreOrbitMaterial(child);
        }
    }

    private void applyDeemphasizedStyle(Node node, double opacity) {
        restoreBaseScale(node);
        node.setOpacity(opacity);
        node.setEffect(null);
    }

    private void restoreBaseStyle(Node node) {
        restoreBaseScale(node);
        node.setOpacity(baseOpacities.getOrDefault(node, 1.0));
        node.setEffect(null);
    }

    private void restoreBaseScale(Node node) {
        double baseScale = baseScales.getOrDefault(node, 1.0);
        node.setScaleX(baseScale);
        node.setScaleY(baseScale);
        node.setScaleZ(baseScale);
    }

    private OrbitMeshSelection getOrbitMeshSelection(Group orbit) {
        Object baseNode = orbit.getProperties().get(OrbitVisualizer.ORBIT_BASE_MESH_KEY);
        Object highlightNode = orbit.getProperties().get(OrbitVisualizer.ORBIT_HIGHLIGHT_MESH_KEY);
        if (baseNode instanceof MeshView baseMesh && highlightNode instanceof MeshView highlightMesh) {
            return new OrbitMeshSelection(baseMesh, highlightMesh);
        }
        return null;
    }

    private record OrbitMeshSelection(MeshView baseMesh, MeshView highlightMesh) {
    }

    private void brightenOrbitMaterial(Node node) {
        if (!(node instanceof Shape3D shape)) {
            return;
        }
        if (!(shape.getMaterial() instanceof PhongMaterial material)) {
            return;
        }
        Color[] baseColors = orbitSegmentMaterials.get(material);
        if (baseColors == null) {
            return;
        }
        material.setDiffuseColor(baseColors[0].brighter().brighter());
        material.setSpecularColor(baseColors[1].brighter());
    }

    private void restoreOrbitMaterial(Node node) {
        if (!(node instanceof Shape3D shape)) {
            return;
        }
        if (!(shape.getMaterial() instanceof PhongMaterial material)) {
            return;
        }
        Color[] baseColors = orbitSegmentMaterials.get(material);
        if (baseColors == null) {
            return;
        }
        material.setDiffuseColor(baseColors[0]);
        material.setSpecularColor(baseColors[1]);
    }
}
