package com.teamgannon.trips.routing.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class SparseTransit {

    private SparseStarRecord source;
    private SparseStarRecord target;

    private double distance;

    public @NotNull String getName() {
        return source.getRecordId().toString() + "," + target.getRecordId().toString();
    }

}
