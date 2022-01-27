package com.teamgannon.trips.planetarymodelling;

import lombok.Data;

@Data
public class CometDescription {

    private String id;

    private String name;

    private String belongstoStar;

    private double mass;

    private double radius;

    private double semiMajorAxis;

    private double semiMinorAxis;

    private double eccentricity;

}
