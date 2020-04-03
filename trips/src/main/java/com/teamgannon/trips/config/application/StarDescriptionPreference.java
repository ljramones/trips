package com.teamgannon.trips.config.application;

import com.teamgannon.trips.stardata.StellarType;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StarDescriptionPreference {

    private StellarType startClass;

    private Color color;

    private float size;

    public static StarDescriptionPreference createStarDescription(StellarType startClass, Color color, float size) {
        return StarDescriptionPreference
                .builder()
                .startClass(startClass)
                .color(color)
                .size(size)
                .build();
    }


}
