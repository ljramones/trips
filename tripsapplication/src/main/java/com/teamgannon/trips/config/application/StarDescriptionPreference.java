package com.teamgannon.trips.config.application;

import com.teamgannon.trips.stardata.StellarType;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class StarDescriptionPreference implements Serializable {

    private static final long serialVersionUID = 477224322891821357L;

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
