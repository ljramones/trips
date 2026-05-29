package com.teamgannon.trips.spaceshipmodeller.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransportNodeCatalogSeeder}. Mirrors the
 * {@link StationCatalogSeederTest} pattern: confirms the listener delegates exactly once per
 * {@code ApplicationReadyEvent} and survives a service-side exception without propagating it.
 * <p>
 * The sync-by-id contract itself lives on the service (and is covered there); this class is
 * focused on the wiring guarantee — namely, that the new seeder bean (added in v2 Phase D.8
 * Step 5) is actually invoked, mirroring the StationCatalogSeeder / WeaponInstallationCatalogSeeder
 * / MegastructureCatalogSeeder behaviour. Prior to D.8, this wiring did not exist —
 * {@link TransportNodeService#syncCatalogEntries()} was an unwired method waiting for a future
 * seeder.
 */
@ExtendWith(MockitoExtension.class)
class TransportNodeCatalogSeederTest {

    @Mock
    private TransportNodeService service;

    private TransportNodeCatalogSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new TransportNodeCatalogSeeder(service);
    }

    @Test
    @DisplayName("ApplicationReadyEvent triggers exactly one delegation to syncCatalogEntries")
    void readyEventDelegatesOnce() {
        when(service.syncCatalogEntries()).thenReturn(0);
        seeder.syncOnApplicationReady();
        verify(service, times(1)).syncCatalogEntries();
    }

    @Test
    @DisplayName("Service-side exception is caught so startup is not crashed")
    void serviceExceptionIsSwallowed() {
        when(service.syncCatalogEntries()).thenThrow(new RuntimeException("DB unreachable"));
        // No throwable should escape the listener — this would crash Spring's
        // ApplicationReadyEvent broadcaster otherwise and the rest of the
        // application would not see the event.
        seeder.syncOnApplicationReady();
        verify(service, times(1)).syncCatalogEntries();
    }
}
