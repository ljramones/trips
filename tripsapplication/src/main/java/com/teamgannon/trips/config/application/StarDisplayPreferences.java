package com.teamgannon.trips.config.application;

import com.teamgannon.trips.jpa.model.StarDetailsPersist;
import com.teamgannon.trips.stardata.StellarType;
import javafx.scene.paint.Color;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class StarDisplayPreferences implements Serializable {

    private static final long serialVersionUID = -1370182140736011820L;

    /**
     * the map of stellar attributes
     */
    private Map<StellarType, StarDescriptionPreference> starMap = new HashMap<>();

    /**
     * the initial defaults if none exist
     */
    public void setDefaults() {
        starMap.put(StellarType.O, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.O, Color.DARKBLUE, 10));
        starMap.put(StellarType.B, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.B, Color.MEDIUMBLUE, 8));
        starMap.put(StellarType.A, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.A, Color.LIGHTBLUE, 6));
        starMap.put(StellarType.F, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.F, Color.LIGHTYELLOW, 1));
        starMap.put(StellarType.G, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.G, Color.YELLOW, 1));
        starMap.put(StellarType.K, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.K, Color.ORANGE, 0.8f));
        starMap.put(StellarType.M, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.M, Color.RED, .5f));
        starMap.put(StellarType.L, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.L, Color.DARKRED, .3f));
        starMap.put(StellarType.T, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.T, Color.PURPLE, .2f));
        starMap.put(StellarType.Y, StarDescriptionPreference.createStarDescription(UUID.randomUUID().toString(), StellarType.Y, Color.MEDIUMVIOLETRED, .2f));
    }


    /**
     * set the stars from what was in the database
     *
     * @param starDetailsPersistList the list of data from the DB
     */
    public void setStars(List<StarDetailsPersist> starDetailsPersistList) {
        starDetailsPersistList.stream().map(StarDescriptionPreference::createStarDescription).forEach(starDescriptionPreference
                -> starMap.put(starDescriptionPreference.getStarClass(), starDescriptionPreference));
    }

    public List<StarDetailsPersist> getStarDetails() {
        List<StarDetailsPersist> starDetailsPersistList = new ArrayList<>();
        for (StellarType type : starMap.keySet()) {
            StarDetailsPersist starDetailsPersist = starMap.get(type).toStarDetailsPersist();
            starDetailsPersistList.add(starDetailsPersist);
        }
        return starDetailsPersistList;
    }

    /**
     * get based on stellar type
     *
     * @param type the type
     * @return the description
     */
    public StarDescriptionPreference get(StellarType type) {
        return starMap.get(type);
    }

}
