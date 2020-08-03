package com.teamgannon.trips.config.application;

import javafx.scene.paint.Color;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LinkDisplayPreferences {

    private boolean showLinks = true;

    private boolean showDistances = false;

    private List<LinkDescriptor> linkDescriptorList = new ArrayList<>();

    public LinkDisplayPreferences() {
        createLinks();
    }

    public void createLinks() {
        linkDescriptorList.add(LinkDescriptor.createLinkDescriptor(1, Color.CYAN, 9));
        linkDescriptorList.add(LinkDescriptor.createLinkDescriptor(2, Color.ALICEBLUE, 6));
        linkDescriptorList.add(LinkDescriptor.createLinkDescriptor(3, Color.CORAL, 4));
        linkDescriptorList.add(LinkDescriptor.createLinkDescriptor(4, Color.IVORY, 1));
    }

}
