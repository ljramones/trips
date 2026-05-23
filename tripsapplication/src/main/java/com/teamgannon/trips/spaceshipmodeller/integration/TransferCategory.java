package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * Broad grouping of {@link TransferType}s, used to organise the UI selector.
 */
public enum TransferCategory {

    /** Established orbital-mechanics transfers. */
    REALISTIC("Realistic"),

    /** Hard-SF but physically grounded high-performance transfers. */
    ADVANCED("Advanced"),

    /** Speculative or theoretical concepts; formulas are illustrative. */
    EXOTIC("Exotic");

    private final String label;

    TransferCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
