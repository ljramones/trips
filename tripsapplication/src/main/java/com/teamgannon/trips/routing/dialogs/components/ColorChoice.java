package com.teamgannon.trips.routing.dialogs.components;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColorChoice {

    private boolean selected;

    private Color swatch;

}
