package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * Cost/performance class of a {@link TransferType}, driving UI colour coding.
 */
public enum TransferCost {

    /** Cheap on delta-V (UI: green). */
    EFFICIENT,

    /** Expensive but fast (UI: orange). */
    EXPENSIVE_FAST,

    /** Speculative / exotic (UI: purple). */
    EXOTIC
}
