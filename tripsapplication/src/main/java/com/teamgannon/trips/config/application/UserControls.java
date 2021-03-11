package com.teamgannon.trips.config.application;

import javafx.scene.input.KeyCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class UserControls {

    /**
     * true is engineer sense
     * false is pilot sense
     * <p>
     * the meaning of this is when the mouse or keyboard causes screen movement/rotation, which direction is taken
     * (positive or megative)
     */
    private boolean controlSense = true;

    /**
     * move left
     */
    private KeyCode left;

    /**
     * move right
     */
    private KeyCode right;

    /**
     * what is the drag key
     */
    private KeyCode drag;

    public void reset() {
        controlSense = true;
    }
}
