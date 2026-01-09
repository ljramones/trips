package com.teamgannon.trips.routing.model;

import lombok.Data;

import java.util.UUID;

@Data
public class SparseStarRecord {

    private String recordId;

    private String starName;

    private double[] actualCoordinates = new double[3];

    // Manual getters/setters (Lombok @Data should generate these but adding explicitly)
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public void setActualCoordinates(double[] actualCoordinates) {
        this.actualCoordinates = actualCoordinates;
    }

    public double[] getActualCoordinates() {
        return actualCoordinates;
    }

}
