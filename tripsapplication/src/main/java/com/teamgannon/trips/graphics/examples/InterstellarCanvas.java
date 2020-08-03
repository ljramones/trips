package com.teamgannon.trips.graphics.examples;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

/**
 * Used to create an animation sequence on the canvas that displays the stars
 * <p>
 * Created by larrymitchell on 2017-01-29.
 */
public class InterstellarCanvas extends Canvas {

    public Runnable setup;
    public Runnable draw;
    protected double mouseX;
    protected double mouseY;
    protected boolean mousePressed = false;
    protected long startFrameCount = System.currentTimeMillis();
    protected long frameCount = System.currentTimeMillis();
    private AnimationTimer animationTimer;

    public InterstellarCanvas() {
        this.init();
    }

    public InterstellarCanvas(double width, double height) {
        super(width, height);
        this.init();
    }

    private void init() {
        this.addEventFilter(MouseEvent.ANY, e -> {
                    if (e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                        this.mouseX = e.getX();
                        this.mouseY = e.getY();
                    } else if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        this.mousePressed = true;
                    } else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
                        this.mousePressed = false;
                    }
                }
        );
        this.animationTimer = new AnimationTimer() {

            public void handle(long now) {
                ++InterstellarCanvas.this.frameCount;
                Optional.ofNullable(draw).ifPresent(Runnable::run);
            }
        };
    }


    public final void start() {
        this.animationTimer.start();
    }

    public final void stop() {
        this.animationTimer.stop();
    }

}
