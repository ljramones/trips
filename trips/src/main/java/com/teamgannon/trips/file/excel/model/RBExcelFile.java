package com.teamgannon.trips.file.excel.model;

import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class RBExcelFile {

    private String fileName;

    private String author;

    List<AstrographicObject> astrographicObjects = new ArrayList<>();

    public void addStar(RBStar star) {
        astrographicObjects.add(star.toAstrographicObject());
    }

}
