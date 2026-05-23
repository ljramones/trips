package com.teamgannon.trips.spaceshipmodeller.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link MassBudget} arithmetic and validation. */
class MassBudgetTest {

    @Test
    @DisplayName("dry / wet / ratio / fractions compute correctly")
    void coreArithmetic() {
        MassBudget m = new MassBudget(100, 50, 200, 20, 10, 20);
        assertEquals(200, m.dryMassTons(), 1e-9);
        assertEquals(400, m.wetMassTons(), 1e-9);
        assertEquals(2.0, m.massRatio(), 1e-9);
        assertEquals(0.5, m.propellantFraction(), 1e-9);
        assertEquals(50.0, m.dryMassPercent(), 1e-9);
    }

    @Test
    @DisplayName("zero dry mass yields an infinite ratio")
    void zeroDryGivesInfiniteRatio() {
        assertTrue(Double.isInfinite(new MassBudget(0, 0, 100, 0, 0, 0).massRatio()));
    }

    @Test
    @DisplayName("no propellant yields a ratio of 1")
    void noPropellantRatioIsOne() {
        assertEquals(1.0, new MassBudget(100, 0, 0, 0, 0, 0).massRatio(), 1e-9);
    }

    @Test
    @DisplayName("negative mass components are rejected")
    void negativeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MassBudget(-1, 0, 0, 0, 0, 0));
    }
}
