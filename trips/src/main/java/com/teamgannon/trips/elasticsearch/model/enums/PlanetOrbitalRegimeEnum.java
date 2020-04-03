package com.teamgannon.trips.elasticsearch.model.enums;

/**
 * Defines a planet byt it orbital type
 * <p>
 * see: https://en.wikipedia.org/wiki/List_of_planet_types
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
public enum PlanetOrbitalRegimeEnum {

    /**
     * An exoplanet that orbits two stars.
     */
    circumbinaryPlanet,

    /**
     * Two planetary-mass objects orbiting each other.
     */
    doublePlanet,

    /**
     * A planetary-mass object that orbits its star, which does not represent an overwhelming proportion of the mass
     * in its orbital zone and does not control the orbital parameters of those objects (antonym: major planet)
     */
    dwarfPlanet,

    /**
     * A gas giant that orbits its star in an eccentric orbit.
     */
    eccentricJupiter,

    /**
     * A planet that does not orbit the Sun, but a different star, a stellar remnant, or a brown dwarf.
     */
    exoplanet,

    /**
     * An exoplanet outside the Milky Way.
     */
    extragalacticPlanet,

    /**
     * A Goldilocks planet is a planet that falls within a star's habitable zone. The name comes from the children's
     * fairy tale of Goldilocks and the Three Bears, in which a little girl chooses from sets of three items,
     * ignoring the ones that are too extreme (large or small, hot or cold, etc.), and settling on the one in the
     * middle, which is "just right".
     */
    goldilocksPlanet,

    /**
     * Hot Jupiters are a class of extrasolar planets whose characteristics are similar to Jupiter, but that have
     * high surface temperatures because they orbit very close—between approximately 0.015 and 0.5 astronomical
     * units (2.2×106 and 74.8×106 km)—to their parent stars, whereas Jupiter orbits its parent star (the Sun)
     * at 5.2 astronomical units (780×106 km), causing low surface temperatures.
     */
    hotJupiter,

    /**
     * A hot Neptune is an extrasolar planet in an orbit close to its star (normally less than one astronomical
     * unit away), with a mass similar to that of Uranus or Neptune.
     */
    hotNeptune,

    /**
     * The planets whose orbits lie within the orbit of Earth.
     * <p>
     * The terms "inferior planet" and "superior planet" were originally used in the geocentric cosmology of
     * Claudius Ptolemy to differentiate as 'inferior' those planets (Mercury and Venus) whose epicycle
     * remained collinear with Earth and the Sun, compared to the 'superior' planets (Mars, Jupiter,
     * and Saturn) that did not.
     */
    inferiorPlanet,

    /**
     * The inner planets are those planets in the Solar System that have orbits smaller than the asteroid belt.
     * <p>
     * The four inner or terrestrial planets have dense, rocky compositions, few or no moons, and no ring systems.
     * They are composed largely of refractory minerals, such as the silicates, which form their crusts and mantles,
     * and metals, such as iron and nickel, which form their cores. Three of the four inner planets (Venus,
     * Earth and Mars) have atmospheres substantial enough to generate weather; all have impact craters and
     * tectonic surface features, such as rift valleys and volcanos. The term inner planet should not be confused
     * with inferior planet, which designates those planets that are closer to the Sun than Earth is (i.e.
     * Mercury and Venus).
     */
    innerPlanet,

    /**
     * Planetary-mass objects which orbit stars that dominate their orbital zone and comprise the vast
     * majority of the mass in that zone (antonym: dwarf planet)
     */
    majorPlanet,

    /**
     * The outer planets are those planets in the Solar System beyond the asteroid belt, and hence refers to the
     * gas giants.
     */
    outerPlanet,

    /**
     * Pulsar planets are planets that are found orbiting pulsars, or rapidly rotating neutron stars.
     */
    pulsarPlanet,


    /**
     * A rogue planet is a planetary-mass object that orbits the galaxy directly.
     * interstellar planet
     */
    roguePlanet,

    /**
     * The planets whose orbits lie outside the orbit of Earth.
     */
    superiorPlanet,

    /**
     * The discovery of a pair of co-orbital exoplanets has been reported but later retracted.One possibility for
     * the habitable zone is a trojan planet of a gas giant close to its star.
     */
    trojanPlanet

}
