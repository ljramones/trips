package com.teamgannon.trips.spaceshipmodeller.integration;

import java.util.List;

import static com.teamgannon.trips.spaceshipmodeller.integration.TransferCategory.ADVANCED;
import static com.teamgannon.trips.spaceshipmodeller.integration.TransferCategory.EXOTIC;
import static com.teamgannon.trips.spaceshipmodeller.integration.TransferCategory.REALISTIC;
import static com.teamgannon.trips.spaceshipmodeller.integration.TransferCost.EFFICIENT;
import static com.teamgannon.trips.spaceshipmodeller.integration.TransferCost.EXPENSIVE_FAST;

/**
 * The kind of orbital/interstellar transfer to plan, from established orbital mechanics through hard-SF to
 * speculative concepts.
 * <p>
 * Each constant carries a {@link TransferCategory} (UI grouping), a {@link TransferCost} (UI colour), a
 * human-readable label and a one-line description. {@link TransferCalculator} implements a delta-V and
 * transit-time model for every type (real-ish for the realistic ones, clearly-approximate for the exotic
 * ones), and {@link TransferSuitability} decides which a given ship's drive can actually perform.
 */
public enum TransferType {

    // ---- Realistic ---------------------------------------------------------
    HOHMANN("Hohmann", REALISTIC, EFFICIENT,
            "Minimum-energy two-burn transfer between circular orbits."),
    BI_ELLIPTIC("Bi-elliptic", REALISTIC, EFFICIENT,
            "Three-burn transfer via a high apoapsis; beats Hohmann for large radius ratios."),
    HIGH_ENERGY("High-energy (Type II)", REALISTIC, EXPENSIVE_FAST,
            "Faster non-minimum-energy ellipse: more delta-V, less time."),
    LOW_THRUST_APPROX("Low-thrust spiral", REALISTIC, EFFICIENT,
            "Continuous electric-propulsion spiral; very efficient, very slow."),
    OBERTH("Oberth maneuver", REALISTIC, EFFICIENT,
            "Burn deep in the gravity well for extra efficiency."),
    AEROBRAKING("Aerobraking-assisted", REALISTIC, EFFICIENT,
            "Uses the destination atmosphere to capture; saves the arrival burn."),
    GRAVITY_ASSIST("Gravity-assist chain", REALISTIC, EFFICIENT,
            "Planetary flybys trade time for large delta-V savings."),
    RESONANT_PHASING("Resonant / phasing", REALISTIC, EFFICIENT,
            "Phasing orbits to rendezvous; cheap but adds revolutions."),

    // ---- Advanced (hard SF) ------------------------------------------------
    BRACHISTOCHRONE("Brachistochrone", ADVANCED, EXPENSIVE_FAST,
            "Constant-thrust flip-and-burn: accelerate to the midpoint, then decelerate."),
    FAST_TRANSIT("Fast transit", ADVANCED, EXPENSIVE_FAST,
            "High-thrust direct transfer spending part of the delta-V budget for speed."),
    MINIMUM_TIME("Minimum time", ADVANCED, EXPENSIVE_FAST,
            "Spend the entire delta-V budget to arrive as fast as possible."),
    HYBRID_CHEM_ELECTRIC("Hybrid chemical + electric", ADVANCED, EFFICIENT,
            "Chemical escape burn followed by an electric spiral cruise."),
    LOW_ENERGY_WSB("Low-energy (weak stability boundary)", ADVANCED, EFFICIENT,
            "Weak-stability-boundary capture: minimal delta-V, long duration."),

    // ---- Exotic / theoretical ---------------------------------------------
    RELATIVISTIC("Relativistic transfer", EXOTIC, TransferCost.EXOTIC,
            "Near-light-speed cruise (~0.3c); coordinate time shown, with time dilation."),
    LASER_SAIL_BEAM("Laser sail / beam-riding", EXOTIC, TransferCost.EXOTIC,
            "Beam-pushed sail; no propellant, cruises at a fixed fraction of c."),
    BUSSARD_RAMJET_TRANSIT("Bussard ramjet", EXOTIC, TransferCost.EXOTIC,
            "Interstellar ramjet collecting fuel en route; relativistic cruise."),
    ANTIMATTER_TORCH("Antimatter torch", EXOTIC, TransferCost.EXOTIC,
            "Antimatter flip-and-burn at extreme thrust and exhaust velocity."),
    WORMHOLE("Wormhole transit", EXOTIC, TransferCost.EXOTIC,
            "Speculative shortcut: near-instant traversal. No known drive enables it."),
    ALCUBIERRE_WARP("Alcubierre warp", EXOTIC, TransferCost.EXOTIC,
            "Speculative warp bubble; effective superluminal cruise, no reaction mass."),
    JUMP_DRIVE("Jump drive / hyperspace", EXOTIC, TransferCost.EXOTIC,
            "Sci-fi discrete jump after a charge period."),
    QUANTUM_TELEPORT("Quantum teleport / exotic matter", EXOTIC, TransferCost.EXOTIC,
            "Highly theoretical near-instantaneous transfer via exotic matter.");

    private final String label;
    private final TransferCategory category;
    private final TransferCost cost;
    private final String description;

    TransferType(String label, TransferCategory category, TransferCost cost, String description) {
        this.label = label;
        this.category = category;
        this.cost = cost;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public TransferCategory category() {
        return category;
    }

    public TransferCost cost() {
        return cost;
    }

    public String description() {
        return description;
    }

    /**
     * @param category the category to filter by
     * @return the types in that category, in declaration order
     */
    public static List<TransferType> byCategory(TransferCategory category) {
        return java.util.Arrays.stream(values()).filter(t -> t.category == category).toList();
    }
}
