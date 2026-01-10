package com.teamgannon.trips.utility;

public final class ColorDefaults {

    private ColorDefaults() {
    }

    public static double[] defaultColorArray() {
        return new double[]{0.6666667, 0.73333335, 0.8};
    }

    public static javafx.scene.paint.Color defaultColor() {
        return javafx.scene.paint.Color.color(0.6666667, 0.73333335, 0.8);
    }
}
