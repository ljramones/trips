package com.terranrepublic.assets;

/**
 * How much a {@link SpaceAsset} can move under its own control.
 *
 * <p>v2 Phase D.7 extended the original three station-scoped values
 * ({@code FIXED}, {@code STATIONKEEPING}, {@code MANEUVERABLE}) with three additional
 * values covering the megastructure scale. The axis is the same — "how much can the
 * object move?" — but at megastructure scale the distinction between "limited",
 * "freely mobile", and "autonomously interstellar" becomes meaningful (Troy is
 * MOBILE_LIMITED with ORION pulses; the Death Star is MOBILE; Dahak is
 * MOBILE_AUTONOMOUS as a starfaring vessel disguised as a moon).
 *
 * <p>The pre-Phase-D.7 {@link StationDesign} invariant "FIXED forbids auxiliaryDrive"
 * remains unchanged; the new values all permit an auxiliary drive.
 *
 * <p>Declaration order matters: combobox population sites ({@code Mobility.values()})
 * present these in ascending order of mobility, which is the user-facing reading.
 */
public enum Mobility {
    FIXED,
    STATIONKEEPING,
    MANEUVERABLE,
    MOBILE_LIMITED,
    MOBILE,
    MOBILE_AUTONOMOUS
}
