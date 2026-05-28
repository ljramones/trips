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
 * Unit tests for {@link StationCatalogSeeder}. The seed-on-empty *logic* lives on the service
 * (and is covered there); this class confirms that the listener delegates exactly once per
 * application-ready event and survives a service-side exception without propagating it.
 */
@ExtendWith(MockitoExtension.class)
class StationCatalogSeederTest {

    @Mock
    private StationDesignerService service;

    private StationCatalogSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new StationCatalogSeeder(service);
    }

    @Test
    @DisplayName("ApplicationReadyEvent triggers exactly one delegation to the service")
    void readyEventDelegatesOnce() {
        when(service.seedFromCatalogIfEmpty()).thenReturn(1);
        seeder.seedOnApplicationReady();
        verify(service, times(1)).seedFromCatalogIfEmpty();
    }

    @Test
    @DisplayName("Service-side exception is caught so it does not crash the startup")
    void serviceExceptionIsSwallowed() {
        when(service.seedFromCatalogIfEmpty()).thenThrow(new RuntimeException("DB unreachable"));
        // No throwable should escape the listener — this would crash Spring's
        // ApplicationReadyEvent broadcaster otherwise and the rest of the
        // application would not see the event.
        seeder.seedOnApplicationReady();
        verify(service, times(1)).seedFromCatalogIfEmpty();
    }
}
