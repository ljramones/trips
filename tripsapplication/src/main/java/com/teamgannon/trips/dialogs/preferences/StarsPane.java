package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.StarDescriptionPreference;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.listener.PreferencesUpdater;
import com.teamgannon.trips.stardata.StellarType;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class StarsPane extends Pane {
    private final static String STAR_PANE_TITLE = "Change Star Colors";
    private final static String STAR_PANE_TITLE_MODIFIED = "Change Star Colors - *modified*";
    private final PreferencesUpdater updater;
    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final StarDisplayPreferences starDisplayPreferences;

    private final TextField oClassRadiusTextField = new TextField();
    private final TextField bClassRadiusTextField = new TextField();
    private final TextField aClassRadiusTextField = new TextField();
    private final TextField fClassRadiusTextField = new TextField();
    private final TextField gClassRadiusTextField = new TextField();
    private final TextField kClassRadiusTextField = new TextField();
    private final TextField mClassRadiusTextField = new TextField();
    private final TextField lClassRadiusTextField = new TextField();
    private final TextField tClassRadiusTextField = new TextField();
    private final TextField yClassRadiusTextField = new TextField();

    private final TextField oClassColorTextField = new TextField();
    private final TextField bClassColorTextField = new TextField();
    private final TextField aClassColorTextField = new TextField();
    private final TextField fClassColorTextField = new TextField();
    private final TextField gClassColorTextField = new TextField();
    private final TextField kClassColorTextField = new TextField();
    private final TextField mClassColorTextField = new TextField();
    private final TextField lClassColorTextField = new TextField();
    private final TextField tClassColorTextField = new TextField();
    private final TextField yClassColorTextField = new TextField();

    private final ColorPicker oClassColorPicker = new ColorPicker();
    private final ColorPicker bClassColorPicker = new ColorPicker();
    private final ColorPicker aClassColorPicker = new ColorPicker();
    private final ColorPicker fClassColorPicker = new ColorPicker();
    private final ColorPicker gClassColorPicker = new ColorPicker();
    private final ColorPicker kClassColorPicker = new ColorPicker();
    private final ColorPicker mClassColorPicker = new ColorPicker();
    private final ColorPicker lClassColorPicker = new ColorPicker();
    private final ColorPicker tClassColorPicker = new ColorPicker();
    private final ColorPicker yClassColorPicker = new ColorPicker();

    private final TitledPane starPane;

    public StarsPane(StarDisplayPreferences starDisplayPreferences, PreferencesUpdater updater) {
        this.starDisplayPreferences = starDisplayPreferences;
        this.updater = updater;
        VBox vBox = new VBox();

        Pane pane1 = createStarPane(starDisplayPreferences.getStarMap());
        starPane = new TitledPane(STAR_PANE_TITLE, pane1);
        vBox.getChildren().add(starPane);

        this.getChildren().add(vBox);
    }

    private Pane createStarPane(Map<StellarType, StarDescriptionPreference> starMap) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        Label stellarTypeHead = new Label("Stellar Type");
        Label radiusHead = new Label("Radius (Sol Units)");
        Label colorValueHead = new Label("Color Value");
        Label colorPickerHead = new Label("Color Swatch");
        gridPane.add(stellarTypeHead, 0, 0);
        gridPane.add(radiusHead, 1, 0);
        gridPane.add(colorValueHead, 2, 0);
        gridPane.add(colorPickerHead, 3, 0);

        StarDescriptionPreference oStar = starMap.get(StellarType.O);
        Label oClassLabel = new Label("O Class:");
        oClassLabel.setFont(font);
        gridPane.add(oClassLabel, 0, 1);
        gridPane.add(oClassRadiusTextField, 1, 1);
        oClassRadiusTextField.setText(Float.toString(oStar.getSize()));
        gridPane.add(oClassColorTextField, 2, 1);
        oClassColorTextField.setText(oStar.getColor().toString());
        gridPane.add(oClassColorPicker, 3, 1);
        oClassColorPicker.setValue(oStar.getColor());
        oClassColorPicker.setOnAction(e -> {
            // color
            Color c = oClassColorPicker.getValue();
            // set text of the label to RGB value of color
            oClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference bStar = starMap.get(StellarType.B);
        Label bClassLabel = new Label("B Class:");
        bClassLabel.setFont(font);
        gridPane.add(bClassLabel, 0, 2);
        gridPane.add(bClassRadiusTextField, 1, 2);
        bClassRadiusTextField.setText(Float.toString(bStar.getSize()));
        gridPane.add(bClassColorTextField, 2, 2);
        bClassColorTextField.setText(bStar.getColor().toString());
        gridPane.add(bClassColorPicker, 3, 2);
        bClassColorPicker.setValue(bStar.getColor());
        bClassColorPicker.setOnAction(e -> {
            // color
            Color c = bClassColorPicker.getValue();
            // set text of the label to RGB value of color
            bClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference aStar = starMap.get(StellarType.A);
        Label aClassLabel = new Label("A Class:");
        aClassLabel.setFont(font);
        gridPane.add(aClassLabel, 0, 3);
        gridPane.add(aClassRadiusTextField, 1, 3);
        aClassRadiusTextField.setText(Float.toString(aStar.getSize()));
        gridPane.add(aClassColorTextField, 2, 3);
        aClassColorTextField.setText(aStar.getColor().toString());
        gridPane.add(aClassColorPicker, 3, 3);
        bClassColorPicker.setValue(bStar.getColor());
        aClassColorPicker.setOnAction(e -> {
            // color
            Color c = aClassColorPicker.getValue();
            // set text of the label to RGB value of color
            aClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference fStar = starMap.get(StellarType.F);
        Label fClassLabel = new Label("F Class:");
        fClassLabel.setFont(font);
        gridPane.add(fClassLabel, 0, 4);
        gridPane.add(fClassRadiusTextField, 1, 4);
        fClassRadiusTextField.setText(Float.toString(fStar.getSize()));
        gridPane.add(fClassColorTextField, 2, 4);
        fClassColorTextField.setText(fStar.getColor().toString());
        gridPane.add(fClassColorPicker, 3, 4);
        fClassColorPicker.setValue(fStar.getColor());
        fClassColorPicker.setOnAction(e -> {
            // color
            Color c = fClassColorPicker.getValue();
            // set text of the label to RGB value of color
            fClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference gStar = starMap.get(StellarType.G);
        Label gClassLabel = new Label("G Class:");
        gClassLabel.setFont(font);
        gridPane.add(gClassLabel, 0, 5);
        gridPane.add(gClassRadiusTextField, 1, 5);
        gClassRadiusTextField.setText(Float.toString(gStar.getSize()));
        gridPane.add(gClassColorTextField, 2, 5);
        gClassColorTextField.setText(gStar.getColor().toString());
        gridPane.add(gClassColorPicker, 3, 5);
        gClassColorPicker.setValue(gStar.getColor());
        gClassColorPicker.setOnAction(e -> {
            // color
            Color c = gClassColorPicker.getValue();
            // set text of the label to RGB value of color
            gClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference kStar = starMap.get(StellarType.K);
        Label kClassLabel = new Label("K Class:");
        kClassLabel.setFont(font);
        gridPane.add(kClassLabel, 0, 6);
        gridPane.add(kClassRadiusTextField, 1, 6);
        kClassRadiusTextField.setText(Float.toString(kStar.getSize()));
        gridPane.add(kClassColorTextField, 2, 6);
        kClassColorTextField.setText(kStar.getColor().toString());
        gridPane.add(kClassColorPicker, 3, 6);
        kClassColorPicker.setValue(kStar.getColor());
        kClassColorPicker.setOnAction(e -> {
            // color
            Color c = kClassColorPicker.getValue();
            // set text of the label to RGB value of color
            kClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference mStar = starMap.get(StellarType.M);
        Label mClassLabel = new Label("M Class:");
        mClassLabel.setFont(font);
        gridPane.add(mClassLabel, 0, 7);
        gridPane.add(mClassRadiusTextField, 1, 7);
        mClassRadiusTextField.setText(Float.toString(mStar.getSize()));
        gridPane.add(mClassColorTextField, 2, 7);
        mClassColorTextField.setText(mStar.getColor().toString());
        gridPane.add(mClassColorPicker, 3, 7);
        mClassColorPicker.setValue(mStar.getColor());
        mClassColorPicker.setOnAction(e -> {
            // color
            Color c = mClassColorPicker.getValue();
            // set text of the label to RGB value of color
            mClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference lStar = starMap.get(StellarType.L);
        Label lClassLabel = new Label("L Class:");
        lClassLabel.setFont(font);
        gridPane.add(lClassLabel, 0, 8);
        gridPane.add(lClassRadiusTextField, 1, 8);
        lClassRadiusTextField.setText(Float.toString(lStar.getSize()));
        gridPane.add(lClassColorTextField, 2, 8);
        lClassColorTextField.setText(lStar.getColor().toString());
        gridPane.add(lClassColorPicker, 3, 8);
        lClassColorPicker.setValue(lStar.getColor());
        lClassColorPicker.setOnAction(e -> {
            // color
            Color c = lClassColorPicker.getValue();
            // set text of the label to RGB value of color
            lClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference tStar = starMap.get(StellarType.T);
        Label tClassLabel = new Label("T Class:");
        tClassLabel.setFont(font);
        gridPane.add(tClassLabel, 0, 9);
        gridPane.add(tClassRadiusTextField, 1, 9);
        tClassRadiusTextField.setText(Float.toString(tStar.getSize()));
        gridPane.add(tClassColorTextField, 2, 9);
        tClassColorTextField.setText(tStar.getColor().toString());
        gridPane.add(tClassColorPicker, 3, 9);
        tClassColorPicker.setValue(tStar.getColor());
        tClassColorPicker.setOnAction(e -> {
            // color
            Color c = tClassColorPicker.getValue();
            // set text of the label to RGB value of color
            tClassColorTextField.setText(c.toString());
        });

        StarDescriptionPreference yStar = starMap.get(StellarType.Y);
        Label yClassLabel = new Label("Y Class:");
        yClassLabel.setFont(font);
        gridPane.add(yClassLabel, 0, 10);
        gridPane.add(yClassRadiusTextField, 1, 10);
        yClassRadiusTextField.setText(Float.toString(yStar.getSize()));
        gridPane.add(yClassColorTextField, 2, 10);
        yClassColorTextField.setText(yStar.getColor().toString());
        gridPane.add(yClassColorPicker, 3, 10);
        yClassColorPicker.setValue(yStar.getColor());
        yClassColorPicker.setOnAction(e -> {
            // color
            Color c = yClassColorPicker.getValue();
            // set text of the label to RGB value of color
            yClassColorTextField.setText(c.toString());
        });

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetColorsClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Change");
        addBtn.setOnAction(this::changeColorsClicked);
        hBox.getChildren().add(addBtn);

        gridPane.add(hBox, 0, 11, 3, 1);


        /////////////////////

        // set event listeners
        oClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                oClassFieldChanged();
            }
        });

        // set event listeners
        bClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                bClassFieldChanged();
            }
        });

        // set event listeners
        aClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                aClassFieldChanged();
            }
        });

        // set event listeners
        fClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                fClassFieldChanged();
            }
        });

        // set event listeners
        gClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                gClassFieldChanged();
            }
        });

        // set event listeners
        kClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                kClassFieldChanged();
            }
        });

        // set event listeners
        mClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                mClassFieldChanged();
            }
        });

        // set event listeners
        lClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                lClassFieldChanged();
            }
        });

        // set event listeners
        tClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                tClassFieldChanged();
            }
        });

        // set event listeners
        yClassColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                yClassFieldChanged();
            }
        });


        return gridPane;
    }

    private void changeColorsClicked(ActionEvent actionEvent) {
        Map<StellarType, StarDescriptionPreference> starMap = starDisplayPreferences.getStarMap();

        StarDescriptionPreference oStar = starMap.get(StellarType.O);
        oStar.setSize(Float.parseFloat(oClassRadiusTextField.getText()));
        oStar.setColor(oClassColorPicker.getValue());

        StarDescriptionPreference bStar = starMap.get(StellarType.B);
        bStar.setSize(Float.parseFloat(bClassRadiusTextField.getText()));
        bStar.setColor(bClassColorPicker.getValue());

        StarDescriptionPreference aStar = starMap.get(StellarType.A);
        aStar.setSize(Float.parseFloat(aClassRadiusTextField.getText()));
        aStar.setColor(aClassColorPicker.getValue());

        StarDescriptionPreference fStar = starMap.get(StellarType.F);
        fStar.setSize(Float.parseFloat(fClassRadiusTextField.getText()));
        fStar.setColor(fClassColorPicker.getValue());

        StarDescriptionPreference gStar = starMap.get(StellarType.G);
        gStar.setSize(Float.parseFloat(gClassRadiusTextField.getText()));
        gStar.setColor(gClassColorPicker.getValue());

        StarDescriptionPreference kStar = starMap.get(StellarType.K);
        kStar.setSize(Float.parseFloat(kClassRadiusTextField.getText()));
        kStar.setColor(kClassColorPicker.getValue());

        StarDescriptionPreference mStar = starMap.get(StellarType.M);
        mStar.setSize(Float.parseFloat(mClassRadiusTextField.getText()));
        mStar.setColor(mClassColorPicker.getValue());

        StarDescriptionPreference lStar = starMap.get(StellarType.L);
        lStar.setSize(Float.parseFloat(lClassRadiusTextField.getText()));
        lStar.setColor(lClassColorPicker.getValue());

        StarDescriptionPreference tStar = starMap.get(StellarType.T);
        tStar.setSize(Float.parseFloat(tClassRadiusTextField.getText()));
        tStar.setColor(oClassColorPicker.getValue());

        StarDescriptionPreference yStar = starMap.get(StellarType.Y);
        yStar.setSize(Float.parseFloat(yClassRadiusTextField.getText()));
        yStar.setColor(yClassColorPicker.getValue());

        updater.changeStarPreferences(starDisplayPreferences);

    }

    private void resetColorsClicked(ActionEvent actionEvent) {
        starDisplayPreferences.reset();

        // reset O class
        StarDescriptionPreference oStar = starDisplayPreferences.getStarMap().get(StellarType.O);
        oClassRadiusTextField.setText(Float.toString(oStar.getSize()));
        oClassColorTextField.setText(oStar.getColor().toString());
        oClassColorPicker.setValue(oStar.getColor());

        // reset B class
        StarDescriptionPreference bStar = starDisplayPreferences.getStarMap().get(StellarType.B);
        bClassRadiusTextField.setText(Float.toString(bStar.getSize()));
        bClassColorTextField.setText(bStar.getColor().toString());
        bClassColorPicker.setValue(bStar.getColor());

        // reset A class
        StarDescriptionPreference aStar = starDisplayPreferences.getStarMap().get(StellarType.A);
        aClassRadiusTextField.setText(Float.toString(aStar.getSize()));
        aClassColorTextField.setText(aStar.getColor().toString());
        aClassColorPicker.setValue(aStar.getColor());

        // reset F class
        StarDescriptionPreference fStar = starDisplayPreferences.getStarMap().get(StellarType.F);
        fClassRadiusTextField.setText(Float.toString(fStar.getSize()));
        fClassColorTextField.setText(fStar.getColor().toString());
        fClassColorPicker.setValue(fStar.getColor());

        // reset G class
        StarDescriptionPreference gStar = starDisplayPreferences.getStarMap().get(StellarType.G);
        gClassRadiusTextField.setText(Float.toString(gStar.getSize()));
        gClassColorTextField.setText(gStar.getColor().toString());
        gClassColorPicker.setValue(gStar.getColor());

        // reset K class
        StarDescriptionPreference kStar = starDisplayPreferences.getStarMap().get(StellarType.K);
        kClassRadiusTextField.setText(Float.toString(kStar.getSize()));
        kClassColorTextField.setText(kStar.getColor().toString());
        kClassColorPicker.setValue(kStar.getColor());

        // reset M class
        StarDescriptionPreference mStar = starDisplayPreferences.getStarMap().get(StellarType.M);
        mClassRadiusTextField.setText(Float.toString(mStar.getSize()));
        mClassColorTextField.setText(mStar.getColor().toString());
        mClassColorPicker.setValue(mStar.getColor());

        // reset L class
        StarDescriptionPreference lStar = starDisplayPreferences.getStarMap().get(StellarType.L);
        lClassRadiusTextField.setText(Float.toString(lStar.getSize()));
        lClassColorTextField.setText(lStar.getColor().toString());
        lClassColorPicker.setValue(lStar.getColor());

        // reset T class
        StarDescriptionPreference tStar = starDisplayPreferences.getStarMap().get(StellarType.T);
        tClassRadiusTextField.setText(Float.toString(tStar.getSize()));
        tClassColorTextField.setText(tStar.getColor().toString());
        tClassColorPicker.setValue(tStar.getColor());

        // reset Y class
        StarDescriptionPreference yStar = starDisplayPreferences.getStarMap().get(StellarType.Y);
        yClassRadiusTextField.setText(Float.toString(yStar.getSize()));
        yClassColorTextField.setText(yStar.getColor().toString());
        yClassColorPicker.setValue(yStar.getColor());

    }

    private void aClassFieldChanged() {
        try {
            Color color = Color.valueOf(aClassColorTextField.getText());
            aClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", aClassColorTextField.getText());
            showErrorAlert("Change A Class Color", aClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void fClassFieldChanged() {
        try {
            Color color = Color.valueOf(fClassColorTextField.getText());
            fClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", fClassColorTextField.getText());
            showErrorAlert("Change F Class Color", fClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void gClassFieldChanged() {
        try {
            Color color = Color.valueOf(gClassColorTextField.getText());
            gClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", gClassColorTextField.getText());
            showErrorAlert("Change G Class Color", gClassColorTextField.getText() + " is an invalid color");
        }
    }


    private void kClassFieldChanged() {
        try {
            Color color = Color.valueOf(kClassColorTextField.getText());
            kClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", kClassColorTextField.getText());
            showErrorAlert("Change K Class Color", kClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void mClassFieldChanged() {
        try {
            Color color = Color.valueOf(mClassColorTextField.getText());
            mClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", mClassColorTextField.getText());
            showErrorAlert("Change M Class Color", mClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void lClassFieldChanged() {
        try {
            Color color = Color.valueOf(lClassColorTextField.getText());
            lClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", lClassColorTextField.getText());
            showErrorAlert("Change L Class Color", lClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void tClassFieldChanged() {
        try {
            Color color = Color.valueOf(tClassColorTextField.getText());
            tClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", tClassColorTextField.getText());
            showErrorAlert("Change T Class Color", tClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void yClassFieldChanged() {
        try {
            Color color = Color.valueOf(yClassColorTextField.getText());
            yClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", yClassColorTextField.getText());
            showErrorAlert("Change Y Class Color", yClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void bClassFieldChanged() {
        try {
            Color color = Color.valueOf(bClassColorTextField.getText());
            bClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", bClassColorTextField.getText());
            showErrorAlert("Change B Class Color", bClassColorTextField.getText() + " is an invalid color");
        }
    }

    private void oClassFieldChanged() {
        try {
            Color color = Color.valueOf(oClassColorTextField.getText());
            oClassColorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", oClassColorTextField.getText());
            showErrorAlert("Change O Class Color", oClassColorTextField.getText() + " is an invalid color");
        }
    }


    private Pane starsDefinitionPane(StarDisplayPreferences starDisplayPreferences) {
        GridPane pane = new GridPane();

        pane.add(new Separator(), 1, 1, 1, starDisplayPreferences.getStarMap().size() + 1);

        int i = 0;
        for (StellarType starType : starDisplayPreferences.getStarMap().keySet()) {
            createStarLine(pane, 3 + i, starDisplayPreferences.getStarMap().get(starType));
        }

        return pane;
    }

    private void createStarLine(GridPane pane, int row, StarDescriptionPreference starDescriptionPreference) {
        pane.add(
                new Label(
                        String.format("Stellar Class %s:", starDescriptionPreference.getStarClass().getValue())),
                2, row);

        ColorPicker gridColorPicker = new ColorPicker(starDescriptionPreference.getColor());
        pane.add(gridColorPicker, 3, row);
        TextField link1 = new TextField(Float.toString(starDescriptionPreference.getSize()));
        pane.add(link1, 4, row);
    }

}
