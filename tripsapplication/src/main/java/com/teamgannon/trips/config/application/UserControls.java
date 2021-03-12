package com.teamgannon.trips.config.application;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class UserControls {

    public final static double SLOW_SPEED = 0.05;
    public final static double NORMAL_SPEED = 0.2;
    public final static double FAST_SPEED = 2;
    public final static double FASTEST_SPEED = 5;


    /**
     * true is engineer sense
     * false is pilot sense
     * <p>
     * the meaning of this is when the mouse or keyboard causes screen movement/rotation, which direction is taken
     * (positive or megative)
     */
    private boolean controlSense = true;

    private double slow = 0.05;

    private double normal = 0.2;

    private double fast = 2;

    private double veryFast = 5;

    public void reset() {
        controlSense = true;
    }
}
