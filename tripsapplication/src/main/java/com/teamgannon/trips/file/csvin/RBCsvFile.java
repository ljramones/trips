package com.teamgannon.trips.file.csvin;

import lombok.Builder;
import lombok.Data;

@Data
public class RBCsvFile {

    private String datasetName;
    private String fileName;
    private String author;

    private long size=0;

    public void addSize(int loopCounter) {
        size += loopCounter;
    }

}
