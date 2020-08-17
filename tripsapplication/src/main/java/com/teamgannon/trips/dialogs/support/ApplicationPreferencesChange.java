package com.teamgannon.trips.dialogs.support;

import com.teamgannon.trips.config.application.ApplicationPreferences;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApplicationPreferencesChange {

    private ChangeTypeEnum changeType;

    private ApplicationPreferences applicationPreferences;
}
