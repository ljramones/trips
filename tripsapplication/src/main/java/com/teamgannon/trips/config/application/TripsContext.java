package com.teamgannon.trips.config.application;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.config.application.model.ApplicationPreferences;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.constellation.Constellation;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.TransitSettings;
import com.teamgannon.trips.jpa.model.TripsPrefs;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.BulkLoadService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.SystemPreferencesService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@Component
public class TripsContext {

    private final DatabaseManagementService databaseManagementService;
    private final SystemPreferencesService systemPreferencesService;
    private final BulkLoadService bulkLoadService;

    public TripsContext(DatabaseManagementService databaseManagementService,
                        SystemPreferencesService systemPreferencesService,
                        BulkLoadService bulkLoadService) {
        this.databaseManagementService = databaseManagementService;
        this.systemPreferencesService = systemPreferencesService;
        this.bulkLoadService = bulkLoadService;
    }

    private ScreenSize screenSize = ScreenSize
            .builder()
            .sceneWidth(Universe.boxWidth)
            .sceneHeight(Universe.boxHeight)
            .depth(Universe.boxDepth)
            .spacing(20)
            .build();

    private AppViewPreferences appViewPreferences = new AppViewPreferences();

    private ApplicationPreferences appPreferences = new ApplicationPreferences();

    private SearchContext searchContext = new SearchContext();

    private TripsPrefs tripsPrefs = new TripsPrefs();

    private TransitSettings transitSettings = new TransitSettings();

    private ScriptContext scriptContext = new ScriptContext();

    private boolean showWarningOnZoom = true;

    public DataSetDescriptor getDataSetDescriptor() {
        try {
            return searchContext.getAstroSearchQuery().getDataSetContext().getDescriptor();
        } catch (Exception e) {
            log.error("No dataset descriptor available:" + e.getMessage());
            return null;
        }
    }

    public void setDataSetContext(DataSetContext dataSetContext) {
        getSearchContext().setCurrentDataSet(dataSetContext.getDescriptor().getDataSetName());
        searchContext.getAstroSearchQuery().setDataSetContext(dataSetContext);
        systemPreferencesService.updateDataSet(dataSetContext.getDescriptor());
    }

    /**
     * this hold the current plot data
     */
    private CurrentPlot currentPlot = new CurrentPlot();

    /**
     * the constellation set
     */
    private Map<String, Constellation> constellationMap = new HashMap<>();

    /**
     * the data set context
     *
     * @return the data set context
     */
    public DataSetContext getDataSetContext() {
        return searchContext.getDataSetContext();
    }

    public void removeDataSet(DataSetDescriptor dataSetDescriptor) {
        searchContext.removeDataSet(dataSetDescriptor);
        if (tripsPrefs.getDatasetName() != null) {
            if (tripsPrefs.getDatasetName().equals(dataSetDescriptor.getDataSetName())) {
                tripsPrefs.setDatasetName(null);
            }
        }
        systemPreferencesService.saveTripsPrefs(tripsPrefs);
        bulkLoadService.removeDataSet(dataSetDescriptor);
    }

    public void addDataSet(DataSetDescriptor dataSetDescriptor) {
        searchContext.addDataSet(dataSetDescriptor);
    }
}
