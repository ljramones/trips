package com.teamgannon.trips.search;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.stardata.StellarType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The search query
 * <p>
 * Created by larrymitchell on 2017-04-18.
 */
@Slf4j
@Data
public class AstroSearchQuery {

    /**
     * this is used for context since multiple datasets can be used
     */
    private String dataSetName;

    /**
     * this is intended to be used to determine how far out the sphere that we search should be
     */
    private double distanceFromCenterStar = 20.0;

    private boolean fuelSearch = false;
    private boolean worldSearch = false;
    private boolean portSearch = false;
    private boolean popSearch = false;
    private boolean politySearch = false;
    private boolean techSearch = false;
    private boolean productSearch = false;
    private boolean milSpaceSearch = false;
    private boolean milPlanetSearch = false;
    private boolean anomalySearch = false;
    private boolean otherSearch = false;

    private boolean realStars = true;

    private boolean fictionalStars = true;

    private Set<String> polities = new HashSet<>();

    private Set<StellarType> stellarTypes = new HashSet<>();

    private Set<String> fuelTypes = new HashSet<>();

    private Set<String> worldTypes = new HashSet<>();

    private Set<String> portTypes = new HashSet<>();

    private Set<String> populationTypes = new HashSet<>();

    private Set<String> techTypes = new HashSet<>();

    private Set<String> productTypes = new HashSet<>();

    private Set<String> milSpaceTypes = new HashSet<>();

    private Set<String> milPlanTypes = new HashSet<>();

    private boolean recenter = false;

    private double xMinus;
    private double xPlus;
    private double yPlus;
    private double yMinus;
    private double zMinus;
    private double zPlus;

    private UUID centrePoint;
    private double[] centerCoordinates = new double[3];
    private String centerStar = "Sol";

    public AstroSearchQuery() {
        centerCoordinates[0] = 0;
        centerCoordinates[1] = 0;
        centerCoordinates[2] = 0;
    }

    public void setCenterRanging(StarDisplayRecord star, double distance) {
        this.centerCoordinates = star.getActualCoordinates();
        this.centerStar = star.getStarName();
        this.recenter = true;
        this.distanceFromCenterStar = distance;
        centrePoint = star.getRecordId();
        xMinus = centerCoordinates[0] - distance;
        xPlus = centerCoordinates[0] + distance;

        yPlus = centerCoordinates[1] + distance;
        yMinus = centerCoordinates[1] - distance;

        zPlus = centerCoordinates[2] + distance;
        zMinus = centerCoordinates[2] - distance;
    }

    public String getCenterRangingCube() {
        return "Range Cube is :" + String.format("(x[%5.2f, %5.2f]),", xMinus, xPlus) +
                String.format("(y[%5.2f, %5.2f]),", yMinus, yPlus) +
                String.format("(z[%5.2f, %5.2f])", zMinus, zPlus);
    }

    public void addPolity(String polity) {
        polities.add(polity);
    }

    public void addStellarType(String stellarTypeName) {
        try {
            StellarType stellarType = StellarType.valueOf(stellarTypeName);
            stellarTypes.add(stellarType);
        } catch (Exception e) {
            log.error("there is no enum for " + stellarTypeName);
        }
    }

    public void addFuelType(String fuel) {
        fuelTypes.add(fuel);
    }

    public void addWorldType(String world) {
        worldTypes.add(world);
    }

    public void addPortType(String port) {
        portTypes.add(port);
    }

    public void addPopulationType(String pop) {
        populationTypes.add(pop);
    }

    public void addTech(String tech) {
        techTypes.add(tech);
    }

    public void addProduct(String product) {
        productTypes.add(product);
    }

    public void addMilSpace(String milSpace) {
        milSpaceTypes.add(milSpace);
    }

    public void addMilPlan(String milPlan) {
        milPlanTypes.add(milPlan);
    }

    public void addProducts(List<String> selections) {
        for (String product : selections) {
            this.addProduct(product);
        }
    }

    public void addPorts(List<String> ports) {
        for (String port : ports) {
            this.addPortType(port);
        }
    }

    public void addMilPlans(List<String> selections) {
        for (String milPlan : selections) {
            this.addMilPlan(milPlan);
        }
    }

    public void addMilSpaces(List<String> selections) {
        for (String milSpace : selections) {
            this.addMilSpace(milSpace);
        }
    }

    public void addWorldTypes(List<String> selections) {
        for (String world : selections) {
            this.addWorldType(world);
        }
    }

    public void addPopulationTypes(List<String> selections) {
        for (String pop : selections) {
            this.addPopulationType(pop);
        }
    }

    public void addFuelTypes(List<String> selections) {
        for (String fuel : selections) {
            this.addFuelType(fuel);
        }
    }

    public void addTechs(List<String> selections) {
        for (String tech : selections) {
            this.addTech(tech);
        }
    }

    public void addStellarTypes(List<String> selection) {
        for (String stellarType : selection) {
            this.addStellarType(stellarType);
        }
    }

    public void addPolities(List<String> politySelections) {
        for (String polity : politySelections) {
            this.addPolity(polity);
        }
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public void zeroCenter() {
        centerCoordinates[0] = 0;
        centerCoordinates[1] = 0;
        centerCoordinates[2] = 0;
    }
}
