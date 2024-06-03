package com.teamgannon.trips.events;

import com.teamgannon.trips.config.application.model.UserControls;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserControlsChangeEvent extends ApplicationEvent {
    private final UserControls userControls;

    public UserControlsChangeEvent(Object source, UserControls userControls) {
        super(source);
        this.userControls = userControls;
    }
}
