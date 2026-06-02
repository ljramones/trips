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

    private boolean realStars = true;

    private boolean fictionalStars = false;

    private @NotNull Set<StellarType> stellarTypes = new HashSet<>();

    // Spectral component filtering (Chuck's special query)
    private boolean spectralComponentFilterEnabled = false;
    private @NotNull Set<String> spectralClassLetters = new HashSet<>();  // O, B, A, F, G, K, M, etc.
    private @NotNull Set<String> spectralSubtypes = new HashSet<>();       // 0-9
    private @NotNull Set<String> luminosityClasses = new HashSet<>();      // I, II, III, IV, V, VI, VII

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

    public void clearStellarTypes() {
        stellarTypes = new HashSet<>();
    }

    public void clearSpectralComponentFilter() {
        spectralComponentFilterEnabled = false;
        spectralClassLetters = new HashSet<>();
        spectralSubtypes = new HashSet<>();
        luminosityClasses = new HashSet<>();
    }

    public void setSpectralComponentFilter(Set<String> classLetters, Set<String> subtypes, Set<String> lumClasses) {
        this.spectralComponentFilterEnabled = true;
        this.spectralClassLetters = classLetters != null ? classLetters : new HashSet<>();
        this.spectralSubtypes = subtypes != null ? subtypes : new HashSet<>();
        this.luminosityClasses = lumClasses != null ? lumClasses : new HashSet<>();
    }

    public boolean hasSpectralComponentFilter() {
        return spectralComponentFilterEnabled &&
                (!spectralClassLetters.isEmpty() || !spectralSubtypes.isEmpty() || !luminosityClasses.isEmpty());
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
        return "Range Cube is :" + "(x[%5.2f, %5.2f]),".formatted(xMinus, xPlus) +
                "(y[%5.2f, %5.2f]),".formatted(yMinus, yPlus) +
                "(z[%5.2f, %5.2f])".formatted(zMinus, zPlus);
    }

    public void addStellarType(String stellarTypeName) {
        try {
            StellarType stellarType = StellarType.valueOf(stellarTypeName);
            stellarTypes.add(stellarType);
        } catch (Exception e) {
            log.error("there is no enum for {}", stellarTypeName);
        }
    }

    public void addStellarTypes(@NotNull List<String> selection) {

        for (String stellarType : selection) {
            this.addStellarType(stellarType);
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
