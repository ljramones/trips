package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.GenerationProgressListener.Phase;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig.Size;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for GenerationProgressListener functionality.
 */
class ProgressListenerTest {

    // ===========================================
    // Progress Listener Callbacks
    // ===========================================

    @Nested
    @DisplayName("Progress listener callback tests")
    class CallbackTests {

        @Test
        @DisplayName("All phases fire callbacks in order")
        void allPhasesFireCallbacks() {
            List<Phase> startedPhases = new ArrayList<>();
            List<Phase> completedPhases = new ArrayList<>();
            boolean[] generationCompleted = {false};

            GenerationProgressListener listener = new GenerationProgressListener() {
                @Override
                public void onPhaseStarted(Phase phase, String description) {
                    startedPhases.add(phase);
                }

                @Override
                public void onProgressUpdate(Phase phase, double progress) {
                    // Progress updates are fired
                }

                @Override
                public void onPhaseCompleted(Phase phase) {
                    completedPhases.add(phase);
                }

                @Override
                public void onGenerationCompleted() {
                    generationCompleted[0] = true;
                }
            };

            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.DUEL)  // Smallest for fast test
                .plateCount(7)
                .build();

            PlanetGenerator.generate(config, listener);

            // Verify all phases were started in order
            assertThat(startedPhases).containsExactly(
                Phase.MESH_GENERATION,
                Phase.ADJACENCY_GRAPH,
                Phase.PLATE_ASSIGNMENT,
                Phase.BOUNDARY_DETECTION,
                Phase.ELEVATION_CALCULATION,
                Phase.CLIMATE_CALCULATION,
                Phase.EROSION_CALCULATION
            );

            // Verify all phases were completed in order
            assertThat(completedPhases).containsExactly(
                Phase.MESH_GENERATION,
                Phase.ADJACENCY_GRAPH,
                Phase.PLATE_ASSIGNMENT,
                Phase.BOUNDARY_DETECTION,
                Phase.ELEVATION_CALCULATION,
                Phase.CLIMATE_CALCULATION,
                Phase.EROSION_CALCULATION
            );

            // Verify generation completed callback was fired
            assertThat(generationCompleted[0]).isTrue();
        }

        @Test
        @DisplayName("Progress updates are within valid range")
        void progressUpdatesInValidRange() {
            List<Double> progressValues = new ArrayList<>();

            GenerationProgressListener listener = new GenerationProgressListener() {
                @Override
                public void onPhaseStarted(Phase phase, String description) {}

                @Override
                public void onProgressUpdate(Phase phase, double progress) {
                    progressValues.add(progress);
                }

                @Override
                public void onPhaseCompleted(Phase phase) {}

                @Override
                public void onGenerationCompleted() {}
            };

            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.DUEL)
                .plateCount(7)
                .build();

            PlanetGenerator.generate(config, listener);

            // All progress values should be between 0.0 and 1.0
            for (double progress : progressValues) {
                assertThat(progress).isBetween(0.0, 1.0);
            }
        }

        @Test
        @DisplayName("Phase descriptions are non-empty")
        void phaseDescriptionsNonEmpty() {
            List<String> descriptions = new ArrayList<>();

            GenerationProgressListener listener = new GenerationProgressListener() {
                @Override
                public void onPhaseStarted(Phase phase, String description) {
                    descriptions.add(description);
                }

                @Override
                public void onProgressUpdate(Phase phase, double progress) {}

                @Override
                public void onPhaseCompleted(Phase phase) {}

                @Override
                public void onGenerationCompleted() {}
            };

            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.DUEL)
                .plateCount(7)
                .build();

            PlanetGenerator.generate(config, listener);

            assertThat(descriptions).isNotEmpty();
            for (String desc : descriptions) {
                assertThat(desc).isNotBlank();
            }
        }
    }

    // ===========================================
    // Null Listener Handling
    // ===========================================

    @Nested
    @DisplayName("Null listener handling")
    class NullListenerTests {

        @Test
        @DisplayName("Null listener uses NO_OP")
        void nullListenerUsesNoOp() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.DUEL)
                .plateCount(7)
                .build();

            // Should not throw with null listener
            assertThatCode(() -> PlanetGenerator.generate(config, null))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("NO_OP listener works correctly")
        void noOpListenerWorks() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.DUEL)
                .plateCount(7)
                .build();

            // Should not throw with NO_OP listener
            assertThatCode(() -> PlanetGenerator.generate(config, GenerationProgressListener.NO_OP))
                .doesNotThrowAnyException();
        }
    }

    // ===========================================
    // Phase Enum Tests
    // ===========================================

    @Nested
    @DisplayName("Phase enum tests")
    class PhaseEnumTests {

        @Test
        @DisplayName("All phases have descriptions")
        void allPhasesHaveDescriptions() {
            for (Phase phase : Phase.values()) {
                assertThat(phase.getDescription())
                    .as("Phase %s should have a description", phase)
                    .isNotBlank();
            }
        }

        @Test
        @DisplayName("Phase count matches generation steps")
        void phaseCountMatchesSteps() {
            // Should have 7 phases for the generation pipeline
            assertThat(Phase.values()).hasSize(7);
        }
    }

    // ===========================================
    // Overall Progress Calculation
    // ===========================================

    @Nested
    @DisplayName("Overall progress calculation")
    class OverallProgressTests {

        @Test
        @DisplayName("Overall progress starts at 0 for first phase")
        void overallProgressStartsAtZero() {
            double progress = GenerationProgressListener.calculateOverallProgress(
                Phase.MESH_GENERATION, 0.0);

            assertThat(progress).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Overall progress ends at 1 for last phase")
        void overallProgressEndsAtOne() {
            double progress = GenerationProgressListener.calculateOverallProgress(
                Phase.EROSION_CALCULATION, 1.0);

            assertThat(progress).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Overall progress increases across phases")
        void overallProgressIncreases() {
            double[] progresses = new double[Phase.values().length];

            for (int i = 0; i < Phase.values().length; i++) {
                progresses[i] = GenerationProgressListener.calculateOverallProgress(
                    Phase.values()[i], 0.5);
            }

            // Each subsequent phase should have higher overall progress
            for (int i = 1; i < progresses.length; i++) {
                assertThat(progresses[i]).isGreaterThan(progresses[i - 1]);
            }
        }

        @Test
        @DisplayName("Overall progress is proportional within phases")
        void overallProgressProportionalWithinPhase() {
            Phase phase = Phase.PLATE_ASSIGNMENT;

            double progressStart = GenerationProgressListener.calculateOverallProgress(phase, 0.0);
            double progressMiddle = GenerationProgressListener.calculateOverallProgress(phase, 0.5);
            double progressEnd = GenerationProgressListener.calculateOverallProgress(phase, 1.0);

            assertThat(progressMiddle).isGreaterThan(progressStart);
            assertThat(progressEnd).isGreaterThan(progressMiddle);
        }
    }

    // ===========================================
    // Constructor Tests
    // ===========================================

    @Nested
    @DisplayName("Constructor with listener")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with listener works")
        void constructorWithListener() {
            List<Phase> phases = new ArrayList<>();

            GenerationProgressListener listener = new GenerationProgressListener() {
                @Override
                public void onPhaseStarted(Phase phase, String description) {
                    phases.add(phase);
                }

                @Override
                public void onProgressUpdate(Phase phase, double progress) {}

                @Override
                public void onPhaseCompleted(Phase phase) {}

                @Override
                public void onGenerationCompleted() {}
            };

            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.DUEL)
                .plateCount(7)
                .build();

            PlanetGenerator generator = new PlanetGenerator(config, listener);
            GeneratedPlanet planet = generator.generate();

            assertThat(planet).isNotNull();
            assertThat(phases).hasSize(7);  // All phases were tracked
        }

        @Test
        @DisplayName("generateDefault with listener works")
        void generateDefaultWithListener() {
            boolean[] completed = {false};

            GenerationProgressListener listener = new GenerationProgressListener() {
                @Override
                public void onPhaseStarted(Phase phase, String description) {}

                @Override
                public void onProgressUpdate(Phase phase, double progress) {}

                @Override
                public void onPhaseCompleted(Phase phase) {}

                @Override
                public void onGenerationCompleted() {
                    completed[0] = true;
                }
            };

            GeneratedPlanet planet = PlanetGenerator.generateDefault(listener);

            assertThat(planet).isNotNull();
            assertThat(completed[0]).isTrue();
        }
    }
}
