package com.teamgannon.trips.dialogs.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ViewPreferencesChange {

    /**
     * holds color changes
     */
    private ColorChangeResult colorChangeResult;

    /**
     * holds color changes
     */
    private ApplicationPreferencesChange applicationPreferencesChange;

}
