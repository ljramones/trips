package com.teamgannon.trips.dialogs.support;

import java.util.Arrays;

public enum DataFormatEnum {

    CH_VIEW("chview import"),
    RB_CSV("csv(RB) import"),
    CSV("csv import"),
    RB_EXCEL("excel(RB) import"),
    EXCEL("excel import"),
    JSON("json import");

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
