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
 * Unit tests for {@link GateNetworkCatalogSeeder}. Mirrors the four existing seeder tests
 * (Station/Weapon/Transport/Megastructure): confirms the listener delegates exactly once per
 * {@code ApplicationReadyEvent} and survives a service-side exception without propagating it.
 *
 * <p>v2 Phase E.1 §5 — first seeder for a top-level catalog entity outside the sealed
 * hierarchies. Wiring matches the four existing seeders; only the service type differs.
 */
@ExtendWith(MockitoExtension.class)
class GateNetworkCatalogSeederTest {

    @Mock
    private GateNetworkDesignerService service;

    private GateNetworkCatalogSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new GateNetworkCatalogSeeder(service);
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
