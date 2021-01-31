package com.teamgannon.trips.dialogs.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryFields {

    private final Map<String, String> fieldsMap = Stream.of(new String[][]{
            {"ID", "(string) the GUID of the star"},
            {"ACTUAL_MASS", "(double) the actual mass of the star"},
            {"ANOMALY", "(boolean) the actual mass of the star"},
            {"BPG", "(double) the actual mass of the star"},
            {"BPRP", "(double) the actual mass of the star"},
            {"CATALOG_ID_LIST", "(string) the actual mass of the star"},
            {"CONSTELLATION_NAME", "(string) the actual mass of the star"},
            {"DEC_DEG", "(double) the actual mass of the star"},
            {"DISPLAY_NAME", "(string) the actual mass of the star"},
            {"DISTANCE", "(double) the actual mass of the star"},
            {"EXOPLANETS", "(boolean) the actual mass of the star"},
            {"FUEL_TYPE", "(string) the actual mass of the star"},
            {"GRP", "(double) the actual mass of the star"},
            {"LUMINOSITY", "(string) the actual mass of the star"},
            {"MAGB", "(double) the actual mass of the star"},
            {"MAGI", "(double) the actual mass of the star"},
            {"MAGR", "(double) the actual mass of the star"},
            {"MAGU", "(double) the actual mass of the star"},
            {"MAGV", "(double) the actual mass of the star"},
            {"MASS", "(double) the actual mass of the star"},
            {"MIL_PLAN_TYPE", "(string) the actual mass of the star"},
            {"MIL_SPACE_TYPE", "(string) the actual mass of the star"},
            {"MISC_NUM1", "(double) the actual mass of the star"},
            {"MISC_NUM2", "(double) the actual mass of the star"},
            {"MISC_NUM3", "(double) the actual mass of the star"},
            {"MISC_NUM4", "(double) the actual mass of the star"},
            {"MISC_NUM5", "(double) the actual mass of the star"},
            {"MISC_TEXT1", "(string) the actual mass of the star"},
            {"MISC_TEXT2", "(string) the actual mass of the star"},
            {"MISC_TEXT3", "(string) the actual mass of the star"},
            {"MISC_TEXT4", "(string) the actual mass of the star"},
            {"MISC_TEXT5", "(string) the actual mass of the star"},
            {"NOTES", "(string) the actual mass of the star"},
            {"ORTHO_SPECTRAL_CLASS", "(string) the actual mass of the star"},
            {"OTHER", "(boolean) the actual mass of the star"},
            {"PARALLAX", "(double) the actual mass of the star"},
            {"PMDEC", "(double) the actual mass of the star"},
            {"PMRA", "(double) the actual mass of the star"},
            {"POPULATION_TYPE", "(string) the actual mass of the star"},
            {"PORT_TYPE", "(string) the actual mass of the star"},
            {"PRODUCT_TYPE", "(string) the actual mass of the star"},
            {"RA", "(double) the actual mass of the star"},
            {"RADIAL_VELOCITY", "(double) the actual mass of the star"},
            {"RADIUS", "(double) the actual mass of the star"},
            {"REAL_STAR", "(boolean) the actual mass of the star"},
            {"RS_CDEG", "(double) the actual mass of the star"},
            {"SOURCE", "(string) the actual mass of the star"},
            {"SPECTRAL_CLASS", "(string) the actual mass of the star"},
            {"TECH_TYPE", "(string) the actual mass of the star"},
            {"TEMPERATURE", "(double) the actual mass of the star"},
            {"WORLD_TYPE", "(string) the actual mass of the star"},
            {"X", "(double) the actual mass of the star"},
            {"Y", "(double) the actual mass of the star"},
            {"Z", "(double) the actual mass of the star"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private final List<String> operators = Stream.of(
            "=    Equal to",
            ">    Greater than",
            "<    Less than",
            ">=   Greater than or equal to",
            "<=   Less than or equal to",
            "<>   Not equal to",
            "ALL  TRUE if all of the subquery values meet the condition",
            "AND  TRUE if all the conditions separated by AND is TRUE",
            "ANY  TRUE if any of the subquery values meet the condition",
            "BETWEEN TRUE if the operand is within the range of comparisons",
            "EXIST TRUE if the subquery returns one or more records",
            "IN   TRUE if the operand is equal to one of a list of expressions",
            "LIKE TRUE if the operand matches a pattern",
            "NOT  Displays a record if the condition(s) is NOT TRUE",
            "OR   TRUE if any of the conditions separated by OR is TRUE",
            "SOME TRUE if any of the subquery values meet the condition"
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
