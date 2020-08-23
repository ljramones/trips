package com.teamgannon.trips.dialogs.preferencespanes.model;

import com.teamgannon.trips.jpa.model.GraphPersistValues;
import lombok.Data;

@Data
public class GraphEnables {

    private boolean displayGrid = true;

    private boolean displayStems = true;

    private boolean displayLabels = true;

    private boolean displayLegend = true;

    public static GraphEnables getDefaults() {
        GraphEnables graphEnables = new GraphEnables();
        graphEnables.setDisplayGrid(true);
        graphEnables.setDisplayStems(true);
        graphEnables.setDisplayLabels(true);
        graphEnables.setDisplayLegend(true);
        return graphEnables;
    }

    public void assignEnables(GraphPersistValues graphPersistValues) {
        displayGrid = graphPersistValues.isDisplayGrid();
        displayStems = graphPersistValues.isDisplayStems();
        displayLabels = graphPersistValues.isDisplayLabels();
        displayLegend = graphPersistValues.isDisplayLegend();
    }
}
