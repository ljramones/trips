package com.teamgannon.trips.spaceshipmodeller.propulsion;

import java.util.List;

/**
 * Catalogue of propulsion systems available to the Spaceship Modeller, spanning mature chemistry through
 * hard-sci-fi torch and interstellar drives.
 * <p>
 * Each constant carries a {@link Category} and a fully populated {@link DriveSpecs} performance envelope.
 * Figures are representative engineering estimates drawn from real programmes (NERVA, Daedalus, Dawn) and
 * the hard-sci-fi literature; they are intended to be plausible rather than authoritative, and to give the
 * {@code rules} engine meaningful envelopes to validate against.
 *
 * @author TRIPS Spaceship Modeller
 */
public enum DriveType {

    /** Storable/cryogenic bipropellant chemistry. The workhorse: huge thrust, poor efficiency. */
    CHEMICAL_BIPROPELLANT(Category.CHEMICAL, DriveSpecs.builder()
            .isp(300, 465)
            .thrustToWeight(50, 100)
            .typicalThrustMN(0.01, 8.0)
            .minDryMassPercent(8)
            .powerMW(0)
            .thrustLevel(ThrustLevel.VERY_HIGH)
            .radiator(RadiatorLevel.NONE)
            .atmosphereCapable(true)
            .landingCapable(true)
            .chartRegion("High thrust, low Isp")
            .sciFiReferences("Apollo / Saturn V", "The Martian (MAV)", "SpaceX Merlin/Raptor")
            .notes("Mature, throttleable, restartable; the only family routinely used for crewed launch and landing.")
            .build()),

    /** Solid chemical motor. Even more thrust than bipropellant, but cannot be throttled or restarted. */
    SOLID_ROCKET(Category.CHEMICAL, DriveSpecs.builder()
            .isp(250, 285)
            .thrustToWeight(60, 120)
            .typicalThrustMN(0.1, 15.0)
            .minDryMassPercent(12)
            .powerMW(0)
            .thrustLevel(ThrustLevel.EXTREME)
            .radiator(RadiatorLevel.NONE)
            .atmosphereCapable(true)
            .landingCapable(false)
            .chartRegion("Very high thrust, low Isp")
            .constraints(DesignConstraint.advisory("NON_RESTARTABLE", "Once ignited the motor burns to depletion; no throttle or shutdown."))
            .sciFiReferences("Space Shuttle SRB", "Ariane boosters")
            .notes("Excellent for boost stages; useless for the fine control a landing demands.")
            .build()),

    /** Gridded electrostatic ion engine. Sips xenon, accelerates it to enormous exhaust velocity. */
    ION_GRIDDED(Category.ELECTRIC, DriveSpecs.builder()
            .isp(3000, 10000)
            .thrustToWeight(1e-5, 1e-4)
            .typicalThrustMN(1e-7, 5e-4)
            .minDryMassPercent(35)
            .powerMW(1.0)
            .thrustLevel(ThrustLevel.VERY_LOW)
            .radiator(RadiatorLevel.MINIMAL)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Very low thrust, high Isp")
            .constraints(
                    DesignConstraint.blocking("VACUUM_ONLY", "Operates only in hard vacuum."),
                    DesignConstraint.advisory("REQUIRES_POWER", "Needs a sustained electrical supply (solar or reactor)."))
            .sciFiReferences("NASA Dawn", "Deep Space 1")
            .notes("Months-long burns build large delta-V from a tiny propellant load.")
            .build()),

    /** Hall-effect thruster. More thrust than gridded ion at slightly lower efficiency. */
    HALL_EFFECT(Category.ELECTRIC, DriveSpecs.builder()
            .isp(1500, 3000)
            .thrustToWeight(1e-4, 1e-3)
            .typicalThrustMN(1e-6, 1e-3)
            .minDryMassPercent(30)
            .powerMW(0.5)
            .thrustLevel(ThrustLevel.LOW)
            .radiator(RadiatorLevel.MINIMAL)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Low thrust, high Isp")
            .constraints(DesignConstraint.blocking("VACUUM_ONLY", "Operates only in hard vacuum."))
            .sciFiReferences("SpaceX Starlink", "modern station-keeping satellites")
            .notes("The most flight-proven electric propulsion for orbital manoeuvring.")
            .build()),

    /** Variable Specific Impulse Magnetoplasma Rocket. Trades thrust against Isp in flight. */
    VASIMR(Category.ELECTRIC, DriveSpecs.builder()
            .isp(3000, 12000)
            .thrustToWeight(1e-4, 5e-3)
            .typicalThrustMN(1e-5, 5e-3)
            .minDryMassPercent(30)
            .powerMW(200)
            .thrustLevel(ThrustLevel.LOW)
            .radiator(RadiatorLevel.MODERATE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Variable: low thrust, very high Isp")
            .constraints(
                    DesignConstraint.blocking("VACUUM_ONLY", "Operates only in hard vacuum."),
                    DesignConstraint.advisory("HIGH_POWER", "Useful thrust demands tens to hundreds of megawatts."))
            .sciFiReferences("Ad Astra Rocket Company (real prototype)")
            .notes("Can tune from high-thrust spiral-out to high-Isp cruise; gated by power supply.")
            .build()),

    /** Solid-core nuclear thermal rocket. A reactor superheats hydrogen propellant. */
    NUCLEAR_THERMAL(Category.NUCLEAR_THERMAL, DriveSpecs.builder()
            .isp(850, 1000)
            .thrustToWeight(3, 10)
            .typicalThrustMN(0.05, 1.0)
            .minDryMassPercent(20)
            .powerMW(0)
            .thrustLevel(ThrustLevel.HIGH)
            .radiator(RadiatorLevel.MINIMAL)
            .atmosphereCapable(true)
            .landingCapable(false)
            .chartRegion("High thrust, moderate Isp")
            .constraints(DesignConstraint.advisory("RADIOACTIVE_EXHAUST", "Exhaust is radioactive; unsuitable for use near inhabited surfaces."))
            .sciFiReferences("Project Rover / NERVA (real)", "2010: Odyssey Two (Leonov)")
            .notes("Roughly double chemical Isp at still-usable thrust; a strong interplanetary workhorse.")
            .build()),

    /** Nuclear-electric: a reactor powers an electric thruster. High Isp, big radiators. */
    NUCLEAR_ELECTRIC(Category.NUCLEAR_ELECTRIC, DriveSpecs.builder()
            .isp(5000, 10000)
            .thrustToWeight(1e-4, 1e-3)
            .typicalThrustMN(1e-5, 1e-2)
            .minDryMassPercent(30)
            .powerMW(10)
            .thrustLevel(ThrustLevel.LOW)
            .radiator(RadiatorLevel.EXTENSIVE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Low thrust, very high Isp")
            .constraints(
                    DesignConstraint.blocking("VACUUM_ONLY", "Operates only in hard vacuum."),
                    DesignConstraint.blocking("LARGE_RADIATORS", "Reactor waste heat demands extensive radiators."))
            .sciFiReferences("Project Prometheus / JIMO (real concept)")
            .notes("Decouples power source from thruster; ideal for long unmanned cargo runs.")
            .build()),

    /** Open-cycle gas-core nuclear rocket. Fissioning plasma pushes Isp far above solid-core. */
    GAS_CORE_NUCLEAR(Category.NUCLEAR_THERMAL, DriveSpecs.builder()
            .isp(3000, 7000)
            .thrustToWeight(1, 5)
            .typicalThrustMN(0.05, 3.0)
            .minDryMassPercent(25)
            .powerMW(0)
            .thrustLevel(ThrustLevel.HIGH)
            .radiator(RadiatorLevel.MODERATE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("High thrust and high Isp (torch-adjacent)")
            .constraints(
                    DesignConstraint.blocking("RADIOACTIVE_EXHAUST", "Open cycle vents fissioning plasma; never use near habitats."),
                    DesignConstraint.advisory("EXOTIC_ENGINEERING", "Containing a fissioning gas core is unproven engineering."))
            .sciFiReferences("Open-cycle gas-core concept (real studies)")
            .notes("Rare combination of high thrust and high Isp, at the cost of a radioactive plume.")
            .build()),

    /** Orion-style nuclear-pulse drive: shaped charges detonate against a pusher plate. */
    ORION_PULSE(Category.NUCLEAR_PULSE, DriveSpecs.builder()
            .isp(3000, 100000)
            .thrustToWeight(2, 4)
            .typicalThrustMN(10, 1000)
            .minDryMassPercent(30)
            .powerMW(0)
            .thrustLevel(ThrustLevel.EXTREME)
            .radiator(RadiatorLevel.MINIMAL)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Extreme thrust, high Isp")
            .constraints(
                    DesignConstraint.blocking("NUCLEAR_FALLOUT", "Detonations produce fallout; cannot be used in or near a biosphere."),
                    DesignConstraint.advisory("TREATY_BANNED", "Banned by real-world atmospheric/space test treaties."))
            .sciFiReferences("Project Orion (real)", "Footfall (Niven & Pournelle)", "Deep Impact (Messiah)")
            .notes("Uniquely scales to enormous ships; the only near-term drive that pushes megatonne hulls hard.")
            .build()),

    /** Alias entry for Orion-style nuclear-pulse craft in asset catalogs. */
    ORION(Category.NUCLEAR_PULSE, DriveSpecs.builder()
            .isp(3000, 100000)
            .thrustToWeight(2, 4)
            .typicalThrustMN(10, 1000)
            .minDryMassPercent(30)
            .powerMW(0)
            .thrustLevel(ThrustLevel.EXTREME)
            .radiator(RadiatorLevel.MINIMAL)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Extreme thrust, high Isp")
            .constraints(
                    DesignConstraint.blocking("NUCLEAR_FALLOUT", "Detonations produce fallout; cannot be used in or near a biosphere."),
                    DesignConstraint.advisory("TREATY_BANNED", "Banned by real-world atmospheric/space test treaties."))
            .sciFiReferences("Project Orion (real)", "Footfall (Niven & Pournelle)", "Deep Impact (Messiah)")
            .notes("Orion-style shaped-charge pulse drive.")
            .build()),

    /** Continuous fusion torch: a true torchship, high thrust AND high Isp, paid for in radiators. */
    FUSION_TORCH(Category.FUSION, DriveSpecs.builder()
            .isp(10000, 1000000)
            .thrustToWeight(0.01, 1.0)
            .typicalThrustMN(0.1, 100)
            .minDryMassPercent(25)
            .powerMW(0)
            .thrustLevel(ThrustLevel.VERY_HIGH)
            .radiator(RadiatorLevel.MASSIVE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("High thrust AND high Isp (torchship)")
            .constraints(
                    DesignConstraint.blocking("MASSIVE_RADIATORS", "Continuous fusion dumps waste heat that dominates the dry mass."),
                    DesignConstraint.advisory("EXOTIC_ENGINEERING", "Sustained net-positive fusion thrust is unproven."))
            .sciFiReferences("The Expanse (Epstein Drive)", "Larry Niven 'Known Space' fusion drives")
            .notes("The classic hard-SF torchship; reduces interplanetary trips to days.")
            .build()),

    /** Inertial-confinement fusion pulse drive (Daedalus-class). Modest thrust, extreme Isp. */
    FUSION_PULSE(Category.FUSION, DriveSpecs.builder()
            .isp(200000, 1000000)
            .thrustToWeight(1e-4, 1e-2)
            .typicalThrustMN(0.01, 10)
            .minDryMassPercent(20)
            .powerMW(0)
            .thrustLevel(ThrustLevel.MODERATE)
            .radiator(RadiatorLevel.EXTENSIVE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Moderate thrust, extreme Isp")
            .constraints(
                    DesignConstraint.blocking("INTERSTELLAR_SCALE", "Sized for interstellar precursor missions, not in-system hops."),
                    DesignConstraint.advisory("HE3_FUEL", "Baseline designs burn deuterium/helium-3, a scarce fuel."))
            .sciFiReferences("Project Daedalus (real)", "Project Icarus")
            .notes("Pellet detonations at kilohertz rates; a credible first interstellar flyby drive.")
            .build()),

    /** Epstein drive: a high-efficiency magnetic-bottle fusion torch (The Expanse). */
    EPSTEIN_DRIVE(Category.FUSION, DriveSpecs.builder()
            .isp(11000, 1500000)
            .thrustToWeight(0.05, 3.0)
            .typicalThrustMN(0.5, 200)
            .minDryMassPercent(25)
            .powerMW(0)
            .thrustLevel(ThrustLevel.VERY_HIGH)
            .radiator(RadiatorLevel.MASSIVE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("High thrust AND very high Isp (efficient torchship)")
            .constraints(
                    DesignConstraint.blocking("MASSIVE_RADIATORS", "Sustained fusion dumps waste heat that dominates the dry mass."),
                    DesignConstraint.advisory("EXOTIC_ENGINEERING", "Solomon Epstein's efficiency is unexplained by known engineering."))
            .sciFiReferences("The Expanse (Epstein Drive)")
            .notes("High-efficiency fusion torch enabling sustained multi-g burns across the system; "
                    + "the drive that opened the Solar System in The Expanse.")
            .build()),

    /** Terran Republic standard fusion thrust drive: capable mid-tier torch (Caine Riordan universe). */
    TERRAN_FUSION_DRIVE(Category.FUSION, DriveSpecs.builder()
            .isp(12000, 250000)
            .thrustToWeight(0.01, 1.2)
            .typicalThrustMN(0.2, 80)
            .minDryMassPercent(25)
            .powerMW(0)
            .thrustLevel(ThrustLevel.HIGH)
            .radiator(RadiatorLevel.MODERATE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Balanced fusion thrust (capable mid-tier torch)")
            .constraints(
                    DesignConstraint.blocking("HEAT_REJECTION", "Sustained fusion needs real radiators, though less than a hard torch."),
                    DesignConstraint.advisory("SHIFT_PAIRED", "Sublight thrust drive; interstellar legs are flown under shift, not modelled here."))
            .sciFiReferences("Caine Riordan / Terran Republic (Charles E. Gannon)")
            .notes("United Earth / Terran Republic workhorse fusion thrust drive: reliable and capable, but a "
                    + "step behind the Ktoran Dominion's engines.")
            .build()),

    /** Hkh'Rkh warrior-race fusion drive: brute thrust, modest efficiency (Caine Riordan universe). */
    HKHRKH_THRUST(Category.FUSION, DriveSpecs.builder()
            .isp(8000, 120000)
            .thrustToWeight(0.05, 2.5)
            .typicalThrustMN(0.5, 120)
            .minDryMassPercent(25)
            .powerMW(0)
            .thrustLevel(ThrustLevel.VERY_HIGH)
            .radiator(RadiatorLevel.MODERATE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Brute-force fusion thrust (high thrust, modest Isp)")
            .constraints(
                    DesignConstraint.blocking("HEAT_REJECTION", "Hard-driven cores demand radiators."),
                    DesignConstraint.advisory("RUGGED_OVERBUILD", "Favours raw thrust and survivability over fuel economy."))
            .sciFiReferences("Caine Riordan / Terran Republic (Charles E. Gannon)")
            .notes("Hkh'Rkh drive philosophy: a warrior race that prizes closing speed and durability, "
                    + "trading efficiency for raw acceleration.")
            .build()),

    /** Antimatter beam-core drive: pion exhaust from proton-antiproton annihilation. */
    ANTIMATTER_BEAM_CORE(Category.ANTIMATTER, DriveSpecs.builder()
            .isp(100000, 10000000)
            .thrustToWeight(1e-3, 1e-1)
            .typicalThrustMN(0.1, 50)
            .minDryMassPercent(15)
            .powerMW(0)
            .thrustLevel(ThrustLevel.HIGH)
            .radiator(RadiatorLevel.MASSIVE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("High thrust, extreme Isp")
            .constraints(
                    DesignConstraint.blocking("ANTIMATTER_STORAGE", "Requires magnetic confinement of antimatter; catastrophic on failure."),
                    DesignConstraint.blocking("GAMMA_SHIELDING", "Annihilation gammas demand heavy crew shielding."),
                    DesignConstraint.blocking("MASSIVE_RADIATORS", "A large fraction of annihilation energy becomes waste heat."))
            .sciFiReferences("Avatar (ISV Venture Star)", "Robert Forward antimatter rocket studies")
            .notes("The highest energy density known; gated entirely by antimatter production and storage.")
            .build()),

    /** Beam-pushed laser sail. Carries no reaction mass; thrust comes from an external laser. */
    LASER_SAIL(Category.BEAMED, DriveSpecs.builder()
            .isp(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            .thrustToWeight(1e-6, 1e-4)
            .typicalThrustMN(1e-9, 1e-6)
            .minDryMassPercent(95)
            .powerMW(0)
            .thrustLevel(ThrustLevel.NEGLIGIBLE)
            .radiator(RadiatorLevel.NONE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Negligible thrust, no reaction mass")
            .constraints(
                    DesignConstraint.blocking("EXTERNAL_BEAM_REQUIRED", "Acceleration depends on a remote laser array; no onboard thrust."),
                    DesignConstraint.advisory("NO_DECELERATION", "Cannot easily decelerate without a beam at the destination."))
            .sciFiReferences("Breakthrough Starshot (real)", "The Mote in God's Eye (Niven & Pournelle)")
            .notes("Reaction-mass-free: nearly all mass is structure and payload, accelerated by photons.")
            .build()),

    /** Solar sail. Thrust from reflected sunlight; weakest of all, but free and inexhaustible inbound. */
    SOLAR_SAIL(Category.BEAMED, DriveSpecs.builder()
            .isp(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            .thrustToWeight(1e-7, 1e-5)
            .typicalThrustMN(1e-10, 1e-7)
            .minDryMassPercent(98)
            .powerMW(0)
            .thrustLevel(ThrustLevel.NEGLIGIBLE)
            .radiator(RadiatorLevel.NONE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Negligible thrust, no reaction mass")
            .constraints(
                    DesignConstraint.blocking("EXTERNAL_BEAM_REQUIRED", "Relies on sunlight; thrust falls off with the square of solar distance."),
                    DesignConstraint.advisory("INNER_SYSTEM_ONLY", "Practical thrust is confined to the inner system."))
            .sciFiReferences("IKAROS (real)", "Sunjammer", "Arthur C. Clarke 'The Wind from the Sun'")
            .notes("No propellant and no power draw; pays for it with the lowest thrust of any drive.")
            .build()),

    /** Bussard interstellar ramjet. Scoops interstellar hydrogen as fusion fuel at relativistic speed. */
    BUSSARD_RAMJET(Category.INTERSTELLAR, DriveSpecs.builder()
            .isp(1000000, 10000000)
            .thrustToWeight(1e-4, 1e-2)
            .typicalThrustMN(0.01, 100)
            .minDryMassPercent(80)
            .powerMW(0)
            .thrustLevel(ThrustLevel.MODERATE)
            .radiator(RadiatorLevel.MASSIVE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Speed-dependent thrust, extreme Isp")
            .constraints(
                    DesignConstraint.blocking("REQUIRES_HIGH_VELOCITY", "The magnetic scoop only collects useful fuel above a high cruise speed."),
                    DesignConstraint.blocking("MAGNETIC_SCOOP", "Demands a planet-scale magnetic ram field."),
                    DesignConstraint.advisory("EXOTIC_ENGINEERING", "Net thrust may be defeated by scoop drag in sparse media."))
            .sciFiReferences("Tau Zero (Poul Anderson)", "A World Out of Time (Niven)", "Star Trek (Bussard collectors)")
            .notes("Needs no stored fuel once up to speed, the dream of fuel-free interstellar cruise.")
            .build()),

    /** Ktoran Dominion advanced drive: high thrust AND high Isp, generations beyond human fusion. */
    KTORAN_ADVANCED(Category.EXOTIC, DriveSpecs.builder()
            .isp(100000, 2000000)
            .thrustToWeight(0.1, 3.0)
            .typicalThrustMN(0.5, 150)
            .minDryMassPercent(15)
            .powerMW(0)
            .thrustLevel(ThrustLevel.VERY_HIGH)
            .radiator(RadiatorLevel.MODERATE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Very high thrust AND very high Isp (beyond Terran tech)")
            .constraints(
                    DesignConstraint.blocking("HEAT_REJECTION", "Even superior heat management still needs radiators."),
                    DesignConstraint.advisory("EXOTIC_ENGINEERING", "Dominion drive internals are not understood by human science."))
            .sciFiReferences("Caine Riordan / Terran Republic (Charles E. Gannon)")
            .notes("The Ktoran Dominion's signature drive: both faster-accelerating and far more efficient than "
                    + "human fusion, reflecting an arrogant, ancient technological lead.")
            .build()),

    /** Grtul gate transit: a fixed-gate drive mode for cataloguing gate-capable assets. */
    GRTUL_GATE(Category.EXOTIC, DriveSpecs.builder()
            .isp(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            .thrustToWeight(0, 0)
            .typicalThrustMN(0, 0)
            .minDryMassPercent(100)
            .powerMW(0)
            .thrustLevel(ThrustLevel.NEGLIGIBLE)
            .radiator(RadiatorLevel.NONE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Gate transit; no onboard thrust")
            .constraints(DesignConstraint.blocking("GATE_REQUIRED", "Requires a compatible gate or fixed transit infrastructure."))
            .sciFiReferences("Caine Riordan / Terran Republic (Charles E. Gannon)")
            .notes("Catalog discriminator for assets that move via Grtul gate transit rather than onboard propulsion.")
            .build()),

    /** Posleen normal-space drive used for non-hyper transit in the Posleen setting. */
    POSLEEN_NORMAL_SPACE(Category.EXOTIC, DriveSpecs.builder()
            .isp(100000, 1000000)
            .thrustToWeight(0.05, 1.0)
            .typicalThrustMN(0.1, 80)
            .minDryMassPercent(20)
            .powerMW(0)
            .thrustLevel(ThrustLevel.HIGH)
            .radiator(RadiatorLevel.MODERATE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("High thrust, exotic Isp")
            .constraints(DesignConstraint.advisory("EXOTIC_ENGINEERING", "Normal-space drive physics are setting-specific."))
            .sciFiReferences("Legacy of the Aldenata / Posleen War")
            .notes("Catalog entry for Posleen normal-space manoeuvre drives.")
            .build()),

    /** Galactic hyperdrive: strategic FTL transit mode, not a landing or tactical thruster. */
    GALACTIC_HYPER(Category.EXOTIC, DriveSpecs.builder()
            .isp(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            .thrustToWeight(0, 0)
            .typicalThrustMN(0, 0)
            .minDryMassPercent(100)
            .powerMW(0)
            .thrustLevel(ThrustLevel.NEGLIGIBLE)
            .radiator(RadiatorLevel.NONE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Strategic FTL transit; no tactical thrust")
            .constraints(DesignConstraint.blocking("FTL_ONLY", "Hyperdrive does not provide local manoeuvre thrust."))
            .sciFiReferences("Galactic-scale space opera hyperdrive")
            .notes("Catalog entry for assets whose strategic movement is via hyperdrive.")
            .build()),

    /** Astrophage/Taumoeba spin drive: near-total mass-to-light conversion (Project Hail Mary). */
    SPIN_DRIVE(Category.EXOTIC, DriveSpecs.builder()
            .isp(1000000, 20000000)
            .thrustToWeight(0.1, 2.0)
            .typicalThrustMN(0.1, 100)
            .minDryMassPercent(25)
            .powerMW(0)
            .thrustLevel(ThrustLevel.VERY_HIGH)
            .radiator(RadiatorLevel.MASSIVE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("Extreme Isp with usable thrust (near-total mass conversion)")
            .constraints(
                    DesignConstraint.blocking("MASSIVE_RADIATORS", "Near-light exhaust still leaves enormous waste heat to radiate."),
                    DesignConstraint.advisory("EXOTIC_BIOLOGY", "Relies on Astrophage/Taumoeba converting mass to light at near-total efficiency."))
            .sciFiReferences("Project Hail Mary (Astrophage spin drive)")
            .notes("Living-fuel drive emitting Petrova-frequency light at near-total mass conversion; "
                    + "near-c exhaust at usable thrust enables ~1.5g interstellar cruise.")
            .build()),

    /** No installed drive. Useful for fixed assets where propulsion is structurally absent. */
    NONE(Category.EXOTIC, DriveSpecs.builder()
            .isp(0, 0)
            .thrustToWeight(0, 0)
            .typicalThrustMN(0, 0)
            .minDryMassPercent(100)
            .powerMW(0)
            .thrustLevel(ThrustLevel.NEGLIGIBLE)
            .radiator(RadiatorLevel.NONE)
            .atmosphereCapable(false)
            .landingCapable(false)
            .chartRegion("No installed drive")
            .notes("Structural absence of a drive; use for fixed assets and placeholders.")
            .build());

    private final Category category;
    private final DriveSpecs specs;

    DriveType(Category category, DriveSpecs specs) {
        this.category = category;
        this.specs = specs;
    }

    /** @return the family this drive belongs to */
    public Category category() {
        return category;
    }

    /** @return the full performance envelope for this drive */
    public DriveSpecs specs() {
        return specs;
    }

    /** @return the average specific impulse, in seconds */
    public double ispAverageSeconds() {
        return specs.ispAverageSeconds();
    }

    /** @return {@code true} if this drive is realistically able to land */
    public boolean suitableForLanding() {
        return specs.suitableForLanding();
    }

    /** @return {@code true} if this drive carries no reaction mass */
    public boolean reactionless() {
        return specs.reactionless();
    }

    /**
     * @param category the family to filter by
     * @return all drives in the given category, in declaration order
     */
    public static List<DriveType> byCategory(Category category) {
        return java.util.Arrays.stream(values())
                .filter(d -> d.category == category)
                .toList();
    }
}
