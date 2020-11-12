package com.teamgannon.trips.experiments;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ObjectDescriptor {

    private String name;

    private double x;

    private double y;

    private double z;

    private Color color;

    public String toString() {
        return "Name:" + name + ":: x=" + x + ", y=" + y + ", z=" + z;
    }

}
