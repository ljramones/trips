package com.teamgannon.trips.file.chview.model;

import javafx.scene.paint.Color;
import lombok.Data;


/**
 * Used to describe a route
 * <p>
 * Created by larrymitchell on 2017-02-10.
 */
@Data
public class RouteDescriptor {

    private int number;

    private String name;

    private Color color;

    private short style;

}
