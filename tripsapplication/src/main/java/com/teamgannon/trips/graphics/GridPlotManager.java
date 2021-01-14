package com.teamgannon.trips.graphics;

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

    private final @NotNull Map<Node, Label> shapeToLabel;

    private final Group scaleGroup = new Group();
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

    private static final String scaleString = "Scale: 1 grid is %.2f ly square";


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

        // iterate over z dimension
        int zDivisions = (int) ceil(width / gridIncrement);
        double x = 0.0;
        for (int i = 0; i <= zDivisions; i++) {
            Point3D from = new Point3D(x, 0, 0);
            Point3D to = new Point3D(x, 0, depth);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    from, to, lineWidth, colorPalette.getGridColor(), colorPalette.getLabelFont().toFont());
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
                    from, to, lineWidth, colorPalette.getGridColor(), colorPalette.getLabelFont().toFont());
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
    public void rebuildGrid(@NotNull AstrographicTransformer transformer, @NotNull ColorPalette colorPalette) {

        ScalingParameters parameters = transformer.getScalingParameters();

        // clear old grid
        gridGroup.getChildren().clear();

        log.info("rebuilding grid scale increment: " + parameters.getScaleIncrement());

        // rebuild grid
        createGrid(transformer, colorPalette);

        // now rebuild scale legend
        rebuildScaleLegend((int) parameters.getScaleIncrement());
    }

    private void createGrid(@NotNull AstrographicTransformer transformer, @NotNull ColorPalette colorPalette) {

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


    private void drawZLineSegments(@NotNull AstrographicTransformer transformer, @NotNull ColorPalette colorPalette,
                                   double zDivs, double minX, double maxX, double beginZ,
                                   double increment) {
        for (int i = 0; i < (ceil(zDivs / 2) + 1); i++) {
            double[] fromPointX = new double[]{signum(minX) * ceil(abs(minX)), 0, beginZ};
            double[] toPointX = new double[]{signum(maxX) * ceil(abs(maxX)), 0, beginZ};
            String label = Integer.toString((int) beginZ);
            LineSegment lineSegmentX = LineSegment.getTransformedLine(transformer, width, depth, fromPointX, toPointX);
            Node gridLineSegmentX = createLineSegment(
                    lineSegmentX.getFrom(), lineSegmentX.getTo(),
                    lineWidth, colorPalette.getGridColor(),
                    label, false);
            gridGroup.getChildren().add(gridLineSegmentX);
            beginZ += increment;
        }
    }

    private void drawXLineSegments(@NotNull AstrographicTransformer transformer, @NotNull ColorPalette colorPalette, double minZ, double maxZ, double xDivs, double beginX, double increment) {
        for (int i = 0; i < (ceil(xDivs / 2)); i++) {
            double[] fromPointZ = new double[]{beginX, 0, signum(minZ) * ceil(abs(minZ))};
            double[] toPointZ = new double[]{beginX, 0, signum(maxZ) * ceil(abs(maxZ))};
            String label = Integer.toString((int) beginX);
            LineSegment lineSegmentZ = LineSegment.getTransformedLine(transformer, width, depth, fromPointZ, toPointZ);
            Node gridLineSegmentZ = createLineSegment(
                    lineSegmentZ.getFrom(), lineSegmentZ.getTo(),
                    lineWidth, colorPalette.getGridColor(),
                    label, true);
            gridGroup.getChildren().add(gridLineSegmentZ);
            beginX -= increment;
        }
    }

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

    ////////////  label updates

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
