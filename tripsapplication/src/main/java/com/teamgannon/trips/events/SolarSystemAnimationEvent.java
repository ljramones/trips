package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when animation controls are used in the solar system view.
 */
@Getter
public class SolarSystemAnimationEvent extends ApplicationEvent {

    public enum AnimationAction {
        PLAY,
        PAUSE,
        TOGGLE_PLAY_PAUSE,
        RESET,
        SET_SPEED
    }

    private final AnimationAction action;

    /**
     * Speed multiplier for SET_SPEED action.
     * Values: 1.0 = real-time, 86400 = 1 day/sec, etc.
     */
    private final double speedMultiplier;

    /**
     * Constructor for play/pause/reset actions.
     */
    public SolarSystemAnimationEvent(Object source, AnimationAction action) {
        super(source);
        this.action = action;
        this.speedMultiplier = 1.0;
    }

    /**
     * Constructor for speed change action.
     */
    public SolarSystemAnimationEvent(Object source, double speedMultiplier) {
        super(source);
        this.action = AnimationAction.SET_SPEED;
        this.speedMultiplier = speedMultiplier;
    }
}
