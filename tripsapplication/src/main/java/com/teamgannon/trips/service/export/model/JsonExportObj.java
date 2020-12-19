package com.teamgannon.trips.service.export.model;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JsonExportObj {

    /**
     * the data descriptor for these object
     */
    private DataSetDescriptorDTO descriptor;

    /**
     * the astrographic objects themselves
     */
    List<AstrographicObject> astrographicObjectList = new ArrayList<>();

}
