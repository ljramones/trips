package com.teamgannon.trips.file.csvin;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class RegCSVFile {

    public static final int MAX_BAD_ROW_SAMPLES = 10;

    private boolean readSuccess;

    private String processMessage;

    private Dataset dataset;

    private DataSetDescriptor dataSetDescriptor;

    private long size = 0;
    private long numbAccepts = 0;
    private long numbRejects = 0;

    private double maxDistance = 0;

    @Getter(AccessLevel.NONE)
    private final List<String> badRowSamples = new ArrayList<>(MAX_BAD_ROW_SAMPLES);

    public void incRejects() {
        numbRejects += 1;
    }

    public void incAccepts() {
        numbAccepts += 1;
    }

    public void incTotal() {
        size += 1;
    }

    /**
     * Record a bad-row sample alongside bumping the reject count. The sample
     * list is capped at {@link #MAX_BAD_ROW_SAMPLES}; additional rejects still
     * count, they just stop accumulating snippet strings. (Issue 38)
     */
    public void recordBadRow(String snippet) {
        if (badRowSamples.size() < MAX_BAD_ROW_SAMPLES) {
            badRowSamples.add(snippet);
        }
        numbRejects += 1;
    }

    public List<String> getBadRowSamples() {
        return Collections.unmodifiableList(badRowSamples);
    }

}
