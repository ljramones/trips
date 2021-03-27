package com.teamgannon.trips.graphics;

import com.teamgannon.trips.config.application.CurrentPlot;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.LineSegment;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;

@Slf4j
public class GridPlotManager {

    private static final String scaleString = "Scale: 1 grid is %.2f ly square";

    /**
     * contains the labels so we can redraw when the display shifts
     */
    private final @NotNull Map<Node, Label> shapeToLabel;

    /**
     * the drawing group for the scale
     */
    private final Group scaleGroup = new Group();

    /**
     * the drawing group for the grid
     */
    private final Group gridGroup = new Group();

    /**
     * used to control label visibility
     */
    private final Group labelDisplayGroup = new Group();
    private final double spacing;
    private final double width;
    private final double depth;
    private final ColorPalette colorPalette;
    private final SubScene subScene;
    private final double lineWidth;
    private Label scaleText;
    private double controlPaneOffset;


    /**
     * constructor
     *
     * @param spacing the spacing
     * @param width   the screen width
     * @param depth   the screen depth
     */
    public GridPlotManager(@NotNull Group world,
                           @NotNull Group sceneRoot,
                           SubScene subScene,
                           double spacing,
                           double width, double depth,
                           ColorPalette colorPalette) {

        this.spacing = spacing;
        this.width = width;
        this.depth = depth;
        this.colorPalette = colorPalette;
        this.shapeToLabel = new HashMap<>();
        this.subScene = subScene;

        this.lineWidth = 0.5;

        sceneRoot.getChildren().add(scaleGroup);
        sceneRoot.getChildren().add(labelDisplayGroup);
        world.getChildren().add(gridGroup);

        buildInitialGrid();
        buildInitialScaleLegend();
    }


    //////////////   public controls  //////////////////

    public @NotNull Group getGridGroup() {
        return gridGroup;
    }

    /**
     * toggle the grid
     *
     * @param gridToggle the status for the grid
     */
    public void toggleGrid(boolean gridToggle) {
        gridGroup.setVisible(gridToggle);
        labelDisplayGroup.setVisible(gridToggle);
    }

    public void toggleVisibility() {
        boolean flag = !gridGroup.isVisible();
        gridGroup.setVisible(flag);
        labelDisplayGroup.setVisible(flag);
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
    }

    public void updateScale() {
        scaleText.setTranslateX(subScene.getWidth() - 430);
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
    private void createGrid(double gridIncrement, @NotNull ColorPalette colorPalette) {

        gridGroup.getTransforms().add(
                new Translate(-width / 2.0, 0, -depth / 2.0)
        );

        // iterate over y dimension
        int yDivisions = (int) ceil(width / gridIncrement);
        double x = 0.0;
        for (int i = 0; i <= yDivisions; i++) {
            Point3D from = new Point3D(x, 0, 0);
            Point3D to = new Point3D(x, depth, 0);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    from, to, lineWidth,
                    colorPalette.getGridColor(), colorPalette.getLabelFont().toFont());
            gridGroup.getChildren().add(lineSegment);
            x += gridIncrement;
        }

        // iterate over x dimension
        int xDivisions = (int) ceil(depth / gridIncrement);
        double y = 0.0;
        for (int i = 0; i <= xDivisions; i++) {
            Point3D from = new Point3D(0, y, 0);
            Point3D to = new Point3D(width, y, 0);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    from, to, lineWidth,
                    colorPalette.getGridColor(), colorPalette.getLabelFont().toFont());
            gridGroup.getChildren().add(lineSegment);
            y += gridIncrement;
        }

        gridGroup.setVisible(true);
    }

    ////

    /**
     * rebuild the grid with the specified transformation characteristics
     *
     * @param transformer the transformer
     * @param currentPlot the current plot summary
     */
    public void rebuildGrid(@NotNull AstrographicTransformer transformer, CurrentPlot currentPlot) {

        // remember to clear the labels first
        shapeToLabel.clear();
        labelDisplayGroup.getChildren().clear();
        log.info("CLEARED GRID LABELS!!");

        ScalingParameters parameters = transformer.getScalingParameters();

        // clear old grid
        gridGroup.getChildren().clear();

        log.info("rebuilding grid scale increment: " + parameters.getScaleIncrement());

        // rebuild grid
        createGrid(transformer, currentPlot);

        // now rebuild scale legend
        rebuildScaleLegend((int) parameters.getScaleIncrement());
    }

    private void createGrid(@NotNull AstrographicTransformer transformer, @NotNull CurrentPlot currentPlot) {

        ScalingParameters parameters = transformer.getScalingParameters();
        double minY = parameters.getMinY();
        double maxY = parameters.getMaxY();
        double yDivs = ceil(parameters.getYRange() / 5);

        double minX = parameters.getMinX();
        double maxX = parameters.getMaxX();
        double xDivs = ceil(parameters.getXRange() / 5);

        // create x division lines
        drawXLineSegments(transformer, currentPlot, minY, maxY, xDivs, 0, 5);
        drawXLineSegments(transformer, currentPlot, minY, maxY, xDivs, 0, -5);

        // create y division lines
        drawYLineSegments(transformer, currentPlot, yDivs, minX, maxX, 0, 5);
        drawYLineSegments(transformer, currentPlot, yDivs, minX, maxX, 0, -5);

        if (gridGroup.isVisible()) {
            gridGroup.setVisible(true);
        }

    }


    private void drawYLineSegments(@NotNull AstrographicTransformer transformer,
                                   @NotNull CurrentPlot currentPlot,
                                   double yDivs, double minX, double maxX, double beginY,
                                   double increment) {

        // get zero plane based on star center coordinates
        double zZero = currentPlot.getCenterCoordinates()[1];

        for (int i = 0; i < (ceil(yDivs / 2) + 1); i++) {
            double[] fromPointX = new double[]{signum(minX) * ceil(abs(minX)), beginY, zZero};
            double[] toPointX = new double[]{signum(maxX) * ceil(abs(maxX)), beginY, zZero};
            String label = Integer.toString((int) beginY);
            LineSegment lineSegmentY = LineSegment.getTransformedLine(transformer, width, depth, fromPointX, toPointX);
            Node gridLineSegmentY = createLineSegment(
                    lineSegmentY.getFrom(), lineSegmentY.getTo(),
                    lineWidth, currentPlot.getColorPalette().getGridColor(),
                    label, false);
            gridGroup.getChildren().add(gridLineSegmentY);
            beginY += increment;
        }
    }

    private void drawXLineSegments(@NotNull AstrographicTransformer transformer,
                                   @NotNull CurrentPlot currentPlot,
                                   double minY, double maxY, double xDivs, double beginX, double increment) {

        // get zero plane based on star center coordinates
        double zZero = currentPlot.getCenterCoordinates()[1];

        for (int i = 0; i < (ceil(xDivs / 2)); i++) {
            double[] fromPointZ = new double[]{beginX, signum(minY) * ceil(abs(minY)), zZero};
            double[] toPointZ = new double[]{beginX, signum(maxY) * ceil(abs(maxY)), zZero};
            String label = Integer.toString((int) beginX);
            LineSegment lineSegmentX = LineSegment.getTransformedLine(transformer, width, depth, fromPointZ, toPointZ);
            Node gridLineSegmentX = createLineSegment(
                    lineSegmentX.getFrom(), lineSegmentX.getTo(),
                    lineWidth, currentPlot.getColorPalette().getGridColor(),
                    label, true);
            gridGroup.getChildren().add(gridLineSegmentX);
            beginX -= increment;
        }
    }


    /////////////////////////////////////
    // line segment
    /////////////////////////////////////

    /**
     * create a line segment based on the
     *
     * @param origin the from point
     * @param target the to point
     * @param width  the linewidth
     * @param color  the color of the line
     * @param tag    the value to
     * @param sense  which end to place a label
     * @return the actual 3-d line
     */
    public @NotNull Node createLineSegment(@NotNull Point3D origin,
                                           @NotNull Point3D target,
                                           double width,
                                           Color color,
                                           @Nullable String tag,
                                           boolean sense) {

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        // create cylinder and color it with phong material
        Cylinder line = StellarEntityFactory.createCylinder(width, color, height);

        Group lineGroup = new Group();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);
        if (tag != null) {
            Label label = createLabel(tag);
            Sphere pointSphere = createPointSphere(label);
            if (sense) {
                pointSphere.setTranslateX(origin.getX());
                pointSphere.setTranslateY(origin.getY());
                pointSphere.setTranslateZ(origin.getZ());
            } else {
                pointSphere.setTranslateX(target.getX());
                pointSphere.setTranslateY(target.getY());
                pointSphere.setTranslateZ(target.getZ());
            }
            lineGroup.getChildren().add(pointSphere);
        }

        return lineGroup;
    }

    public @NotNull Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(new Font("Arial", 6));
        label.setTextFill(Color.WHEAT);
        return label;
    }

    private @NotNull Sphere createPointSphere(@NotNull Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHEAT);
        material.setSpecularColor(Color.WHEAT);
        Sphere sphere = new Sphere(1);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(sphere, label);
        return sphere;
    }

    ///////////////////////////////////////////////////////////////////
    ////////////  label updates
    ///////////////////////////////////////////////////////////////////


    /**
     * update labels
     */
    public void updateLabels(@NotNull InterstellarSpacePane interstellarSpacePane) {
        shapeToLabel.forEach((node, label) -> {
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            //Clipping Logic
            //if coordinates are outside of the scene it could
            //stretch the screen so don't transform them
            double xs = coordinates.getX();
            double ys = coordinates.getY();

            double x;
            double y;

            Bounds ofParent = interstellarSpacePane.getBoundsInParent();
            if (ofParent.getMinX() > 0) {
                x = xs - ofParent.getMinX();
            } else {
                x = xs;
            }
            if (ofParent.getMinY() >= 0) {
                y = ys - ofParent.getMinY() - controlPaneOffset;
            } else {
                y = ys < 0 ? ys - controlPaneOffset : ys + controlPaneOffset;
            }

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

    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }
}
