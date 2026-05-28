package com.teamgannon.trips.spaceshipmodeller.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Triggers a one-shot seed of the {@code WEAPON_INSTALLATION} table from
 * {@link com.terranrepublic.assets.Catalog Catalog} once the Spring context is fully started.
 * <p>
 * Same architecture as {@link StationCatalogSeeder}: a separate {@code @Component} listener that
 * delegates to the service's idempotent {@code seedFromCatalogIfEmpty()}, swallowing service-side
 * exceptions so a seed failure cannot block the {@link ApplicationReadyEvent} broadcast.
 */
@Slf4j
@Component
public class WeaponInstallationCatalogSeeder {

    private final WeaponInstallationDesignerService service;

    public WeaponInstallationCatalogSeeder(WeaponInstallationDesignerService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnApplicationReady() {
        try {
            int seeded = service.seedFromCatalogIfEmpty();
            if (seeded > 0) {
                log.info("Phase B seed: {} weapon installation(s) inserted from Catalog into an empty "
                        + "WEAPON_INSTALLATION table", seeded);
            }
        } catch (Exception e) {
            log.error("Phase B weapon-installation seed failed; table will be empty until the next launch", e);
        }
    }
}
