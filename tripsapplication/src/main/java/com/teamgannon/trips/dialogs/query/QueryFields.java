package com.teamgannon.trips.dialogs.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryFields {

    private final Map<String, String> fieldsMap = Stream.of(new String[][]{
            {"ID", "(string) the unique identifier of the star (internal usage only)"},
            {"ACTUAL_MASS", "(double) the actual mass of the star"},
            {"ANOMALY", "(boolean) indicates that this star is an anomaly"},
            {"BPG", "(double) "},
            {"BPRP", "(double) "},
            {"CATALOG_ID_LIST", "(string) "},
            {"CONSTELLATION_NAME", "(string) "},
            {"DEC_DEG", "(double) "},
            {"DECLINATION", "(double) the declination"},
            {"DISPLAY_NAME", "(string) the name that will be shown for the star"},
            {"DISTANCE", "(double) the distance in LY form Sol"},
            {"EXOPLANETS", "(boolean) whether this star has registered exoplanets (future)"},
            {"FUEL_TYPE", "(string) the fuel type"},
            {"GRP", "(double) "},
            {"LUMINOSITY", "(string) the stellar lumminosity"},
            {"MAGB", "(double) "},
            {"MAGI", "(double) "},
            {"MAGR", "(double) "},
            {"MAGU", "(double) "},
            {"MAGV", "(double) "},
            {"MASS", "(double)"},
            {"MIL_PLAN_TYPE", "(string) military planet type"},
            {"MIL_SPACE_TYPE", "(string) military space type"},
            {"MISC_NUM1", "(double) miscellaneous number #1"},
            {"MISC_NUM2", "(double) miscellaneous number #2"},
            {"MISC_NUM3", "(double) miscellaneous number #3"},
            {"MISC_NUM4", "(double) miscellaneous number #4"},
            {"MISC_NUM5", "(double) miscellaneous number #5"},
            {"MISC_TEXT1", "(string) miscellaneous text #1"},
            {"MISC_TEXT2", "(string) miscellaneous text #2"},
            {"MISC_TEXT3", "(string) miscellaneous text #3"},
            {"MISC_TEXT4", "(string) miscellaneous text #4"},
            {"MISC_TEXT5", "(string) miscellaneous text #5"},
            {"NOTES", "(string) notes on the star"},
            {"ORTHO_SPECTRAL_CLASS", "(string) shortened spectral class"},
            {"OTHER", "(boolean) indicates that the star is marked as other"},
            {"PARALLAX", "(double) the parallax of the star"},
            {"PMDEC", "(double) "},
            {"PMRA", "(double) "},
            {"POPULATION_TYPE", "(string) the population type of the star in powers of 10"},
            {"PORT_TYPE", "(string) the port type of the star"},
            {"PRODUCT_TYPE", "(string) the product type of the star"},
            {"RA", "(double) the right ascension of the star"},
            {"RADIAL_VELOCITY", "(double) the radial velocity of the star"},
            {"RADIUS", "(double) the radius of the star"},
            {"REAL_STAR", "(boolean) indicates whether thhis is a real star or fictional"},
            {"RS_CDEG", "(double) "},
            {"SOURCE", "(string) the source of where this star informationwas gathered"},
            {"SPECTRAL_CLASS", "(string) the full spectral class"},
            {"TECH_TYPE", "(string) the tech type of this star"},
            {"TEMPERATURE", "(double) the surface stellar temperature"},
            {"WORLD_TYPE", "(string) the world type"},
            {"X", "(double) the x coordinate of the star"},
            {"Y", "(double) the y coordinate of the star"},
            {"Z", "(double) the z coordinate of the star"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private final List<String> operators = Stream.of(
            "=    Equal to",
            ">    Greater than",
            "<    Less than",
            ">=   Greater than or equal to",
            "<=   Less than or equal to",
            "<>   Not equal to",
            "ALL  (TRUE if all of the subquery values meet the condition)",
            "AND  (TRUE if all the conditions separated by AND is TRUE)",
            "ANY  (TRUE if any of the subquery values meet the condition)",
            "BETWEEN (TRUE if the operand is within the range of comparisons)",
            "EXIST (TRUE if the subquery returns one or more records)",
            "IN   (TRUE if the operand is equal to one of a list of expressions)",
            "LIKE (TRUE if the operand matches a pattern)",
            "NOT  (Displays a record if the condition(s) is NOT TRUE)",
            "OR   (TRUE if any of the conditions separated by OR is TRUE)",
            "SOME (TRUE if any of the subquery values meet the condition)"
    ).collect(Collectors.toList());

    private final List<String> examples = Stream.of(
            "DISPLAY_NAME = 'Wolf 359'",
            "DISTANCE < 10",
            "TEMPERATURE > 6000",
            "(DISTANCE <10) AND (TEMPERATURE >6000)",
            "DISPLAY_NAME LIKE 'D%'",
            "(DISPLAY_NAME LIKE 'D%') OR (DISTANCE < 20)"
    ).collect(Collectors.toList());

    public List<String> getStarObjectFields() {
        return fieldsMap.keySet().stream().map(key -> key + "::" + fieldsMap.get(key)).collect(Collectors.toList());
    }

    /**
     * get the list of ANSI SQL operators
     *
     * @return the operator list
     */
    public List<String> getOperators() {
        return operators;
    }

    public List<String> getExamples() {
        return examples;
    }
}
