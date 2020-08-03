package com.teamgannon.trips.config.application;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class LinkDescriptor {

    @Builder.Default
    private int linkNumber = 1;

    @Builder.Default
    private Color color = Color.CYAN;

    @Builder.Default
    private int linkLength = 9;

    public static LinkDescriptor createLinkDescriptor(int number, Color color, int length) {
        return LinkDescriptor.builder()
                .linkNumber(number)
                .color(color)
                .linkLength(length)
                .build();
    }

}
