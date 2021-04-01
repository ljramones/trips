package com.teamgannon.trips.objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MeshViewShapeFactory {

    private final FXMLLoader fxmlLoader = new FXMLLoader();

    public Group starCentral() {
        try {

            fxmlLoader.setLocation(this.getClass().getResource("centralStar.fxml"));
            Group highlightStar = fxmlLoader.load();

            log.info("highlight star loaded");
            return highlightStar;
        } catch (IOException e) {
            // exception handling
            log.error("failed to laod the star:" + e.getMessage());
            return null;
        }
    }

    public Group star4pt() {
        try {

            fxmlLoader.setLocation(this.getClass().getResource("star4pt.fxml"));
            Group highlightStar = fxmlLoader.load();

            log.info("highlight star loaded");
            return highlightStar;
        } catch (IOException e) {
            // exception handling
            log.error("failed to laod the star:" + e.getMessage());
            return null;
        }
    }


    public Group star5pt() {
        try {
            fxmlLoader.setLocation(this.getClass().getResource("star5pt.fxml"));
            Group highlightStar = fxmlLoader.load();

            log.info("highlight star loaded");
            return highlightStar;
        } catch (IOException e) {
            // exception handling
            log.error("failed to laod the star:" + e.getMessage());
            return null;
        }
    }

    public Group starMoravian() {
        try {
            fxmlLoader.setLocation(this.getClass().getResource("moravian.fxml"));
            Group highlightStar = fxmlLoader.load();

            log.info("highlight star loaded");
            return highlightStar;
        } catch (IOException e) {
            // exception handling
            log.error("failed to laod the star:" + e.getMessage());
            return null;
        }
    }

    public MeshView pyramid() {
        try {
            fxmlLoader.setLocation(this.getClass().getResource("pyramid.fxml"));
            MeshView pyramidModel = fxmlLoader.load();
            log.info("loaded");
            return pyramidModel;
        }
        catch (IOException e) {
            // exception handling
            log.error("fail:"+e.getMessage());
            return null;
        }
    }

}
