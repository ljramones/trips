package com.teamgannon.trips.jpa.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class GraphEnablesPersist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private boolean displayPolities = true;

    private boolean displayGrid = true;

    private boolean displayStems = true;

    private boolean displayLabels = true;

    private boolean displayLegend = true;

    private boolean displayRoutes = true;

    public static @NotNull GraphEnablesPersist getDefaults() {
        GraphEnablesPersist graphEnablesPersist = new GraphEnablesPersist();
        graphEnablesPersist.setDisplayPolities(true);
        graphEnablesPersist.setDisplayGrid(true);
        graphEnablesPersist.setDisplayStems(true);
        graphEnablesPersist.setDisplayLabels(true);
        graphEnablesPersist.setDisplayLegend(true);
        graphEnablesPersist.setDisplayRoutes(true);
        return graphEnablesPersist;
    }

    public void setDefault() {
        displayPolities = true;
        displayGrid = true;
        displayStems = true;
        displayLabels = true;
        displayLegend = true;
        displayRoutes = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GraphEnablesPersist that = (GraphEnablesPersist) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
