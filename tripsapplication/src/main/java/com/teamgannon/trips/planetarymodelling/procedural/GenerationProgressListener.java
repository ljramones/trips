package com.teamgannon.trips.planetarymodelling.procedural;

/**
 * Callback interface for monitoring procedural planet generation progress.
 * Implement this interface to receive updates during terrain generation.
 *
 * <p>Progress callbacks are invoked on the generation thread (not the JavaFX thread).
 * If updating UI components, use Platform.runLater() to switch to the JavaFX thread.
 *
 * <p>Example usage:
 * <pre>{@code
 * GenerationProgressListener listener = new GenerationProgressListener() {
 *     @Override
 *     public void onPhaseStarted(Phase phase, String description) {
 *         Platform.runLater(() -> statusLabel.setText("Starting: " + description));
 *     }
 *
 *     @Override
 *     public void onProgressUpdate(Phase phase, double progress) {
 *         Platform.runLater(() -> progressBar.setProgress(progress));
 *     }
 *
 *     @Override
 *     public void onPhaseCompleted(Phase phase) {
 *         // Phase completed
 *     }
 *
 *     @Override
 *     public void onGenerationCompleted() {
 *         Platform.runLater(() -> statusLabel.setText("Generation complete!"));
 *     }
 * };
 *
 * PlanetConfig config = PlanetConfig.builder().build();
 * PlanetGenerator generator = new PlanetGenerator(config, listener);
 * GeneratedPlanet planet = generator.generate();
 * }</pre>
 */
public interface GenerationProgressListener {

    /**
     * Phases of the procedural planet generation pipeline.
     */
    enum Phase {
        /** Creating the icosahedral mesh structure */
        MESH_GENERATION("Generating mesh"),
        /** Building polygon adjacency graph */
        ADJACENCY_GRAPH("Building adjacency graph"),
        /** Assigning polygons to tectonic plates */
        PLATE_ASSIGNMENT("Assigning tectonic plates"),
        /** Detecting and classifying plate boundaries */
        BOUNDARY_DETECTION("Detecting plate boundaries"),
        /** Calculating terrain elevations */
        ELEVATION_CALCULATION("Calculating elevations"),
        /** Assigning climate zones */
        CLIMATE_CALCULATION("Calculating climate zones"),
        /** Running erosion simulation and river formation */
        EROSION_CALCULATION("Simulating erosion");

        private final String description;

        Phase(String description) {
            this.description = description;
        }

        /**
         * Returns a human-readable description of this phase.
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Called when a generation phase starts.
     *
     * @param phase       The phase that is starting
     * @param description Human-readable description of what's happening
     */
    void onPhaseStarted(Phase phase, String description);

    /**
     * Called periodically during a phase to report progress.
     *
     * @param phase    The current phase
     * @param progress Progress value from 0.0 (just started) to 1.0 (complete)
     */
    void onProgressUpdate(Phase phase, double progress);

    /**
     * Called when a generation phase completes.
     *
     * @param phase The phase that completed
     */
    void onPhaseCompleted(Phase phase);

    /**
     * Called when the entire generation process completes successfully.
     */
    void onGenerationCompleted();

    /**
     * Called if generation fails with an error.
     *
     * @param phase The phase during which the error occurred
     * @param error The exception that caused the failure
     */
    default void onGenerationError(Phase phase, Exception error) {
        // Default implementation does nothing
    }

    /**
     * Returns the overall progress across all phases as a value from 0.0 to 1.0.
     *
     * @param currentPhase  The current phase
     * @param phaseProgress Progress within the current phase (0.0 to 1.0)
     * @return Overall progress from 0.0 to 1.0
     */
    static double calculateOverallProgress(Phase currentPhase, double phaseProgress) {
        Phase[] phases = Phase.values();
        int phaseIndex = currentPhase.ordinal();
        double phaseWeight = 1.0 / phases.length;
        return (phaseIndex * phaseWeight) + (phaseProgress * phaseWeight);
    }

    /**
     * A no-op listener that does nothing. Useful for testing or when progress
     * tracking is not needed.
     */
    GenerationProgressListener NO_OP = new GenerationProgressListener() {
        @Override
        public void onPhaseStarted(Phase phase, String description) {}

        @Override
        public void onProgressUpdate(Phase phase, double progress) {}

        @Override
        public void onPhaseCompleted(Phase phase) {}

        @Override
        public void onGenerationCompleted() {}
    };
}
