package com.teamgannon.trips.graphics;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.search.SearchContext;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
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

    private final AstrographicTransformer astrographicTransformer;
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
    public AstrographicPlotter(TripsContext tripsContext) {

        this.searchContext = tripsContext.getSearchContext();
        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        this.astrographicTransformer = new AstrographicTransformer(
                tripsContext.getAppPreferences().getGridsize());
    }

    public static Color getColor(double[] colors) {
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
     * @param astrographicObjects the list of objects to draw
     * @param centerCoordinates   the center of the plot
     * @param colorPalette        the color palette to draw
     */
    public void drawAstrographicData(DataSetDescriptor dataSetDescriptor,
                                     List<AstrographicObject> astrographicObjects,
                                     double[] centerCoordinates,
                                     ColorPalette colorPalette,
                                     StarDisplayPreferences starDisplayPreferences) {

        // clear old drawing
        interstellarSpacePane.clearStars();

        interstellarSpacePane.setDataSetContext(dataSetDescriptor);

        // find the min/max values to plot
        astrographicTransformer.findMinMaxValues(astrographicObjects, centerCoordinates);
        ScalingParameters scalingParameters = astrographicTransformer.getScalingParameters();
        log.info("New Plot Scaling parameters:" + scalingParameters);
        interstellarSpacePane.getGridPlotManager().rebuildGrid(astrographicTransformer, colorPalette);

        // plot all stars
        for (AstrographicObject astrographicObject : astrographicObjects) {
            try {
                // create a star record object
                double[] ords = astrographicObject.getCoordinates();
                double[] correctedOrds = astrographicTransformer.transformOrds(ords);

                // draw the star
                if (drawable(astrographicObject)) {
                    StarDisplayRecord record = StarDisplayRecord.fromAstrographicObject(astrographicObject);
                    record.setCoordinates(new Point3D(correctedOrds[0], correctedOrds[1], correctedOrds[2]));
                    interstellarSpacePane.drawStar(record,
                            searchContext.getAstroSearchQuery().getCenterStar(),
                            colorPalette,
                            starDisplayPreferences);
//                    log.info("\nstar: {}\n", astrographicObject);
                } else {
                    log.warn("star record is not drawable:{}", astrographicObject);
                }
            } catch (IllegalArgumentException iae) {
                log.error("Star color is invalid:{}", astrographicObject);
            }
        }
        // draw the routes for this descriptor
        interstellarSpacePane.redrawRoutes(dataSetDescriptor.getRoutes());

        String data = String.format("%s records plotted from dataset %s.",
                astrographicObjects.size(),
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
    private boolean drawable(AstrographicObject starRecord) {
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
