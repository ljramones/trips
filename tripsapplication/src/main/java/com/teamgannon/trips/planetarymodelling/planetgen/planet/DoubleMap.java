package com.teamgannon.trips.planetarymodelling.planetgen.planet;

import java.util.Arrays;

public class DoubleMap {

    public final int width;
    public final int height;

    private final double[] data;

    public DoubleMap(int width, int height) {
        this.width = width;
        this.height = height;

        data = new double[width * height];
    }

    public DoubleMap(int width, int height, double initialValue) {
        this(width, height);

        Arrays.fill(data, initialValue);
    }

    public void setValue(int x, int y, double value) {
        data[x + y * width] = value;
    }

    public double getValue(int x, int y) {
        return data[x + y * width];
    }
}