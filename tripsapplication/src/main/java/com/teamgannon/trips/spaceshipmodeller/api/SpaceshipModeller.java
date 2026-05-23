package com.teamgannon.trips.spaceshipmodeller.api;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.Category;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationEngine;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationResult;

import java.util.List;

/**
 * Public facade for the Spaceship Modeller module.
 * <p>
 * This is the single entry point the rest of TRIPS should depend on: it hides the {@code builder},
 * {@code rules} and {@code propulsion} packages behind a small, stable surface. It is deliberately a plain
 * object (no Spring annotations) so it can be used directly, unit-tested, or registered as a
 * {@code @Bean}/{@code @Component} by the application layer when wiring it into the JavaFX UI, the transfer
 * planner, or the 3D view.
 *
 * <pre>{@code
 * SpaceshipModeller modeller = new SpaceshipModeller();
 * SpaceshipDesign design = modeller.newDesign("Donnager")
 *         .shipClass(ShipClass.MOTHERSHIP)
 *         .driveType(DriveType.FUSION_TORCH)
 *         // ...mass and carried craft...
 *         .build();
 * ValidationResult result = modeller.validate(design);
 * }</pre>
 *
 * @author TRIPS Spaceship Modeller
 */
public class SpaceshipModeller {

    private final ValidationEngine validationEngine;

    /** Creates a modeller with the default {@link ValidationEngine}. */
    public SpaceshipModeller() {
        this(new ValidationEngine());
    }

    /**
     * Creates a modeller with a supplied validation engine (useful for tests or custom rule sets).
     *
     * @param validationEngine the engine to validate designs with
     */
    public SpaceshipModeller(ValidationEngine validationEngine) {
        this.validationEngine = validationEngine;
    }

    /**
     * Begins a new design.
     *
     * @param name display name
     * @return a fluent builder
     */
    public SpaceshipBuilder newDesign(String name) {
        return SpaceshipBuilder.create(name);
    }

    /**
     * Validates a design against the rules engine.
     *
     * @param design the design to check
     * @return the validation result
     */
    public ValidationResult validate(SpaceshipDesign design) {
        return validationEngine.validate(design);
    }

    /** @return all drives known to the modeller, in catalogue order */
    public List<DriveType> availableDrives() {
        return List.of(DriveType.values());
    }

    /**
     * @param category the family to filter by
     * @return the drives in the given category
     */
    public List<DriveType> drivesByCategory(Category category) {
        return DriveType.byCategory(category);
    }
}
