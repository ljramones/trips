package com.teamgannon.trips.spaceshipmodeller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Triggers a one-shot sync of the {@code WEAPON_INSTALLATION} table from
 * {@link com.terranrepublic.assets.Catalog Catalog} once the Spring context is fully started.
 * <p>
 * Same architecture as {@link StationCatalogSeeder}: a separate {@code @Component} listener that
 * delegates to the service's {@code syncCatalogEntries()}, swallowing service-side exceptions so
 * a sync failure cannot block the {@link ApplicationReadyEvent} broadcast.
 * <p>
 * v2 Phase D.8 §3.1 — the sync is insert-only and idempotent at the row level: each Catalog
 * entry is inserted only when {@code existsById} returns false. Re-runs after the first launch
 * insert zero rows.
 */
@Slf4j
@Component
public class WeaponInstallationCatalogSeeder {

    private final WeaponInstallationDesignerService service;

    public WeaponInstallationCatalogSeeder(WeaponInstallationDesignerService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady() {
        try {
            int inserted = service.syncCatalogEntries();
            if (inserted > 0) {
                log.info("Phase D.8 sync: {} new weapon installation(s) inserted from Catalog into the "
                        + "WEAPON_INSTALLATION table", inserted);
            }
        } catch (Exception e) {
            log.error("Weapon-installation catalog sync failed; table may be incomplete until the next launch", e);
        }
    }
}
