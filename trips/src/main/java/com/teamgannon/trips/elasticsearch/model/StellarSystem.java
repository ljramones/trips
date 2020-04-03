package com.teamgannon.trips.elasticsearch.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Description of the stellar system
 * <p>
 * Created by larrymitchell on 2017-01-24.
 */
@Slf4j
@Data
@Document(
        indexName = "stellarsystem",
        type = "stellarsystem",
        shards = 1, replicas = 0,
        refreshInterval = "-1"
)
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
    private Set<String> starIds = new HashSet<>();

    /**
     * a set of planets in the system
     */
    private Set<String> planetIds = new HashSet<>();

    /**
     * a set of object that are not planets, asteroids, can be man made,etc
     */
    private Set<String> systemObjects = new HashSet<>();

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
     * add a star
     *
     * @param starId the id to add
     */
    public void addStar(String starId) {
        starIds.add(starId);
    }

    /**
     * remoe a star
     *
     * @param starId the star to remove
     */
    public void removeStar(String starId) {
        starIds.remove(starId);
    }

    /**
     * elasticsearch cannot deal with nested complex objects properly so I do this crazy hack
     *
     * @return the set of system objects
     */
    public Set<SystemObject> getSystemObjects() {
        Set<SystemObject> systemObjectSet = new HashSet<>();
        for (String sysObjectStr : systemObjects) {
            SystemObject systemObject = new SystemObject().toSystemObject(sysObjectStr);
            systemObjectSet.add(systemObject);
        }
        return systemObjectSet;
    }

    /**
     * convert and add the system objects
     *
     * @param systemObjectSet the set of sysm object to add
     */
    public void setSystemObjects(Set<SystemObject> systemObjectSet) {
        systemObjectSet.forEach(this::addSystemObject);
    }

    /**
     * add a system object
     *
     * @param systemObject the system object
     */
    public void addSystemObject(SystemObject systemObject) {
        String sysObjStr = systemObject.convertToString();
        systemObjects.add(sysObjStr);
    }

    //
    public OortCloud getOortCloud() {
        return new OortCloud().toOortCloud(oortCloud);
    }

    public void setOortCloud(OortCloud oortCloud1) {
        oortCloud = oortCloud1.convertToString();
    }

    ////

    public AsteroidBelt getAsteroidBelt() {
        return new AsteroidBelt().toAsteroidBelt(asteroidBelt);
    }

    public void setAsteroidBelt(AsteroidBelt belt) {
        asteroidBelt = belt.convertToString();
    }

    //

    public KuiperBelt getKuiperBelt() {
        return new KuiperBelt().toKuiperBelt(kuiperBelt);
    }

    public void setKuiperBelt(KuiperBelt belt) {
        kuiperBelt = belt.convertToString();
    }

}
