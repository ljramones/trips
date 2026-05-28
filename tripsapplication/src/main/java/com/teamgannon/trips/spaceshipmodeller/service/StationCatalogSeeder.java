package com.teamgannon.trips.spaceshipmodeller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Triggers a one-shot seed of the {@code STATION_DESIGN} table from
 * {@link com.terranrepublic.assets.Catalog Catalog} once the Spring context is fully started.
 * <p>
 * Mirrors the {@code seedTemplatesIfEmpty} pattern used by {@code SpaceshipDesignerPanel}, but
 * decoupled from any UI: the table is empty at first launch even before the user opens a panel,
 * so the seed has to fire at application-ready time. {@link ApplicationReadyEvent} guarantees
 * Flyway has already run by the time this listener is invoked.
 * <p>
 * The seed is idempotent: re-runs after the first launch are no-ops because
 * {@link StationDesignerService#seedFromCatalogIfEmpty()} checks the row count before inserting.
 * v2 §6 decision Q4 pinned this behaviour.
 */
@Slf4j
@Component
public class StationCatalogSeeder {

    private final StationDesignerService stationDesignerService;

    public StationCatalogSeeder(StationDesignerService stationDesignerService) {
        this.stationDesignerService = stationDesignerService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnApplicationReady() {
        try {
            int seeded = stationDesignerService.seedFromCatalogIfEmpty();
            if (seeded > 0) {
                log.info("Phase A seed: {} station(s) inserted from Catalog into an empty STATION_DESIGN table",
                        seeded);
            }
        } catch (Exception e) {
            log.error("Phase A seed failed; stations will be empty until the next launch", e);
        }
    }
}
