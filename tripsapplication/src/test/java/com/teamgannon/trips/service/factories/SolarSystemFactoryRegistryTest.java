package com.teamgannon.trips.service.factories;

import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins {@link SolarSystemFactoryRegistry}: priority ordering, applicability
 * filtering, and graceful handling of {@code null} input. Issue 18.
 */
class SolarSystemFactoryRegistryTest {

    @Nested
    @DisplayName("select")
    class Select {

        @Test
        @DisplayName("returns the first applicable factory in injection order")
        void returnsFirstApplicable() {
            SolarSystemFactory solOnly = stub("sol", star -> "Sol".equals(star.getDisplayName()));
            SolarSystemFactory fallback = stub("fallback", star -> true);
            SolarSystemFactoryRegistry registry = new SolarSystemFactoryRegistry(List.of(solOnly, fallback));

            StarObject sol = star("Sol");
            StarObject vega = star("Vega");

            assertEquals("sol", registry.select(sol).orElseThrow().name());
            assertEquals("fallback", registry.select(vega).orElseThrow().name());
        }

        @Test
        @DisplayName("falls through to a later factory when earlier ones don't apply")
        void fallsThrough() {
            SolarSystemFactory neverApplies = stub("never", star -> false);
            SolarSystemFactory specific = stub("specific", star -> "Vega".equals(star.getDisplayName()));
            SolarSystemFactoryRegistry registry =
                    new SolarSystemFactoryRegistry(List.of(neverApplies, specific));

            assertEquals("specific", registry.select(star("Vega")).orElseThrow().name());
        }

        @Test
        @DisplayName("returns empty when no factory applies")
        void emptyWhenNoneApply() {
            SolarSystemFactoryRegistry registry = new SolarSystemFactoryRegistry(
                    List.of(stub("nope", star -> false)));

            assertTrue(registry.select(star("Sol")).isEmpty());
        }

        @Test
        @DisplayName("returns empty for null input")
        void emptyForNullStar() {
            SolarSystemFactoryRegistry registry = new SolarSystemFactoryRegistry(
                    List.of(stub("any", star -> true)));

            assertTrue(registry.select(null).isEmpty(),
                    "null star should short-circuit without consulting factories");
        }
    }

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("delegates to the selected factory")
        void delegatesToSelectedFactory() {
            StarObject star = star("Vega");
            SolarSystemFactory neverApplies = stub("never", candidate -> false);
            SolarSystemFactory selected = stub("selected", candidate -> true);
            SolarSystemFactoryRegistry registry = new SolarSystemFactoryRegistry(List.of(neverApplies, selected));

            SolarSystemFactoryResult result = registry.generate(star);

            assertEquals("selected", result.factoryName());
            assertSame(star, result.sourceStar());
        }

        @Test
        @DisplayName("throws when no factory applies")
        void throwsWhenNoFactoryApplies() {
            SolarSystemFactoryRegistry registry = new SolarSystemFactoryRegistry(List.of(
                    stub("nope", star -> false)));

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> registry.generate(star("Vega")));

            assertTrue(error.getMessage().contains("Vega"));
        }
    }

    @Nested
    @DisplayName("factoryNames")
    class FactoryNames {

        @Test
        @DisplayName("preserves injection order")
        void preservesOrder() {
            SolarSystemFactoryRegistry registry = new SolarSystemFactoryRegistry(List.of(
                    stub("alpha", star -> true),
                    stub("beta", star -> true),
                    stub("gamma", star -> true)));

            assertEquals(List.of("alpha", "beta", "gamma"), registry.factoryNames());
        }

        @Test
        @DisplayName("returns an empty list when no factories are registered")
        void emptyWhenNoFactories() {
            SolarSystemFactoryResult bogus = SolarSystemFactoryResult.alreadyExisted("anything", null);
            assertNotNull(bogus); // sanity-touch the result type so test compiles cleanly
            assertEquals(List.of(), new SolarSystemFactoryRegistry(List.of()).factoryNames());
        }
    }

    @Nested
    @DisplayName("SolarSystemFactoryResult")
    class ResultRecord {

        @Test
        @DisplayName("alreadyExisted carries zero counts")
        void alreadyExistedIsZero() {
            StarObject star = star("Sol");
            SolarSystemFactoryResult r = SolarSystemFactoryResult.alreadyExisted("sol", star);
            assertEquals("sol", r.factoryName());
            assertSame(star, r.sourceStar());
            assertEquals(0, r.planetsCreated());
            assertEquals(0, r.moonsCreated());
            assertEquals(0, r.featuresCreated());
            assertTrue(r.alreadyExisted());
            assertEquals(0, r.totalBodiesCreated());
        }

        @Test
        @DisplayName("totalBodiesCreated sums planets, moons, and features")
        void totalSums() {
            SolarSystemFactoryResult r = new SolarSystemFactoryResult(
                    "procedural-accrete", star("Vega"), 5, 3, 2, false);
            assertEquals(10, r.totalBodiesCreated());
            assertFalse(r.alreadyExisted());
        }
    }

    // ---- helpers ----

    private static StarObject star(String displayName) {
        StarObject s = new StarObject();
        s.setId(displayName + "-id");
        s.setDisplayName(displayName);
        return s;
    }

    private static SolarSystemFactory stub(String name, java.util.function.Predicate<StarObject> applies) {
        return new SolarSystemFactory() {
            @Override public String name() { return name; }
            @Override public boolean appliesTo(StarObject star) { return star != null && applies.test(star); }
            @Override public SolarSystemFactoryResult generate(StarObject star) {
                return new SolarSystemFactoryResult(name, star, 0, 0, 0, false);
            }
        };
    }
}
