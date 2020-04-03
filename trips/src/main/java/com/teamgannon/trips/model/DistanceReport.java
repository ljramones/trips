package com.teamgannon.trips.model;


import java.util.ArrayList;
import java.util.List;

public class DistanceReport {

    private List<DistanceToFrom> distanceToFromList = new ArrayList<>();

    public void addDistanceToFrom(DistanceToFrom distanceToFrom) {
        distanceToFromList.add(distanceToFrom);
    }

    public List<DistanceToFrom> getDistanceList() {
        // create the sorting comparator
        DistanceToFromComparator comparator = new DistanceToFromComparator();

        // sort the list in place based on the ranking comparator
        distanceToFromList.sort(comparator);

        // return the sorted list
        return distanceToFromList;
    }

}
