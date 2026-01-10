package com.teamgannon.trips.starplotting;

import javafx.scene.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransitionState {

    private  Node node;

    private double xScale;

    private double yScale;

    private double zScale;

}
