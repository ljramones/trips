package com.teamgannon.trips.config.application;

import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.search.SearchContext;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class TripsContext {

    private @NotNull AppViewPreferences appViewPreferences = new AppViewPreferences();

    private @NotNull ApplicationPreferences appPreferences = new ApplicationPreferences();

    private @NotNull SearchContext searchContext = new SearchContext();

    private @NotNull DataSetContext dataSetContext = new DataSetContext();

}
