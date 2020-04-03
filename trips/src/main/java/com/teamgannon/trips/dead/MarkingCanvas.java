package com.teamgannon.trips.dead;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * the marking canvas
 * <p>
 * Created by larrymitchell on 2017-01-31.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class MarkingCanvas extends Canvas {

    private boolean displayGrid = false;

    private boolean displayScale = false;

    private double spacing = 50;

    private String scale = "Scale: 1 grid=3 ly";

    private GraphicsContext gc;

    /**
     * Creates an empty instance of Canvas.
     */
    public MarkingCanvas() {
    }

    /**
     * Creates a new instance of Canvas with the given size.
     *
     * @param width  width of the canvas
     * @param height height of the canvas
     */
    public MarkingCanvas(double width, double height) {
        super(width, height);
        gc = this.getGraphicsContext2D();
    }

    public void showScale() {
        if (displayScale) {
            gc.setFont(new Font(10));
            gc.setFill(Color.WHITE);
            gc.fillText(scale, 670, 670);
        }
    }

    public void clearScale() {
        Text text = new Text(scale);
        text.setFont(new Font(10));
        double width = text.getBoundsInLocal().getWidth();
        double height = text.getBoundsInLocal().getHeight();
        gc.clearRect(670, 660, width, height);
    }

    /**
     * draw a grid if the grid flag is set
     */
    public void drawGrid() {
        if (displayGrid) {

            final int width = (int) getWidth();
            final int height = (int) getHeight();

            final int hLineCount = (int) Math.floor((height + 1) / spacing);
            final int vLineCount = (int) Math.floor((width + 1) / spacing);
            gc.setStroke(Color.WHITE);
            for (int i = 0; i < hLineCount; i++) {
                gc.strokeLine(0, snap((i + 1) * spacing), width, snap((i + 1) * spacing));
            }

            gc.setStroke(Color.WHITE);
            for (int i = 0; i < vLineCount; i++) {
                gc.strokeLine(snap((i + 1) * spacing), 0, snap((i + 1) * spacing), height);
            }
        }
    }

    /**
     * clear the grid
     */
    public void clearGrid() {
        displayGrid = false;
        // clear this canvas of any drawing
        gc.clearRect(0, 0, getWidth(), getHeight());
    }

    public void toggleScale() {
        if (!displayScale) {
            displayScale = true;
            showScale();
        } else {
            displayScale = false;
            clearScale();
        }
    }

    /**
     * toggle whether the grid is show
     */
    public void toggleGrid() {
        if (!displayGrid) {
            displayGrid = true;
            drawGrid();
        } else {
            displayGrid = false;
            clearGrid();
        }
    }

    private double snap(double y) {
        return ((int) y) + 0.5;
    }

}
