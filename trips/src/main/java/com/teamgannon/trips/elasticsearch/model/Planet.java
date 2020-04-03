package com.teamgannon.trips.elasticsearch.model;

import com.teamgannon.trips.elasticsearch.model.enums.PlanetCompositionType;
import com.teamgannon.trips.elasticsearch.model.enums.PlanetMassRegimeEnum;
import com.teamgannon.trips.elasticsearch.model.enums.PlanetOrbitalRegimeEnum;
import com.teamgannon.trips.elasticsearch.model.enums.PlanetaryProductType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Planet description
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Data
@Document(indexName = "planet", type = "planet", shards = 1, replicas = 0, refreshInterval = "-1")
public class Planet implements Serializable {

    private static final long serialVersionUID = -8247913075072030684L;

    @Id
    private String id;

    /**
     * the stellar system that this belongs to
     */
    private String stellarSystem;

    /**
     * planet mass classification
     */
    private PlanetMassRegimeEnum planetMassRegimeEnum;

    /**
     * the orbital classification of this planet
     */
    private PlanetOrbitalRegimeEnum planetOrbitalRegimeEnum;

    /**
     * the composition type of this planet
     */
    private PlanetCompositionType PlanetCompositionType;

    /**
     * planetary name
     */
    private String name;

    /**
     * orbital parameters of this planet with respect to its star
     */
    private OrbitalParameters orbitalParameters;

    /**
     * planetary mass
     */
    private double planetMass;

    /**
     * the population size
     */
    private double populationSize;

    /**
     * list of polities present on the planet
     */
    private String polity;

    /**
     * the technology level
     */
    private int techLevel;

    /**
     * whether there is a space port present
     */
    private boolean spacePort;

    /**
     * whether there a military on the planet
     */
    private boolean militaryPlanetside;

    /**
     * whether there is a military in space
     */
    private boolean militarySpaceside;

    /**
     * all the type of planets present
     */
    private Set<PlanetaryProductType> products = new HashSet<>();

    /**
     * whether there is an anomaly present
     */
    private boolean anomalyPresent;

    /**
     * whether there are ruins present
     */
    private boolean ruinsPresent;

    /**
     * the set of moons that are
     */
    Set<Planet> moons = new HashSet<>();


}
