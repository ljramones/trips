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
 * - Manage visibility and LOD with camera-aware updates
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
 *
 * // When camera moves/zooms - update LOD
 * nebulaManager.updateLODFromCamera(cameraX, cameraY, cameraZ, zoomLevel);
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

    /**
     * Current zoom level for LOD calculations
     */
    private double currentZoomLevel = 1.0;

    /**
     * Current camera position for LOD calculations
     */
    private double cameraX, cameraY, cameraZ;

    /**
     * LOD manager for distance-based detail adjustment
     */
    private final NebulaLODManager lodManager = NebulaLODManager.getInstance();

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

            // Convert to RingConfiguration with LOD
            RingConfiguration config = NebulaConfigConverter.toRingConfiguration(
                    nebula, adapter, distance, currentZoomLevel);

            // Check if nebula was culled by LOD
            if (config == null) {
                log.debug("Nebula '{}' culled by LOD at distance {:.1f}ly", nebula.getName(), distance);
                return;
            }

            // Create renderer - uses centroid rebasing by default to avoid float precision issues
            // Centroid rebasing keeps mesh vertices small (near zero) while using double-precision
            // group transforms for world placement
            RingFieldRenderer renderer = new RingFieldRenderer(config, new Random(nebula.getSeed()));

            // Set current LOD level on renderer for tracking
            NebulaLODManager.LODLevel lodLevel = lodManager.getCurrentLevel(nebula.getId());
            if (lodLevel != null) {
                renderer.setCurrentLodLevel(lodLevel.name());
            }

            // Position in screen coordinates - this triggers centroid computation and group translation
            double[] screenPos = NebulaConfigConverter.toScreenPosition(nebula, adapter);
            renderer.setPosition(screenPos[0], screenPos[1], screenPos[2]);

            // Add the renderer's group to the scene - DO NOT extract the mesh!
            // The group's translation is essential for centroid rebasing to work correctly
            nebulaGroup.getChildren().add(renderer.getGroup());

            log.info("Nebula '{}' rendered at screenPos=({}, {}, {}), LOD={}, particles={}",
                    nebula.getName(), screenPos[0], screenPos[1], screenPos[2],
                    lodLevel, config.numElements());

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
     * Set the current zoom level for LOD calculations.
     *
     * @param zoomLevel the zoom level (1.0 = normal)
     */
    public void setZoomLevel(double zoomLevel) {
        this.currentZoomLevel = zoomLevel;
    }

    /**
     * Update LOD based on camera position.
     * Call this when the camera moves or zooms to potentially update nebula detail levels.
     *
     * @param cameraX   camera X position in light-years
     * @param cameraY   camera Y position in light-years
     * @param cameraZ   camera Z position in light-years
     * @param zoomLevel current zoom level
     */
    public void updateLODFromCamera(double cameraX, double cameraY, double cameraZ, double zoomLevel) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.currentZoomLevel = zoomLevel;

        // Check each active nebula for LOD changes
        List<String> nebulaesToRebuild = new ArrayList<>();

        for (Map.Entry<String, Nebula> entry : activeNebulae.entrySet()) {
            String nebulaId = entry.getKey();
            Nebula nebula = entry.getValue();

            // Calculate new distance
            double distance = nebula.distanceTo(cameraX, cameraY, cameraZ);

            // Get current and new LOD levels
            NebulaLODManager.LODLevel currentLevel = lodManager.getCurrentLevel(nebulaId);
            NebulaLODManager.LODLevel newLevel = lodManager.calculateLOD(nebulaId, distance, zoomLevel);

            // If LOD changed significantly, mark for rebuild
            if (currentLevel != newLevel) {
                log.debug("Nebula '{}' LOD changed: {} -> {} at distance {:.1f}ly",
                        nebula.getName(), currentLevel, newLevel, distance);
                nebulaesToRebuild.add(nebulaId);
            }
        }

        // Rebuild nebulae with changed LOD
        for (String nebulaId : nebulaesToRebuild) {
            Nebula nebula = activeNebulae.get(nebulaId);
            RingFieldRenderer oldRenderer = activeRenderers.get(nebulaId);

            if (nebula != null && oldRenderer != null) {
                // Remove old renderer
                nebulaGroup.getChildren().remove(oldRenderer.getGroup());
                oldRenderer.dispose();
                activeRenderers.remove(nebulaId);

                // Re-render with new LOD
                renderNebula(nebula);
            }
        }

        if (!nebulaesToRebuild.isEmpty()) {
            log.info("Rebuilt {} nebulae due to LOD changes", nebulaesToRebuild.size());
        }
    }

    /**
     * Get the LOD summary for debugging.
     */
    public String getLODSummary() {
        return lodManager.getLODSummary();
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
        lodManager.clearAllCaches();
        if (parentGroup != null) {
            parentGroup.getChildren().remove(nebulaGroup);
        }
    }
}
