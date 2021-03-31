package com.teamgannon.trips.transits;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.UUID;

@Data
public class TransitRangeDef {

    private UUID bandId;

    private boolean enabled;

    private String bandName;

    private double upperRange;

    private double lowerRange;

    private double lineWidth;

    private String color;

    @JsonIgnore
    public Color getBandColor() {
        return Color.valueOf(color);
    }

    @JsonIgnore
    public void setBandColor(Color bandColor) {
        color = bandColor.toString();
    }

}
