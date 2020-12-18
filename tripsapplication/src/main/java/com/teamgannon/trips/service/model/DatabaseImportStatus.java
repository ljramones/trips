package com.teamgannon.trips.service.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The database import status
 * <p>
 * Created by larrymitchell on 2017-01-23.
 */
@Data
public class DatabaseImportStatus {

    /**
     * processing time
     */
    private long processingTime = 0;

    /**
     * the total number of records processed
     */
    private long totalRecords;

    /**
     * the records successfully read
     */
    private long recordsSuccess;

    /**
     * records failed to be read
     */
    private long recordsFailed;

    /**
     * the list of failed ids
     */
    private @NotNull List<Long> idsThatFailed = new ArrayList<>();

    public void incTotal() {
        totalRecords++;
    }

    public void incRecordsSuccess() {
        recordsSuccess++;
    }

    public void incRecordsFailed() {
        recordsFailed++;
    }

    public void addId(Long id) {
        idsThatFailed.add(id);
    }

}
