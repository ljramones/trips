package com.teamgannon.trips.jpa.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Moon {

    @Id
    private UUID id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Moon moon = (Moon) o;
        return id != null && Objects.equals(id, moon.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
