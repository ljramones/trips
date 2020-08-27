package com.teamgannon.trips.routing;

import com.teamgannon.trips.dataset.enums.GridLines;
import javafx.scene.paint.Color;
import lombok.Data;

/**
 * Description of the Route
 * <p>
 * Contains an array of route display definitions
 * Programmer determines max number allowed
 * This merely controls the display of routes of type, not
 * the storage of the routes themselves.
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Data
public class RouteDescriptor {


    /**
     * Boolean Display this Route type
     */
    private boolean routeDisp = true;

    /**
     * Hexadecimal Color value of route lines
     */
    private double[] routeColor = new double[]{0xAA, 0xBB, 0xCC};
    /**
     * Style of Route lines
     */
    private GridLines routeStyle = GridLines.Solid;

    public Color getRouteColor() {
        return Color.color(routeColor[0], routeColor[1], routeColor[2]);
    }

    public void setRouteColor(Color color) {
        routeColor[0] = color.getRed();
        routeColor[1] = color.getGreen();
        routeColor[2] = color.getBlue();
    }

}
