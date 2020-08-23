package com.teamgannon.trips.jpa.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class GraphEnablesPersist {

    @Id
    private String id;

    private boolean displayGrid = true;

    private boolean displayStems = true;

    private boolean displayLabels = true;

    private boolean displayLegend = true;

    public static GraphEnablesPersist getDefaults() {
        GraphEnablesPersist graphEnablesPersist = new GraphEnablesPersist();
        graphEnablesPersist.setDisplayGrid(true);
        graphEnablesPersist.setDisplayStems(true);
        graphEnablesPersist.setDisplayLabels(true);
        graphEnablesPersist.setDisplayLegend(true);
        return graphEnablesPersist;
    }

    public void setDefault() {
        displayGrid = true;
        displayStems = true;
        displayLabels = true;
        displayLegend = true;
    }

}
