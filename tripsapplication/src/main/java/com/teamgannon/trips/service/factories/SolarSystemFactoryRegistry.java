package com.teamgannon.trips.service.factories;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Looks up the {@link SolarSystemFactory} that should build a system for a
 * given star. Spring injects every {@code SolarSystemFactory} bean into the
 * constructor in {@link org.springframework.core.annotation.Order @Order}
 * sequence; the registry returns the first one whose {@link
 * SolarSystemFactory#appliesTo} returns {@code true}.
 * <p>
 * Issue 18 of the codebase-review remediation — see {@link SolarSystemFactory}.
 */
@Slf4j
@Component
public class SolarSystemFactoryRegistry {

    private final List<SolarSystemFactory> factories;

    public SolarSystemFactoryRegistry(List<SolarSystemFactory> factories) {
        this.factories = List.copyOf(factories);
        log.info("SolarSystemFactoryRegistry initialised with {} factory(ies): {}",
                this.factories.size(),
                this.factories.stream().map(SolarSystemFactory::name).toList());
    }

    /**
     * Find the highest-priority factory that claims the given star.
     * Empty if no factory applies (in practice the procedural factory's
     * {@code appliesTo} returns {@code true} for any star, so this should
     * not happen in production).
     */
    public Optional<SolarSystemFactory> select(StarObject star) {
        if (star == null) {
            return Optional.empty();
        }
        return factories.stream()
                .filter(f -> f.appliesTo(star))
                .findFirst();
    }

    /** Test/inspection accessor: list of factory names in priority order. */
    public List<String> factoryNames() {
        return factories.stream().map(SolarSystemFactory::name).toList();
    }
}
