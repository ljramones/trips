package com.teamgannon.trips.starplotting;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class ObjectDescriptor {

    private String name;

    private double x;

    private double y;

    private double z;

    private Color color;

    public @NotNull String toString() {
        return "Name:" + name + ":: x=" + x + ", y=" + y + ", z=" + z;
    }

}
