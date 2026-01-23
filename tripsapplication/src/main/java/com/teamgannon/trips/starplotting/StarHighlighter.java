package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Handles star highlighting with blinking animation effects.
 * Used when a star is selected from the side panel or other UI elements.
 */
@Slf4j
public class StarHighlighter {

    /**
     * Duration of scale transition animation in seconds.
     */
    private static final double SCALE_TRANSITION_DURATION_SECONDS = 2.0;

    /**
     * Scale multiplier for animation start/end states.
     */
    private static final double ANIMATION_SCALE_MULTIPLIER = 2.0;

    /**
     * Number of blink cycles for star highlighting.
     */
    private static final int HIGHLIGHT_BLINK_CYCLES = 100;

    private final TripsContext tripsContext;
    private final SpecialStarMeshManager meshManager;
    private final StarAnimationManager animationManager;
    private final StarClickHandler clickHandler;

    /**
     * The currently highlighted star node.
     */
    private Node highlightStar;

    /**
     * The display group where stars are rendered.
     */
    private Group stellarDisplayGroup;

    /**
     * Factory for creating context menus.
     */
    private ContextMenuFactory contextMenuFactory;

    /**
     * Functional interface for context menu creation.
     */
    @FunctionalInterface
    public interface ContextMenuFactory {
        void createContextMenu(StarDisplayRecord record, Node star);
    }

    public StarHighlighter(TripsContext tripsContext,
                           SpecialStarMeshManager meshManager,
                           StarAnimationManager animationManager,
                           StarClickHandler clickHandler) {
        this.tripsContext = tripsContext;
        this.meshManager = meshManager;
        this.animationManager = animationManager;
        this.clickHandler = clickHandler;
    }

    /**
     * Initialize with the stellar display group.
     */
    public void initialize(Group stellarDisplayGroup) {
        this.stellarDisplayGroup = stellarDisplayGroup;
    }

    /**
     * Set the context menu factory.
     */
    public void setContextMenuFactory(ContextMenuFactory factory) {
        this.contextMenuFactory = factory;
    }

    /**
     * Highlight a star by its ID with a blinking animation.
     *
     * @param starId the ID of the star to highlight
     */
    public void highlightStar(@NotNull String starId) {
        if (stellarDisplayGroup == null) {
            log.warn("Stellar display group not initialized");
            return;
        }

        // Remove existing highlight star
        removeCurrentHighlight();

        // Get the star to highlight
        Node starShape = tripsContext.getCurrentPlot().getStar(starId);
        if (starShape == null) {
            log.warn("Star not found for highlighting: {}", starId);
            return;
        }

        StarDisplayRecord record = (StarDisplayRecord) starShape.getUserData();
        if (record == null) {
            log.warn("No star record found for star: {}", starId);
            return;
        }

        // Create highlight overlay
        highlightStar = createHighlightOverlay(record);
        if (highlightStar == null) {
            return;
        }

        // Add to display and start animation
        stellarDisplayGroup.getChildren().add(highlightStar);
        startBlinkAnimation(highlightStar);

        log.info("Highlighting star: {}", record.getStarName());
    }

    /**
     * Remove the current highlight star if present.
     */
    public void removeCurrentHighlight() {
        if (highlightStar != null && stellarDisplayGroup != null) {
            stellarDisplayGroup.getChildren().remove(highlightStar);
            highlightStar = null;
        }
    }

    /**
     * Create a highlight overlay for a star.
     */
    private Node createHighlightOverlay(@NotNull StarDisplayRecord record) {
        Color color = record.getStarColor();
        Node highlight = meshManager.createHighlightStar(color);

        if (highlight == null) {
            log.error("Failed to create highlight star");
            return null;
        }

        // Copy position from original star
        Point3D position = record.getCoordinates();
        highlight.setTranslateX(position.getX());
        highlight.setTranslateY(position.getY());
        highlight.setTranslateZ(position.getZ());
        highlight.setVisible(true);

        // Attach data and context menu
        highlight.setUserData(record);
        if (contextMenuFactory != null) {
            contextMenuFactory.createContextMenu(record, highlight);
        } else {
            clickHandler.setupContextMenu(record, highlight);
        }

        return highlight;
    }

    /**
     * Start the blink animation for a highlight star.
     */
    private void startBlinkAnimation(@NotNull Node star) {
        // Set up completion callback to remove highlight when animation finishes
        animationManager.setOnHighlightFinished(node -> {
            log.info("Highlight animation finished, removing highlight star");
            if (stellarDisplayGroup != null) {
                stellarDisplayGroup.getChildren().remove(node);
            }
            if (node == highlightStar) {
                highlightStar = null;
            }
        });

        // Start the animation
        animationManager.startHighlightAnimation(
                star,
                HIGHLIGHT_BLINK_CYCLES,
                SCALE_TRANSITION_DURATION_SECONDS,
                ANIMATION_SCALE_MULTIPLIER
        );

        log.info("Started blink animation with {} cycles", HIGHLIGHT_BLINK_CYCLES);
    }

    /**
     * Check if a star is currently highlighted.
     */
    public boolean hasHighlight() {
        return highlightStar != null;
    }

    /**
     * Get the currently highlighted star node.
     */
    public Node getHighlightStar() {
        return highlightStar;
    }
}
