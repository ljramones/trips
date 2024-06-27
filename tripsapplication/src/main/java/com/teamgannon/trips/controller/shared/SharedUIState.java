package com.teamgannon.trips.controller.shared;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class SharedUIState {
    private boolean labelsOn;
    private boolean polities;
    private boolean routesOn;
    private boolean transitsOn;
    private boolean starsOn;
    private boolean gridOn;
    private boolean extensionsOn;
    private boolean scaleOn;
    private boolean sidePaneOn;

}

