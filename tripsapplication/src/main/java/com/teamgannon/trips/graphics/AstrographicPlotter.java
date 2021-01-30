package com.teamgannon.trips.graphics;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.SearchContext;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * This plots a astrographic list of object to the panes
 * <p>
 * Created by larrymitchell on 2017-02-13.
 */
@Slf4j
@Component
public class AstrographicPlotter {

    private final SearchContext searchContext;

    private final @NotNull AstrographicTransformer astrographicTransformer;
    private final ColorPalette colorPalette;
    /**
     * the drawing surface for the astrographic plotter
     */
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * for dependency injection
     *
     * @param tripsContext the trips context
     */
    public AstrographicPlotter(@NotNull TripsContext tripsContext) {

        this.searchContext = tripsContext.getSearchContext();
        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        this.astrographicTransformer = new AstrographicTransformer(tripsContext.getAppPreferences().getGridsize());
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
     * @param centerCoordinates the center of the plot
     * @param colorPalette      the color palette to draw
     */
    public void drawAstrographicData(@NotNull DataSetDescriptor dataSetDescriptor,
                                     @NotNull List<StarObject> starObjects,
                                     double[] centerCoordinates,
                                     @NotNull ColorPalette colorPalette,
                                     @NotNull StarDisplayPreferences starDisplayPreferences,
                                     CivilizationDisplayPreferences civilizationDisplayPreferences) {

        interstellarSpacePane.setupPlot(dataSetDescriptor, centerCoordinates, starDisplayPreferences, civilizationDisplayPreferences);

        // find the min/max values to plot
        astrographicTransformer.findMinMaxValues(starObjects, centerCoordinates);
        ScalingParameters scalingParameters = astrographicTransformer.getScalingParameters();
        log.info("New Plot Scaling parameters:" + scalingParameters);
        interstellarSpacePane.getGridPlotManager().rebuildGrid(astrographicTransformer, colorPalette);

        // plot all stars
        for (StarObject starObject : starObjects) {
            try {
                // create a star record object
                double[] ords = starObject.getCoordinates();
                double[] correctedOrds = astrographicTransformer.transformOrds(ords);

                // draw the star
                if (drawable(starObject)) {
                    StarDisplayRecord record = StarDisplayRecord.fromAstrographicObject(starObject, starDisplayPreferences);
                    if (record != null) {
                        record.setCoordinates(new Point3D(correctedOrds[0], correctedOrds[1], correctedOrds[2]));
                        interstellarSpacePane.plotStar(record,
                                searchContext.getAstroSearchQuery().getCenterStar(),
                                colorPalette,
                                starDisplayPreferences);
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
        // draw the routes for this descriptor
        interstellarSpacePane.redrawRoutes(dataSetDescriptor.getRoutes());

        interstellarSpacePane.updateLabels();
        String data = String.format("%s records plotted from dataset %s.",
                starObjects.size(),
                dataSetDescriptor.getDataSetName());
        showInfoMessage("Load Astrographic Format", data);
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
