package com.teamgannon.trips.planetarymodelling.procedural.impact;

/**
 * Defines radial height profiles for different crater and volcano types.
 * Each profile returns a height value for a given normalized distance from the center (0.0 to 1.0).
 *
 * <p>Height values are normalized:
 * <ul>
 *   <li>Negative values = depression (crater floor)</li>
 *   <li>Positive values = elevation (rim, central peak, volcano cone)</li>
 *   <li>Zero = no modification</li>
 * </ul>
 *
 * <p>Profile types based on real impact crater and volcano morphologies:
 * <ul>
 *   <li>Simple craters: Small impacts (&lt;15km on Earth), bowl-shaped</li>
 *   <li>Complex craters: Large impacts, with central peaks or rings</li>
 *   <li>Volcanic profiles: Various eruption styles creating different landforms</li>
 * </ul>
 */
public enum CraterProfile {

    /**
     * Simple bowl-shaped crater with smooth interior and raised rim.
     * Typical of small impacts on rocky bodies.
     * Profile: Parabolic depression with rim peak at ~0.85 radius.
     */
    SIMPLE_ROUND {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance > 1.0) return 0.0;

            // Smooth parabolic bowl with raised rim
            double d = normalizedDistance;

            // Interior depression (parabolic)
            double floor = -1.0 * (1.0 - d * d);

            // Rim peak around 0.85 radius
            double rimPeak = Math.exp(-Math.pow((d - 0.85) / 0.08, 2)) * 0.3;

            // Blend floor and rim
            if (d < 0.7) {
                return floor;
            } else {
                double blend = smoothstep(0.7, 0.85, d);
                return floor * (1 - blend) + (floor + rimPeak) * blend;
            }
        }
    },

    /**
     * Simple crater with flat floor and raised rim.
     * Common in sedimentary terrain or with floor flooding.
     * Profile: Flat floor at depth, steep walls, rim peak.
     */
    SIMPLE_FLAT {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance > 1.0) return 0.0;

            double d = normalizedDistance;

            // Flat floor
            if (d < 0.6) {
                return -0.8;
            }

            // Steep wall transition
            if (d < 0.8) {
                double t = (d - 0.6) / 0.2;
                return -0.8 + smoothstep(0, 1, t) * 0.8;
            }

            // Rim with peak
            double rimPeak = Math.exp(-Math.pow((d - 0.85) / 0.07, 2)) * 0.25;
            double falloff = 1.0 - smoothstep(0.85, 1.0, d);
            return rimPeak * falloff;
        }
    },

    /**
     * Complex crater with central peak and flat floor.
     * Forms in larger impacts due to rebound of crater floor.
     * Profile: Central peak, flat annular floor, rim.
     */
    COMPLEX_FLAT {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance > 1.0) return 0.0;

            double d = normalizedDistance;

            // Central peak (Gaussian)
            double centralPeak = Math.exp(-Math.pow(d / 0.15, 2)) * 0.4;

            // Flat annular floor
            double floor = -0.6;
            if (d < 0.25) {
                floor = centralPeak - 0.1;
            } else if (d < 0.7) {
                floor = -0.6;
            }

            // Wall and rim
            if (d >= 0.7 && d < 0.85) {
                double t = (d - 0.7) / 0.15;
                floor = -0.6 + smoothstep(0, 1, t) * 0.7;
            }

            // Rim
            if (d >= 0.85) {
                double rimPeak = Math.exp(-Math.pow((d - 0.88) / 0.06, 2)) * 0.2;
                double falloff = 1.0 - smoothstep(0.88, 1.0, d);
                return rimPeak * falloff;
            }

            return floor;
        }
    },

    /**
     * Complex crater with terraced interior walls.
     * Forms in very large impacts with slumping walls.
     * Profile: Terraced steps descending to floor.
     */
    COMPLEX_STEPS {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance > 1.0) return 0.0;

            double d = normalizedDistance;

            // Central region
            if (d < 0.3) {
                return -0.7 + Math.exp(-Math.pow(d / 0.1, 2)) * 0.25;
            }

            // Terraced walls (3 steps)
            if (d < 0.5) {
                return -0.7;
            } else if (d < 0.6) {
                return -0.5;
            } else if (d < 0.7) {
                return -0.3;
            } else if (d < 0.8) {
                return -0.1;
            }

            // Rim
            double rimPeak = Math.exp(-Math.pow((d - 0.87) / 0.06, 2)) * 0.2;
            double falloff = 1.0 - smoothstep(0.87, 1.0, d);
            return rimPeak * falloff;
        }
    },

    /**
     * Multi-ring basin from very large impacts.
     * Multiple concentric rings of elevated terrain.
     * Profile: Series of ring ridges at different radii.
     */
    COMPLEX_RINGS {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance > 1.0) return 0.0;

            double d = normalizedDistance;
            double height = -0.5;  // Base depression

            // Inner ring at 0.3
            height += Math.exp(-Math.pow((d - 0.3) / 0.05, 2)) * 0.3;

            // Middle ring at 0.55
            height += Math.exp(-Math.pow((d - 0.55) / 0.05, 2)) * 0.25;

            // Outer ring at 0.8
            height += Math.exp(-Math.pow((d - 0.8) / 0.05, 2)) * 0.2;

            // Main rim at 0.92
            height += Math.exp(-Math.pow((d - 0.92) / 0.04, 2)) * 0.3;

            // Falloff beyond rim
            if (d > 0.92) {
                height *= 1.0 - smoothstep(0.92, 1.0, d);
            }

            return height;
        }
    },

    /**
     * Dome volcano with gentle slopes.
     * Forms from viscous lava building up around vent.
     * Profile: Gentle Gaussian dome shape.
     */
    DOME_VOLCANO {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance >= 1.0) return 0.0;

            double d = normalizedDistance;

            // Gentle dome (wide Gaussian) with smooth falloff to zero at edge
            double dome = Math.exp(-Math.pow(d / 0.5, 2)) * 0.8;

            // Small crater at summit
            if (d < 0.1) {
                dome -= (1 - d / 0.1) * 0.15;
            }

            // Apply smooth falloff near edge to ensure zero at d=1.0
            if (d > 0.7) {
                dome *= 1.0 - smoothstep(0.7, 1.0, d);
            }

            return dome;
        }
    },

    /**
     * Stratovolcano with steep conical slopes.
     * Classic volcanic cone shape from explosive eruptions.
     * Profile: Steep linear cone with summit crater.
     */
    STRATO_VOLCANO {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance > 1.0) return 0.0;

            double d = normalizedDistance;

            // Steep cone (approximately linear profile)
            double cone = (1.0 - d) * 1.2;

            // Summit crater
            if (d < 0.12) {
                // Crater rim at ~0.1
                double rimBoost = Math.exp(-Math.pow((d - 0.1) / 0.03, 2)) * 0.15;
                cone = cone * 0.85 + rimBoost;

                // Crater depression at center
                if (d < 0.08) {
                    cone -= (1 - d / 0.08) * 0.2;
                }
            }

            // Smooth falloff at base
            if (d > 0.85) {
                cone *= 1.0 - smoothstep(0.85, 1.0, d);
            }

            return cone;
        }
    },

    /**
     * Shield volcano with broad, gently sloping flanks.
     * Forms from fluid basaltic lava flows (e.g., Hawaiian volcanoes).
     * Profile: Very gentle parabolic dome, much wider than tall.
     */
    SHIELD_VOLCANO {
        @Override
        public double getHeight(double normalizedDistance) {
            if (normalizedDistance > 1.0) return 0.0;

            double d = normalizedDistance;

            // Very gentle dome (height:width ratio ~1:20)
            double shield = (1.0 - d * d) * 0.5;

            // Summit caldera
            if (d < 0.15) {
                // Caldera rim
                double rimBoost = Math.exp(-Math.pow((d - 0.12) / 0.04, 2)) * 0.1;
                shield += rimBoost;

                // Caldera floor
                if (d < 0.1) {
                    double calderaDepth = (1 - d / 0.1) * 0.15;
                    shield -= calderaDepth;
                }
            }

            return shield;
        }
    };

    /**
     * Returns the height modification at a given normalized distance from center.
     *
     * @param normalizedDistance Distance from center as fraction of radius (0.0 = center, 1.0 = edge)
     * @return Height modification value (negative = depression, positive = elevation)
     */
    public abstract double getHeight(double normalizedDistance);

    /**
     * Returns whether this profile represents a crater (depression) or volcano (elevation).
     */
    public boolean isCrater() {
        return switch (this) {
            case SIMPLE_ROUND, SIMPLE_FLAT, COMPLEX_FLAT, COMPLEX_STEPS, COMPLEX_RINGS -> true;
            case DOME_VOLCANO, STRATO_VOLCANO, SHIELD_VOLCANO -> false;
        };
    }

    /**
     * Returns whether this profile represents a volcanic feature.
     */
    public boolean isVolcano() {
        return !isCrater();
    }

    /**
     * Gets the typical depth/height multiplier for this profile type.
     * Larger craters/volcanoes should use larger base heights.
     */
    public double getTypicalHeightMultiplier() {
        return switch (this) {
            case SIMPLE_ROUND -> 1.0;
            case SIMPLE_FLAT -> 0.8;
            case COMPLEX_FLAT -> 1.2;
            case COMPLEX_STEPS -> 1.3;
            case COMPLEX_RINGS -> 1.5;
            case DOME_VOLCANO -> 0.6;
            case STRATO_VOLCANO -> 1.4;
            case SHIELD_VOLCANO -> 0.4;
        };
    }

    /**
     * Smooth step interpolation function.
     * Returns 0 if x < edge0, 1 if x > edge1, smooth curve between.
     */
    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }
}
