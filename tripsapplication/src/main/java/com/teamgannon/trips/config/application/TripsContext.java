package com.teamgannon.trips.config.application;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.config.application.model.ApplicationPreferences;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.constellation.Constellation;
import com.teamgannon.trips.events.ColorPaletteChangeEvent;
import com.teamgannon.trips.events.GraphEnablesPersistEvent;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.TransitSettings;
import com.teamgannon.trips.jpa.model.TripsPrefs;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.BulkLoadService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.SystemPreferencesService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application-wide mutable singleton state — plot context, search context, view
 * preferences, dataset cursor, constellation map.
 *
 * <h2>Threading invariant (Phase 3.1)</h2>
 * <p>
 * All mutations to {@code TripsContext} fields run on the JavaFX Application
 * Thread. Reads are also expected on the FX thread; cross-thread reads are
 * tolerated but values may be stale.
 * <p>
 * The invariant is preserved as follows:
 * <ul>
 *   <li>The mutator methods on this class ({@link #setDataSetContext},
 *       {@link #addDataSet}, {@link #removeDataSet}, the Lombok-generated
 *       setters) are only ever invoked from controllers / panes that already
 *       run on the FX thread.</li>
 *   <li>The two {@code @EventListener} methods on this class
 *       ({@link #onColorPaletteChangeEvent}, {@link #onGraphEnablesPersistEvent})
 *       defensively re-dispatch via {@link FxThread#runOnFxThread} because
 *       Spring delivers events on the publisher's thread, which may be a
 *       background {@code Task} or {@code @Async}-method.</li>
 *   <li>{@link #constellationMap} is a {@link ConcurrentHashMap} —
 *       {@link com.teamgannon.trips.constellation.ConstellationLoader} populates
 *       it during startup (potentially from a background loader), while the FX
 *       thread reads it for dialogs like {@code FindAllByConstellationDialog}.</li>
 * </ul>
 *
 * A fuller redesign (immutable snapshots, mutator service, etc.) is tracked
 * as a Phase 7 follow-up — see {@code trips-full-codebase-review-2026.md}.
 */
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
            log.error("No dataset descriptor available", e);
            return null;
        }
    }

    public void setDataSetContext(DataSetContext dataSetContext) {
        if (dataSetContext == null || dataSetContext.getDescriptor() == null) {
            log.warn("setDataSetContext called with null dataset descriptor.");
            return;
        }
        getSearchContext().setCurrentDataSet(dataSetContext.getDescriptor().getDataSetName());
        searchContext.getAstroSearchQuery().setDataSetContext(dataSetContext);
        systemPreferencesService.updateDataSet(dataSetContext.getDescriptor());
    }

    /**
     * this hold the current plot data
     */
    private CurrentPlot currentPlot = new CurrentPlot();

    /**
     * The constellation set, populated at startup by
     * {@code com.teamgannon.trips.constellation.ConstellationLoader}.
     * <p>
     * {@link ConcurrentHashMap} so the loader (potentially off-FX-thread) can
     * write while the FX thread reads from dialogs like
     * {@code FindAllByConstellationDialog}.
     */
    private Map<String, Constellation> constellationMap = new ConcurrentHashMap<>();

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

    @EventListener
    public void onColorPaletteChangeEvent(ColorPaletteChangeEvent event) {
        // Phase 3.1 / 2.2 pattern: defensive FX-thread wrap. See class Javadoc.
        FxThread.runOnFxThread(() -> {
            getAppViewPreferences().setColorPalette(event.getColorPalette());
            log.info("Color palette changed to {}", event.getColorPalette());
        });
    }

    @EventListener
    public void onGraphEnablesPersistEvent(GraphEnablesPersistEvent event) {
        // Phase 3.1 / 2.2 pattern: defensive FX-thread wrap. See class Javadoc.
        FxThread.runOnFxThread(() ->
                getAppViewPreferences().setGraphEnablesPersist(event.getGraphEnablesPersist()));
    }
}
