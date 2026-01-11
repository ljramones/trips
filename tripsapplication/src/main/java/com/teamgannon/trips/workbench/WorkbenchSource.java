package com.teamgannon.trips.workbench;

public class WorkbenchSource {

    private final WorkbenchSourceType type;
    private final String name;
    private final String location;

    private WorkbenchSource(WorkbenchSourceType type, String name, String location) {
        this.type = type;
        this.name = name;
        this.location = location;
    }

    public static WorkbenchSource localCsv(String name, String path) {
        return new WorkbenchSource(WorkbenchSourceType.LOCAL_CSV, name, path);
    }

    public static WorkbenchSource urlCsv(String url) {
        String name = url;
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash + 1 < url.length()) {
            name = url.substring(lastSlash + 1);
        }
        return new WorkbenchSource(WorkbenchSourceType.URL_CSV, name, url);
    }

    public static WorkbenchSource gaiaTap(String name, String adql) {
        return new WorkbenchSource(WorkbenchSourceType.GAIA_TAP, name, adql);
    }

    public static WorkbenchSource simbadTap(String name, String adql) {
        return new WorkbenchSource(WorkbenchSourceType.SIMBAD_TAP, name, adql);
    }

    public static WorkbenchSource vizierTap(String name, String adql) {
        return new WorkbenchSource(WorkbenchSourceType.VIZIER_TAP, name, adql);
    }

    public WorkbenchSourceType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
