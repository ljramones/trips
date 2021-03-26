package com.teamgannon.trips.routing.example;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TreeViewWithPlaceholder extends Application {


    private static class TreeViewPlaceholderSkin<T> extends TreeViewSkin<T> {

        private StackPane placeholderRegion;
        private Label placeholderLabel;

        public TreeViewPlaceholderSkin(TreeView<T> control) {
            super(control);
            installPlaceholderSupport();
        }

        private void installPlaceholderSupport() {
            registerChangeListener(getSkinnable().rootProperty(), e -> updatePlaceholderSupport());
            updatePlaceholderSupport();
        }

        /**
         * Updating placeholder/flow visibilty depending on whether or not the tree
         * is considered empty.
         *
         * Basically copied from TableViewSkinBase.
         */
        private void updatePlaceholderSupport() {
            if (isTreeEmpty()) {
                if (placeholderRegion == null) {
                    placeholderRegion = new StackPane();
                    placeholderRegion.getStyleClass().setAll("placeholder");
                    getChildren().add(placeholderRegion);

                    placeholderLabel = new Label("No treeItems");
                    placeholderRegion.getChildren().setAll(placeholderLabel);
                }
            }
            getVirtualFlow().setVisible(!isTreeEmpty());
            if (placeholderRegion != null)
                placeholderRegion.setVisible(isTreeEmpty());
        }


        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);
            if (placeholderRegion != null && placeholderRegion.isVisible()) {
                placeholderRegion.resizeRelocate(x, y, w, h);
            }
        }

        private boolean isTreeEmpty() {
            return getSkinnable().getRoot() == null;
        }

    }

    private Parent createContent() {
        TreeView<String> tree = new TreeView<>() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new TreeViewPlaceholderSkin<>(this);
            }

        };

        Button toggle = new Button("toggleRoot");
        toggle.setOnAction(e -> {
            TreeItem<String> root = tree.getRoot();
            tree.setRoot(root == null ? new TreeItem<>("root") : null);
        });
        BorderPane content = new BorderPane(tree);
        content.setBottom(toggle);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        //stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }



}