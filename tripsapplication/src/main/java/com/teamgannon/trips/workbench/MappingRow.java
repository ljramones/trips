package com.teamgannon.trips.workbench;

public class MappingRow {

    private final String sourceField;
    private final String targetField;

    public MappingRow(String sourceField, String targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }

    public String getSourceField() {
        return sourceField;
    }

    public String getTargetField() {
        return targetField;
    }
}
