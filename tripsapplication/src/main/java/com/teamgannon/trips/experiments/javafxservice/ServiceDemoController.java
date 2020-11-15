package com.teamgannon.trips.experiments.javafxservice;


import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

/**
 * @author jdeters
 */
public class ServiceDemoController extends Pane {

    private final static Logger LOGGER = Logger.getLogger(ServiceDemoController.class.getName());

    private Button disconnectButton = new Button("Disconnect");

    private Button cancelButton = new Button("Cancel");

    private Label messagesLabel = new Label("statusMessageLabel");

    private ProgressIndicator serviceRunningIndicator = new ProgressIndicator();

    private Button connectButton = new Button("Connect");

    private ConnectService connectService;
    private DisconnectService disconnectService;

    private StringProperty statusMessagesProperty;
    private BooleanProperty connectedProperty;

    public ServiceDemoController() {

        VBox vBox = new VBox();
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(20.0, 20.0, 20.0, 20.0));
        hBox.getChildren().add(connectButton);
        connectButton.setOnAction(this::connect);
        hBox.getChildren().add(disconnectButton);
        disconnectButton.setOnAction(this::disconnect);

        hBox.getChildren().add(messagesLabel);
        hBox.getChildren().add(cancelButton);
        cancelButton.setOnAction(this::cancel);
        hBox.getChildren().add(serviceRunningIndicator);
        vBox.getChildren().add(hBox);
        this.getChildren().add(vBox);

    }

    private void cancel(ActionEvent actionEvent) {
        LOGGER.info("cancel");
        connectService.cancel();
        disconnectService.cancel();
    }


    public void initialize() {
        connectService = new ConnectService();
        disconnectService = new DisconnectService();

        BooleanBinding anyServiceRunning = connectService.runningProperty().or(disconnectService.runningProperty());
        serviceRunningIndicator.visibleProperty().bind(anyServiceRunning);
        cancelButton.visibleProperty().bind(anyServiceRunning);
        connectButton.disableProperty().bind(connectedProperty().or(anyServiceRunning));
        disconnectButton.disableProperty().bind(connectedProperty().not().or(anyServiceRunning));
        messagesLabel.textProperty().bind(statusMessagesProperty());

        connectService.messageProperty().addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
            statusMessagesProperty().set(newValue);
        });
        disconnectService.messageProperty().addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
            statusMessagesProperty().set(newValue);
        });

        statusMessagesProperty().set("Disconnected.");
    }

    public void connect(ActionEvent actionEvent) {
        LOGGER.info("connect");
        disconnectService.cancel();
        connectService.restart();
    }

    public void disconnect(ActionEvent actionEvent) {
        LOGGER.info("disconnect");
        connectService.cancel();
        disconnectService.restart();
    }

    private StringProperty statusMessagesProperty() {
        if (statusMessagesProperty == null) {
            statusMessagesProperty = new SimpleStringProperty();
        }
        return statusMessagesProperty;
    }

    private BooleanProperty connectedProperty() {
        if (connectedProperty == null) {
            connectedProperty = new SimpleBooleanProperty(Boolean.FALSE);
        }
        return connectedProperty;
    }

    private class ConnectService extends Service<Void> {

        @Override
        protected void succeeded() {
            statusMessagesProperty().set("Connected.");
            connectedProperty().set(true);
        }

        @Override
        protected void failed() {
            statusMessagesProperty().set("Connecting failed.");
            LOGGER.severe(getException().getMessage());
            connectedProperty().set(false);
        }

        @Override
        protected void cancelled() {
            statusMessagesProperty().set("Connecting cancelled.");
            connectedProperty().set(false);
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("  Connecting....  ");
                    Thread.sleep(1000);
                    updateMessage(" 1 second ");
                    Thread.sleep(1000);
                    updateMessage(" 2 second ");
                    Thread.sleep(1000);
                    updateMessage(" 3 second ");
                    Thread.sleep(1000);
                    updateMessage(" 4 second ");
                    Thread.sleep(1000);
                    updateMessage(" 5 second ");
                    // DEMO: uncomment to provoke "Not on FX application thread"-Exception:
                    // connectButton.setVisible(false);
                    updateMessage("Waiting for server feedback.");
                    Thread.sleep(3000);
                    return null;
                }
            };
        }

    }

    private class DisconnectService extends Service<Void> {

        @Override
        protected void succeeded() {
            statusMessagesProperty().set("");
            connectedProperty().set(false);
        }

        @Override
        protected void cancelled() {
            statusMessagesProperty().set("Disconnecting cancelled.");
            connectedProperty().set(false);
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Disconnecting....");
                    Thread.sleep(5000);
                    updateMessage("Waiting for server feedback.");
                    Thread.sleep(5000);
                    return null;
                }
            };
        }

    }

}
