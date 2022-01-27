package com.teamgannon.trips.jpa.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class StarDetailsPersist implements Serializable {

    @Id
    private String id;

    /**
     * the stelaar class
     */
    @Column(unique = true)
    private String stellarClass;

    /**
     * the star color
     */
    private String starColor;

    /**
     * defined in SOL units (x Sol)
     */
    private float radius = 10;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        StarDetailsPersist that = (StarDetailsPersist) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
