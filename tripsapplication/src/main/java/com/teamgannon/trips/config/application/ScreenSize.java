package com.teamgannon.trips.config.application;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenSize {

    private double sceneWidth;
    private double sceneHeight;
    private double depth;
    private double spacing;

}
