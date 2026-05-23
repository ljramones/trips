package com.teamgannon.trips.spaceshipmodeller.planner;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Published when a transfer plan should be highlighted as a trajectory in the Solar System view.
 * <p>
 * Fired when a plan is created from a system, and when a plan is selected in the Transfer Planner. The
 * Solar System view draws it only if the currently displayed system matches {@link #getSolarSystemId()}.
 */
@Getter
public class ShowTransferTrajectoryEvent extends ApplicationEvent {

    private final String solarSystemId;
    private final double originAu;
    private final double destinationAu;

    public ShowTransferTrajectoryEvent(Object source, String solarSystemId,
                                       double originAu, double destinationAu) {
        super(source);
        this.solarSystemId = solarSystemId;
        this.originAu = originAu;
        this.destinationAu = destinationAu;
    }
}
