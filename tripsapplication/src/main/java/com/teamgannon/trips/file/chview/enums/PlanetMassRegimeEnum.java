package com.teamgannon.trips.file.chview.enums;

/**
 * Definition the mass range for a planet
 * <p>
 * see: https://en.wikipedia.org/wiki/List_of_planet_types
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
public enum PlanetMassRegimeEnum {

    /**
     * A massive planet. They are most commonly composed primarily of 'gas' (hydrogen and helium) or 'ices'
     * (volatiles such as water, methane, and ammonia), but may also be composed primarily of rock.
     * Regardless of their bulk compositions, giant planets normally have thick atmospheres of hydrogen
     * and helium.
     */
    giantPlanet,

    /**
     * Mesoplanets are planetary bodies with sizes smaller than Mercury but larger than Ceres. The term was
     * coined by Isaac Asimov. Assuming "size" is defined by linear dimension (or by volume), mesoplanets should
     * be approximately 1,000 km to 5,000 km in diameter.
     */
    mesoPlanet,

    /**
     * A mini-Neptune (sometimes known as a gas dwarf or transitional planet) is a planet smaller than Uranus and
     * Neptune, up to 10 Earth masses. Those planets have thick hydrogen–helium atmospheres, probably with deep
     * layers of ice, rock or liquid oceans (made of water, ammonia, a mixture of both, or heavier volatiles).
     */
    miniNeptune,

    /**
     * Planetary-mass object, an object which is hydrostatically round due to self-gravitation, but whose mass
     * is insufficient to initiate fusion at its core to become a star.
     */
    planetMo,

    /**
     * either a brown dwarf—an object with a size larger than a planet but smaller than a star—that has formed by
     * processes that typically yield planets; or a sub-brown dwarf, —an object smaller than a brown dwarf that
     * does not orbit a star.
     */
    planetar,

    /**
     * A super-Earth is an extrasolar planet with a mass higher than Earth's, but substantially below the mass of the
     * Solar System's smaller gas giants Uranus and Neptune, which are 15 and 17 Earth masses respectively.
     */
    superEarth,

    /**
     * A super-Jupiter is an astronomical object that's more massive than the planet Jupiter.
     */
    superJupiter,

    /**
     * Sub-Earth is a classification of planets "substantially less massive" than Earth and Venus.
     */
    subEarth

}
