package com.teamgannon.trips.routing;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class PossibleRoutes {

    private String desiredPath;

    private List<RoutingMetric> routes = new ArrayList<>();

}
