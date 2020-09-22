package com.teamgannon.trips.service.model;

public enum ExportFileType {

    CSV("csv"),
    EXCEL("excel"),
    JSON("json");

    private final String fileType;

    ExportFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }
}
