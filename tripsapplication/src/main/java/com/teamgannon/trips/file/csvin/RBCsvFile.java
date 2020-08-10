package com.teamgannon.trips.file.csvin;

import com.teamgannon.trips.dialogs.Dataset;
import lombok.Data;

@Data
public class RBCsvFile {

    private Dataset dataset;

    private long size = 0;
    private long numbAccepts = 0;
    private long numbRejects = 0;

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
