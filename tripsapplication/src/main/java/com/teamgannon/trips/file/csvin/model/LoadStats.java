package com.teamgannon.trips.file.csvin.model;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadStats {

    private Dataset dataSet;

    private int loopCounter;

    private long totalCount;

    private boolean readComplete;

    private RegCSVFile csvFile;

    private double maxDistance;

    public void incLoopCounter() {
        loopCounter++;
    }

    public void clearLoopCounter() {
        loopCounter = 0;
    }

    public void addToTotalCount(int loopCounter) {
        totalCount += loopCounter;
    }
}
