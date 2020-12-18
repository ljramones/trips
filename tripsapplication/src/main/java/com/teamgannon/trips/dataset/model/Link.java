package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.dataset.enums.GridLines;
import javafx.scene.paint.Color;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * The link between two stars
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Data
public class Link {

    /**
     * Boolean Display this link type
     */
    private boolean displayLink = true;

    /**
     * Minimum distance for this type in LY
     */
    private double linkMinDistance = 1.0;

    /**
     * Maximum distance for this type in LY
     */
    private double linkMaxDistance = 4.0;

    /**
     * Boolean Display Link ID for this type
     */
    private boolean dispLindId = false;

    /**
     * Line style, solid or dotted
     */
    private @NotNull GridLines linkStyle = GridLines.Solid;

    /**
     * Hexadecimal Color value of link lines
     */
    private double @NotNull [] linkColor = new double[]{0xAA, 0xBB, 0xCC};
    /**
     * Font to display this link ID in
     */
    private @NotNull FontDescriptor linkFont = new FontDescriptor("Arial", 10);

    public @NotNull Color getLinkColor() {
        return Color.color(linkColor[0], linkColor[1], linkColor[2]);
    }

    public void setLinkColor(@NotNull Color color) {
        linkColor[0] = color.getRed();
        linkColor[1] = color.getGreen();
        linkColor[2] = color.getBlue();
    }

}
