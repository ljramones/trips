package com.teamgannon.trips.spaceshipmodeller.rules;

/**
 * Severity of a {@link ValidationMessage}.
 *
 * @author TRIPS Spaceship Modeller
 */
public enum Severity {

    /** A physically or logically invalid design that must be corrected. */
    ERROR,

    /** A plausible but questionable choice the designer should reconsider. */
    WARNING,

    /** Informational note; no action required. */
    INFO
}
