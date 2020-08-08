package com.teamgannon.trips.dialogs;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RBCsvFile {

    private List<AstrographicObject> astrographicObjects = new ArrayList<>();
    private String fileName;
    private String author;

}
