package com.teamgannon.trips.dialogs.support;

import com.teamgannon.trips.config.application.model.ColorPalette;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ColorChangeResult {

    private ChangeTypeEnum changeType;

    private ColorPalette colorPalette;

}
