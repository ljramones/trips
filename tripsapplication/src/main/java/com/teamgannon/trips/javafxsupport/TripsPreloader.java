package com.teamgannon.trips.javafxsupport;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TripsPreloader extends Preloader {

    private Stage preloaderStage;

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;

        VBox loading = new VBox(20);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Terran Republic Interstellar \nPlotting System (TRIPS)");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 20);
        titleLabel.setFont(font);
        hBox1.getChildren().add(titleLabel);
        loading.getChildren().add(hBox1);

        HBox hBox2 = new HBox();
        final ImageView selectedImage = new ImageView();
        Image image1 = loadImageWithFallback("/images/CTR_emblem_metal.jpg");
        if (image1 != null) {
            selectedImage.setImage(image1);
            hBox2.getChildren().add(selectedImage);
        }
        loading.getChildren().addAll(hBox2);

        loading.setMaxWidth(Region.USE_PREF_SIZE);
        loading.setMaxHeight(Region.USE_PREF_SIZE);

        HBox hBox3 = new HBox();
        hBox3.setAlignment(Pos.CENTER);
        hBox3.getChildren().add(new ProgressBar());
        loading.getChildren().add(hBox3);

        HBox hBox4 = new HBox();
        hBox4.setAlignment(Pos.CENTER);
        hBox4.getChildren().add(new Label("Please wait..."));
        loading.getChildren().add(hBox4);

        HBox hBox5 = new HBox();
        hBox5.setAlignment(Pos.CENTER);
        hBox5.getChildren().add(new Label("Version " + getVersionString()));
        loading.getChildren().add(hBox5);

        BorderPane root = new BorderPane(loading);
        Scene scene = new Scene(root);

        primaryStage.setWidth(900);
        primaryStage.setHeight(800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Load version information from properties file.
     * Falls back to "unknown" if properties file cannot be read.
     */
    private String getVersionString() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            if (is != null) {
                props.load(is);
                String version = props.getProperty("app.version", "unknown");
                String releaseDate = props.getProperty("app.releaseDate", "");
                if (!releaseDate.isEmpty()) {
                    return version + " - " + releaseDate;
                }
                return version;
            }
        } catch (IOException e) {
            // Fall through to default
        }
        return "unknown";
    }

    /**
     * Load image with fallback handling if resource is missing.
     */
    private Image loadImageWithFallback(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            // Fall through to return null
        }
        return null;
    }

    /**
     * Catch and handle state change notifications.
     *
     * @param stateChangeNotification the state notification
     */
    public void handleStateChangeNotification(@NotNull StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}
