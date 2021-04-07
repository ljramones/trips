package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.LinkDescriptor;
import com.teamgannon.trips.config.application.LinkDisplayPreferences;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

public class LinksPane extends Pane {

    private final LinkDisplayPreferences linksDefinition;

    public LinksPane(@NotNull LinkDisplayPreferences linksDefinition, String style) {
        this.linksDefinition = linksDefinition;

        this.setStyle(style);

        GridPane gridPane = new GridPane();
        gridPane.setStyle(style);

        gridPane.setPadding(new Insets(10, 10, 10, 10));

        gridPane.add(new Label("       "), 1, 1);

        Pane pane1 = linksDescriptionPane(linksDefinition);
        gridPane.add(pane1, 1, 2);

        gridPane.add(new Label("      "), 1, 3);

        this.getChildren().add(gridPane);
    }

    private @NotNull Pane linksDescriptionPane(@NotNull LinkDisplayPreferences linksDefinition) {
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(10, 10, 10, 10));

        pane.add(new Separator(), 1, 1, 1, 6);

        CheckBox showLinks = new CheckBox("Display Links");
        showLinks.setSelected(linksDefinition.isShowLinks());
        pane.add(showLinks, 2, 1, 2, 1);

        CheckBox showDistances = new CheckBox("Display Distances");
        showDistances.setSelected(linksDefinition.isShowDistances());
        pane.add(showDistances, 2, 2, 2, 1);

        createLinkLine(pane, 3, linksDefinition.getLinkDescriptorList().get(0));
        createLinkLine(pane, 4, linksDefinition.getLinkDescriptorList().get(1));
        createLinkLine(pane, 5, linksDefinition.getLinkDescriptorList().get(2));
        createLinkLine(pane, 6, linksDefinition.getLinkDescriptorList().get(3));

        return pane;
    }

    private void createLinkLine(@NotNull GridPane pane, int row, @NotNull LinkDescriptor linkDescriptor) {
        pane.add(
                new Label(
                        String.format("Link %d:", linkDescriptor.getLinkNumber())),
                2, row);
        TextField link1 = new TextField(Integer.toString(linkDescriptor.getLinkLength()));
        link1.setPrefWidth(30);
        pane.add(link1, 3, row);
        ColorPicker gridColorPicker = new ColorPicker(linkDescriptor.getColor());
        pane.add(gridColorPicker, 4, row);
    }

    public void reset() {
           // not used currently
    }
}
