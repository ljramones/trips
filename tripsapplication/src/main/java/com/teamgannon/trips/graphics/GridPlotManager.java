package com.teamgannon.trips.graphics;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.LineSegment;
import com.teamgannon.trips.graphics.entities.MoveableGroup;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;

@Slf4j
public class GridPlotManager {

    private final Map<Node, Label> shapeToLabel;

    private final Group scaleGroup = new Group();
    private final Group gridGroup = new Group();

    private final Group world;
    private Group sceneRoot;
    private double spacing;
    private double width;
    private double depth;
    private ColorPalette colorPalette;


    private final SubScene subScene;

    private final double lineWidth;

    private static final String scaleString = "Scale: 1 grid is %.2f ly square";

    private final Font font = Font.font("Verdana", 20);

    private Label scaleText;


    /**
     * constructor
     *
     * @param spacing the spacing
     * @param width   the screen width
     * @param depth   the screen depth
     */
    public GridPlotManager(Group world,
                           Group sceneRoot,
                           SubScene subScene,
                           double spacing,
                           double width, double depth,
                           ColorPalette colorPalette) {

        this.world = world;
        this.spacing = spacing;
        this.width = width;
        this.depth = depth;
        this.colorPalette = colorPalette;
        this.shapeToLabel = new HashMap<>();
        this.subScene = subScene;

        this.lineWidth = 0.5;

        sceneRoot.getChildren().add(scaleGroup);
        world.getChildren().add(gridGroup);

        buildInitialGrid();
        buildInitialScaleLegend();
    }


    //////////////   public controls  //////////////////

    public Group getGridGroup() {
        return gridGroup;
    }

    /**
     * toggle the grid
     *
     * @param gridToggle the status for the grid
     */
    public void toggleGrid(boolean gridToggle) {
        gridGroup.setVisible(gridToggle);
    }

    public void toggleVisibility() {
        gridGroup.setVisible(!gridGroup.isVisible());
    }

    public boolean isVisible() {
        return gridGroup.isVisible();
    }

    /**
     * toggle the scale
     *
     * @param scaleOn the status of the scale
     */
    public void toggleScale(boolean scaleOn) {
        scaleGroup.setVisible(scaleOn);
    }

    ///////////////////// SCALE DRAWING  //////////////////

    /**
     * rebuild the scale legend
     *
     * @param newScaleLegend the new scale legend
     */
    private void rebuildScaleLegend(int newScaleLegend) {
        scaleGroup.getChildren().clear();
        createScaleLegend(newScaleLegend);
    }

    /**
     * build the scale for the display
     */
    private void buildInitialScaleLegend() {
        createScaleLegend(spacing);
    }



    private void createScaleLegend(double scaleValue) {
        scaleText = new Label(String.format(scaleString, scaleValue));
        scaleText.setFont(Font.font("Verdana", 20));
        scaleText.setTextFill(colorPalette.getLegendColor());
        updateScale();
        scaleGroup.getChildren().add(scaleText);
        log.info("shapes:{}", shapeToLabel.size());
    }

    public void updateScale() {
        scaleText.setTranslateX(subScene.getWidth() - 250);
        scaleText.setTranslateY(subScene.getHeight() - 30);
        scaleText.setTranslateZ(0);
    }

    ///////////////////// GRID DRAWING //////////////////


    private void buildInitialGrid() {
        createGrid(spacing, colorPalette);
    }

    /**
     * create a gird based on parameter supplied
     *
     * @param gridIncrement the grid increment
     * @param colorPalette  the color palette
     */
    private void createGrid(double gridIncrement, ColorPalette colorPalette) {

        gridGroup.getTransforms().add(
                new Translate(-width / 2.0, 0, -depth / 2.0)
        );

        // iterate over z dimension
        int zDivisions = (int) ceil(width / gridIncrement);
        double x = 0.0;
        for (int i = 0; i <= zDivisions; i++) {
            Point3D from = new Point3D(x, 0, 0);
            Point3D to = new Point3D(x, 0, depth);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    from, to, lineWidth, colorPalette.getGridColor());
            gridGroup.getChildren().add(lineSegment);
            x += gridIncrement;
        }

        // iterate over x dimension
        int xDivisions = (int) ceil(depth / gridIncrement);
        double z = 0.0;
        for (int i = 0; i <= xDivisions; i++) {
            Point3D from = new Point3D(0, 0, z);
            Point3D to = new Point3D(width, 0, z);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    from, to, lineWidth, colorPalette.getGridColor());
            gridGroup.getChildren().add(lineSegment);
            z += gridIncrement;
        }

        gridGroup.setVisible(true);
    }

    ////

    /**
     * rebuild the grid with the specified transformation characteristics
     *
     * @param transformer  the transformer
     * @param colorPalette the color palette
     */
    public void rebuildGrid(AstrographicTransformer transformer, ColorPalette colorPalette) {

        ScalingParameters parameters = transformer.getScalingParameters();

        // clear old grid
        gridGroup.getChildren().clear();

        log.info("rebuilding grid scale increment: " + parameters.getScaleIncrement());

        // rebuild grid
        createGrid(transformer, colorPalette);

        // now rebuild scale legend
        rebuildScaleLegend((int) parameters.getScaleIncrement());
    }

    private void createGrid(AstrographicTransformer transformer, ColorPalette colorPalette) {

        ScalingParameters parameters = transformer.getScalingParameters();
        double minZ = parameters.getMinZ();
        double maxZ = parameters.getMaxZ();
        double zDivs = ceil(parameters.getZRange() / 5);

        double minX = parameters.getMinX();
        double maxX = parameters.getMaxX();
        double xDivs = ceil(parameters.getXRange() / 5);


        // create z division lines
        drawXLineSegments(transformer, colorPalette, minZ, maxZ, xDivs, 0, 5);
        drawXLineSegments(transformer, colorPalette, minZ, maxZ, xDivs, 0, -5);

        // create x division lines
        drawZLineSegments(transformer, colorPalette, zDivs, minX, maxX, 0, 5);
        drawZLineSegments(transformer, colorPalette, zDivs, minX, maxX, 0, -5);

        gridGroup.setVisible(true);

    }


    private void drawZLineSegments(AstrographicTransformer transformer, ColorPalette colorPalette, double zDivs, double minX, double maxX, double beginZ, double increment) {
        for (int i = 0; i < (ceil(zDivs / 2) + 1); i++) {
            double[] fromPointX = new double[]{signum(minX) * ceil(abs(minX)), 0, beginZ};
            double[] toPointX = new double[]{signum(maxX) * ceil(abs(maxX)), 0, beginZ};
            String label = Integer.toString((int) beginZ);
            LineSegment lineSegmentX = LineSegment.getTransformedLine(transformer, width, depth, fromPointX, toPointX);
            Node gridLineSegmentX = CustomObjectFactory.createLineSegment(
                    lineSegmentX.getFrom(), lineSegmentX.getTo(),
                    lineWidth, colorPalette.getGridColor(),
                    label, false);
            gridGroup.getChildren().add(gridLineSegmentX);
            beginZ += increment;
        }
    }

    private void drawXLineSegments(AstrographicTransformer transformer, ColorPalette colorPalette, double minZ, double maxZ, double xDivs, double beginX, double increment) {
        for (int i = 0; i < (ceil(xDivs / 2)); i++) {
            double[] fromPointZ = new double[]{beginX, 0, signum(minZ) * ceil(abs(minZ))};
            double[] toPointZ = new double[]{beginX, 0, signum(maxZ) * ceil(abs(maxZ))};
            String label = Integer.toString((int) beginX);
            LineSegment lineSegmentZ = LineSegment.getTransformedLine(transformer, width, depth, fromPointZ, toPointZ);
            Node gridLineSegmentZ = CustomObjectFactory.createLineSegment(
                    lineSegmentZ.getFrom(), lineSegmentZ.getTo(),
                    lineWidth, colorPalette.getGridColor(),
                    label, true);
            gridGroup.getChildren().add(gridLineSegmentZ);
            beginX -= increment;
        }
    }

    ////////////  label updates

    /**
     * update labels
     */
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
