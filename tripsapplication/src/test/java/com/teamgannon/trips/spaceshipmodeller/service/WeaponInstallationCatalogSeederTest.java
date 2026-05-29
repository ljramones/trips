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
    @DisplayName("ApplicationReadyEvent triggers exactly one delegation to syncCatalogEntries")
    void readyEventDelegatesOnce() {
        when(service.syncCatalogEntries()).thenReturn(2);
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
