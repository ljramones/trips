package com.teamgannon.trips.spaceshipmodeller.propulsion;

import java.util.ArrayList;
import java.util.List;

/**
 * The full performance envelope and engineering profile of a {@link DriveType}.
 * <p>
 * Values are stored as ranges (min/max) because real and hard-sci-fi drives span wide operating bands
 * depending on tuning. Specific impulse is the single source of truth for efficiency; exhaust velocity is
 * <em>derived</em> from it on demand (see {@link #exhaustVelocityAverageKmps()}) so the two can never drift
 * out of sync.
 * <p>
 * Reaction-mass-free drives (photon and solar sails) report an infinite specific impulse; callers should
 * use {@link #reactionless()} to branch rather than dividing by it.
 *
 * @param ispMinSeconds       lower bound of specific impulse, in seconds (may be {@link Double#POSITIVE_INFINITY})
 * @param ispMaxSeconds       upper bound of specific impulse, in seconds (may be {@link Double#POSITIVE_INFINITY})
 * @param thrustToWeightMin   lower bound of thrust-to-weight ratio (dimensionless)
 * @param thrustToWeightMax   upper bound of thrust-to-weight ratio (dimensionless)
 * @param typicalThrustMinMN  lower bound of typical thrust, in meganewtons
 * @param typicalThrustMaxMN  upper bound of typical thrust, in meganewtons
 * @param minDryMassPercent   minimum plausible dry mass as a percentage of wet mass for this drive
 * @param powerRequirementMW  electrical power the drive demands, in megawatts (0 if self-powered/external)
 * @param thrustLevel         qualitative thrust band
 * @param radiatorLevel       waste-heat radiator demand
 * @param atmosphereCapable   {@code true} if the drive can operate within a planetary atmosphere
 * @param landingCapable      {@code true} if the drive is throttleable/restartable enough for landings
 * @param chartRegion         where the drive sits on the classic Isp-versus-thrust engine chart
 * @param constraints         inherent engineering/operational constraints
 * @param sciFiReferences     notable real programmes or fiction featuring the drive
 * @param notes               free-form designer notes
 * @author TRIPS Spaceship Modeller
 */
public record DriveSpecs(
        double ispMinSeconds,
        double ispMaxSeconds,
        double thrustToWeightMin,
        double thrustToWeightMax,
        double typicalThrustMinMN,
        double typicalThrustMaxMN,
        double minDryMassPercent,
        double powerRequirementMW,
        ThrustLevel thrustLevel,
        RadiatorLevel radiatorLevel,
        boolean atmosphereCapable,
        boolean landingCapable,
        String chartRegion,
        List<DesignConstraint> constraints,
        List<String> sciFiReferences,
        String notes
) {

    /** Standard gravity (m/s^2) used to convert specific impulse to exhaust velocity. */
    public static final double STANDARD_GRAVITY = 9.80665;

    /**
     * Compact constructor: validates ordering and makes the collection fields immutable.
     */
    public DriveSpecs {
        if (ispMinSeconds > ispMaxSeconds) {
            throw new IllegalArgumentException("ispMinSeconds must not exceed ispMaxSeconds");
        }
        if (thrustToWeightMin > thrustToWeightMax) {
            throw new IllegalArgumentException("thrustToWeightMin must not exceed thrustToWeightMax");
        }
        if (minDryMassPercent < 0 || minDryMassPercent > 100) {
            throw new IllegalArgumentException("minDryMassPercent must be within [0, 100]");
        }
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        sciFiReferences = sciFiReferences == null ? List.of() : List.copyOf(sciFiReferences);
        chartRegion = chartRegion == null ? "" : chartRegion;
        notes = notes == null ? "" : notes;
    }

    /** @return {@code true} for drives that carry no reaction mass (sails); their Isp is infinite */
    public boolean reactionless() {
        return Double.isInfinite(ispMinSeconds);
    }

    /** @return midpoint of the specific-impulse band, in seconds (infinite for reactionless drives) */
    public double ispAverageSeconds() {
        return (ispMinSeconds + ispMaxSeconds) / 2.0;
    }

    /**
     * Converts a specific impulse to exhaust velocity.
     *
     * @param ispSeconds specific impulse in seconds
     * @return exhaust velocity in kilometres per second
     */
    public static double exhaustVelocityKmps(double ispSeconds) {
        return ispSeconds * STANDARD_GRAVITY / 1000.0;
    }

    /** @return exhaust velocity at the low end of the Isp band, in km/s */
    public double exhaustVelocityMinKmps() {
        return exhaustVelocityKmps(ispMinSeconds);
    }

    /** @return exhaust velocity at the high end of the Isp band, in km/s */
    public double exhaustVelocityMaxKmps() {
        return exhaustVelocityKmps(ispMaxSeconds);
    }

    /** @return exhaust velocity at the midpoint of the Isp band, in km/s (infinite for sails) */
    public double exhaustVelocityAverageKmps() {
        return exhaustVelocityKmps(ispAverageSeconds());
    }

    /** @return midpoint of the thrust-to-weight band */
    public double thrustToWeightAverage() {
        return (thrustToWeightMin + thrustToWeightMax) / 2.0;
    }

    /** @return midpoint of the typical-thrust band, in meganewtons */
    public double typicalThrustAverageMN() {
        return (typicalThrustMinMN + typicalThrustMaxMN) / 2.0;
    }

    /**
     * @return {@code true} if the drive is realistically able to lift off and land: it must be flagged
     * landing-capable <em>and</em> be able to exceed a thrust-to-weight ratio of 1
     */
    public boolean suitableForLanding() {
        return landingCapable && thrustToWeightMax >= 1.0;
    }

    /** @return {@code true} if the drive demands dedicated waste-heat radiators */
    public boolean requiresRadiators() {
        return radiatorLevel.requiresDedicatedRadiators();
    }

    /** @return the blocking subset of {@link #constraints()} */
    public List<DesignConstraint> blockingConstraints() {
        return constraints.stream().filter(DesignConstraint::blocking).toList();
    }

    /**
     * @param code constraint identifier to look for
     * @return {@code true} if this drive declares a constraint with the given code
     */
    public boolean hasConstraint(String code) {
        return constraints.stream().anyMatch(c -> c.code().equals(code));
    }

    /**
     * @return a fresh builder for assembling a {@code DriveSpecs}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link DriveSpecs}. Keeps the large {@link DriveType} catalogue readable.
     */
    public static final class Builder {
        private double ispMinSeconds;
        private double ispMaxSeconds;
        private double thrustToWeightMin;
        private double thrustToWeightMax;
        private double typicalThrustMinMN;
        private double typicalThrustMaxMN;
        private double minDryMassPercent;
        private double powerRequirementMW;
        private ThrustLevel thrustLevel = ThrustLevel.MODERATE;
        private RadiatorLevel radiatorLevel = RadiatorLevel.NONE;
        private boolean atmosphereCapable;
        private boolean landingCapable;
        private String chartRegion = "";
        private final List<DesignConstraint> constraints = new ArrayList<>();
        private final List<String> sciFiReferences = new ArrayList<>();
        private String notes = "";

        private Builder() {
        }

        /** Sets the specific-impulse band in seconds. */
        public Builder isp(double minSeconds, double maxSeconds) {
            this.ispMinSeconds = minSeconds;
            this.ispMaxSeconds = maxSeconds;
            return this;
        }

        /** Sets the thrust-to-weight band (dimensionless). */
        public Builder thrustToWeight(double min, double max) {
            this.thrustToWeightMin = min;
            this.thrustToWeightMax = max;
            return this;
        }

        /** Sets the typical-thrust band in meganewtons. */
        public Builder typicalThrustMN(double minMN, double maxMN) {
            this.typicalThrustMinMN = minMN;
            this.typicalThrustMaxMN = maxMN;
            return this;
        }

        /** Sets the minimum plausible dry-mass percentage of wet mass. */
        public Builder minDryMassPercent(double percent) {
            this.minDryMassPercent = percent;
            return this;
        }

        /** Sets the electrical power demand in megawatts. */
        public Builder powerMW(double megawatts) {
            this.powerRequirementMW = megawatts;
            return this;
        }

        /** Sets the qualitative thrust band. */
        public Builder thrustLevel(ThrustLevel level) {
            this.thrustLevel = level;
            return this;
        }

        /** Sets the waste-heat radiator demand. */
        public Builder radiator(RadiatorLevel level) {
            this.radiatorLevel = level;
            return this;
        }

        /** Marks whether the drive can run inside an atmosphere. */
        public Builder atmosphereCapable(boolean capable) {
            this.atmosphereCapable = capable;
            return this;
        }

        /** Marks whether the drive can perform landings. */
        public Builder landingCapable(boolean capable) {
            this.landingCapable = capable;
            return this;
        }

        /** Sets the descriptive Isp-versus-thrust chart region. */
        public Builder chartRegion(String region) {
            this.chartRegion = region;
            return this;
        }

        /** Adds one or more inherent constraints. */
        public Builder constraints(DesignConstraint... constraints) {
            for (DesignConstraint c : constraints) {
                this.constraints.add(c);
            }
            return this;
        }

        /** Adds one or more sci-fi or real-programme references. */
        public Builder sciFiReferences(String... references) {
            for (String r : references) {
                this.sciFiReferences.add(r);
            }
            return this;
        }

        /** Sets free-form designer notes. */
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        /** @return the assembled, immutable {@link DriveSpecs} */
        public DriveSpecs build() {
            return new DriveSpecs(
                    ispMinSeconds, ispMaxSeconds,
                    thrustToWeightMin, thrustToWeightMax,
                    typicalThrustMinMN, typicalThrustMaxMN,
                    minDryMassPercent, powerRequirementMW,
                    thrustLevel, radiatorLevel,
                    atmosphereCapable, landingCapable,
                    chartRegion, constraints, sciFiReferences, notes);
        }
    }
}
