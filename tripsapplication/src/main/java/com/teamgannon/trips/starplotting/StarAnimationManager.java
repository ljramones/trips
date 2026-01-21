package com.teamgannon.trips.starplotting;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Manages animations for star objects.
 * <p>
 * This class handles:
 * <ul>
 *   <li>Scale pulsing/blinking animations for star highlighting</li>
 *   <li>Rotation animations for central stars</li>
 *   <li>Transition state management to restore original properties</li>
 *   <li>Animation lifecycle (start, stop, cleanup)</li>
 * </ul>
 * <p>
 * Only one highlight animation can be active at a time. Starting a new
 * animation will stop and clean up the previous one.
 */
@Slf4j
public class StarAnimationManager {

    // =========================================================================
    // Constants
    // =========================================================================

    /**
     * Default duration for scale transition in seconds.
     */
    private static final double DEFAULT_SCALE_DURATION_SECONDS = 2.0;

    /**
     * Default scale multiplier for animation.
     */
    private static final double DEFAULT_SCALE_MULTIPLIER = 2.0;

    /**
     * Default number of blink cycles.
     */
    private static final int DEFAULT_BLINK_CYCLES = 100;

    /**
     * Default rotation duration in seconds.
     */
    private static final double DEFAULT_ROTATION_DURATION_SECONDS = 10.0;

    /**
     * Default rotation angle in degrees.
     */
    private static final double DEFAULT_ROTATION_ANGLE = 360.0;

    // =========================================================================
    // State
    // =========================================================================

    /**
     * Current scale transition for blinking.
     */
    @Getter
    private ScaleTransition scaleTransition;

    /**
     * Saved state before animation started.
     */
    private TransitionState transitionState;

    /**
     * Current rotation transition for central star.
     */
    @Getter
    private RotateTransition rotateTransition;

    /**
     * Currently animated highlight node.
     */
    @Getter
    private Node highlightNode;

    /**
     * Callback when highlight animation finishes.
     */
    private Consumer<Node> onHighlightFinished;

    // =========================================================================
    // Highlight (Blink) Animation
    // =========================================================================

    /**
     * Start a blinking highlight animation on a star.
     * <p>
     * Any existing highlight animation will be stopped and cleaned up first.
     *
     * @param node the node to animate
     */
    public void startHighlightAnimation(@NotNull Node node) {
        startHighlightAnimation(node, DEFAULT_BLINK_CYCLES);
    }

    /**
     * Start a blinking highlight animation with custom cycle count.
     *
     * @param node       the node to animate
     * @param cycleCount number of blink cycles
     */
    public void startHighlightAnimation(@NotNull Node node, int cycleCount) {
        startHighlightAnimation(node, cycleCount, DEFAULT_SCALE_DURATION_SECONDS, DEFAULT_SCALE_MULTIPLIER);
    }

    /**
     * Start a blinking highlight animation with full customization.
     *
     * @param node            the node to animate
     * @param cycleCount      number of blink cycles
     * @param durationSeconds duration of each scale cycle
     * @param scaleMultiplier scale factor for animation
     */
    public void startHighlightAnimation(@NotNull Node node,
                                         int cycleCount,
                                         double durationSeconds,
                                         double scaleMultiplier) {
        // Stop any existing animation
        stopHighlightAnimation();

        log.info("Starting highlight animation with {} cycles", cycleCount);

        this.highlightNode = node;

        // Save original scale values
        double xScale = node.getScaleX();
        double yScale = node.getScaleY();
        double zScale = node.getScaleZ();
        transitionState = new TransitionState(node, xScale, yScale, zScale);

        // Create scale transition
        scaleTransition = new ScaleTransition(Duration.seconds(durationSeconds), node);
        scaleTransition.setFromX(xScale * scaleMultiplier);
        scaleTransition.setFromY(yScale * scaleMultiplier);
        scaleTransition.setFromZ(zScale * scaleMultiplier);
        scaleTransition.setToX(xScale / scaleMultiplier);
        scaleTransition.setToY(yScale / scaleMultiplier);
        scaleTransition.setToZ(zScale / scaleMultiplier);
        scaleTransition.setCycleCount(cycleCount);
        scaleTransition.setAutoReverse(true);

        // Set completion handler
        scaleTransition.setOnFinished(e -> {
            log.info("Highlight animation finished");
            if (onHighlightFinished != null) {
                onHighlightFinished.accept(node);
            }
            cleanupHighlightAnimation();
        });

        scaleTransition.play();
    }

    /**
     * Stop the current highlight animation and restore original scale.
     */
    public void stopHighlightAnimation() {
        if (scaleTransition != null) {
            log.info("Stopping existing highlight animation");
            scaleTransition.stop();

            // Restore original scale
            if (transitionState != null) {
                Node node = transitionState.getNode();
                if (node != null) {
                    node.setScaleX(transitionState.getXScale());
                    node.setScaleY(transitionState.getYScale());
                    node.setScaleZ(transitionState.getZScale());
                }
            }

            cleanupHighlightAnimation();
        }
    }

    /**
     * Clean up highlight animation state.
     */
    private void cleanupHighlightAnimation() {
        scaleTransition = null;
        transitionState = null;
        highlightNode = null;
    }

    /**
     * Set callback for when highlight animation finishes.
     *
     * @param callback the callback to invoke with the animated node
     */
    public void setOnHighlightFinished(@Nullable Consumer<Node> callback) {
        this.onHighlightFinished = callback;
    }

    /**
     * Check if a highlight animation is currently running.
     *
     * @return true if animation is running
     */
    public boolean isHighlightAnimationRunning() {
        return scaleTransition != null &&
               scaleTransition.getStatus() == Animation.Status.RUNNING;
    }

    // =========================================================================
    // Rotation Animation
    // =========================================================================

    /**
     * Start a continuous rotation animation on a node.
     *
     * @param node the node to rotate
     */
    public void startRotationAnimation(@NotNull Node node) {
        startRotationAnimation(node, DEFAULT_ROTATION_DURATION_SECONDS, DEFAULT_ROTATION_ANGLE);
    }

    /**
     * Start a rotation animation with custom parameters.
     *
     * @param node            the node to rotate
     * @param durationSeconds duration of one full rotation
     * @param angle           rotation angle in degrees
     */
    public void startRotationAnimation(@NotNull Node node, double durationSeconds, double angle) {
        stopRotationAnimation();

        log.info("Starting rotation animation");

        rotateTransition = new RotateTransition(Duration.seconds(durationSeconds), node);
        rotateTransition.setByAngle(angle);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.play();
    }

    /**
     * Stop the current rotation animation.
     */
    public void stopRotationAnimation() {
        if (rotateTransition != null) {
            log.info("Stopping rotation animation");
            rotateTransition.stop();
            rotateTransition = null;
        }
    }

    /**
     * Check if a rotation animation is currently running.
     *
     * @return true if animation is running
     */
    public boolean isRotationAnimationRunning() {
        return rotateTransition != null &&
               rotateTransition.getStatus() == Animation.Status.RUNNING;
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Stop all animations.
     */
    public void stopAllAnimations() {
        stopHighlightAnimation();
        stopRotationAnimation();
    }

    /**
     * Get the saved transition state.
     *
     * @return the transition state, or null if no animation is active
     */
    public @Nullable TransitionState getTransitionState() {
        return transitionState;
    }

    /**
     * Check if any animation is running.
     *
     * @return true if any animation is active
     */
    public boolean isAnyAnimationRunning() {
        return isHighlightAnimationRunning() || isRotationAnimationRunning();
    }

    // =========================================================================
    // Factory Methods for Common Animations
    // =========================================================================

    /**
     * Create a subtle pulse animation (small scale change, fast).
     *
     * @param node the node to animate
     * @return this manager for chaining
     */
    public StarAnimationManager pulseSubtle(@NotNull Node node) {
        startHighlightAnimation(node, 50, 0.5, 1.2);
        return this;
    }

    /**
     * Create a dramatic pulse animation (large scale change, slow).
     *
     * @param node the node to animate
     * @return this manager for chaining
     */
    public StarAnimationManager pulseDramatic(@NotNull Node node) {
        startHighlightAnimation(node, 20, 3.0, 3.0);
        return this;
    }

    /**
     * Create a quick flash animation (few cycles, fast).
     *
     * @param node the node to animate
     * @return this manager for chaining
     */
    public StarAnimationManager flash(@NotNull Node node) {
        startHighlightAnimation(node, 10, 0.3, 1.5);
        return this;
    }
}
