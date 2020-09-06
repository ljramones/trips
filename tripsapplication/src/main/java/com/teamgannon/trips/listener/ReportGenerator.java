package com.teamgannon.trips.listener;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.Node;

import java.util.Map;
import java.util.UUID;

public interface ReportGenerator {

    void generateDistanceReport(StarDisplayRecord starDescriptor, Map<UUID, Node> starLookup);

}
