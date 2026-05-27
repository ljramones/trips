package com.teamgannon.trips.dialogs.dataset.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class FileProcessResult {

    private boolean success;

    private String message;

    private DataSetDescriptor dataSetDescriptor;

    /**
     * Rows the importer skipped because of malformed input (per-row parse
     * failures caught by the narrowed catch in
     * {@code RegularStarCatalogCsvReader}). Zero on a clean import.
     * (Issue 38)
     */
    private long rejectCount;

    /**
     * Snippets describing the first N bad rows (row number, exception type,
     * exception message). Never null — empty list on a clean import.
     * (Issue 38)
     */
    private List<String> sampleBadRows = Collections.emptyList();

}
