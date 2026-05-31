package com.teamgannon.trips.spaceshipmodeller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Triggers a one-shot sync of the {@code GATE_NETWORK} table from
 * {@link com.terranrepublic.assets.Catalog Catalog} once the Spring context is fully started.
 *
 * <p>v2 Phase E.1 §5 — same architecture as the four sealed-hierarchy seeders (Station,
 * WeaponInstallation, TransportNode, Megastructure). Delegates to
 * {@link GateNetworkDesignerService#syncCatalogEntries()}, swallowing service-side exceptions
 * so a sync failure cannot block the {@link ApplicationReadyEvent} broadcast.
 *
 * <p>Vacuous today — Catalog ships zero canonical {@code GateNetwork} constants in Phase E.1;
 * the seeder logs nothing on boot. Phase E.2 populates the canonical networks (Aldenata
 * Civilian/Military, Posleen, etc.) and this seeder activates without further code change.
 */
@Slf4j
@Component
public class GateNetworkCatalogSeeder {

    private final GateNetworkDesignerService service;

    public GateNetworkCatalogSeeder(GateNetworkDesignerService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady() {
        try {
            int inserted = service.syncCatalogEntries();
            if (inserted > 0) {
                log.info("Phase E.1 sync: {} new gate network(s) inserted from Catalog into the "
                        + "GATE_NETWORK table", inserted);
            }
        } catch (Exception e) {
            log.error("Gate-network catalog sync failed; table may be incomplete until the next launch", e);
        }
    }
}
