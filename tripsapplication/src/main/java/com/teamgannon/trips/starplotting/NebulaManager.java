package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.repository.NebulaRepository;
import com.teamgannon.trips.particlefields.InterstellarRingAdapter;
import com.teamgannon.trips.particlefields.RingConfiguration;
import com.teamgannon.trips.particlefields.RingFieldRenderer;
import com.teamgannon.trips.service.NebulaConfigConverter;
import javafx.scene.Group;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Manages nebula rendering in the interstellar view.
 * <p>
 * Responsibilities:
 * - Query nebulae within plot range from repository
 * - Create and manage RingFieldRenderer instances for each nebula
 * - Handle animation updates with efficient per-frame position updates
 * - Manage visibility and LOD
 * <p>
 * Usage:
 * <pre>{@code
 * // During plot setup
 * nebulaManager.setParentGroup(interstellarSpacePane.getWorld());
 * nebulaManager.setAdapter(new InterstellarRingAdapter(50.0));
 *
 * // When plotting stars
 * nebulaManager.renderNebulaeInRange(datasetName, centerX, centerY, centerZ, plotRadius);
 *
 * // In animation loop - efficient per-frame updates
 * nebulaManager.updateAnimation(timeScale);
 * }</pre>
 */
@Slf4j
@Component
public class NebulaManager {

    private final NebulaRepository nebulaRepository;

    /**
     * Parent group to add nebula renderers to (typically InterstellarSpacePane.world)
     */
    private Group parentGroup;

    /**
     * Container group for all nebula renderers
     */
    @Getter
    private final Group nebulaGroup = new Group();

    /**
     * Adapter for coordinate conversion
     */
    private InterstellarRingAdapter adapter;

    /**
     * Active renderers keyed by nebula ID
     */
    private final Map<String, RingFieldRenderer> activeRenderers = new HashMap<>();

    /**
     * Currently rendered nebulae
     */
    private final Map<String, Nebula> activeNebulae = new HashMap<>();

    /**
     * Current plot center for LOD calculations
     */
    private double plotCenterX, plotCenterY, plotCenterZ;

    /**
     * Whether nebulae are visible
     */
    @Getter
    private boolean visible = true;

    /**
     * Whether animation is enabled
     */
    @Getter
    private boolean animationEnabled = true;

    // Note: With efficient position updates in RingFieldRenderer, we no longer
    // need frame counting or periodic mesh refresh - updates can be every frame

    public NebulaManager(NebulaRepository nebulaRepository) {
        this.nebulaRepository = nebulaRepository;
        nebulaGroup.setVisible(true);
    }

    /**
     * Set the parent group to add nebula renderers to.
     * Should be called during initialization.
     *
     * @param parentGroup the parent group (e.g., InterstellarSpacePane.world)
     */
    public void setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
        if (!parentGroup.getChildren().contains(nebulaGroup)) {
            parentGroup.getChildren().add(nebulaGroup);
        }
    }

    /**
     * Set the adapter for coordinate conversion.
     *
     * @param adapter the interstellar ring adapter
     */
    public void setAdapter(InterstellarRingAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Render all nebulae within the plot range.
     *
     * @param datasetName the dataset to query
     * @param centerX     plot center X (light-years)
     * @param centerY     plot center Y (light-years)
     * @param centerZ     plot center Z (light-years)
     * @param plotRadius  plot radius (light-years)
     */
    public void renderNebulaeInRange(String datasetName,
                                      double centerX, double centerY, double centerZ,
                                      double plotRadius) {
        if (adapter == null) {
            log.warn("Adapter not set, cannot render nebulae");
            return;
        }

        log.info("DIAG Adapter: baseScale={}, zoomLevel={}, scaleFactor={}",
                adapter.getBaseScale(), adapter.getZoomLevel(), adapter.getScaleFactor());

        // Store plot center for LOD calculations
        this.plotCenterX = centerX;
        this.plotCenterY = centerY;
        this.plotCenterZ = centerZ;

        // Clear existing renderers
        clearRenderers();

        // Query nebulae in range
        List<Nebula> nebulae = nebulaRepository.findInPlotRange(
                datasetName, centerX, centerY, centerZ, plotRadius);

        log.info("Found {} nebulae in plot range for dataset '{}'", nebulae.size(), datasetName);

        // Render each nebula
        for (Nebula nebula : nebulae) {
            renderNebula(nebula);
        }

        // Update visibility
        nebulaGroup.setVisible(visible);
    }

    /**
     * Render a single nebula.
     */
    private void renderNebula(Nebula nebula) {
        try {
            // Calculate distance for LOD
            double distance = NebulaConfigConverter.calculateDistance(
                    nebula, plotCenterX, plotCenterY, plotCenterZ);

            // Convert to RingConfiguration
            RingConfiguration config = NebulaConfigConverter.toRingConfiguration(
                    nebula, adapter, distance);

            // Create renderer - uses centroid rebasing by default to avoid float precision issues
            // Centroid rebasing keeps mesh vertices small (near zero) while using double-precision
            // group transforms for world placement
            RingFieldRenderer renderer = new RingFieldRenderer(config, new Random(nebula.getSeed()));

            // Position in screen coordinates - this triggers centroid computation and group translation
            double[] screenPos = NebulaConfigConverter.toScreenPosition(nebula, adapter);
            renderer.setPosition(screenPos[0], screenPos[1], screenPos[2]);

            // Add the renderer's group to the scene - DO NOT extract the mesh!
            // The group's translation is essential for centroid rebasing to work correctly
            nebulaGroup.getChildren().add(renderer.getGroup());

            log.info("Nebula '{}' rendered at screenPos=({}, {}, {}), group translate=({}, {}, {})",
                    nebula.getName(), screenPos[0], screenPos[1], screenPos[2],
                    renderer.getGroup().getTranslateX(),
                    renderer.getGroup().getTranslateY(),
                    renderer.getGroup().getTranslateZ());

            // Track
            activeRenderers.put(nebula.getId(), renderer);
            activeNebulae.put(nebula.getId(), nebula);

        } catch (Exception e) {
            log.error("Failed to render nebula '{}': {}", nebula.getName(), e.getMessage(), e);
        }
    }

    /**
     * Clear all active renderers.
     */
    public void clearRenderers() {
        for (RingFieldRenderer renderer : activeRenderers.values()) {
            renderer.dispose();
        }
        activeRenderers.clear();
        activeNebulae.clear();
        nebulaGroup.getChildren().clear();
    }

    /**
     * Update animation for all active nebulae.
     * Call this every frame.
     *
     * @param timeScale animation speed multiplier
     */
    public void updateAnimation(double timeScale) {
        if (!animationEnabled || !visible) {
            return;
        }

        for (RingFieldRenderer renderer : activeRenderers.values()) {
            renderer.update(timeScale);
            // Use efficient position update every frame (no full mesh rebuild)
            renderer.updateMeshPositions();
        }
    }

    /**
     * Refresh meshes for all active renderers.
     *
     * @deprecated Use {@link #updateAnimation(double)} which now handles efficient
     * per-frame position updates. This method is kept for compatibility but
     * updateAnimation already calls updateMeshPositions() every frame.
     */
    @Deprecated
    public void refreshMeshes() {
        for (RingFieldRenderer renderer : activeRenderers.values()) {
            renderer.updateMeshPositions();
        }
    }

    /**
     * Set visibility of all nebulae.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        nebulaGroup.setVisible(visible);
    }

    /**
     * Toggle visibility.
     */
    public void toggleVisibility() {
        setVisible(!visible);
    }

    /**
     * Set animation enabled/disabled.
     */
    public void setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
    }

    /**
     * Get the number of active nebulae.
     */
    public int getActiveNebulaCount() {
        return activeRenderers.size();
    }

    /**
     * Get the total particle count across all active nebulae.
     */
    public int getTotalParticleCount() {
        return activeRenderers.values().stream()
                .mapToInt(RingFieldRenderer::getElementCount)
                .sum();
    }

    /**
     * Get list of active nebula names.
     */
    public List<String> getActiveNebulaNames() {
        return activeNebulae.values().stream()
                .map(Nebula::getName)
                .sorted()
                .toList();
    }

    /**
     * Check if a specific nebula is currently rendered.
     */
    public boolean isNebulaActive(String nebulaId) {
        return activeRenderers.containsKey(nebulaId);
    }

    /**
     * Get renderer for a specific nebula (for debugging/inspection).
     */
    public Optional<RingFieldRenderer> getRenderer(String nebulaId) {
        return Optional.ofNullable(activeRenderers.get(nebulaId));
    }

    /**
     * Dispose of all resources.
     */
    public void dispose() {
        clearRenderers();
        if (parentGroup != null) {
            parentGroup.getChildren().remove(nebulaGroup);
        }
    }
}
