package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CivilizationDisplayPreferencesChangeEvent extends ApplicationEvent {
    private final CivilizationDisplayPreferences civilizationDisplayPreferences;

    public CivilizationDisplayPreferencesChangeEvent(Object source, CivilizationDisplayPreferences civilizationDisplayPreferences) {
        super(source);
        this.civilizationDisplayPreferences = civilizationDisplayPreferences;
    }
}
