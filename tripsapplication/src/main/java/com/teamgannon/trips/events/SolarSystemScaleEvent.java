package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when scale mode or zoom level changes in the solar system view.
 */
@Getter
public class SolarSystemScaleEvent extends ApplicationEvent {

    public enum ScaleChangeType {
        SCALE_MODE,
        ZOOM_LEVEL
    }

    private final ScaleChangeType changeType;

    /**
     * For SCALE_MODE: true = logarithmic, false = linear
     * For ZOOM_LEVEL: not used (see zoomLevel field)
     */
    private final boolean logarithmic;

    /**
     * The zoom level (1.0 = default, >1 = zoomed in, <1 = zoomed out)
     */
    private final double zoomLevel;

    /**
     * Constructor for scale mode change.
     */
    public SolarSystemScaleEvent(Object source, boolean logarithmic) {
        super(source);
        this.changeType = ScaleChangeType.SCALE_MODE;
        this.logarithmic = logarithmic;
        this.zoomLevel = 1.0;
    }

    /**
     * Constructor for zoom level change.
     */
    public SolarSystemScaleEvent(Object source, double zoomLevel) {
        super(source);
        this.changeType = ScaleChangeType.ZOOM_LEVEL;
        this.logarithmic = false;
        this.zoomLevel = zoomLevel;
    }
}
