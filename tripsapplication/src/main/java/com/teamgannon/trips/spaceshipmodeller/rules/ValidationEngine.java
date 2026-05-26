package com.teamgannon.trips.spaceshipmodeller.rules;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DesignConstraint;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveSpecs;
import com.teamgannon.trips.spaceshipmodeller.propulsion.ThrustLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies physics- and rules-of-thumb-based validation to a {@link SpaceshipDesign}.
 * <p>
 * The engine is stateless and side-effect free: {@link #validate(SpaceshipDesign)} gathers every finding
 * into a {@link ValidationResult}. Rules are grouped into focused private methods so the rule set can grow
 * without the entry point becoming unwieldy.
 * <p>
 * Current rule groups:
 * <ul>
 *   <li>mass budget &mdash; mass ratio sanity and the drive's minimum dry-mass fraction;</li>
 *   <li>radiators &mdash; high-efficiency drives must allocate radiator mass;</li>
 *   <li>landing/atmosphere &mdash; classes that land need landing-capable propulsion;</li>
 *   <li>mothership &mdash; only carrier-capable hulls may carry craft, with no nested motherships;</li>
 *   <li>crew &mdash; crew aboard implies life-support mass;</li>
 *   <li>delta-V &mdash; a first-order plausibility check;</li>
 *   <li>drive constraints &mdash; surfaces the drive's inherent operational limits.</li>
 * </ul>
 *
 * @author TRIPS Spaceship Modeller
 */
public class ValidationEngine {

    /** Propellant fraction above which a design is flagged as implausibly tankage-heavy. */
    private static final double EXTREME_PROPELLANT_FRACTION = 0.95;

    /** Delta-V (km/s) below which an in-system design is warned as under-fuelled. */
    private static final double LOW_DELTA_V_KMPS = 1.0;

    /**
     * Validates a complete design.
     *
     * @param design the design to check; must not be {@code null}
     * @return a result containing all errors, warnings and informational findings
     */
    public ValidationResult validate(SpaceshipDesign design) {
        if (design == null) {
            throw new IllegalArgumentException("design must not be null");
        }
        List<ValidationMessage> messages = new ArrayList<>();

        validateMassBudget(design, messages);
        validateRadiators(design, messages);
        validateLandingCapability(design, messages);
        validateMothership(design, messages);
        validateCrew(design, messages);
        validateDeltaV(design, messages);
        validateDriveConstraints(design, messages);

        return new ValidationResult(messages);
    }

    private void validateMassBudget(SpaceshipDesign design, List<ValidationMessage> out) {
        MassBudget budget = design.massBudget();
        DriveSpecs specs = design.driveSpecs();

        if (budget.dryMassTons() <= 0) {
            out.add(ValidationMessage.error("MASS_NO_DRY",
                    "The design has no dry mass; structure, engine and payload masses are all zero."));
            return;
        }

        // Reactionless drives legitimately carry no propellant, so skip the ratio checks for them.
        if (!specs.reactionless()) {
            double massRatio = budget.massRatio();
            if (massRatio <= 1.0) {
                out.add(ValidationMessage.error("MASS_NO_PROPELLANT",
                        "Mass ratio is %.2f: the design carries no propellant and can produce no delta-V."
                                .formatted(massRatio)));
            }
            if (budget.propellantFraction() > EXTREME_PROPELLANT_FRACTION) {
                out.add(ValidationMessage.warning("MASS_EXTREME_PROPELLANT",
                        "Propellant is %.0f%% of wet mass; tankage and structure may be unrealistically light."
                                .formatted(budget.propellantFraction() * 100)));
            }
        }

        double dryPercent = budget.dryMassPercent();
        if (dryPercent < specs.minDryMassPercent()) {
            out.add(ValidationMessage.error("MASS_DRY_TOO_LOW",
                    ("Dry mass is %.1f%% of wet mass, below the %.0f%% minimum for the %s; "
                            + "the structure cannot survive this propellant fraction.")
                            .formatted(dryPercent, specs.minDryMassPercent(), design.driveType().name())));
        }
    }

    private void validateRadiators(SpaceshipDesign design, List<ValidationMessage> out) {
        DriveSpecs specs = design.driveSpecs();
        double radiatorMass = design.massBudget().radiatorMassTons();

        if (specs.requiresRadiators() && radiatorMass <= 0) {
            out.add(ValidationMessage.error("RADIATOR_MISSING",
                    ("The %s demands %s waste-heat radiators, but no radiator mass is allocated.")
                            .formatted(design.driveType().name(), specs.radiatorLevel().label())));
        } else if (!specs.requiresRadiators() && radiatorMass > 0) {
            out.add(ValidationMessage.info("RADIATOR_UNNEEDED",
                    "Radiator mass is allocated, but the %s needs no dedicated radiators."
                            .formatted(design.driveType().name())));
        }
    }

    private void validateLandingCapability(SpaceshipDesign design, List<ValidationMessage> out) {
        if (!design.shipClass().routinelyLands()) {
            return;
        }
        DriveSpecs specs = design.driveSpecs();

        if (!specs.suitableForLanding()) {
            out.add(ValidationMessage.error("LANDING_DRIVE_UNSUITABLE",
                    ("A %s is expected to land, but the %s cannot (it is not landing-capable or its "
                            + "thrust-to-weight never exceeds 1).")
                            .formatted(design.shipClass().label(), design.driveType().name())));
        }
        if (!specs.atmosphereCapable()) {
            out.add(ValidationMessage.warning("LANDING_NO_ATMOSPHERE",
                    ("The %s cannot operate in an atmosphere; this %s is limited to airless-body landings.")
                            .formatted(design.driveType().name(), design.shipClass().label())));
        }
        if (specs.hasConstraint("RADIOACTIVE_EXHAUST") || specs.hasConstraint("NUCLEAR_FALLOUT")) {
            out.add(ValidationMessage.error("LANDING_HAZARDOUS_EXHAUST",
                    ("The %s produces hazardous exhaust and must not land on or near an inhabited surface.")
                            .formatted(design.driveType().name())));
        }
    }

    private void validateMothership(SpaceshipDesign design, List<ValidationMessage> out) {
        List<CarriedCraft> carried = design.carriedCraft();
        if (carried.isEmpty()) {
            return;
        }

        if (!design.shipClass().carrierCapable()) {
            out.add(ValidationMessage.error("CARRY_NOT_CAPABLE",
                    "A %s cannot carry other craft; only carrier-capable classes may."
                            .formatted(design.shipClass().label())));
        }

        for (CarriedCraft craft : carried) {
            if (craft.shipClass().carrierCapable()) {
                out.add(ValidationMessage.error("CARRY_NESTED_MOTHERSHIP",
                        "Cannot carry a %s (\"%s\"): carrier-class craft cannot themselves be carried."
                                .formatted(craft.shipClass().label(), craft.name())));
            } else if (!craft.shipClass().carriable()) {
                out.add(ValidationMessage.error("CARRY_CLASS_TOO_LARGE",
                        "Cannot carry a %s (\"%s\"): the class is too large to be embarked."
                                .formatted(craft.shipClass().label(), craft.name())));
            }
        }

        double carriedMass = design.totalCarriedMassTons();
        double payload = design.massBudget().payloadMassTons();
        if (carriedMass > payload) {
            out.add(ValidationMessage.error("CARRY_EXCEEDS_PAYLOAD",
                    ("Carried craft mass (%.0f t) exceeds the payload allowance (%.0f t).")
                            .formatted(carriedMass, payload)));
        } else if (payload > 0 && carriedMass > 0.8 * payload) {
            out.add(ValidationMessage.warning("CARRY_PAYLOAD_TIGHT",
                    ("Carried craft use %.0f%% of the payload allowance, leaving little for cargo or stores.")
                            .formatted(carriedMass / payload * 100)));
        }
    }

    private void validateCrew(SpaceshipDesign design, List<ValidationMessage> out) {
        if (design.crewComplement() > 0 && design.massBudget().crewMassTons() <= 0) {
            out.add(ValidationMessage.warning("CREW_NO_LIFE_SUPPORT",
                    ("%d crew are aboard, but no crew/life-support mass is budgeted.")
                            .formatted(design.crewComplement())));
        }
    }

    private void validateDeltaV(SpaceshipDesign design, List<ValidationMessage> out) {
        double deltaV = design.estimateDeltaVKmps();
        if (Double.isNaN(deltaV)) {
            out.add(ValidationMessage.info("DELTAV_NA",
                    ("The %s is reaction-mass-free; the rocket-equation delta-V estimate does not apply.")
                            .formatted(design.driveType().name())));
            return;
        }
        if (deltaV < LOW_DELTA_V_KMPS) {
            out.add(ValidationMessage.warning("DELTAV_LOW",
                    ("Estimated delta-V is only %.2f km/s; the design may be unable to complete useful manoeuvres.")
                            .formatted(deltaV)));
        }

        // Heavy hulls on feeble drives manoeuvre impractically slowly.
        ThrustLevel thrust = design.driveSpecs().thrustLevel();
        if (design.shipClass().carrierCapable() && thrust.ordinal() <= ThrustLevel.VERY_LOW.ordinal()) {
            out.add(ValidationMessage.warning("THRUST_LOW_FOR_HULL",
                    ("%s thrust on a %s implies very long acceleration times for so large a hull.")
                            .formatted(thrust.label(), design.shipClass().label())));
        }
    }

    private void validateDriveConstraints(SpaceshipDesign design, List<ValidationMessage> out) {
        for (DesignConstraint constraint : design.driveSpecs().blockingConstraints()) {
            out.add(ValidationMessage.info("DRIVE_CONSTRAINT",
                    ("%s inherent limit [%s]: %s")
                            .formatted(design.driveType().name(), constraint.code(), constraint.description())));
        }
    }
}
