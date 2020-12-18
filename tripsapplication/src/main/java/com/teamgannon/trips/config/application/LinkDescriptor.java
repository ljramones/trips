package com.teamgannon.trips.config.application;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;


@Data
@Builder
public class LinkDescriptor implements Serializable {

    private static final long serialVersionUID = -2500991529523405863L;

    @Builder.Default
    private int linkNumber = 1;

    @Builder.Default
    private @NotNull Color color = Color.CYAN;

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
