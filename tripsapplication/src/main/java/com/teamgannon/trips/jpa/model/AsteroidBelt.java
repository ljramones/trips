package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Data
@Entity
public class AsteroidBelt {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    @Column(name = "DATASETNAME")
    private String dataSetName;

    private double innerRadius;

    private double outerRadius;

    private double density;

    private double diameter;

}
