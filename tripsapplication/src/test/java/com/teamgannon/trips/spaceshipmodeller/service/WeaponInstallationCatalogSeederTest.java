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

@ExtendWith(MockitoExtension.class)
class WeaponInstallationCatalogSeederTest {

    @Mock
    private WeaponInstallationDesignerService service;

    private WeaponInstallationCatalogSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new WeaponInstallationCatalogSeeder(service);
    }

    @Test
    @DisplayName("ApplicationReadyEvent triggers exactly one delegation to the service")
    void readyEventDelegatesOnce() {
        when(service.seedFromCatalogIfEmpty()).thenReturn(2);
        seeder.seedOnApplicationReady();
        verify(service, times(1)).seedFromCatalogIfEmpty();
    }

    @Test
    @DisplayName("Service-side exception is caught so startup is not crashed")
    void serviceExceptionIsSwallowed() {
        when(service.seedFromCatalogIfEmpty()).thenThrow(new RuntimeException("DB unreachable"));
        seeder.seedOnApplicationReady();
        verify(service, times(1)).seedFromCatalogIfEmpty();
    }
}
