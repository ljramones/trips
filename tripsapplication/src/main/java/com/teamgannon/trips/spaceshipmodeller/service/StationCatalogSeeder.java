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
 * The sync is idempotent at the row level (v2 Phase D.8 §3.1): each Catalog entry is inserted
 * only when {@code existsById} returns false. Re-runs after the first launch insert zero rows
 * because every catalog id is already present. Replaces the pre-D.8 seed-on-empty contract,
 * which silently swallowed every Catalog change after first launch.
 */
@Slf4j
@Component
public class StationCatalogSeeder {

    private final StationDesignerService stationDesignerService;

    public StationCatalogSeeder(StationDesignerService stationDesignerService) {
        this.stationDesignerService = stationDesignerService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady() {
        try {
            int inserted = stationDesignerService.syncCatalogEntries();
            if (inserted > 0) {
                log.info("Phase D.8 sync: {} new station(s) inserted from Catalog into the STATION_DESIGN table",
                        inserted);
            }
        } catch (Exception e) {
            log.error("Station catalog sync failed; stations may be incomplete until the next launch", e);
        }
    }
}
