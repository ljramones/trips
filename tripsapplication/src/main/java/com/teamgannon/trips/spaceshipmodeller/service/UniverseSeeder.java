package com.teamgannon.trips.spaceshipmodeller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Triggers a one-shot sync of the {@code UNIVERSE} table from
 * {@link com.terranrepublic.assets.Catalog Catalog} once the Spring context is fully started.
 *
 * <p>v2 Phase F.1 §4.2 — same architecture as the existing seeders (Station, WeaponInstallation,
 * TransportNode, Megastructure, GateNetwork). Delegates to
 * {@link UniverseDesignerService#syncCatalogEntries()}, swallowing service-side exceptions so a
 * sync failure cannot block the {@link ApplicationReadyEvent} broadcast.
 *
 * <p>Vacuous in F.1 — Catalog ships zero canonical Universe constants. The 15 universes ship via
 * the V15 Flyway migration's INSERT statements (not via Spring-side seeding). The seeder is
 * present for pipeline symmetry: every catalog entity family has a Spring-side seeder, even if
 * its content lives in migrations rather than code constants. Future F.x phases that add
 * Catalog-level Universe constants (e.g. a built-in "Sandbox" universe) activate this seeder
 * without further code change.
 */
@Slf4j
@Component
public class UniverseSeeder {

    private final UniverseDesignerService service;

    public UniverseSeeder(UniverseDesignerService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady() {
        try {
            int inserted = service.syncCatalogEntries();
            if (inserted > 0) {
                log.info("Phase F.1 sync: {} new universe(s) inserted from Catalog into the "
                        + "UNIVERSE table", inserted);
            }
        } catch (Exception e) {
            log.error("Universe catalog sync failed; table may be incomplete until the next launch", e);
        }
    }
}
