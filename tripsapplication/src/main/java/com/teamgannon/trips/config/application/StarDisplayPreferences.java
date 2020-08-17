package com.teamgannon.trips.config.application;

import com.teamgannon.trips.stardata.StellarType;
import javafx.scene.paint.Color;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class StarDisplayPreferences  implements Serializable {

    private static final long serialVersionUID = 3420111848302502658L;

    /**
     * the radius to show stars
     */
    private int viewRadius = 25;

    private boolean showStarName = true;

    private String starFont;

    private List<StarDescriptionPreference> starMap = new ArrayList<>();

    public StarDisplayPreferences() {
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.O, Color.DARKBLUE, 10));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.B, Color.MEDIUMBLUE, 8));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.A, Color.LIGHTBLUE, 6));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.F, Color.LIGHTYELLOW, 1));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.G, Color.YELLOW, 1));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.K, Color.ORANGE, 0.8f));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.M, Color.RED, .5f));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.L, Color.DARKRED, .3f));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.T, Color.PURPLE, .2f));
        starMap.add(StarDescriptionPreference.createStarDescription(StellarType.Y, Color.MEDIUMVIOLETRED, .2f));
    }

}
