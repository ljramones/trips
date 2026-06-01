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
 * Unit tests for {@link UniverseSeeder}. Mirrors the existing seeder tests
 * (Station/Weapon/Transport/Megastructure/GateNetwork): confirms the listener delegates exactly
 * once per {@code ApplicationReadyEvent} and survives a service-side exception without
 * propagating it.
 *
 * <p>v2 Phase F.1 §4.2 — first F-series catalog seeder. Wiring matches the five existing
 * seeders; only the service type differs. Vacuous in F.1 (Catalog ships no canonical Universe
 * constants; the 15 rows ship via V16 migration INSERTs).
 */
@ExtendWith(MockitoExtension.class)
class UniverseSeederTest {

    @Mock
    private UniverseDesignerService service;

    private UniverseSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new UniverseSeeder(service);
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
        seeder.syncOnApplicationReady();
        verify(service, times(1)).syncCatalogEntries();
    }
}
