package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.jpa.model.StarDetailsPersist;
import com.teamgannon.trips.stellarmodelling.StellarType;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class StarDescriptionPreference implements Serializable {

    @Serial
    private static final long serialVersionUID = 477224322891821357L;


    private String id;

    private StellarType starClass;

    private Color color;

    private float size;

    public static StarDescriptionPreference createStarDescription(@NotNull StarDetailsPersist starDetailsPersist) {
        StellarType stellarType = StellarType.valueOf(starDetailsPersist.getStellarClass());
        return StarDescriptionPreference
                .builder()
                .id(starDetailsPersist.getId())
                .starClass(stellarType)
                .color(Color.valueOf(starDetailsPersist.getStarColor()))
                .size(starDetailsPersist.getRadius())
                .build();
    }

    public static StarDescriptionPreference createStarDescription(String id, String starClass, @NotNull String color, float size) {
        StellarType stellarType = StellarType.valueOf(starClass);
        return StarDescriptionPreference
                .builder()
                .id(id)
                .starClass(stellarType)
                .color(Color.valueOf(color))
                .size(size)
                .build();
    }

    public static StarDescriptionPreference createStarDescription(String id, String starClass, Color color, float size) {
        StellarType stellarType = StellarType.valueOf(starClass);
        return StarDescriptionPreference
                .builder()
                .id(id)
                .starClass(stellarType)
                .color(color)
                .size(size)
                .build();
    }

    public static StarDescriptionPreference createStarDescription(String id, StellarType startClass, Color color, float size) {
        return StarDescriptionPreference
                .builder()
                .id(id)
                .starClass(startClass)
                .color(color)
                .size(size)
                .build();
    }

    public @NotNull StarDetailsPersist toStarDetailsPersist() {
        StarDetailsPersist starDetailsPersist = new StarDetailsPersist();
        starDetailsPersist.setId(this.getId());
        starDetailsPersist.setRadius(this.getSize());
        starDetailsPersist.setStarColor(this.getColor().toString());
        starDetailsPersist.setStellarClass(this.getStarClass().toString());
        return starDetailsPersist;
    }


}
