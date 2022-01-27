package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.jpa.model.StarDetailsPersist;
import com.teamgannon.trips.stellarmodelling.StellarType;
import javafx.scene.paint.Color;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Data
public class StarDisplayPreferences implements Serializable {

    @Serial
    private static final long serialVersionUID = 2286938075665280787L;

    /**
     * the map of stellar attributes
     */
    private @NotNull Map<StellarType, StarDescriptionPreference> starMap = new HashMap<>();

    /**
     * the number of star labels to show onscreen
     */
    private int numberOfVisibleLabels = 30;

    /**
     * the initial defaults if none exist
     */
    public void setDefaults() {
        starMap.put(StellarType.O, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.O,
                Color.LIGHTBLUE,
                4));
        starMap.put(StellarType.B, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.B,
                Color.LIGHTBLUE,
                3.5f));
        starMap.put(StellarType.A, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.A,
                Color.LIGHTBLUE,
                3f));
        starMap.put(StellarType.F, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.F,
                Color.YELLOW,
                2.5f));
        starMap.put(StellarType.G, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.G,
                Color.YELLOW,
                2.5f));
        starMap.put(StellarType.K, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.K,
                Color.ORANGE,
                2f));
        starMap.put(StellarType.M, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.M,
                Color.RED,
                1.5f));
        starMap.put(StellarType.L, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.L,
                Color.MEDIUMVIOLETRED,
                1.5f));
        starMap.put(StellarType.T, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.T,
                Color.MEDIUMVIOLETRED,
                1f));
        starMap.put(StellarType.Y, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.Y,
                Color.MEDIUMVIOLETRED,
                1f));
        starMap.put(StellarType.D, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.D,
                Color.MEDIUMVIOLETRED,
                1f));
        starMap.put(StellarType.Q, StarDescriptionPreference.createStarDescription(
                UUID.randomUUID().toString(),
                StellarType.Q,
                Color.MEDIUMVIOLETRED,
                1f));
    }

    public void reset() {
        StarDescriptionPreference oStar = starMap.get(StellarType.O);
        oStar.setColor(Color.LIGHTBLUE);
        oStar.setSize(4f);

        StarDescriptionPreference bStar = starMap.get(StellarType.B);
        bStar.setColor(Color.LIGHTBLUE);
        bStar.setSize(3.5f);

        StarDescriptionPreference aStar = starMap.get(StellarType.A);
        aStar.setColor(Color.LIGHTBLUE);
        aStar.setSize(3f);

        StarDescriptionPreference fStar = starMap.get(StellarType.F);
        fStar.setColor(Color.YELLOW);
        fStar.setSize(2.5f);

        StarDescriptionPreference gStar = starMap.get(StellarType.G);
        gStar.setColor(Color.YELLOW);
        gStar.setSize(2.5f);

        StarDescriptionPreference kStar = starMap.get(StellarType.K);
        kStar.setColor(Color.ORANGE);
        kStar.setSize(2f);

        StarDescriptionPreference mStar = starMap.get(StellarType.M);
        mStar.setColor(Color.RED);
        mStar.setSize(1.5f);

        StarDescriptionPreference lStar = starMap.get(StellarType.L);
        lStar.setColor(Color.MEDIUMVIOLETRED);
        lStar.setSize(1.5f);

        StarDescriptionPreference tStar = starMap.get(StellarType.T);
        tStar.setColor(Color.MEDIUMVIOLETRED);
        tStar.setSize(1f);

        StarDescriptionPreference yStar = starMap.get(StellarType.Y);
        yStar.setColor(Color.MEDIUMVIOLETRED);
        yStar.setSize(1f);

        StarDescriptionPreference dStar = starMap.get(StellarType.D);
        dStar.setColor(Color.MEDIUMVIOLETRED);
        dStar.setSize(1f);

        StarDescriptionPreference qStar = starMap.get(StellarType.Q);
        qStar.setColor(Color.MEDIUMVIOLETRED);
        qStar.setSize(1f);
    }

    /**
     * set the stars from what was in the database
     *
     * @param starDetailsPersistList the list of data from the DB
     */
    public void setStars(@NotNull List<StarDetailsPersist> starDetailsPersistList) {
        starDetailsPersistList.stream().map(StarDescriptionPreference::createStarDescription).forEach(starDescriptionPreference
                -> starMap.put(starDescriptionPreference.getStarClass(), starDescriptionPreference));
    }

    public @NotNull List<StarDetailsPersist> getStarDetails() {
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
