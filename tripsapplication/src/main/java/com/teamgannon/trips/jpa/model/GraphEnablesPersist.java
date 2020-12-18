package com.teamgannon.trips.jpa.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
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

}
