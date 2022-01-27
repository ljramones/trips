package com.teamgannon.trips.jpa.model;

import javafx.scene.paint.Color;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class TransitSettings {

    @Id
    private String id;

    private double upperDistance = 9;

    private double lowerDistance = 3;

    private double lineWidth = 1;

    private String lineColor = Color.CYAN.toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TransitSettings that = (TransitSettings) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
