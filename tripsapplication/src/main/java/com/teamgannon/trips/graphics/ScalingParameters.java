package com.teamgannon.trips.graphics;

import lombok.Data;

@Data
public class ScalingParameters {

    private double scalingFactor;

    private double minX = 0;

    private double maxX = 0;

    private double minY = 0;

    private double maxY = 0;

    private double minZ = 0;

    private double maxZ = 0;

    private double gridScale = 1;
    private double scaleIncrement;

    public double getXRange() {
        return maxX - minX;
    }

    public double getYRange() {
        return maxY - minY;
    }

    public double getZRange() {
        return maxZ - minZ;
    }

}
