package com.teamgannon.trips.graphics;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.Xform;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GridPlotManager {

    private final Xform gridGroup = new Xform();
    private final Xform extensionsGroup;
    private final Xform scaleGroup = new Xform();

    private final int width;
    private final int depth;
    private final int spacing;
    private final double lineWidth;

    private static final String scaleString = "Scale: 1 grid is %d ly square";

    /**
     * the general color palette of the graph
     */
    private final ColorPalette colorPalette;


    /**
     * constructor
     *
     * @param extensionsGroup the extensions group
     * @param spacing         the spacing
     * @param width           the screen width
     * @param depth           the screen depth
     * @param lineWidth       the linewidth
     */
    public GridPlotManager(Xform extensionsGroup,
                           int spacing, int width, int depth, double lineWidth,
                           ColorPalette colorPalette) {
        this.extensionsGroup = extensionsGroup;
        this.spacing = spacing;
        this.width = width;
        this.depth = depth;
        this.lineWidth = lineWidth;
        this.colorPalette = colorPalette;
        this.gridGroup.setWhatAmI("Planar Grid");
        this.scaleGroup.setWhatAmI("Reference Scale");

        buildGrid();
        buildInitialScaleLegend();
    }

    public Xform getGridGroup() {
        return gridGroup;
    }

    public Xform getScaleGroup() {
        return scaleGroup;
    }

    /**
     * toggle the grid
     *
     * @param gridToggle the status for the grid
     */
    public void toggleGrid(boolean gridToggle) {
        gridGroup.setVisible(gridToggle);
        extensionsGroup.setVisible(gridToggle);
    }

    /**
     * toggle the extensions
     *
     * @param extensionsOn the status of the extensions
     */
    public void toggleExtensions(boolean extensionsOn) {
        if (gridGroup.isVisible()) {
            extensionsGroup.setVisible(extensionsOn);
        }
    }


    public void extensionsOn(boolean starsOn) {
        extensionsGroup.setVisible(starsOn);
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
        createGrid((int) parameters.getGridScale(), colorPalette);

        // now rebuild scale legend
        rebuildScaleLegend((int) parameters.getScaleIncrement());
    }


    /**
     * build a fresh grid
     */
    private void buildGrid() {
        createGrid(spacing, colorPalette);
    }

    /**
     * create a gird based on parameter supplied
     *
     * @param gridIncrement the grid increment
     * @param colorPalette  the color palette
     */
    private void createGrid(int gridIncrement, ColorPalette colorPalette) {

        gridGroup.setTranslate(-width / 2.0, 0, -depth / 2.0);

        // iterate over z dimension
        int zDivisions = width / gridIncrement;
        double x = 0.0;
        for (int i = 0; i <= zDivisions; i++) {
            Point3D from = new Point3D(x, 0, 0);
            Point3D to = new Point3D(x, 0, depth);
            Node lineSegment = CustomObjectFactory.createLineSegment(from, to, lineWidth, colorPalette.getGridColor());
            gridGroup.getChildren().add(lineSegment);
            x += gridIncrement;
        }

        // iterate over x dimension
        int xDivisions = depth / gridIncrement;
        double z = 0.0;
        for (int i = 0; i <= xDivisions; i++) {
            Point3D from = new Point3D(0, 0, z);
            Point3D to = new Point3D(width, 0, z);
            Node lineSegment = CustomObjectFactory.createLineSegment(from, to, lineWidth, colorPalette.getGridColor());
            gridGroup.getChildren().add(lineSegment);
            z += gridIncrement;
        }

        gridGroup.setVisible(true);
    }

    //////////////////


    ////////////////////  SCALE Group  /////////////////


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

    private void createScaleLegend(int scaleValue) {
        Text scaleText = new Text(String.format(scaleString, scaleValue));
        scaleText.setFont(Font.font("Verdana", 20));
        scaleText.setFill(colorPalette.getLegendColor());
        scaleGroup.getChildren().add(scaleText);
        scaleGroup.setTranslate(50, 350, 0);
        scaleGroup.setVisible(true);
        log.info("show scale");
    }


}
