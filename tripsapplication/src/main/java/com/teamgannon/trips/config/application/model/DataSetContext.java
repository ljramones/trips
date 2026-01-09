package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class DataSetContext {

    /**
     * the valid
     */
    private DataSetDescriptor descriptor;

    private boolean validDescriptor = false;

    public DataSetContext(DataSetDescriptor dataSetDescriptor) {
        setDataDescriptor(dataSetDescriptor);
    }

    public void setDataDescriptor(DataSetDescriptor dataSetDescriptor) {
        descriptor = dataSetDescriptor;
        validDescriptor = true;
    }

    // Manual setter (Lombok @Data should generate this but adding explicitly)
    public void setValidDescriptor(boolean validDescriptor) {
        this.validDescriptor = validDescriptor;
    }

}
