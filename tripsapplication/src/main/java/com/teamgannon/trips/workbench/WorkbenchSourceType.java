package com.teamgannon.trips.workbench;

public enum WorkbenchSourceType {
    LOCAL_CSV("Local CSV"),
    URL_CSV("CSV URL"),
    GAIA_TAP("Gaia TAP"),
    SIMBAD_TAP("SIMBAD TAP");

    private final String label;

    WorkbenchSourceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static WorkbenchSourceType fromLabel(String label) {
        for (WorkbenchSourceType type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        return LOCAL_CSV;
    }
}
