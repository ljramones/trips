package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.Category;

import java.util.EnumSet;
import java.util.Set;

/**
 * Decides which {@link TransferType}s a given ship's drive can realistically perform.
 * <p>
 * Rules are intentionally simple and drive-category based: impulsive transfers need a reaction drive;
 * spirals need electric/continuous drives; brachistochrone-class needs a high-performance (fusion/antimatter/
 * pulse) drive; and the speculative exotic types are gated to the most advanced drive available (antimatter),
 * standing in for "very advanced ship".
 */
public final class TransferSuitability {

    private static final Set<Category> ELECTRIC_LIKE =
            EnumSet.of(Category.ELECTRIC, Category.NUCLEAR_ELECTRIC, Category.FUSION, Category.INTERSTELLAR);
    private static final Set<Category> HYBRID_CAPABLE =
            EnumSet.of(Category.CHEMICAL, Category.ELECTRIC, Category.NUCLEAR_ELECTRIC, Category.NUCLEAR_THERMAL);
    private static final Set<Category> HIGH_PERFORMANCE =
            EnumSet.of(Category.FUSION, Category.ANTIMATTER, Category.NUCLEAR_PULSE);

    private TransferSuitability() {
    }

    /**
     * @param type the transfer type
     * @param ship the ship attempting it
     * @return {@code true} if this ship's drive can perform the transfer
     */
    public static boolean suitable(TransferType type, SpaceshipDesign ship) {
        if (ship == null) {
            return false;
        }
        Category cat = ship.driveType().category();
        double isp = ship.driveSpecs().ispAverageSeconds();
        boolean reactionless = ship.driveSpecs().reactionless();
        boolean highPerformance = HIGH_PERFORMANCE.contains(cat) || isp >= 50_000;

        return switch (type) {
            // impulsive orbital transfers: any reaction drive
            case HOHMANN, BI_ELLIPTIC, HIGH_ENERGY, OBERTH, AEROBRAKING,
                 GRAVITY_ASSIST, RESONANT_PHASING, LOW_ENERGY_WSB -> !reactionless;
            case LOW_THRUST_APPROX -> ELECTRIC_LIKE.contains(cat);
            case HYBRID_CHEM_ELECTRIC -> HYBRID_CAPABLE.contains(cat);
            // hard-SF high-thrust torch transfers
            case BRACHISTOCHRONE, FAST_TRANSIT, MINIMUM_TIME -> highPerformance && !reactionless;
            // exotic, drive-specific
            case RELATIVISTIC -> isp >= 100_000 && !reactionless;
            case LASER_SAIL_BEAM -> cat == Category.BEAMED;
            case BUSSARD_RAMJET_TRANSIT -> cat == Category.INTERSTELLAR;
            case ANTIMATTER_TORCH -> cat == Category.ANTIMATTER;
            // purely speculative: gated to the most advanced drive available
            case WORMHOLE, ALCUBIERRE_WARP, JUMP_DRIVE, QUANTUM_TELEPORT -> cat == Category.ANTIMATTER;
        };
    }
}
