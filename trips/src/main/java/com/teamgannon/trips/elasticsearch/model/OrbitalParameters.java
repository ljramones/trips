package com.teamgannon.trips.elasticsearch.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Orbital parameters
 * <p>
 * Much of this is from http://www.braeunig.us/space/orbmech.htm
 * <p>
 * See there for any figures
 * <p>
 * To mathematically describe an orbit one must define six quantities, called orbital elements. They are
 * <p>
 * - Semi-Major Axis, a
 * - Eccentricity, e
 * - Inclination, i
 * - Argument of Periapsis,
 * - Time of Periapsis Passage, T
 * - Longitude of Ascending Node,
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
public class OrbitalParameters implements Serializable {

    private static final long serialVersionUID = 1989386375417394396L;

    /**
     * Semi-Major Axis, a
     * <p>
     * The semi-major axis is one-half of the major axis and represents a satellite's mean distance from its primary.
     */
    private double semiMajorAxis;

    /**
     * Eccentricity, e
     * <p>
     * Eccentricity is the distance between the foci divided by the length of the major axis and is a number between
     * zero and one. An eccentricity of zero indicates a circle
     */
    private double eccentricity;

    /**
     * Inclination, i
     * <p>
     * Inclination is the angular distance between a satellite's orbital plane and the equator of its primary
     * (or the ecliptic plane in the case of heliocentric, or sun centered, orbits). An inclination of zero degrees
     * indicates an orbit about the primary's equator in the same direction as the primary's rotation, a direction
     * called prograde (or direct). An inclination of 90 degrees indicates a polar orbit. An inclination
     * of 180 degrees indicates a retrograde equatorial orbit. A retrograde orbit is one in which a satellite
     * moves in a direction opposite to the rotation of its primary.
     */
    private double inclination;

    /**
     * Argument of Periapsis, small omega
     * <p>
     * eriapsis is the point in an orbit closest to the primary. The opposite of periapsis, the farthest point in
     * an orbit, is called apoapsis. Periapsis and apoapsis are usually modified to apply to the body being orbited,
     * such as perihelion and aphelion for the Sun, perigee and apogee for Earth, perijove and apojove for Jupiter,
     * perilune and apolune for the Moon, etc. The argument of periapsis is the angular distance between the
     * ascending node and the point of periapsis (see Figure 4.3). The time of periapsis passage is the time
     * in which a satellite moves through its point of periapsis.
     */
    private double argumentOfPeriapsis;

    /**
     * Time of Periapsis Passage, T
     */
    private double timeOfPeriapsisPassage;

    /**
     * Longitude of Ascending Node, big Omega
     * <p>
     * Nodes are the points where an orbit crosses a plane, such as a satellite crossing the Earth's equatorial
     * plane. If the satellite crosses the plane going from south to north, the node is the ascending node;
     * if moving from north to south, it is the descending node. The longitude of the ascending node is
     * the node's celestial longitude. Celestial longitude is analogous to longitude on Earth and is
     * measured in degrees counter-clockwise from zero with zero longitude being in the direction of
     * the vernal equinox.
     */
    private double longitudeOfAscendingNode;

    public String convertToString() {
        return convertToString(this);
    }

    public String convertToString(OrbitalParameters parameters) {

        return "semiMajorAxis:" + parameters.getSemiMajorAxis() + "," +
                "eccentricity:" + parameters.getEccentricity() + "," +
                "inclination:" + parameters.getInclination() + "," +
                "argumentOfPeriapsis:" + parameters.getArgumentOfPeriapsis() + "," +
                "timeOfPeriapsisPassage:" + parameters.getTimeOfPeriapsisPassage() + "," +
                "longitudeOfAscendingNode:" + parameters.getLongitudeOfAscendingNode();
    }

    public OrbitalParameters toOrbitalParameters(String parametersStr) {
        OrbitalParameters parameters = new OrbitalParameters();
        String[] fields = parametersStr.split(",");
        try {
            for (String field : fields) {
                String[] attribute = field.split(":");
                switch (attribute[0]) {
                    case "semiMajorAxis":
                        parameters.setSemiMajorAxis(Double.parseDouble(attribute[1]));
                        break;
                    case "eccentricity":
                        parameters.setEccentricity(Double.parseDouble(attribute[1]));
                        break;
                    case "inclination":
                        parameters.setInclination(Double.parseDouble(attribute[1]));
                        break;
                    case "argumentOfPeriapsis":
                        parameters.setArgumentOfPeriapsis(Double.parseDouble(attribute[1]));
                        break;
                    case "timeOfPeriapsisPassage":
                        parameters.setTimeOfPeriapsisPassage(Double.parseDouble(attribute[1]));
                        break;
                    case "longitudeOfAscendingNode":
                        parameters.setLongitudeOfAscendingNode(Double.parseDouble(attribute[1]));
                        break;
                    default:
                        log.error("unknown label: {}", attribute[0]);
                }
            }
        } catch (NumberFormatException nfe) {
            log.error("bad format error:{}", nfe);
        }
        return parameters;
    }


}
