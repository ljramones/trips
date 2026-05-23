package com.teamgannon.trips.spaceshipmodeller.builder;

import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Tests for {@link SpaceshipBuilder}. */
class SpaceshipBuilderTest {

    @Test
    @DisplayName("builds a design with id, timestamp and name")
    void buildsBasicDesign() {
        SpaceshipDesign d = SpaceshipBuilder.create("X")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.CHEMICAL_BIPROPELLANT).build();
        assertNotNull(d.id());
        assertNotNull(d.createdAt());
        assertEquals("X", d.name());
    }

    @Test
    @DisplayName("name is required")
    void requiresName() {
        assertThrows(NullPointerException.class, () -> SpaceshipBuilder.create(null)
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.CHEMICAL_BIPROPELLANT).build());
    }

    @Test
    @DisplayName("ship class and drive are required")
    void requiresClassAndDrive() {
        assertThrows(NullPointerException.class, () -> SpaceshipBuilder.create("X").build());
    }

    @Test
    @DisplayName("carried craft are accumulated")
    void carriesCraft() {
        SpaceshipDesign d = SpaceshipBuilder.create("M")
                .shipClass(ShipClass.MOTHERSHIP).driveType(DriveType.NUCLEAR_THERMAL)
                .carry("V", ShipClass.FIGHTER, 4, 8, "escort").build();
        assertEquals(1, d.carriedCraft().size());
        assertEquals(32.0, d.totalCarriedMassTons(), 1e-9);
    }
}
