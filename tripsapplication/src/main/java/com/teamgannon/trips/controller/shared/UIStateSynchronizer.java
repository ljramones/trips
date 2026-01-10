package com.teamgannon.trips.controller.shared;

import com.teamgannon.trips.controller.UIElement;
import com.teamgannon.trips.events.UIStateChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UIStateSynchronizer {

    private final SharedUIState sharedUIState;

    public UIStateSynchronizer(SharedUIState sharedUIState) {
        this.sharedUIState = sharedUIState;
    }

    @EventListener
    public void onUIStateChangeEvent(UIStateChangeEvent event) {
        UIElement element = event.getElement();
        boolean state = event.isState();
        switch (element) {
            case GRID -> sharedUIState.setGridOn(state);
            case LABELS -> sharedUIState.setLabelsOn(state);
            case POLITIES -> sharedUIState.setPolities(state);
            case STARS -> sharedUIState.setStarsOn(state);
            case ROUTES -> sharedUIState.setRoutesOn(state);
            case TRANSITS -> sharedUIState.setTransitsOn(state);
            case SIDE_PANE -> sharedUIState.setSidePaneOn(state);
            case SCALE -> sharedUIState.setScaleOn(state);
            case EXTENSIONS -> sharedUIState.setExtensionsOn(state);
            default -> log.trace("No SharedUIState mapping for {}", element);
        }
    }
}
