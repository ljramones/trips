package com.teamgannon.trips.jpa.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AsteroidBelt that = (AsteroidBelt) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
