package com.teamgannon.trips.file.excel.normal;

import com.teamgannon.trips.file.csvin.model.AstroCSVStar;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExcelFile {

    private String fileName;
    private String author;

    private DataSetDescriptor descriptor;

}
