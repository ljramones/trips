package com.teamgannon.trips.graphics;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.GraphEnablesPersistEvent;
import com.teamgannon.trips.events.RoutingPanelUpdateEvent;
import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.StarService;
import javafx.geometry.Point3D;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * This plots an astrographic list of object to the panes
 * <p>
 * Created by larrymitchell on 2017-02-13.
 */
@Slf4j
public class PlotManager {

    private final SearchContext searchContext;
    private final TripsContext tripsContext;

    private final CurrentPlot currentPlot;

    private final AstrographicTransformer astrographicTransformer;
    private final StarService starService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * the drawing surface for the astrographic plotter
     */
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * for dependency injection
     *
     * @param tripsContext the trips context
     */
    public PlotManager(TripsContext tripsContext,
                       StarService starService,
                       ApplicationEventPublisher eventPublisher) {

        this.tripsContext = tripsContext;
        this.currentPlot = tripsContext.getCurrentPlot();
        this.searchContext = tripsContext.getSearchContext();
        this.astrographicTransformer = new AstrographicTransformer(tripsContext.getAppPreferences().getGridsize());
        this.starService = starService;
        this.eventPublisher = eventPublisher;
    }

    public void plotByConstellation(String constellationName) {

    }

    /**
     * show a loaded dataset in the plot menu
     */
    public void showPlot(SearchContext searchContext) {

        List<DataSetDescriptor> datasets = new ArrayList<>(searchContext.getDatasetDescriptors());

        if (datasets.isEmpty()) {
            showErrorAlert("Plot Stars", "No datasets loaded, please load one");
            return;
        }

        if (tripsContext.getDataSetContext().isValidDescriptor()) {
            DataSetDescriptor dataSetDescriptor = tripsContext.getDataSetContext().getDescriptor();
            plotStars(dataSetDescriptor, searchContext.getAstroSearchQuery());
            eventPublisher.publishEvent(new RoutingPanelUpdateEvent(this, dataSetDescriptor, getRouteVisibility()));
        } else {

            List<String> dialogData = datasets.stream().map(DataSetDescriptor::getDataSetName).collect(Collectors.toList());

            ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
            dialog.setTitle("Choose data set to display");
            dialog.setHeaderText("Select your choice - (Default display is 20 light years from Earth, use Show Stars filter to change)");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String selected = result.get();

                DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
                if (dataSetDescriptor == null) {
                    log.error("How the hell did this happen");
                    return;
                }

                // update the routing table in the side panel
                eventPublisher.publishEvent(new RoutingPanelUpdateEvent(this, dataSetDescriptor, getRouteVisibility()));

                plotStars(dataSetDescriptor, searchContext.getAstroSearchQuery());
                eventPublisher.publishEvent(new SetContextDataSetEvent(this, dataSetDescriptor));
            }
        }
    }

    /**
     * find the data selected
     *
     * @param selected the selected data
     * @param datasets the datasets
     * @return the descriptor wanted
     */
    private DataSetDescriptor findFromDataSet(String selected, @NotNull List<DataSetDescriptor> datasets) {
        return datasets.stream().filter(dataSetDescriptor -> dataSetDescriptor.getDataSetName().equals(selected)).findFirst().orElse(null);
    }

    /**
     * plot a series of stars that are within the current range of the distance slider
     *
     * @param dataSetDescriptor the data descriptor
     */
    @TrackExecutionTime
    public void plotStars(@NotNull DataSetDescriptor dataSetDescriptor, AstroSearchQuery astroSearchQuery) {

        // get the distance range
        double displayRadius = astroSearchQuery.getUpperDistanceLimit();

        // now query the data and plot
        List<StarObject> starObjects = starService.getAstrographicObjectsOnQuery(searchContext);

        log.info("DB Query returns {} stars", starObjects.size());

        if (!starObjects.isEmpty()) {

            astroSearchQuery.zeroCenter();
            drawAstrographicData(
                    tripsContext.getDataSetDescriptor(),
                    starObjects,
                    displayRadius,
                    astroSearchQuery.getCenterCoordinates(),
                    tripsContext.getAppViewPreferences().getColorPalette(),
                    tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                    tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
            );
            eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset plotted is selection from: " + dataSetDescriptor.getDataSetName()));
        } else {
            showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
        }
    }

    public static @NotNull Color getColor(double[] colors) {
        return Color.color(colors[0], colors[1], colors[2]);
    }

    /**
     * set the pane for plotting stars
     *
     * @param interstellarSpacePane the drawing pane
     */
    public void setInterstellarPane(InterstellarSpacePane interstellarSpacePane) {
        this.interstellarSpacePane = interstellarSpacePane;
    }

    /**
     * draw the ch view file
     *
     * @param starObjects       the list of objects to draw
     * @param displayRadius     the max display radius
     * @param centerCoordinates the center of the plot
     * @param colorPalette      the color palette to draw
     */
    @TrackExecutionTime
    public void drawAstrographicData(@NotNull DataSetDescriptor dataSetDescriptor,
                                     @NotNull List<StarObject> starObjects,
                                     double displayRadius,
                                     double[] centerCoordinates,
                                     @NotNull ColorPalette colorPalette,
                                     @NotNull StarDisplayPreferences starDisplayPreferences,
                                     CivilizationDisplayPreferences civilizationDisplayPreferences) {

        // clear plot first
        currentPlot.clearPlot();

        // clear interstellarPlot
        interstellarSpacePane.clearAll();

        // set center star
        String centerStar = searchContext.getAstroSearchQuery().getCenterStar();

        // setup the star plot
        currentPlot.setupPlot(
                dataSetDescriptor, centerCoordinates,
                centerStar, colorPalette
        );

        // find the min/max values to plot
        astrographicTransformer.findMinMaxValues(starObjects, centerCoordinates);
        ScalingParameters scalingParameters = astrographicTransformer.getScalingParameters();
        log.info("New Plot Scaling parameters:" + scalingParameters);

        // rebuild the grid based on parameters
        interstellarSpacePane.rebuildGrid(centerCoordinates, astrographicTransformer, currentPlot);


        // Pre-filter stars: validate and collect only drawable stars with valid data
        // This moves exception handling outside the main rendering loop for better performance
        List<StarObject> validStars = filterValidStars(starObjects);
        int invalidCount = starObjects.size() - validStars.size();
        if (invalidCount > 0) {
            log.warn("Filtered out {} invalid/non-drawable stars", invalidCount);
        }

        // Plot all valid stars (no try-catch needed - already validated)
        for (StarObject starObject : validStars) {
            double[] ords = starObject.getCoordinates();
            double[] correctedOrds = astrographicTransformer.transformOrds(ords);

            starObject.calculateDisplayScore();
            StarDisplayRecord record = StarDisplayRecord.fromStarObject(starObject, starDisplayPreferences);
            if (record != null) {
                record.setCurrentLabelDisplayScore(displayRadius);
                record.setCoordinates(new Point3D(correctedOrds[0], correctedOrds[1], correctedOrds[2]));
                currentPlot.addRecord(record);
            }
        }

        // Use spatial index for efficient label selection when available
        currentPlot.determineVisibleLabelsWithSpatialIndex(
                starDisplayPreferences.getNumberOfVisibleLabels(),
                displayRadius
        );

        // with our data now plot in interstellar
        interstellarSpacePane.plotStars(currentPlot);
        currentPlot.setPlotActive(true);

        // draw the routes for this descriptor
        interstellarSpacePane.redrawRoutes(dataSetDescriptor.getRoutes());

        // render nebulae in the plot range
        interstellarSpacePane.renderNebulae(
                dataSetDescriptor.getDataSetName(),
                centerCoordinates[0],
                centerCoordinates[1],
                centerCoordinates[2],
                displayRadius,
                scalingParameters.getScalingFactor()
        );

        // draw the labels for this plot
        interstellarSpacePane.updateLabels();

        // messages to end user
        String data = String.format("%s records plotted from dataset %s.",
                starObjects.size(),
                dataSetDescriptor.getDataSetName());

        showInfoMessage("Load Astrographic Format", data);
        eventPublisher.publishEvent(new StatusUpdateEvent(this, data));
    }

    public Map<UUID, RouteVisibility> getRouteVisibility() {
        return currentPlot.getVisibilityMap();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Pre-filters stars to remove invalid or non-drawable entries.
     * <p>
     * This validation pass moves exception handling outside the main rendering loop,
     * improving performance by avoiding try-catch overhead for each star.
     *
     * @param starObjects the list of stars to filter
     * @return list of valid, drawable stars
     */
    @TrackExecutionTime
    private @NotNull List<StarObject> filterValidStars(@NotNull List<StarObject> starObjects) {
        List<StarObject> validStars = new ArrayList<>(starObjects.size());

        for (StarObject star : starObjects) {
            try {
                // Check if drawable (within bounds)
                if (!drawable(star)) {
                    continue;
                }

                // Validate star has required data for display
                if (star.getCoordinates() == null || star.getCoordinates().length < 3) {
                    log.debug("Skipping star with invalid coordinates: {}", star.getDisplayName());
                    continue;
                }

                // Validate spectral class can be parsed (color depends on this)
                String spectralClass = star.getOrthoSpectralClass();
                if (spectralClass == null || spectralClass.isEmpty()) {
                    log.debug("Skipping star with missing spectral class: {}", star.getDisplayName());
                    continue;
                }

                validStars.add(star);
            } catch (Exception e) {
                log.debug("Skipping invalid star {}: {}", star.getDisplayName(), e.getMessage());
            }
        }

        return validStars;
    }

    /**
     * check if the point is in the drawable area
     *
     * @param starRecord the list of ordinates
     * @return true if in the area and false otherwise
     */
    private boolean drawable(@NotNull StarObject starRecord) {
        double[] ordinates = starRecord.getCoordinates();
        if (ordinates[0] > Universe.boxWidth) {
            return false;
        }
        if (ordinates[1] > Universe.boxHeight) {
            return false;
        }
        return !(ordinates[2] > Universe.boxDepth);
    }

    public void changeGraphEnables(GraphEnablesPersist graphEnablesPersist) {
    }

    @EventListener
    public void onGraphEnablesPersistEvent(GraphEnablesPersistEvent event) {

    }

}
