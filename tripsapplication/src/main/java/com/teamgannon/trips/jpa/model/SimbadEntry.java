package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Defines a simbad entry
 * <p>
 * Created by larrymitchell on 2017-02-24.
 */
@Data
@Entity
public class SimbadEntry implements Serializable {

    private static final long serialVersionUID = 4727134487065704499L;

    @Id
    private String id;

    private int recordNumber;

    private String identifier;

    private String oType;

    private String galacticLong;

    private String galacticLat;

    private double parallaxes;

    private double magU;

    private double magB;

    private double magV;

    private double magR;

    private double magI;

    private String spectralType;

    private int numPar;

    private int numChild;

    private int numSiblings;

}
