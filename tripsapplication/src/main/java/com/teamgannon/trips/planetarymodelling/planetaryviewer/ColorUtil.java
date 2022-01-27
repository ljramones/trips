package com.teamgannon.trips.planetarymodelling.planetaryviewer;

import javafx.scene.paint.Color;

public class ColorUtil {

	public static Color toJavafxColor(com.teamgannon.trips.planetarymodelling.planetgen.math.Color color) {
		if (color == null) {
			return Color.TRANSPARENT;
		}
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
}
