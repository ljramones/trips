package com.teamgannon.trips.dialogs.support;

import com.teamgannon.trips.config.application.ColorPalette;
import com.teamgannon.trips.jpa.model.GraphColor;
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
