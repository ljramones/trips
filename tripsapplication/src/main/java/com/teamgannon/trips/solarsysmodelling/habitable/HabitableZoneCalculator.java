package com.teamgannon.trips.solarsysmodelling.habitable;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

@Slf4j
public class HabitableZoneCalculator {

    private final HabitableZoneFluxes fluxes = new HabitableZoneFluxes();

    public static void main(String[] args) {
        HabitableZoneCalculator hz = new HabitableZoneCalculator();
        HabitableZoneFluxes habitableZoneFluxes = hz.findStellarFluxes(5780.0);
        log.info(habitableZoneFluxes.toString());
        Map<HabitableZoneTypesEnum, HabitableZone> zones = hz.getHabitableZones(5780.0, 1);
        log.info(zones.toString());

        double earthEffFlux = hz.flux(5780, 1);

        double earthDistance = hz.auFromSeff(5780, earthEffFlux);

        log.info("done");
    }

    /**
     * Adapted from NASA Exoplanet Archive Insolation Flux formula.
     * (https://exoplanetarchive.ipac.caltech.edu/docs/poet_calculations.html)
     *
     * @param luminosity    the stellar luminosity
     * @param semiMajorAxis the semi major axis
     * @return the effective stellar flux at that distance
     */
    public double flux(double luminosity, double semiMajorAxis) {
        return (pow(1 / semiMajorAxis, 2)) * luminosity;
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

    public Map<HabitableZoneTypesEnum, HabitableZone> getHabitableZones(double tempEffective, double luminosity) {
        Map<HabitableZoneTypesEnum, HabitableZone> zones = new HashMap<>();
        fluxes.findStellarFluxes(tempEffective);
        HabitableZone fullZone = fluxes.getFullZone(luminosity);
        zones.put(HabitableZoneTypesEnum.MAX, fullZone);

        HabitableZone optimalZone = fluxes.getOptimalZone(luminosity);
        zones.put(HabitableZoneTypesEnum.OPTIMAL, optimalZone);

        return zones;
    }


    /////////////////////////

    private HabitableZoneFluxes findStellarFluxes(double tempEffective) {
        fluxes.findStellarFluxes(tempEffective);
        return fluxes;
    }

}
