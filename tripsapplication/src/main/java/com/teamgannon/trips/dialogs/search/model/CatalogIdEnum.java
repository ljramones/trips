package com.teamgannon.trips.dialogs.search.model;

public enum CatalogIdEnum {

    HIPPARCOS("Hipparcos"),
    HD("Henry Draper"),
    BAYER("Bayer"),
    FLAMSTEED("Flamsteed"),
    GLIESE("GLIESE"),
    GAIADR2("Gaia DR2"),
    GAIADR3("Gaia DR3"),
    GAIAEDR3("Gaia EDR3"),
    TYCHO2("Tycho-2"),
    CSI("CSI"),
    TWO_MASS("2MASS"),

    OTHER("Other");

    private final String catalogId;

    CatalogIdEnum(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public static CatalogIdEnum fromString(String text) {
        for (CatalogIdEnum b : CatalogIdEnum.values()) {
            if (b.catalogId.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return OTHER;
    }
}
