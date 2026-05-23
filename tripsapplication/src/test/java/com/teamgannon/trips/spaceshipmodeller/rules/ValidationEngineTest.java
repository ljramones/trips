package com.teamgannon.trips.spaceshipmodeller.rules;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the rule set in {@link ValidationEngine}. */
class ValidationEngineTest {

    private final ValidationEngine engine = new ValidationEngine();

    private static boolean hasCode(ValidationResult result, String code) {
        return result.messages().stream().anyMatch(m -> m.code().equals(code));
    }

    @Test
    @DisplayName("a well-formed fusion frigate is valid")
    void validFusionFrigatePasses() {
        SpaceshipDesign d = SpaceshipBuilder.create("Roci")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.FUSION_TORCH)
                .structureTons(200).engineTons(150).propellantTons(450)
                .payloadTons(50).crewTons(20).radiatorTons(120).crew(4).build();
        assertTrue(engine.validate(d).isValid());
    }

    @Test
    @DisplayName("dry mass below the drive's minimum is an error")
    void lowDryMassFlagged() {
        SpaceshipDesign d = SpaceshipBuilder.create("Bad")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.VASIMR)
                .structureTons(5).engineTons(3).propellantTons(500)
                .payloadTons(1).radiatorTons(30).build();
        ValidationResult r = engine.validate(d);
        assertFalse(r.isValid());
        assertTrue(hasCode(r, "MASS_DRY_TOO_LOW"));
    }

    @Test
    @DisplayName("a radiator-hungry drive without radiators is an error")
    void missingRadiatorsFlagged() {
        SpaceshipDesign d = SpaceshipBuilder.create("NoRad")
                .shipClass(ShipClass.CRUISER).driveType(DriveType.VASIMR)
                .structureTons(100).engineTons(50).propellantTons(50)
                .payloadTons(20).radiatorTons(0).build();
        assertTrue(hasCode(engine.validate(d), "RADIATOR_MISSING"));
    }

    @Test
    @DisplayName("a lander on a non-landing drive is an error")
    void landerWithNonLandingDriveFlagged() {
        SpaceshipDesign d = SpaceshipBuilder.create("L")
                .shipClass(ShipClass.LANDER).driveType(DriveType.ION_GRIDDED)
                .structureTons(50).engineTons(20).propellantTons(20)
                .payloadTons(10).radiatorTons(5).build();
        assertTrue(hasCode(engine.validate(d), "LANDING_DRIVE_UNSUITABLE"));
    }

    @Test
    @DisplayName("a non-carrier hull carrying craft is an error")
    void nonCarrierCarryingCraftFlagged() {
        SpaceshipDesign d = SpaceshipBuilder.create("C")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(100).engineTons(40).propellantTons(60).payloadTons(50)
                .carry("F", ShipClass.FIGHTER, 2, 8, "x").build();
        assertTrue(hasCode(engine.validate(d), "CARRY_NOT_CAPABLE"));
    }

    @Test
    @DisplayName("carrying a carrier-class craft (nested mothership) is an error")
    void nestedMothershipFlagged() {
        SpaceshipDesign d = SpaceshipBuilder.create("M")
                .shipClass(ShipClass.MOTHERSHIP).driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(1000).engineTons(400).propellantTons(600).payloadTons(2000)
                .carry("Sub", ShipClass.CARRIER, 1, 500, "x").build();
        assertTrue(hasCode(engine.validate(d), "CARRY_NESTED_MOTHERSHIP"));
    }

    @Test
    @DisplayName("carried mass exceeding the payload allowance is an error")
    void carriedExceedingPayloadFlagged() {
        SpaceshipDesign d = SpaceshipBuilder.create("M2")
                .shipClass(ShipClass.MOTHERSHIP).driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(1000).engineTons(400).propellantTons(600).payloadTons(10)
                .carry("F", ShipClass.FIGHTER, 12, 8, "x").build();
        assertTrue(hasCode(engine.validate(d), "CARRY_EXCEEDS_PAYLOAD"));
    }

    @Test
    @DisplayName("a cruiser may now carry small craft (carrier-capable)")
    void cruiserCanCarryCraft() {
        SpaceshipDesign d = SpaceshipBuilder.create("CA")
                .shipClass(ShipClass.CRUISER).driveType(DriveType.FUSION_TORCH)
                .structureTons(800).engineTons(400).propellantTons(1000)
                .payloadTons(300).crewTons(150).radiatorTons(400)
                .carry("Pinnace", ShipClass.SHUTTLE, 2, 40, "boarding").build();
        ValidationResult r = engine.validate(d);
        assertFalse(hasCode(r, "CARRY_NOT_CAPABLE"));
        assertTrue(r.isValid());
    }

    @Test
    @DisplayName("a reactionless drive produces a delta-V N/A info note")
    void reactionlessProducesInfo() {
        SpaceshipDesign d = SpaceshipBuilder.create("Sail")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.SOLAR_SAIL)
                .structureTons(1).payloadTons(1).build();
        assertTrue(hasCode(engine.validate(d), "DELTAV_NA"));
    }
}
