package com.teamgannon.trips.file.excel.rb;

import com.teamgannon.trips.file.excel.rb.model.RBStar;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class RBExcelFile {

    @NotNull List<AstrographicObject> astrographicObjects = new ArrayList<>();
    private String fileName;
    private String author;

    public void addStar(@NotNull RBStar star) {
        astrographicObjects.add(star.toAstrographicObject());
    }

}
