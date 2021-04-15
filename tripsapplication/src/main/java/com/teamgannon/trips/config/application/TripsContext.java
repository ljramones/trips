package com.teamgannon.trips.config.application;

import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.jpa.model.TransitSettings;
import com.teamgannon.trips.jpa.model.TripsPrefs;
import com.teamgannon.trips.search.SearchContext;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class TripsContext {

    private @NotNull AppViewPreferences appViewPreferences = new AppViewPreferences();

    private @NotNull ApplicationPreferences appPreferences = new ApplicationPreferences();

    private @NotNull SearchContext searchContext = new SearchContext();

    private @NotNull DataSetContext dataSetContext = new DataSetContext();

    private @NotNull TripsPrefs tripsPrefs = new TripsPrefs();

    private @NotNull TransitSettings transitSettings = new TransitSettings();

    private boolean showWarningOnZoom = true;

    /**
     * this hold the current plot data
     */
    private @NotNull CurrentPlot currentPlot = new CurrentPlot();

    /**
     * the constellation set
     */
    private ConstellationSet constellationSet = new ConstellationSet();

}
