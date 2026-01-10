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
public class PlanetarySystem {

    @Id
    @GeneratedValue
    private UUID systemId;

    private UUID starId;

    /**
     * the dataset name which we are guaranteeing to be unique
     */
    @Column(name = "DATASETNAME")
    private String dataSetName;

    @OneToMany
    @ToString.Exclude
    private Set<Planet> planets = new HashSet<>();

    @OneToMany
    @ToString.Exclude
    private Set<AsteroidBelt> asteroidBelts = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PlanetarySystem that = (PlanetarySystem) o;
        return systemId != null && Objects.equals(systemId, that.systemId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
