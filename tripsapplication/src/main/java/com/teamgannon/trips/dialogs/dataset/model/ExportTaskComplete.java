package com.teamgannon.trips.dialogs.dataset.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.export.ExportResult;
import com.teamgannon.trips.service.export.ExportResults;

public interface ExportTaskComplete {
    /**
     * signal completion of JavaFx Service
     *
     * @param status            whether it passed or failed
     * @param dataset           the dataset definition to load
     * @param fileProcessResult the end result
     * @param errorMessage      whether there is an error message
     */
    void complete(boolean status, DataSetDescriptor dataset, ExportResults fileProcessResult, String errorMessage);
}
