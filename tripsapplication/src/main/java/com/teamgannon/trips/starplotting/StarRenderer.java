package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService.AliasDisplay;
import com.teamgannon.trips.spaceshipmodeller.service.AliasTooltipFormatter;
import com.terranrepublic.assets.AliasTargetKind;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Handles the rendering of individual stars including material caching,
 * geometry creation, and node batching for efficient scene graph updates.
 */
@Slf4j
public class StarRenderer {

    /**
     * Property key for the {@link Tooltip} attached to a star node. Phase F.2 §6.1 — the
     * separate {@code TOOLTIP_INSTALLED} flag from the pre-F.2 cached-tooltip pattern is
     * gone; F1.b uses a single persistent Tooltip per node whose text is refreshed on every
     * mouse-enter via {@link Tooltip#setText(String)}, so the property's mere presence is
     * what indicates installation.
     */
    static final String TOOLTIP_PROPERTY = "trips.star.tooltip";

    /**
     * Glow intensity for hover effect (0.0 to 1.0).
     */
    private static final double HOVER_GLOW_LEVEL = 0.7;

    /**
     * Scale multiplier when hovering over a star.
     */
    private static final double HOVER_SCALE_FACTOR = 1.3;

    /**
     * Cache of PhongMaterial objects by color.
     * Avoids creating duplicate materials for stars of the same color.
     */
    private final Map<Color, PhongMaterial> materialCache = new HashMap<>();

    /**
     * Maps star nodes to their associated labels for coordinated hover effects.
     */
    private final Map<Node, Label> starToLabelMap = new HashMap<>();

    /**
     * Batch collection for star nodes to add to scene graph.
     */
    private final List<Node> pendingStarNodes = new ArrayList<>();

    /**
     * Batch collection for polity nodes.
     */
    private final List<Node> pendingPolityNodes = new ArrayList<>();

    private final StarLODManager lodManager;
    private final StarLabelManager labelManager;
    private final InterstellarScaleManager scaleManager;
    private final SpecialStarMeshManager meshManager;
    private final PolityObjectFactory polityObjectFactory;
    private final StarClickHandler clickHandler;

    /**
     * F.2 §6.1 alias lookup. May be {@code null} when the renderer is constructed in a context
     * without Spring (existing tests, pre-F.2 callers). When null, hover tooltips fall back to
     * "{starName}\nPolity: {polity}" with no alias lines appended.
     */
    @Nullable
    private final AliasDesignerService aliasService;

    /**
     * Callback to register label with current plot.
     */
    private BiConsumer<String, Label> labelRegistrar;

    public StarRenderer(StarLODManager lodManager,
                        StarLabelManager labelManager,
                        InterstellarScaleManager scaleManager,
                        SpecialStarMeshManager meshManager,
                        PolityObjectFactory polityObjectFactory,
                        StarClickHandler clickHandler) {
        this(lodManager, labelManager, scaleManager, meshManager, polityObjectFactory,
                clickHandler, null);
    }

    public StarRenderer(StarLODManager lodManager,
                        StarLabelManager labelManager,
                        InterstellarScaleManager scaleManager,
                        SpecialStarMeshManager meshManager,
                        PolityObjectFactory polityObjectFactory,
                        StarClickHandler clickHandler,
                        @Nullable AliasDesignerService aliasService) {
        this.lodManager = lodManager;
        this.labelManager = labelManager;
        this.scaleManager = scaleManager;
        this.meshManager = meshManager;
        this.polityObjectFactory = polityObjectFactory;
        this.clickHandler = clickHandler;
        this.aliasService = aliasService;
    }

    /**
     * Set the callback for registering labels with the current plot.
     */
    public void setLabelRegistrar(BiConsumer<String, Label> labelRegistrar) {
        this.labelRegistrar = labelRegistrar;
    }

    /**
     * Gets or creates a cached PhongMaterial for the given color.
     *
     * @param color the color for the material
     * @return the cached or newly created material
     */
    public @NotNull PhongMaterial getCachedMaterial(@NotNull Color color) {
        return materialCache.computeIfAbsent(color, c -> {
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(c);
            material.setSpecularColor(c);
            return material;
        });
    }

    /**
     * Create a star node with all decorations (label, polity, context menu).
     *
     * @param record                 the star record
     * @param isCenter               whether this is the center star
     * @param colorPalette           the color palette
     * @param starDisplayPreferences star display preferences
     * @param polityPreferences      polity display preferences
     * @param labelsOn               whether labels are enabled for this star
     * @param politiesOn             whether polities are enabled
     * @return the created star node
     */
    public @NotNull Node createStar(@NotNull StarDisplayRecord record,
                                    boolean isCenter,
                                    @NotNull ColorPalette colorPalette,
                                    StarDisplayPreferences starDisplayPreferences,
                                    @NotNull CivilizationDisplayPreferences polityPreferences,
                                    boolean labelsOn,
                                    boolean politiesOn) {

        Node starShape = createStarGeometry(record, isCenter);
        positionNode(starShape, record.getCoordinates());

        // Add label if enabled
        Label label = null;
        if (labelsOn) {
            label = labelManager.addLabel(starShape, record, colorPalette);
            starToLabelMap.put(starShape, label);
            if (labelRegistrar != null) {
                labelRegistrar.accept(record.getRecordId(), label);
            }
        }

        // Add polity object if enabled and applicable
        if (politiesOn && hasPolity(record)) {
            MeshView polityObject = createPolityObject(record.getPolity(), polityPreferences);
            if (polityObject != null) {
                positionNode(polityObject, record.getCoordinates());
                clickHandler.setupLazyContextMenu(record, polityObject);
                pendingPolityNodes.add(polityObject);
            }
        }

        // Set up context menu
        clickHandler.setupLazyContextMenu(record, starShape);

        // Install lazy tooltip and hover effects
        installLazyTooltipAndHover(starShape, record, label);

        starShape.setId("regularStar");
        starShape.setUserData(record);

        return starShape;
    }

    /**
     * Create the star geometry (sphere or central star mesh).
     */
    private Node createStarGeometry(@NotNull StarDisplayRecord record, boolean isCenter) {
        if (isCenter) {
            return meshManager.createCentralStar();
        }

        PhongMaterial material = getCachedMaterial(record.getStarColor());
        double displayRadius = record.getRadius() * scaleManager.getStarSizeMultiplier();
        StarLODManager.LODLevel lodLevel = lodManager.determineLODLevel(record, false);

        return lodManager.createStarWithLOD(record, displayRadius, material, lodLevel);
    }

    /**
     * Position a node at the given 3D coordinates.
     */
    private void positionNode(Node node, Point3D coordinates) {
        node.setTranslateX(coordinates.getX());
        node.setTranslateY(coordinates.getY());
        node.setTranslateZ(coordinates.getZ());
    }

    /**
     * Check if a star record has a valid polity.
     */
    private boolean hasPolity(@NotNull StarDisplayRecord record) {
        return !record.getPolity().equals("NA") && !record.getPolity().isEmpty();
    }

    /**
     * Create a polity object for a star.
     */
    private MeshView createPolityObject(String polity, CivilizationDisplayPreferences polityPreferences) {
        MeshView polityObject = polityObjectFactory.createPolityObject(polity, polityPreferences);
        if (polityObject == null) {
            log.error("Failed to create polity object for: {}", polity);
        }
        return polityObject;
    }

    /**
     * Installs a tooltip and sets up hover glow effects. Phase F.2 §6.1 — F1.b dynamic
     * per-hover: the Tooltip instance is reused per node, but its text is rebuilt from the
     * current alias state on every mouse-enter so universe activation changes surface
     * immediately on the next hover. The glow effect highlights both the star and its label.
     *
     * @param node   the star node
     * @param record the star record
     * @param label  the associated label (may be null if labels disabled)
     */
    private void installLazyTooltipAndHover(@NotNull Node node,
                                             @NotNull StarDisplayRecord record,
                                             Label label) {
        // Create the glow effect (reused on each hover)
        Glow glowEffect = new Glow(HOVER_GLOW_LEVEL);

        // Store original scale
        final double originalScaleX = node.getScaleX();
        final double originalScaleY = node.getScaleY();
        final double originalScaleZ = node.getScaleZ();

        node.setOnMouseEntered(event -> {
            // F1.b dynamic per-hover: rebuild tooltip text from current alias state
            Tooltip tooltip = (Tooltip) node.getProperties().get(TOOLTIP_PROPERTY);
            if (tooltip == null) {
                tooltip = new Tooltip();
                Tooltip.install(node, tooltip);
                node.getProperties().put(TOOLTIP_PROPERTY, tooltip);
                log.trace("Installed tooltip for star: {}", record.getStarName());
            }
            tooltip.setText(buildStarTooltipText(record));

            // Apply glow effect to star
            node.setEffect(glowEffect);

            // Scale up the star slightly
            node.setScaleX(originalScaleX * HOVER_SCALE_FACTOR);
            node.setScaleY(originalScaleY * HOVER_SCALE_FACTOR);
            node.setScaleZ(originalScaleZ * HOVER_SCALE_FACTOR);

            // Highlight associated label if present
            if (label != null) {
                highlightLabel(label, true);
            }
        });

        node.setOnMouseExited(event -> {
            // Remove glow effect
            node.setEffect(null);

            // Restore original scale
            node.setScaleX(originalScaleX);
            node.setScaleY(originalScaleY);
            node.setScaleZ(originalScaleZ);

            // Remove label highlight if present
            if (label != null) {
                highlightLabel(label, false);
            }
        });
    }

    /**
     * Builds the F.2 §6.1 format-α tooltip text for a star — star name on line 1, polity on
     * line 2, then one line per active alias formatted as "{aliasText} ({universeName})".
     * Pure aside from the alias service call; the formatter itself is unit-tested separately.
     *
     * <p>If {@link #aliasService} is null (test contexts or non-Spring callers) or no active
     * universes have aliased this star, the result is just the first two lines — preserving
     * the legacy "name + polity" payload in a clean multi-line shape.
     */
    @NotNull
    String buildStarTooltipText(@NotNull StarDisplayRecord record) {
        List<AliasDisplay> aliases = (aliasService == null)
                ? List.of()
                : aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, record.getRecordId());
        return AliasTooltipFormatter.formatStarTooltip(record.getStarName(), record.getPolity(), aliases);
    }

    /**
     * Highlight or unhighlight a label.
     *
     * @param label     the label to highlight
     * @param highlight true to highlight, false to restore normal appearance
     */
    private void highlightLabel(@NotNull Label label, boolean highlight) {
        if (highlight) {
            // Trigger the label's hover style
            label.setStyle(
                    """
                    -fx-background-color: rgba(70, 130, 180, 0.8);\
                    -fx-padding: 2px 4px;\
                    -fx-background-radius: 3px;\
                    """
            );
        } else {
            // Restore normal style
            label.setStyle(
                    """
                    -fx-background-color: rgba(0, 0, 0, 0.6);\
                    -fx-padding: 2px 4px;\
                    -fx-background-radius: 3px;\
                    """
            );
        }
    }

    /**
     * Add a star node to the pending batch.
     */
    public void addPendingStarNode(Node node) {
        pendingStarNodes.add(node);
    }

    /**
     * Get pending star nodes for batch addition.
     */
    public List<Node> getPendingStarNodes() {
        return pendingStarNodes;
    }

    /**
     * Get pending polity nodes for batch addition.
     */
    public List<Node> getPendingPolityNodes() {
        return pendingPolityNodes;
    }

    /**
     * Clear all pending nodes after they've been added to the scene graph.
     */
    public void clearPendingNodes() {
        pendingStarNodes.clear();
        pendingPolityNodes.clear();
    }

    /**
     * Clear all caches. Call when starting a new plot.
     */
    public void clearCaches() {
        materialCache.clear();
        pendingStarNodes.clear();
        pendingPolityNodes.clear();
        starToLabelMap.clear();
    }

    /**
     * Get the current material cache size (for logging/debugging).
     */
    public int getMaterialCacheSize() {
        return materialCache.size();
    }
}
