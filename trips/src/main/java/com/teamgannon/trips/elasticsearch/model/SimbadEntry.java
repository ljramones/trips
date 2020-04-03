package com.teamgannon.trips.elasticsearch.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

/**
 * Defines a simbad entry
 *
 * Created by larrymitchell on 2017-02-24.
 */
@Data
@Document(indexName = "simbadentry", type = "simbadentry", shards = 1, replicas = 0, refreshInterval = "-1")
public class SimbadEntry implements Serializable{

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
