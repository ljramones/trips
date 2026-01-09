package com.teamgannon.trips.search;

import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.stellarmodelling.StellarType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private DataSetContext dataSetContext;

    /**
     * this is intended to be used to determine how far out the sphere that we search should be
     */

    private double lowerDistanceLimit = 0.0;

    private double upperDistanceLimit = 20.0;

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

    private boolean fictionalStars = false;

    private @NotNull Set<String> polities = new HashSet<>();

    private @NotNull Set<StellarType> stellarTypes = new HashSet<>();

    private @NotNull Set<String> fuelTypes = new HashSet<>();

    private @NotNull Set<String> worldTypes = new HashSet<>();

    private @NotNull Set<String> portTypes = new HashSet<>();

    private @NotNull Set<String> populationTypes = new HashSet<>();

    private @NotNull Set<String> techTypes = new HashSet<>();

    private @NotNull Set<String> productTypes = new HashSet<>();

    private @NotNull Set<String> milSpaceTypes = new HashSet<>();

    private @NotNull Set<String> milPlanTypes = new HashSet<>();

    private boolean recenter = false;
    private double xMinus;
    private double xPlus;
    private double yPlus;
    private double yMinus;
    private double zMinus;
    private double zPlus;
    private String centrePoint;
    private double[] centerCoordinates = new double[3];
    private String centerStar = "Sol";

    public AstroSearchQuery() {
        centerCoordinates[0] = 0;
        centerCoordinates[1] = 0;
        centerCoordinates[2] = 0;
        dataSetContext = new DataSetContext(new DataSetDescriptor());
        dataSetContext.setValidDescriptor(false);
    }

    public void clearPolities() {
        polities = new HashSet<>();
    }

    public void clearStellarTypes() {
        stellarTypes = new HashSet<>();
    }

    public void clearFuelTypes() {
        fuelTypes = new HashSet<>();
    }

    public void clearWorldTypes() {
        worldTypes = new HashSet<>();
    }

    public void clearPortTypes() {
        portTypes = new HashSet<>();
    }

    public void clearPopulationTypes() {
        populationTypes = new HashSet<>();
    }

    public void clearTechTypes() {
        techTypes = new HashSet<>();
    }

    public void clearProductTypes() {
        productTypes = new HashSet<>();
    }

    public void clearMilSpaceTypes() {
        milSpaceTypes = new HashSet<>();
    }

    public void clearMilPlanTypes() {
        milPlanTypes = new HashSet<>();
    }

    public void setCenterRanging(@NotNull StarDisplayRecord star, double distance) {
        this.centerCoordinates = star.getActualCoordinates();
        this.centerStar = star.getStarName();
        this.recenter = true;
        this.lowerDistanceLimit = 0.0;
        this.upperDistanceLimit = distance;
        centrePoint = star.getRecordId();
        xMinus = centerCoordinates[0] - distance;
        xPlus = centerCoordinates[0] + distance;

        yPlus = centerCoordinates[1] + distance;
        yMinus = centerCoordinates[1] - distance;

        zPlus = centerCoordinates[2] + distance;
        zMinus = centerCoordinates[2] - distance;
    }

    public @NotNull String getCenterRangingCube() {
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

    public void addProducts(@NotNull List<String> selections) {
        for (String product : selections) {
            this.addProduct(product);
        }
    }

    public void addPorts(@NotNull List<String> ports) {
        for (String port : ports) {
            this.addPortType(port);
        }
    }

    public void addMilPlans(@NotNull List<String> selections) {
        for (String milPlan : selections) {
            this.addMilPlan(milPlan);
        }
    }

    public void addMilSpaces(@NotNull List<String> selections) {
        for (String milSpace : selections) {
            this.addMilSpace(milSpace);
        }
    }

    public void addWorldTypes(@NotNull List<String> selections) {
        for (String world : selections) {
            this.addWorldType(world);
        }
    }

    public void addPopulationTypes(@NotNull List<String> selections) {
        for (String pop : selections) {
            this.addPopulationType(pop);
        }
    }

    public void addFuelTypes(@NotNull List<String> selections) {
        for (String fuel : selections) {
            this.addFuelType(fuel);
        }
    }

    public void addTechs(@NotNull List<String> selections) {
        for (String tech : selections) {
            this.addTech(tech);
        }
    }

    public void addStellarTypes(@NotNull List<String> selection) {

        for (String stellarType : selection) {
            this.addStellarType(stellarType);
        }
    }

    public void addPolities(@NotNull List<String> politySelections) {
        for (String polity : politySelections) {
            this.addPolity(polity);
        }
    }

    public void setDescriptor(DataSetDescriptor descriptor) {
        dataSetContext.setDataDescriptor(descriptor);
    }

    public void zeroCenter() {
        centerCoordinates[0] = 0;
        centerCoordinates[1] = 0;
        centerCoordinates[2] = 0;
    }
}
