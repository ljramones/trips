package com.teamgannon.trips.graphics;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.LineSegment;
import com.teamgannon.trips.graphics.entities.Xform;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Math.*;

@Slf4j
public class GridPlotManager {

    private final Xform gridGroup = new Xform();

    private final Xform scaleGroup = new Xform();

    private final double width;
    private final double depth;
    private final double spacing;
    private final double lineWidth;

    private static final String scaleString = "Scale: 1 grid is %.2f ly square";

    /**
     * the general color palette of the graph
     */
    private final ColorPalette colorPalette;


    /**
     * constructor
     *
     * @param spacing the spacing
     * @param width   the screen width
     * @param depth   the screen depth
     */
    public GridPlotManager(Xform world,
                           double spacing, double width, double depth,
                           ColorPalette colorPalette) {

        this.spacing = spacing;
        this.width = width;
        this.depth = depth;
        this.lineWidth = colorPalette.getGridLineWidth();
        this.colorPalette = colorPalette;
        this.gridGroup.setWhatAmI("Planar Grid");
        this.scaleGroup.setWhatAmI("Reference Scale");

        // setup data structures for each independent element
        world.getChildren().add(gridGroup);
        world.getChildren().add(scaleGroup);

        buildInitialGrid();
        buildInitialScaleLegend();
    }

    public Xform getGridGroup() {
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

    //////////////////  Initial  GRID  build   ///////////////////

    /**
     * build a fresh grid
     */
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

        gridGroup.setTranslate(-width / 2.0, 0, -depth / 2.0);

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

    ////////////////////  SCALE Group  /////////////////


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

    /**
     * create a scale legend
     *
     * @param scaleValue the scaling value
     */
    private void createScaleLegend(double scaleValue) {
        Text scaleText = new Text(String.format(scaleString, scaleValue));
        scaleText.setFont(Font.font("Verdana", 20));
        scaleText.setFill(colorPalette.getLegendColor());
        scaleGroup.getChildren().add(scaleText);
        scaleGroup.setTranslate(50, 350, 0);
        scaleGroup.setVisible(true);
        log.info("show scale");
    }

}
