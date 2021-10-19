package com.teamgannon.trips.routing.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class PossibleRoutes {

    private String desiredPath;

    private @NotNull List<RoutingMetric> routes = new ArrayList<>();

}
