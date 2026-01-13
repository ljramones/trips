package com.teamgannon.trips.workbench.model;

import java.util.List;
import java.util.regex.Pattern;

public final class WorkbenchCsvSchema {

    public static final List<String> CSV_HEADER_COLUMNS = List.of(
            "id",
            "dataSetName",
            "displayName",
            "commonName",
            "System Name",
            "Epoch",
            "constellationName",
            "mass",
            "notes",
            "source",
            "catalogIdList",
            "simbadId",
            "Gaia DR2",
            "radius",
            "ra",
            "declination",
            "pmra",
            "pmdec",
            "distance",
            "radialVelocity",
            "spectralClass",
            "temperature",
            "realStar",
            "bprp",
            "bpg",
            "grp",
            "luminosity",
            "magu",
            "magb",
            "magv",
            "magr",
            "magi",
            "other",
            "anomaly",
            "polity",
            "worldType",
            "fuelType",
            "portType",
            "populationType",
            "techType",
            "productType",
            "milSpaceType",
            "milPlanType",
            "age",
            "metallicity",
            "miscText1",
            "miscText2",
            "miscText3",
            "miscText4",
            "miscText5",
            "miscNum1",
            "miscNum2",
            "miscNum3",
            "miscNum4",
            "miscNum5",
            "numExoplanets",
            "absmag",
            "Gaia DR3",
            "x",
            "y",
            "z"
    );

    public static final List<String> REQUIRED_FIELDS = List.of(
            "displayName",
            "spectralClass",
            "ra",
            "declination",
            "distance",
            "realStar"
    );

    public static final String HYG_SOURCE_NAME = "HYG-MERGED-2m";
    public static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    private WorkbenchCsvSchema() {
    }
}
