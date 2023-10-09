package com.teamgannon.trips.graphics;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.RoutingPanelListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import javafx.geometry.Point3D;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * This plots a astrographic list of object to the panes
 * <p>
 * Created by larrymitchell on 2017-02-13.
 */
@Slf4j
public class PlotManager {

    private final SearchContext searchContext;
    private final TripsContext tripsContext;
    private final DatabaseManagementService databaseManagementService;

    private final CurrentPlot currentPlot;

    private final AstrographicTransformer astrographicTransformer;
    private final StarService starService;
    private final DataSetChangeListener dataSetChangeListener;
    private final StatusUpdaterListener statusUpdaterListener;
    private final RoutingPanelListener routingPanelListener;

    /**
     * the drawing surface for the astrographic plotter
     */
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * for dependency injection
     *
     * @param tripsContext the trips context
     */
    public PlotManager(@NotNull TripsContext tripsContext,
                       DatabaseManagementService databaseManagementService,
                       StarService starService,
                       DataSetChangeListener dataSetChangeListener,
                       StatusUpdaterListener statusUpdaterListener,
                       RoutingPanelListener routingPanelListener) {

        this.tripsContext = tripsContext;
        this.currentPlot = tripsContext.getCurrentPlot();
        this.searchContext = tripsContext.getSearchContext();
        this.databaseManagementService = databaseManagementService;
        this.astrographicTransformer = new AstrographicTransformer(tripsContext.getAppPreferences().getGridsize());
        this.starService = starService;
        this.dataSetChangeListener = dataSetChangeListener;
        this.statusUpdaterListener = statusUpdaterListener;
        this.routingPanelListener = routingPanelListener;
    }

    public void plotByConstellation(String constellationName) {

    }

    /**
     * show a loaded dataset in the plot menu
     */
    public void showPlot(SearchContext searchContext) {

        List<DataSetDescriptor> datasets = new ArrayList<>(searchContext.getDatasetDescriptors());

        if (datasets.size() == 0) {
            showErrorAlert("Plot Stars", "No datasets loaded, please load one");
            return;
        }

        if (tripsContext.getDataSetContext().isValidDescriptor()) {
            DataSetDescriptor dataSetDescriptor = tripsContext.getDataSetContext().getDescriptor();
            plotStars(dataSetDescriptor, searchContext.getAstroSearchQuery());
            routingPanelListener.updateRoutingPanel(dataSetDescriptor);
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
                routingPanelListener.updateRoutingPanel(dataSetDescriptor);

                plotStars(dataSetDescriptor, searchContext.getAstroSearchQuery());
                dataSetChangeListener.setContextDataSet(dataSetDescriptor);
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
                    tripsContext.getAppViewPreferences().getColorPallete(),
                    tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                    tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
            );
            statusUpdaterListener.updateStatus("Dataset plotted is selection from: " + dataSetDescriptor.getDataSetName());
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


        // plot all stars
        for (StarObject starObject : starObjects) {
            try {
                // create a star record object
                double[] ords = starObject.getCoordinates();
                double[] correctedOrds = astrographicTransformer.transformOrds(ords);

                // figure out what stars should be plotted in a sphere
                if (drawable(starObject)) {
                    starObject.calculateDisplayScore();
                    StarDisplayRecord record = StarDisplayRecord.fromStarObject(starObject, starDisplayPreferences);
                    if (record != null) {
                        record.setCurrentLabelDisplayScore(displayRadius);
                        record.setCoordinates(new Point3D(correctedOrds[0], correctedOrds[1], correctedOrds[2]));
                        currentPlot.addRecord(record.copy());
                    } else {
                        log.error("astrographic object is bad: {}", starObject);
                    }
                } else {
                    log.warn("star record is not drawable:{}", starObject);
                }
            } catch (IllegalArgumentException iae) {
                log.error("Star color is invalid:{}", starObject);
            }
        }

        currentPlot.determineVisibleLabels(starDisplayPreferences.getNumberOfVisibleLabels());

        // with our data now plot in interstellar
        interstellarSpacePane.plotStars(currentPlot);
        currentPlot.setPlotActive(true);

        // draw the routes for this descriptor
        interstellarSpacePane.redrawRoutes(dataSetDescriptor.getRoutes());

        // draw the labels for this plot
        interstellarSpacePane.updateLabels();

        // messages to end user
        String data = String.format("%s records plotted from dataset %s.",
                starObjects.size(),
                dataSetDescriptor.getDataSetName());

        showInfoMessage("Load Astrographic Format", data);
        statusUpdaterListener.updateStatus(data);
    }

    public Map<UUID, RouteVisibility> getRouteVisibility() {
        return currentPlot.getVisibilityMap();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

}
