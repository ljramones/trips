package com.teamgannon.trips.config.application;

import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.search.SearchContext;
import lombok.Data;

@Data
public class TripsContext {

    private AppViewPreferences appViewPreferences = new AppViewPreferences();

    private ApplicationPreferences appPreferences = new ApplicationPreferences();

    private SearchContext searchContext = new SearchContext();

    private DataSetContext dataSetContext = new DataSetContext();

}
