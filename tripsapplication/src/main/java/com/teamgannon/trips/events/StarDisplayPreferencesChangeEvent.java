package com.teamgannon.trips.events;

import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StarDisplayPreferencesChangeEvent extends ApplicationEvent {
    private final StarDisplayPreferences starDisplayPreferences;

    public StarDisplayPreferencesChangeEvent(Object source, StarDisplayPreferences starDisplayPreferences) {
        super(source);
        this.starDisplayPreferences = starDisplayPreferences;
    }
}
