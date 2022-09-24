package com.teamgannon.trips.dialogs.dataset.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Data;

@Data
public class FileProcessResult {

    private boolean success;

    private String message;

    private DataSetDescriptor dataSetDescriptor;

}
