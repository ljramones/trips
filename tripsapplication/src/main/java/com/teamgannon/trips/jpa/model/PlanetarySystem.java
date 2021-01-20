package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
public class PlanetarySystem {

    @Id
    @GeneratedValue
    private UUID systemId;

    private UUID starId;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    @Column(name="DATASETNAME")
    private String dataSetName;

    @OneToMany
    private Set<Planet> planets = new HashSet<>();

    @OneToMany
    private Set<AsteroidBelt> asteroidBelts = new HashSet<>();


}
