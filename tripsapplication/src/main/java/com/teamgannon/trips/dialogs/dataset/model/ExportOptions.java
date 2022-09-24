package com.teamgannon.trips.dialogs.dataset.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.model.ExportFileType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportOptions {

    private boolean doExport;

    private ExportFileType exportFormat;

    private String fileName;

    private DataSetDescriptor dataset;

}
