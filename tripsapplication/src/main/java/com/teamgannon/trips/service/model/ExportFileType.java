package com.teamgannon.trips.service.model;

public enum ExportFileType {

    CSV("csv"),

    JSON("json"),
    COMPACT("cpt");

    private final String fileType;

    ExportFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }
}
