package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.config.application.model.LinkDescriptor;
import javafx.scene.paint.Color;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class LinkDisplayPreferences implements Serializable {

    @Serial
    private static final long serialVersionUID = -8669652139078807845L;
    private boolean showLinks = true;

    private boolean showDistances = false;

    private @NotNull List<LinkDescriptor> linkDescriptorList = new ArrayList<>();

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
