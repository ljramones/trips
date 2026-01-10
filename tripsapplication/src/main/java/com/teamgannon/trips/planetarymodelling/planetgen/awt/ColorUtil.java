package com.teamgannon.trips.planetarymodelling.planetgen.awt;


import com.teamgannon.trips.planetarymodelling.planetgen.math.Color;

public class ColorUtil {

    private static Color AWT_TRANSPARENT = new Color(0, 0, 0, 0);

    public static Color toAwtColor(com.teamgannon.trips.planetarymodelling.planetgen.math.Color color) {
        if (color == null) {
            return AWT_TRANSPARENT;
        }
        return new Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getAlpha());
    }
}
