package com.teamgannon.trips.config.application;

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

    public void setDataDescriptor(DataSetDescriptor dataDescriptor) {
        descriptor = dataDescriptor;
        validDescriptor = true;
    }

}
