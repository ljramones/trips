package com.teamgannon.trips.solarsysmodelling.habitable;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

@Slf4j
@Data
public class HabitableZoneFluxes {

    double[] seffsun = {1.776, 1.107, 0.356, 0.320, 1.188, 0.99};
    double[] a = {2.136e-4, 1.332e-4, 6.171e-5, 5.547e-5, 1.433e-4, 1.209e-4};
    double[] b = {2.533e-8, 1.580e-8, 1.698e-9, 1.526e-9, 1.707e-8, 1.404e-8};
    double[] c = {-1.332e-11, -8.308e-12, -3.198e-12, -2.874e-12, -8.968e-12, -7.418e-12};
    double[] d = {-3.097e-15, -1.931e-15, -5.575e-16, -5.011e-16, -2.084e-15, -1.713e-15};

    /**
     * columns are identified as the follows
     * i = 1 --> Recent Venus
     * i = 2 --> Runaway Greenhouse
     * i = 3 --> Maximum Greenhouse
     * i = 4 --> Early Mars
     * i = 5 --> Runaway Greenhouse for 5 ME
     * i = 6 --> Runaway Greenhouse for 0.1 ME
     */
    double[][] hzcoeff = {
            // First row: S_effSun(i)
            {0, 0, 0, 0, 0, 0},
            // Second row: a(i)
            {0, 0, 0, 0, 0, 0},
            // Third row:  b(i)
            {0, 0, 0, 0, 0, 0},
            // Fourth row: c(i)
            {0, 0, 0, 0, 0, -0},
            // Fifth row:  d(i)
            {0, 0, 0, 0, 0, 0}
    };



    private double effectiveTemp;

    private double recentVenus;

    private double runawayGreenhouse;

    private double maxGreenhouse;

    private double earlyMars;

    private double runawayGreehouse_5ME;

    private double runawayGreehouse_0_1ME;

    public HabitableZoneFluxes() {
        // initialize the hzcoeff's matrix
        IntStream.range(0, 5).forEach(i -> {
            hzcoeff[0][i] = seffsun[i];
            hzcoeff[1][i] = a[i];
            hzcoeff[2][i] = b[i];
            hzcoeff[3][i] = c[i];
            hzcoeff[4][i] = d[i];
        });
    }

    public void findStellarFluxes(double teff) {

        this.effectiveTemp = teff;

        double[] seff = {0, 0, 0, 0, 0, 0};
        double tstar = teff - 5780.0;

        for (int i = 0; i < 6; i++) {
            seff[i] = seffsun[i] + a[i] * tstar + b[i] * pow(tstar, 2) + c[i] * pow(tstar, 3) + d[i] * pow(tstar, 4);
        }

        this.recentVenus = seff[0];
        this.runawayGreenhouse = seff[1];
        this.maxGreenhouse= seff[2];
        this.earlyMars=seff[3];
        this.runawayGreehouse_5ME = seff[4];
        this.runawayGreehouse_0_1ME = seff[5];

    }

    /**
     * given an effective stellar flux, is the area within the habitation zone
     *
     * @param effectiveStellarFlux the effective stellar flux
     * @return true if it is
     */
    public boolean isInHabitableZone(double effectiveStellarFlux) {
        if (effectiveStellarFlux < earlyMars) {
            log.info("This object is NOT in the Habitable Zone (Beyond Early Mars)");
            return false;
        }
        if (effectiveStellarFlux <= runawayGreenhouse && effectiveStellarFlux > earlyMars) {
            log.info("This object is in the Optimistic Habitable Zone (Between Maximum Greenhouse and Early Mars)");
            return true;
        }
        if (effectiveStellarFlux <= runawayGreenhouse && effectiveStellarFlux > maxGreenhouse) {
            log.info("This object is in the Conservative Habitable Zone (Between Runaway Greenhouse and Maximum Greenhouse)");
            return true;
        }
        if (effectiveStellarFlux <= recentVenus && effectiveStellarFlux > runawayGreenhouse) {
            log.info("This object is in the Optimistic Habitable Zone (Between Recent Venus and Runaway Greenhouse)");
            return true;
        }

        log.info("This object is NOT in the Habitable Zone (Beyond Recent Venus)");
        return false;
    }

    /**
     * get the full range of habitation zones
     *
     * @param luminosity the stellar luminosity
     * @return the inner and out areas in AU
     */
    public HabitableZone getFullZone(double luminosity) {
        double innerRadius = auFromSeff(luminosity, recentVenus);
        double outerRadius = auFromSeff(luminosity, earlyMars);
        return HabitableZone
                .builder()
                .innerRadius(innerRadius)
                .outerRadius(outerRadius)
                .build();
    }

    /**
     * get the optimal habitable zone
     *
     * @param luminosity the stellar luminosity
     * @return the inner and out areas in AU
     */
    public HabitableZone getOptimalZone(double luminosity) {
        double innerRadius = auFromSeff(luminosity, runawayGreenhouse);
        double outerRadius = auFromSeff(luminosity, maxGreenhouse);
        return HabitableZone
                .builder()
                .innerRadius(innerRadius)
                .outerRadius(outerRadius)
                .build();
    }

    /**
     * From Kopparapu et al. 2014. Equation 5, Section 3.1, Page 9
     *
     * @param luminosity the stellar luminosity
     * @param seff       effective stellar flux
     * @return the distance in AU of the object
     */
    public double auFromSeff(double luminosity, double seff) {
        return sqrt(luminosity / seff);
    }

}
