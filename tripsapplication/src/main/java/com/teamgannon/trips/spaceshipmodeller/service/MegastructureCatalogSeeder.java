package com.teamgannon.trips.spaceshipmodeller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Triggers a one-shot sync of the {@code MEGASTRUCTURE} table from
 * {@link com.terranrepublic.assets.Catalog Catalog} once the Spring context is fully started.
 * <p>
 * Mirrors {@link StationCatalogSeeder}: decoupled from any UI, fires on
 * {@link ApplicationReadyEvent} (which guarantees Flyway has already run), delegates to the
 * service's {@code syncCatalogEntries} method, swallows service-side exceptions so a failed
 * sync doesn't crash the application.
 * <p>
 * v2 Phase D.8 §3.1 — the sync is insert-only: on every launch, missing catalog entries are
 * inserted, existing rows are left untouched (no overwrite, no orphan deletion). Re-runs are
 * cheap no-ops once the catalog is fully seeded.
 */
@Slf4j
@Component
public class MegastructureCatalogSeeder {

    private final MegastructureDesignerService service;

    public MegastructureCatalogSeeder(MegastructureDesignerService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady() {
        try {
            int inserted = service.syncCatalogEntries();
            if (inserted > 0) {
                log.info("Phase D.8 sync: {} new megastructure(s) inserted from Catalog into the "
                        + "MEGASTRUCTURE table", inserted);
            }
        } catch (Exception e) {
            log.error("Megastructure catalog sync failed; the table may be incomplete until the next launch", e);
        }
    }
}
