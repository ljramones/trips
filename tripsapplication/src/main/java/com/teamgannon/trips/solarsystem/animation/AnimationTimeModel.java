package com.teamgannon.trips.solarsystem.animation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * Tracks simulation time for orbital animation.
 * Converts wall-clock elapsed time to simulation time via a speed multiplier.
 */
@Slf4j
public class AnimationTimeModel {

    /**
     * The epoch (start time) for simulation - defaults to J2000.0
     */
    @Getter
    private Instant epoch;

    /**
     * Current time in the simulation
     */
    @Getter
    private Instant simulationTime;

    /**
     * Speed multiplier: 1.0 = real-time, 86400 = 1 day per second
     */
    @Getter
    private double speedMultiplier;

    /**
     * Last update timestamp in nanoseconds (from AnimationTimer)
     */
    private long lastUpdateNanos;

    /**
     * Whether the animation has been initialized
     */
    private boolean initialized;

    /**
     * J2000.0 epoch: January 1, 2000, 12:00:00 TT
     */
    public static final Instant J2000_EPOCH = Instant.parse("2000-01-01T12:00:00Z");

    /**
     * Create a new time model starting at J2000 epoch with 1 day/second speed
     */
    public AnimationTimeModel() {
        this(J2000_EPOCH, 86400.0);
    }

    /**
     * Create a new time model with specified epoch and speed
     *
     * @param epoch           the simulation start time
     * @param speedMultiplier initial speed multiplier
     */
    public AnimationTimeModel(Instant epoch, double speedMultiplier) {
        this.epoch = epoch;
        this.simulationTime = epoch;
        this.speedMultiplier = speedMultiplier;
        this.initialized = false;
    }

    /**
     * Update the simulation time based on elapsed wall-clock time.
     * Called each frame by the AnimationTimer.
     *
     * @param nowNanos current time in nanoseconds from AnimationTimer.handle()
     */
    public void update(long nowNanos) {
        if (!initialized) {
            lastUpdateNanos = nowNanos;
            initialized = true;
            return;
        }

        long deltaNanos = nowNanos - lastUpdateNanos;
        lastUpdateNanos = nowNanos;

        // Convert wall-clock delta to simulation delta
        double deltaSeconds = deltaNanos / 1_000_000_000.0;
        double simDeltaSeconds = deltaSeconds * speedMultiplier;

        // Update simulation time
        long simDeltaMillis = (long) (simDeltaSeconds * 1000);
        simulationTime = simulationTime.plusMillis(simDeltaMillis);
    }

    /**
     * Get the elapsed simulation time since epoch in days.
     * This is useful for calculating mean anomaly from orbital period.
     *
     * @return elapsed days since epoch
     */
    public double getElapsedDays() {
        long elapsedMillis = simulationTime.toEpochMilli() - epoch.toEpochMilli();
        return elapsedMillis / (1000.0 * 60 * 60 * 24);
    }

    /**
     * Set the speed multiplier.
     *
     * @param speed new speed multiplier (e.g., 1.0 for real-time, 86400 for 1 day/sec)
     */
    public void setSpeedMultiplier(double speed) {
        this.speedMultiplier = Math.max(0.0, speed);
        log.debug("Animation speed set to {}x", speedMultiplier);
    }

    /**
     * Reset the simulation time back to the epoch.
     */
    public void reset() {
        simulationTime = epoch;
        initialized = false;
        log.debug("Animation time reset to epoch: {}", epoch);
    }

    /**
     * Set a new epoch and reset simulation time to it.
     *
     * @param epoch the new epoch
     */
    public void setEpoch(Instant epoch) {
        this.epoch = epoch;
        this.simulationTime = epoch;
        this.initialized = false;
    }

    /**
     * Jump to a specific simulation time.
     *
     * @param time the time to jump to
     */
    public void jumpTo(Instant time) {
        this.simulationTime = time;
    }

    /**
     * Get a formatted string of the current simulation date.
     *
     * @return formatted date string
     */
    public String getFormattedDate() {
        return simulationTime.toString().substring(0, 10);
    }
}
