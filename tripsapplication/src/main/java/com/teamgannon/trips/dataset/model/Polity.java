package com.teamgannon.trips.dataset.model;

import javafx.scene.paint.Color;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

/**
 * A description of a political entity
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Data
public class Polity {

    @Id
    private UUID id;

    /**
     * Name of this polity
     */
    private String polityName;

    /**
     * the polity color
     */
    private double[] polityColor = new double[]{0xAA, 0xBB, 0xCC};
    /**
     * font to use for polity
     */
    private FontDescriptor polityFont = new FontDescriptor("Arial", 10);
    /**
     * Potential jump distance of this P’s ships
     * <p>
     * When searching for routes, this defines the
     * max jump dist this polity can use to create
     * routes.
     */
    private double pJumpDist = 7.5;

    public Color getPolityColor() {
        return Color.color(polityColor[0], polityColor[1], polityColor[2]);
    }

    public void setPolityColor(Color color) {
        polityColor[0] = color.getRed();
        polityColor[1] = color.getGreen();
        polityColor[2] = color.getBlue();
    }

}
