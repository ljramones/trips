package com.teamgannon.trips.events;

import com.teamgannon.trips.controller.UIElement;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UIStateChangeEvent extends ApplicationEvent {
    private final UIElement element;
    private final boolean state;

    public UIStateChangeEvent(Object source, UIElement element, boolean state) {
        super(source);
        this.element = element;
        this.state = state;
    }

}
