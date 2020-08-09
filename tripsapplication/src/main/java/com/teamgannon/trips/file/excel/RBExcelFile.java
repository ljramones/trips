package com.teamgannon.trips.file.excel;

import com.teamgannon.trips.file.excel.model.RBStar;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class RBExcelFile {

    List<AstrographicObject> astrographicObjects = new ArrayList<>();
    private String fileName;
    private String author;

    public void addStar(RBStar star) {
        astrographicObjects.add(star.toAstrographicObject());
    }

}
