package com.teamgannon.trips.graphics.panes;

import javafx.scene.Node;
import lombok.Data;


/**
 * Selecting stars on a pane
 * <p>
 * Created by larrymitchell on 2017-02-20.
 */
@Data
public class StarSelectionModel {

    /**
     * the star ndoe that we selected
     */
    private Node starNode;

    /**
     * the selection rectangle
     */
    private Node selectionRectangle;

}
