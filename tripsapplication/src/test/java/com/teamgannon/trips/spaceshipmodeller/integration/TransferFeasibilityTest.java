package com.teamgannon.trips.spaceshipmodeller.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests for the three-level {@link TransferFeasibility} rules and tolerances. */
class TransferFeasibilityTest {

    @Test
    @DisplayName("propellant: less = feasible, equal = marginal, more = insufficient")
    void propellantBands() {
        assertEquals(Feasibility.FEASIBLE, TransferFeasibility.propellantStatus(2000, 3000));
        assertEquals(Feasibility.MARGINAL, TransferFeasibility.propellantStatus(3000, 3000));
        assertEquals(Feasibility.INSUFFICIENT, TransferFeasibility.propellantStatus(3100, 3000));
    }

    @Test
    @DisplayName("a hair over from floating point still reads marginal, not insufficient")
    void propellantFloatingPointTieIsMarginal() {
        assertEquals(Feasibility.MARGINAL, TransferFeasibility.propellantStatus(3000.0000001, 3000.0));
    }

    @Test
    @DisplayName("delta-V: >0.5 feasible, 0..0.5 marginal, <0 insufficient")
    void deltaVBands() {
        assertEquals(Feasibility.FEASIBLE, TransferFeasibility.deltaVStatus(10.0, 11.0));
        assertEquals(Feasibility.MARGINAL, TransferFeasibility.deltaVStatus(10.0, 10.3));
        assertEquals(Feasibility.MARGINAL, TransferFeasibility.deltaVStatus(10.0, 10.0));
        assertEquals(Feasibility.INSUFFICIENT, TransferFeasibility.deltaVStatus(10.0, 9.0));
    }

    @Test
    @DisplayName("zero-Δv transfers (sails/wormholes) are feasible regardless of budget")
    void zeroDeltaVIsFeasible() {
        assertEquals(Feasibility.FEASIBLE, TransferFeasibility.deltaVStatus(0.0, Double.NaN));
    }

    @Test
    @DisplayName("overall result is the worse of the two axes")
    void overallIsWorstOfTwo() {
        // plenty of Δv, but propellant exactly used -> marginal overall
        assertEquals(Feasibility.MARGINAL, TransferFeasibility.evaluate(5.0, 5000.0, 3000.0, 3000.0));
        // comfortable on both -> feasible
        assertEquals(Feasibility.FEASIBLE, TransferFeasibility.evaluate(5.0, 5000.0, 100.0, 3000.0));
        // not enough Δv -> insufficient regardless of propellant
        assertEquals(Feasibility.INSUFFICIENT, TransferFeasibility.evaluate(50.0, 5.0, 10.0, 3000.0));
    }
}
