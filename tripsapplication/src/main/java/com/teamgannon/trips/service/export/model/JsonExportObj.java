package com.teamgannon.trips.service.export.model;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JsonExportObj {

    /**
     * the astrographic objects themselves
     */
    List<StarObject> starObjectList = new ArrayList<>();
    /**
     * the data descriptor for these object
     */
    private DataSetDescriptorDTO descriptor;

}
