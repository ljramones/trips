package com.teamgannon.trips.jpa.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class GraphEnablesPersist {

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
