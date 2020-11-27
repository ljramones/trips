package com.teamgannon.trips.file.csvin;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Data;

@Data
public class RegCSVFile {

    private Dataset dataset;

    private DataSetDescriptor dataSetDescriptor;

    private long size = 0;
    private long numbAccepts = 0;
    private long numbRejects = 0;

    private double maxDistance = 0;

    public void incRejects() {
        numbRejects += 1;
    }

    public void incAccepts() {
        numbAccepts += 1;
    }

    public void incTotal() {
        size += 1;
    }

}
