package com.teamgannon.trips.objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MeshViewShapeFactory {

    public Group starCentral() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("centralStar.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }

    public Group star4pt() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("star4pt.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }


    public Group star5pt() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("star5pt.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }

    public Group starMoravian() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("moravian.fxml"));
            return fxmlLoader.load();
        } catch (IOException e) {
            // exception handling
            log.error("failed to load the star:" + e.getMessage());
            return null;
        }
    }

    public MeshView pyramid() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource("pyramid.fxml"));
            MeshView pyramidModel = fxmlLoader.load();
            log.info("loaded");
            return pyramidModel;
        } catch (IOException e) {
            // exception handling
            log.error("fail:" + e.getMessage());
            return null;
        }
    }

}
