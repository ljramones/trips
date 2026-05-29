package com.teamgannon.trips.spaceshipmodeller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Triggers a one-shot sync of the {@code TRANSPORT_NODE} table from
 * {@link com.terranrepublic.assets.Catalog Catalog} once the Spring context is fully started.
 * <p>
 * Same architecture as {@link StationCatalogSeeder} / {@link WeaponInstallationCatalogSeeder} /
 * {@link MegastructureCatalogSeeder}: a separate {@code @Component} listener that delegates to
 * {@link TransportNodeService#syncCatalogEntries()}, swallowing service-side exceptions so a
 * sync failure cannot block the {@link ApplicationReadyEvent} broadcast.
 * <p>
 * v2 Phase D.8 §3.1 — the sync is insert-only and idempotent at the row level: each Catalog
 * entry is inserted only when {@code existsById} returns false. {@code Catalog.all()} carries
 * no canonical {@link com.terranrepublic.infrastructure.TransportNode} entries today, so the
 * sync is vacuous and the seeder logs nothing on every boot. When a future Catalog adds
 * transport-node constants, the seeder activates without further code change. This brings the
 * four-subtype pipeline (Station / WeaponInstallation / TransportNode / Megastructure) into
 * full symmetry at the seeder layer.
 */
@Slf4j
@Component
public class TransportNodeCatalogSeeder {

    private final TransportNodeService service;

    public TransportNodeCatalogSeeder(TransportNodeService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady() {
        try {
            int inserted = service.syncCatalogEntries();
            if (inserted > 0) {
                log.info("Phase D.8 sync: {} new transport node(s) inserted from Catalog into the "
                        + "TRANSPORT_NODE table", inserted);
            }
        } catch (Exception e) {
            log.error("Transport-node catalog sync failed; table may be incomplete until the next launch", e);
        }
    }
}
