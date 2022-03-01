package com.teamgannon.trips.dialogs.search;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Uses a combobox tooltip as the suggestion for auto complete and updates the
 * combo box items accordingly <br />
 * It does not work with space, space and escape cause the combobox to hide and
 * clean the filter ... Send me a PR if you want it to work with all characters
 * -> It should be a custom controller - I KNOW!
 *
 * @param <T>
 * @author wsiqueir
 */
@Slf4j
public class ComboBoxAutoComplete<T> {

    String filter = "";
    private final Stage stage;
    private final ComboBox<T> cmb;
    private final ObservableList<T> originalItems;

    public ComboBoxAutoComplete(Stage stage,
                                @NotNull ComboBox<T> cmb) {
        this.stage = stage;
        this.cmb = cmb;
        originalItems = FXCollections.observableArrayList(cmb.getItems());
        cmb.setOnKeyPressed(this::handleOnKeyPressed);
        cmb.setOnHidden(this::handleOnHiding);
    }

    public void handleOnKeyPressed(@NotNull KeyEvent e) {
        ObservableList<T> filteredList = FXCollections.observableArrayList();
        KeyCode code = e.getCode();

        if (code == KeyCode.MINUS) {
            String txt = e.getText();
            filter += txt;
        }

        if (code.isKeypadKey()) {
            String txt = e.getText();
            filter += txt;
        }

        if (code.isDigitKey()) {
            String txt = e.getText();
            filter += txt;
        }

        if (code.isLetterKey()) {
            String txt = e.getText();
            filter += txt;
        }

        if (code == KeyCode.BACK_SPACE && filter.length() > 0) {
            filter = filter.substring(0, filter.length() - 1);
            cmb.getItems().setAll(originalItems);
        }

        if (code == KeyCode.ESCAPE) {
            filter = "";
        }

        if (filter.length() == 0) {
            filteredList = originalItems;
            cmb.getTooltip().hide();
        } else {
            Stream<T> items = cmb.getItems().stream();
            String txtUsr = unAccent(filter.toLowerCase());
            items.filter(el -> unAccent(el.toString().toLowerCase()).contains(txtUsr)).forEach(filteredList::add);
            cmb.getTooltip().setText(txtUsr);
            double posX = stage.getX() + cmb.getBoundsInParent().getMinX();
            double posY = stage.getY() + cmb.getBoundsInParent().getMinY();
            cmb.getTooltip().show(stage, posX, posY);
            cmb.show();
        }
        log.info(filteredList.toString());
        cmb.getItems().setAll(filteredList);
    }

    public void handleOnHiding(Event e) {
        filter = "";
        cmb.getTooltip().hide();
        T s = cmb.getSelectionModel().getSelectedItem();
        cmb.getItems().setAll(originalItems);
        cmb.getSelectionModel().select(s);
    }

    private String unAccent(@NotNull String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

}
