package com.teamgannon.trips.service.factories;

import com.teamgannon.trips.jpa.model.StarObject;

/**
 * Strategy interface for building a solar system around a given star.
 * <p>
 * Issue 18 of the codebase-review remediation: Sol was historically a
 * "magical special case" — a 1,300-line {@code SolPlanetsInitializer} that
 * hard-wired the eight planets + Pluto + moons + belts, completely bypassing
 * the procedural (ACCRETE) pipeline that every other star uses.
 * <p>
 * This interface lets both paths participate in the same lifecycle. A
 * caller asks {@link SolarSystemFactoryRegistry} for the factory that
 * applies to a given {@link StarObject}, and invokes {@link #generate} —
 * the implementation decides whether to read hand-curated data (Sol) or run
 * the procedural simulation (every other star).
 * <p>
 * Implementations are Spring {@code @Component}s. Spring injects them into
 * the registry in {@link org.springframework.core.annotation.Order @Order}
 * sequence (lower value = higher priority); the registry picks the first
 * one whose {@link #appliesTo} returns {@code true}.
 */
public interface SolarSystemFactory {

    /** Human-readable factory name, used in logs and the result record. */
    String name();

    /**
     * Does this factory know how to build a system for this star?
     * For Sol-specific factories this is typically {@code displayName.equalsIgnoreCase("Sol")};
     * fallback factories (procedural / accrete) return {@code true} for any star.
     */
    boolean appliesTo(StarObject star);

    /**
     * Build and persist the solar system for the given star. Implementations
     * are expected to be idempotent — if a system already exists for the
     * star, the factory should detect it and return a {@code result} with
     * {@code alreadyExisted=true} rather than duplicating rows.
     */
    SolarSystemFactoryResult generate(StarObject star);
}
