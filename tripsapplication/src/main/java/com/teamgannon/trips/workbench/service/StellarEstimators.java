package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Pure-function photometric and spectral estimators used by the Data Workbench
 * enrichment pipeline.
 * <p>
 * Extracted from {@code WorkbenchEnrichmentService} in Phase 4.3 of the
 * codebase-review remediation. No HTTP, no Spring, no JPA — just numeric
 * formulae and small lookup tables. Safe to call from any thread.
 * <p>
 * Methods that accept a {@link StarObject} read its photometric / positional
 * fields and never mutate the entity.
 */
@Slf4j
public final class StellarEstimators {

    /** Sun's absolute V magnitude. */
    private static final double SUN_ABSOLUTE_V_MAGNITUDE = 4.83;

    /** Parsecs per light-year. */
    private static final double PARSECS_PER_LY = 3.26156;

    private StellarEstimators() {
    }

    // ==================== Mass / luminosity / radius ====================

    /**
     * Estimate stellar mass (M☉) for a star with valid magV and distance, via
     * the mass-luminosity relation L/L☉ = (M/M☉)^α. Returns {@code null} if
     * inputs are insufficient or the result is outside the physical range
     * (0.01 - 200 M☉).
     */
    public static Double estimatePhotometricMass(StarObject star) {
        Double luminosity = calculateLuminosityFromMagnitude(star);
        if (luminosity == null || luminosity <= 0) {
            return null;
        }

        // Mass-luminosity relation, piecewise by mass regime:
        //   M > 0.43 M☉, L < 2 L☉   ⇒ α ≈ 4
        //   M > 0.43 M☉, L 2-16 L☉  ⇒ α ≈ 3.5
        //   M > 0.43 M☉, L > 16 L☉  ⇒ α ≈ 3
        //   M < 0.43 M☉              ⇒ L ≈ 0.23 · M^2.3 ⇒ M = (L/0.23)^(1/2.3)
        double mass;
        if (luminosity > 0.033) {
            if (luminosity < 2) {
                mass = Math.pow(luminosity, 1.0 / 4.0);
            } else if (luminosity < 16) {
                mass = Math.pow(luminosity, 1.0 / 3.5);
            } else {
                mass = Math.pow(luminosity, 1.0 / 3.0);
            }
        } else {
            mass = Math.pow(luminosity / 0.23, 1.0 / 2.3);
        }

        if (mass < 0.01 || mass > 200) {
            return null;
        }
        return mass;
    }

    /**
     * Luminosity (L☉) from apparent V magnitude + distance. Distance is
     * heuristically interpreted as parsecs or light-years based on consistency
     * with the star's x/y/z coordinates.
     * <p>
     * L/L☉ = 10^((M_V_sun - M_V) / 2.5) where M_V = m - 5·log10(d_pc / 10).
     */
    public static Double calculateLuminosityFromMagnitude(StarObject star) {
        double magV = star.getMagv();
        double distance = star.getDistance();
        if (distance <= 0) {
            return null;
        }

        double distanceParsecs = inferDistanceParsecs(star, distance);

        // Absolute magnitude via the distance modulus, then luminosity.
        double absoluteMag = magV - 5 * Math.log10(distanceParsecs / 10);
        double luminosity = Math.pow(10, (SUN_ABSOLUTE_V_MAGNITUDE - absoluteMag) / 2.5);

        if (luminosity < 0.000001 || luminosity > 10_000_000) {
            log.debug("Luminosity out of range for star {}: L={}, magV={}, dist={} pc",
                    star.getDisplayName(), luminosity, magV, distanceParsecs);
            return null;
        }
        return luminosity;
    }

    /**
     * Heuristic: HYG data stores distance in parsecs; TRIPS native convention
     * is light-years. If the star's x/y/z coordinate magnitude is close to the
     * distance value (same unit), assume parsecs; if 3.26× the distance,
     * x/y/z is in ly and distance in pc; otherwise default to parsecs for
     * small distances and ly for large.
     */
    private static double inferDistanceParsecs(StarObject star, double distance) {
        double xyzDistance = Math.sqrt(
                star.getX() * star.getX()
                        + star.getY() * star.getY()
                        + star.getZ() * star.getZ());
        if (xyzDistance > 0 && Math.abs(xyzDistance - distance) / distance < 0.1) {
            return distance;
        }
        if (xyzDistance > 0 && Math.abs(xyzDistance / PARSECS_PER_LY - distance) / distance < 0.1) {
            return distance;
        }
        if (distance > 100) {
            return distance / PARSECS_PER_LY;
        }
        return distance;
    }

    /**
     * Estimate stellar radius (R☉) from mass via R/R☉ ≈ M/M☉ ^ α with α=0.8 for
     * M < 1 M☉ and α=0.57 for higher-mass main-sequence stars. Returns 0 if
     * the result is outside the physical range.
     */
    public static double estimateRadiusFromMass(double mass) {
        double radius = mass < 1.0
                ? Math.pow(mass, 0.8)
                : Math.pow(mass, 0.57);
        return (radius < 0.01 || radius > 2000) ? 0 : radius;
    }

    // ==================== Distance from photometry ====================

    /**
     * Estimate distance (light-years) for a star with B and V magnitudes, or
     * with V magnitude + Gaia BP-RP color as a fallback. Returns {@code null}
     * if neither photometric path is viable.
     */
    public static Double estimatePhotometricDistance(StarObject star) {
        double magV = star.getMagv();
        double magB = star.getMagb();
        // Magnitudes can be negative for bright stars (Sirius = -1.46), so check non-zero rather than positive.
        boolean hasMagV = magV != 0;
        boolean hasMagB = magB != 0;

        if (hasMagV && hasMagB) {
            double bMinusV = magB - magV;
            Double absoluteMag = estimateAbsoluteMagnitudeFromBV(bMinusV);
            if (absoluteMag != null) {
                double dist = calculateDistanceFromMagnitudes(magV, absoluteMag);
                if (dist > 0) {
                    return dist;
                }
                log.debug("B-V path rejected: star={}, magV={}, magB={}, B-V={}, absMag={}, dist={}",
                        star.getDisplayName(), magV, magB, bMinusV, absoluteMag, dist);
            }
        }

        double bprp = star.getBprp();
        if (bprp != 0 && hasMagV) {
            // Approximate B-V from Gaia BP-RP for the fallback path: B-V ≈ 0.98·(BP-RP) - 0.02.
            double approxBV = 0.98 * bprp - 0.02;
            Double absoluteMag = estimateAbsoluteMagnitudeFromBV(approxBV);
            if (absoluteMag != null) {
                double dist = calculateDistanceFromMagnitudes(magV, absoluteMag);
                if (dist > 0) {
                    return dist;
                }
                log.debug("BP-RP path rejected: star={}, magV={}, bprp={}, approxBV={}, absMag={}, dist={}",
                        star.getDisplayName(), magV, bprp, approxBV, absoluteMag, dist);
            }
        }

        log.debug("Photometric distance skipped: star={}, magV={}, magB={}, bprp={}",
                star.getDisplayName(), magV, magB, bprp);
        return null;
    }

    /**
     * Absolute V magnitude from B-V colour index via a piecewise polynomial
     * fit covering main-sequence stars from O type to late M. Returns
     * {@code null} for inputs outside [-2.0, 3.0] or for results outside
     * [-10, 22].
     */
    public static Double estimateAbsoluteMagnitudeFromBV(double bMinusV) {
        if (bMinusV < -2.0 || bMinusV > 3.0) {
            return null;
        }

        double bv = bMinusV;
        double bv2 = bv * bv;

        double absoluteMag;
        if (bv < -0.3) {
            // Very hot O-type — extrapolated linearly into the extreme blue.
            absoluteMag = -4.0 + 2.0 * (bv + 0.3);
        } else if (bv < 0.0) {
            // Hot B / early A — roughly linear.
            absoluteMag = -0.5 + 3.5 * bv;
        } else if (bv < 0.4) {
            absoluteMag = -0.5 + 5.0 * bv - 2.0 * bv2;       // A, F
        } else if (bv < 0.8) {
            absoluteMag = 1.2 + 4.5 * bv - 1.5 * bv2;        // G (Sun-like)
        } else if (bv < 1.4) {
            absoluteMag = 2.0 + 4.0 * bv - 0.5 * bv2;        // K
        } else if (bv < 2.0) {
            absoluteMag = 3.5 + 4.5 * bv;                    // M
        } else {
            absoluteMag = 12.5 + 2.0 * (bv - 2.0);           // very-red extrapolation
        }

        if (absoluteMag < -10 || absoluteMag > 22) {
            return null;
        }
        return absoluteMag;
    }

    /**
     * Distance (light-years) from the distance-modulus formula
     * d_pc = 10^((m - M + 5) / 5). Returns 0 if the result is outside the
     * physical range [0.1, 500000] ly.
     */
    public static double calculateDistanceFromMagnitudes(double apparentMag, double absoluteMag) {
        double distanceModulus = apparentMag - absoluteMag;
        double distanceParsecs = Math.pow(10, (distanceModulus + 5) / 5);
        double distanceLy = distanceParsecs * PARSECS_PER_LY;
        if (distanceLy < 0.1 || distanceLy > 500_000) {
            log.debug("Distance rejected: {} ly (apparentMag={}, absoluteMag={}, modulus={})",
                    "%.1f".formatted(distanceLy), apparentMag, absoluteMag, distanceModulus);
            return 0;
        }
        return distanceLy;
    }

    /** Distance (light-years) from parallax in milliarcseconds. 0 for non-positive input. */
    public static double calculateDistanceFromParallax(double parallaxMas) {
        if (parallaxMas <= 0) {
            return 0.0;
        }
        return (1000.0 / parallaxMas) * PARSECS_PER_LY;
    }

    /**
     * Convert RA / Dec (degrees) + distance to Cartesian coordinates. RA is
     * interpreted in hours: {@code raRad = radians(raDeg * 15)}.
     */
    public static double[] calculateCoordinatesFromRaDec(double raDeg, double decDeg, double distance) {
        double raRad = Math.toRadians(raDeg * 15.0);
        double decRad = Math.toRadians(decDeg);
        double x = distance * Math.cos(decRad) * Math.cos(raRad);
        double y = distance * Math.cos(decRad) * Math.sin(raRad);
        double z = distance * Math.sin(decRad);
        return new double[]{x, y, z};
    }

    // ==================== Temperature / spectral conversions ====================

    /**
     * Surface temperature (K) from Gaia BP-RP colour. Piecewise linear fit
     * across spectral regimes; returns {@code null} for BP-RP outside
     * [-0.6, 6.0] or for results outside [1000, 50000] K.
     */
    public static Double estimateTemperatureFromBprp(double bprp) {
        if (bprp < -0.6 || bprp > 6.0) {
            return null;
        }
        double temp;
        if (bprp < 0.0) {
            temp = 10000 - bprp * 30000;       // O/B (10000-40000 K)
        } else if (bprp < 0.5) {
            temp = 10000 - bprp * 5000;        // A  (7500-10000)
        } else if (bprp < 0.8) {
            temp = 8333 - bprp * 3333;         // F  (6000-7500)
        } else if (bprp < 1.0) {
            temp = 7800 - bprp * 2500;         // G  (5300-6000)
        } else if (bprp < 1.5) {
            temp = 7700 - bprp * 2400;         // K  (4000-5300)
        } else if (bprp < 3.0) {
            temp = 5200 - bprp * 800;          // early-mid M  (2800-4000)
        } else {
            temp = 3700 - bprp * 400;          // late M / L (1500-2800)
        }
        if (temp < 1000 || temp > 50000) {
            return null;
        }
        return temp;
    }

    /**
     * MK spectral class from Gaia BP-RP colour (main-sequence assumption).
     * Returns {@code null} for BP-RP outside [-0.6, 6.0]. Based on Pecaut &amp;
     * Mamajek (2013) plus Gaia DR3 calibration documentation.
     */
    public static String estimateSpectralClassFromBprp(double bprp) {
        if (bprp < -0.6 || bprp > 6.0) {
            return null;
        }
        if (bprp < -0.35) return "O";
        if (bprp < -0.15) return "B0V";
        if (bprp <  0.00) return "B5V";
        if (bprp <  0.15) return "A0V";
        if (bprp <  0.30) return "A5V";
        if (bprp <  0.45) return "F0V";
        if (bprp <  0.60) return "F5V";
        if (bprp <  0.75) return "G0V";
        if (bprp <  0.90) return "G5V";
        if (bprp <  1.05) return "K0V";
        if (bprp <  1.25) return "K3V";
        if (bprp <  1.50) return "K5V";
        if (bprp <  1.85) return "M0V";
        if (bprp <  2.20) return "M1V";
        if (bprp <  2.55) return "M2V";
        if (bprp <  2.90) return "M3V";
        if (bprp <  3.30) return "M4V";
        if (bprp <  3.90) return "M5V";
        if (bprp <  4.50) return "M6V";
        if (bprp <  5.20) return "M7V";
        return "M8V";
    }

    /**
     * Surface temperature (K) from MK spectral class (main-sequence assumed).
     * Reads the leading letter (O/B/A/F/G/K/M/L/T/Y) and a single optional
     * subtype digit. Returns {@code null} for blank or unrecognised input.
     */
    public static Double estimateTemperatureFromSpectral(String spectralClass) {
        if (spectralClass == null || spectralClass.isBlank()) {
            return null;
        }
        String spec = spectralClass.trim().toUpperCase();
        char type = spec.charAt(0);
        int subtype = 5;  // default to mid-range when no digit is provided
        if (spec.length() > 1 && Character.isDigit(spec.charAt(1))) {
            subtype = spec.charAt(1) - '0';
        }
        return switch (type) {
            case 'O' -> 50000.0 - subtype * 2000.0;   // O0-O9 ⇒ 50 000 - 32 000 K
            case 'B' -> 30000.0 - subtype * 2000.0;   // B0-B9 ⇒ 30 000 - 12 000 K
            case 'A' -> 10000.0 - subtype *  250.0;
            case 'F' ->  7500.0 - subtype *  150.0;
            case 'G' ->  6000.0 - subtype *   80.0;
            case 'K' ->  5200.0 - subtype *  150.0;
            case 'M' ->  3700.0 - subtype *  130.0;
            case 'L' ->  2400.0 - subtype *  110.0;
            case 'T' ->  1300.0 - subtype *   70.0;
            case 'Y' ->   500.0;
            default  -> null;
        };
    }

    /**
     * MK spectral classification from surface temperature (K), main-sequence
     * assumed. Returns {@code null} for {@code temp <= 0} or {@code > 60 000}.
     */
    public static String estimateSpectralFromTemperature(double temp) {
        if (temp <= 0 || temp > 60000) {
            return null;
        }
        if (temp >= 30000) {
            int subtype = Math.min(9, (int) ((50000 - temp) / 2000));
            return "O" + subtype + "V";
        }
        if (temp >= 10000) {
            int subtype = Math.min(9, (int) ((30000 - temp) / 2000));
            return "B" + subtype + "V";
        }
        if (temp >= 7500) {
            int subtype = Math.min(9, (int) ((10000 - temp) / 250));
            return "A" + subtype + "V";
        }
        if (temp >= 6000) {
            int subtype = Math.min(9, (int) ((7500 - temp) / 150));
            return "F" + subtype + "V";
        }
        if (temp >= 5200) {
            int subtype = Math.min(9, (int) ((6000 - temp) / 80));
            return "G" + subtype + "V";
        }
        if (temp >= 3700) {
            int subtype = Math.min(9, (int) ((5200 - temp) / 150));
            return "K" + subtype + "V";
        }
        if (temp >= 2400) {
            int subtype = Math.min(9, (int) ((3700 - temp) / 130));
            return "M" + subtype + "V";
        }
        if (temp >= 1300) {
            int subtype = Math.min(9, (int) ((2400 - temp) / 110));
            return "L" + subtype;
        }
        if (temp >= 600) {
            int subtype = Math.min(9, (int) ((1300 - temp) / 70));
            return "T" + subtype;
        }
        return "Y0";
    }
}
