package com.teamgannon.trips.jpa.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Planet {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    @Column(name = "DATASETNAME")
    private String dataSetName;

    private String planetName;

    private String description;

    // planetary characteristics

    /**
     * Mass (1024kg or 1021tons) - This is the mass of the planet in septillion (1 followed by 24 zeros) kilograms
     * or sextillion (1 followed by 21 zeros) tons. Strictly speaking tons are measures of weight, not mass,
     * but are used here to represent the mass of one ton of material under Earth gravity.
     */
    private double mass;

    /**
     * Diameter (km or miles) - The diameter of the planet at the equator, the distance through the center of the
     * planet from one point on the equator to the opposite side, in kilometers or miles.
     */
    private double diameter;

    /**
     * Density (kg/m3 or lbs/ft3) - The average density (mass divided by volume) of the whole planet (not including
     * the atmosphere for the terrestrial planets) in kilograms per cubic meter or pounds per cubic foot.
     */
    private double density;

    /**
     * Gravity (m/s2 or ft/s2) - The gravitational acceleration on the surface at the equator in meters per second
     * squared or feet per second squared, including the effects of rotation. For the gas giant planets the
     * gravity is given at the 1 bar pressure level in the atmosphere. The gravity on Earth is designated as 1 "G",
     * so the Earth ratio fact sheets gives the gravity of the other planets in G's.
     */
    private double surfaceGravity;

    /**
     * Escape Velocity (km/s) - Initial velocity, in kilometers per second or miles per second, needed at the surface
     * (at the 1 bar pressure level for the gas giants) to escape the body's gravitational pull, ignoring
     * atmospheric drag.
     */
    private double escapeVelocity;

    /**
     * Rotation Period (hours) - This is the time it takes for the planet to complete one rotation relative to the
     * fixed background stars (not relative to the Sun) in hours. Negative numbers indicate retrograde (backwards
     * relative to the Earth) rotation.
     */
    private double rotationalPeriod;

    /**
     * Length of Day (hours) - The average time in hours for the Sun to move from the noon position in the sky
     * at a point on the equator back to the same position.
     */
    private double lengthOfDay;

    /**
     * Distance from Sun (106 km or 106 miles) - This is the average distance from the planet to the Sun in
     * millions of kilometers or millions of miles, also known as the semi-major axis. All planets have orbits
     * which are elliptical, not perfectly circular, so there is a point in the orbit at which the planet is
     * closest to the Sun, the perihelion, and a point furthest from the Sun, the aphelion. The average distance
     * from the Sun is midway between these two values. The average distance from the Earth to the Sun is defined
     * as 1 Astronomical Unit (AU), so the ratio table gives this distance in AU.
     * * For the Moon, the average distance from the Earth is given.
     */
    private double distanceFromStar;

    /**
     * the rotation of the world around its star in earth years
     */
    private double orbitPeriod;

    /**
     * brightness of the world
     */
    private double magnitude;

    /**
     * the albedo of the world
     */
    private double geometricAlbedo;


    /**
     * the surface temperature of the world
     * in K
     */
    private double surfaceTemp;

    // Orbital characteristics
    /**
     * a (m) : semi-major axis
     * a : semi-major axis (m)
     */
    private double semiMajorAxis;

    /**
     * e : eccentricity (any value of e is supported, i.e. both elliptical and hyperbolic orbits can be used)
     */
    private double eccentricity;

    /**
     * i : inclination (degrees)
     */
    private double inclination;

    /**
     * ω : perigee argument (degrees)
     * aka argument of perifocus or argument of pericenter
     * <p>
     * In astronomy, the argument of periapsis (ω) is a way of talking about the orbit of a planet, asteroid or
     * comet. It is also known as the argument of perihelion or the argument of perifocus. It is the angle
     * (starting from the center of the orbit) between an orbiting body's periapsis and its ascending node.
     * Periapsis is the point when the orbiting object comes the closest to the thing it is orbiting around;
     * for example, the moon is at periapsis when it is closest to the Earth. The ascending node is one of
     * two places where an orbiting object passes through the reference plane, an imaginary flat surface which
     * runs through the object being orbited around. The size of the angle depends on which way the object is
     * orbiting.
     * <p>
     * The angle is measured in the orbital plane and in the direction of motion. For specific types of orbits,
     * words such as "perihelion" (for Sun-centered orbits), "perigee" (for Earth-centered orbits), "pericenter"
     * (general), etc. may replace the word "periapsis".
     * <p>
     * An argument of periapsis of 0° means that the orbiting body will be at its closest approach to the central
     * body at the same moment that it crosses the plane of reference from south to north. An argument of
     * periapsis of 90° means that the orbiting body will reach periapsis at its northmost distance from the
     * plane of reference.
     */
    private double perigee;

    /**
     * Ω : right ascension of the ascending node (degrees)
     * sometimes called L or Longitude
     * also called node
     */
    private double rightAscension;

    /**
     * v (rad) :  true anomaly
     */
    private double trueAnomaly;

    /**
     * M (rad) :  mean anomaly
     */
    private double meanAnomaly;

    /**
     * E (rad) : eccentric anomaly
     */
    private double eccentricAnomaly;

    @OneToMany
    @ToString.Exclude
    private Set<Moon> moons = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Planet planet = (Planet) o;
        return id != null && Objects.equals(id, planet.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
