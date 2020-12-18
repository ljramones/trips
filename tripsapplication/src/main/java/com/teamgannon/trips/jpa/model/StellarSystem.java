package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.model.AsteroidBelt;
import com.teamgannon.trips.model.KuiperBelt;
import com.teamgannon.trips.model.OortCloud;
import com.teamgannon.trips.model.SystemObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Description of the stellar system
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
@Entity
public class StellarSystem implements Serializable {

    private static final long serialVersionUID = -7489324093062267595L;

    @Id
    private String id;

    /**
     * system name
     */
    private String name;

    /**
     * a short description of this system
     */
    private String shortDescription;

    /**
     * a longer, more detailed description of the system
     */
    private String longDescription;

    /**
     * true means that there is more than one star in the system
     */
    private boolean multipleStar;

    /**
     * the type of system
     * StellarSystemType
     */
    private String stellarType;

    /**
     * a set of the stars present in this system
     */
    @Lob
    private String starIds;

    /**
     * a set of planets in the system
     */
    @Lob
    private String planetIds;

    /**
     * a set of object that are not planets, asteroids, can be man made,etc
     */
    @Lob
    private String systemObjects;

    /**
     * a description of the asteroid belt present, null means that there isn't one
     */
    private String asteroidBelt;

    /**
     * a description of a kuiper belt present, null means that there is none
     */
    private String kuiperBelt;

    /**
     * a description of the oort cloud
     */
    private String oortCloud;

    /**
     * add a system object
     *
     * @param systemObject the system object
     */
    public void addSystemObject(@NotNull SystemObject systemObject) {
        String sysObjStr = systemObject.convertToJson();
        List<String> systemObjectsList = getSystemObjects();
        systemObjectsList.add(sysObjStr);
        setSystemObjects(systemObjectsList);
    }

    //
    public OortCloud getOortCloud() {
        return new OortCloud().toOortCloud(oortCloud);
    }

    public void setOortCloud(@NotNull OortCloud oortCloud1) {
        oortCloud = oortCloud1.convertToJson();
    }

    public AsteroidBelt getAsteroidBelt() {
        return new AsteroidBelt().toAsteroidBelt(asteroidBelt);
    }

    ////

    public void setAsteroidBelt(@NotNull AsteroidBelt belt) {
        asteroidBelt = belt.convertToJson();
    }

    public KuiperBelt getKuiperBelt() {
        return new KuiperBelt().toKuiperBelt(kuiperBelt);
    }

    public void setKuiperBelt(@NotNull KuiperBelt belt) {
        kuiperBelt = belt.convertToString();
    }

    public @NotNull List<String> getSystemObjects() {
        if (systemObjects == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(systemObjects.split("\\s*,\\s*"));
    }

    /**
     * convert and add the system objects
     *
     * @param systemObjectSet the set of sysm object to add
     */
    public void setSystemObjects(@NotNull Set<SystemObject> systemObjectSet) {
        systemObjectSet.forEach(this::addSystemObject);
    }

    public void setSystemObjects(@NotNull List<String> stringList) {
        systemObjects = String.join(",", stringList);
    }

    /**
     * add a star
     *
     * @param starId the id to add
     */
    public void addStar(String starId) {
        List<String> starList = getStarIds();
        starList.add(starId);
        setStarIds(starList);
    }

    /**
     * remove a star
     *
     * @param starId the star to remove
     */
    public void removeStar(String starId) {
        List<String> starList = getStarIds();
        starList.remove(starId);
        setStarIds(starList);
    }

    public @NotNull List<String> getStarIds() {
        if (starIds == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(starIds.split("\\s*,\\s*"));
    }

    public void setStarIds(@NotNull List<String> stringList) {
        starIds = String.join(",", stringList);
    }

    public @NotNull List<String> getPlanetIds() {
        if (planetIds == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(planetIds.split("\\s*,\\s*"));
    }

    public void setPlanetIds(@NotNull List<String> stringList) {
        planetIds = String.join(",", stringList);
    }
}
