package com.teamgannon.trips.events;

import com.teamgannon.trips.config.application.model.ColorPalette;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ColorPaletteChangeEvent extends ApplicationEvent {
    private final ColorPalette colorPalette;

    public ColorPaletteChangeEvent(Object source, ColorPalette colorPalette) {
        super(source);
        this.colorPalette = colorPalette;
    }
}
