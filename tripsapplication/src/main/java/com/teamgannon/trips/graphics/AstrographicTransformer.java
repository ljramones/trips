package com.teamgannon.trips.graphics;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.jpa.model.StarObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * This class determines how to map the imported data into the drawable universe
 * <p>
 * Created by larrymitchell on 2017-02-15.
 */
@Slf4j
public class AstrographicTransformer {

    // scaleIncrement
    private final double scaleIncrement;

    // X range limits
    private double minX = 0;
    private double maxX = 0;

    // Y range limits
    private double minY = 0;
    private double maxY = 0;

    // Z range limits
    private double minZ = 0;
    private double maxZ = 0;

    private double[] centerCoordinates;

    /**
     * the scaling parameters
     */
    private ScalingParameters scalingParameters;

    public AstrographicTransformer(double scaleIncrement) {
        this.scaleIncrement = scaleIncrement;
    }

    public ScalingParameters getScalingParameters() {
        return scalingParameters;
    }

    /**
     * transform the original to the drawing area of the screen
     *
     * @param ords the incoming to transform
     * @return the transformed coordinates
     */
    public double @NotNull [] transformOrds(double[] ords) {
        double[] transformOrds = new double[3];

        // transform coordinates
        transformOrds[0] = xTransform(ords[0]);
        transformOrds[1] = yTransform(ords[1]);
        transformOrds[2] = zTransform(ords[2]);

        // transform but for now just past back the original
        return transformOrds;
    }


    /**
     * transform an x coordinate to the 3D system
     *
     * @param x the x coordinate
     * @return the transformed coordinate
     */
    private double xTransform(double x) {
        return (x - centerCoordinates[0]) * scalingParameters.getScalingFactor();
    }

    /**
     * transform a y coordinate to the 3D system
     *
     * @param y the y coordinate
     * @return the transformed coordinate
     */
    private double yTransform(double y) {
        return (y - centerCoordinates[1]) * scalingParameters.getScalingFactor();
    }


    /**
     * transform a z coordinate to the 3D system
     *
     * @param z the z coordinate
     * @return the transformed coordinate
     */
    private double zTransform(double z) {
        return (z - centerCoordinates[2]) * scalingParameters.getScalingFactor();
    }

    /**
     * find the min/max in all coordinates
     *
     * @param starRecords       the star records to check
     * @param centerCoordinates teh center coordinates
     */
    public ScalingParameters findMinMaxValues(@NotNull List<StarObject> starRecords, double[] centerCoordinates) {

        scalingParameters = new ScalingParameters();
        clearRanges();

        this.centerCoordinates = centerCoordinates;

        // scan through the records and determine the range of x,y, and z data values
        for (StarObject starRecord : starRecords) {
            double[] ords = starRecord.getCoordinates();

            // check X limits, keep highest and lowest
            if (ords[0] > maxX) {
                maxX = ords[0];
            }
            if (ords[0] < minX) {
                minX = ords[0];
            }

            // check Y limits, keep highest and lowest
            if (ords[1] > maxY) {
                maxY = ords[1];
            }
            if (ords[1] < minY) {
                minY = ords[1];
            }

            // check Z limits, keep highest and lowest
            if (ords[2] > maxZ) {
                maxZ = ords[2];
            }
            if (ords[2] < minZ) {
                minZ = ords[2];
            }

//            log.info("x={}, y={}, z={}, minY={}, maxY={}", ords[0], ords[1], ords[2], minY, maxY);
        }

        scalingParameters.setMinX(minX);
        scalingParameters.setMaxX(maxX);
        scalingParameters.setMinY(minY);
        scalingParameters.setMaxY(maxY);
        scalingParameters.setMinZ(minZ);
        scalingParameters.setMaxZ(maxZ);

        double scalingFactor = findScalingValues(scalingParameters);
        scalingParameters.setScalingFactor(scalingFactor);

        return scalingParameters;
    }

    private void clearRanges() {
        minX = 0;
        maxX = 0;

        minY = 0;
        maxY = 0;

        minZ = 0;
        maxZ = 0;
    }

    private double findScalingValues(@NotNull ScalingParameters scalingParameters) {
        double maxValue = 0;
        if (scalingParameters.getXRange() > maxValue) {
            maxValue = scalingParameters.getXRange();
        }

        if (scalingParameters.getYRange() > maxValue) {
            maxValue = scalingParameters.getYRange();
        }

        if (scalingParameters.getZRange() > maxValue) {
            maxValue = scalingParameters.getZRange();
        }

        double scalingFactor = Universe.boxHeight / maxValue;

        // determine the grid scale for 5 light year increments
        double gridScale = scalingParameters.getYRange() / scaleIncrement * scalingFactor/2;
        if (gridScale <= 0) {
            log.error("gridScale ={}, scalingParameters={}", gridScale, scalingParameters);
        }
        scalingParameters.setScaleIncrement(scaleIncrement);
        scalingParameters.setGridScale(gridScale);

        // figure out how to scale the plot
        // we do this by the smallest dimension.
        return scalingFactor;
    }

}
