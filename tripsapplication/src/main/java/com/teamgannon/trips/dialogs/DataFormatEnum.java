package com.teamgannon.trips.dialogs;

import java.util.Arrays;

public enum DataFormatEnum {

    CH_VIEW("chview import"),
    RB_CSV("csv(RB) import"),
    RB_EXCEL("excel import"),
    EXPORT("export as csv"),
    SIMBAD("simbad import"),
    HIPPARCOS3("hipp3 import");

    private final String value;

    DataFormatEnum(String value) {
        this.value = value;
    }

    public static DataFormatEnum fromString(String text) {
        return Arrays.stream(DataFormatEnum.values()).filter(b -> b.value.equalsIgnoreCase(text)).findFirst().orElse(null);
    }

    public String getValue() {
        return value;
    }

}
