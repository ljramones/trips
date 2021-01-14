package com.teamgannon.trips.config.application.model;

import javafx.scene.text.Font;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SerialFont {

    private String name;

    private double size;

    public SerialFont(String fontSerial) {
        String[] parts = fontSerial.split(":");
        name = parts[0];
        size = Double.parseDouble(parts[1]);
    }

    public SerialFont(Font font) {
        name = font.getName();
        size = font.getSize();
    }

    public Font toFont() {
        return Font.font(name, size);
    }

    public String toString() {
        return name + ":" + size;
    }
}
