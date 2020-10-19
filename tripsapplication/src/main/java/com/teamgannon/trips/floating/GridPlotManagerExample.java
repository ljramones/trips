package com.teamgannon.trips.floating;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.ceil;

@Slf4j
public class GridPlotManagerExample {

    private final Map<Node, Label> shapeToLabel;

    private final Group scaleGroup = new Group();
    private final Group gridGroup = new Group();

    private final Group world;
    private final Group labelGroup;
    private final SubScene subScene;

    private final double lineWidth;

    private final Label scaleLabel = new Label("Scale: 5 ly");

    private final Font font = new Font("Arial", 15);


    public GridPlotManagerExample(Group labelGroup,
                                  Group world,
                                  Group sceneRoot,
                                  SubScene subScene) {

        this.world = world;
        this.shapeToLabel = new HashMap<>();
        this.labelGroup = labelGroup;
        this.subScene = subScene;

        this.lineWidth = 0.5;
        double spacing = 5;

        sceneRoot.getChildren().add(scaleGroup);
        world.getChildren().add(gridGroup);

        buildInitialGrid();
        setupScale();

    }

    ///////////////////// SCALE DRAWING  //////////////////

    private void setupScale() {
        scaleLabel.setFont(font);
        scaleLabel.setTextFill(Color.WHEAT);
        updateScale();
        scaleGroup.getChildren().add(scaleLabel);
        log.info("shapes:{}", shapeToLabel.size());
    }

    public void updateScale() {
        scaleLabel.setTranslateX(subScene.getWidth() - 100);
        scaleLabel.setTranslateY(subScene.getHeight() - 20);
        scaleLabel.setTranslateZ(0);
    }

    ///////////////////// GRID DRAWING //////////////////

    private void buildInitialGrid() {
        Box box = new Box(10, 10, 10);
        box.setTranslateX(0);
        box.setTranslateY(0);
        box.setTranslateZ(0);
        gridGroup.getChildren().add(box);

        Label boxLabel = new Label("Stupid box");
        boxLabel.setFont(font);
        boxLabel.setTextFill(Color.WHEAT);
        labelGroup.getChildren().add(boxLabel);
        shapeToLabel.put(box, boxLabel);


//        gridGroup.setTranslate(-width / 2.0, 0, -depth / 2.0);
        double gridIncrement = 50.0;

        // iterate over z dimension
        int zDivisions = (int) ceil(Universe.boxWidth / gridIncrement);
        double x = -Universe.boxWidth / 2;
        for (int i = 0; i <= zDivisions; i++) {
            Point3D from = new Point3D(x, 0, -Universe.boxDepth / 2);
            Point3D to = new Point3D(x, 0, Universe.boxDepth / 2);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    from, to, lineWidth, Color.BLUE);
            gridGroup.getChildren().add(lineSegment);
            x += gridIncrement;
        }

        // iterate over x dimension
        int xDivisions = (int) ceil(Universe.boxDepth / gridIncrement);
        double z = -Universe.boxDepth / 2;
        for (int i = 0; i <= xDivisions; i++) {
            Point3D from = new Point3D(-Universe.boxWidth / 2, 0, z);
            Point3D to = new Point3D(Universe.boxWidth / 2, 0, z);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    from, to, lineWidth, Color.BLUE);
            gridGroup.getChildren().add(lineSegment);
            z += gridIncrement;
        }
        updateLabels();
    }

    public void updateLabels() {
        shapeToLabel.forEach((node, label) -> {
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            //Clipping Logic
            //if coordinates are outside of the scene it could
            //stretch the screen so don't transform them
            double x = coordinates.getX();
            double y = coordinates.getY();

            // is it left of the view?
            if (x < 0) {
                x = 0;
            }

            // is it right of the view?
            if ((x + label.getWidth() + 5) > subScene.getWidth()) {
                x = subScene.getWidth() - (label.getWidth() + 5);
            }

            // is it above the view?
            if (y < 0) {
                y = 0;
            }

            // is it below the view
            if ((y + label.getHeight()) > subScene.getHeight()) {
                y = subScene.getHeight() - (label.getHeight() + 5);
            }

            //update the local transform of the label.
            label.getTransforms().setAll(new Translate(x, y));
        });

    }

}
