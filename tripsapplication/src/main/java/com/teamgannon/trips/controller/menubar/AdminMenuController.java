package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.dialogs.sesame.SesameNameResolverDialog;
import javafx.event.ActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the Admin menu.
 * Handles administrative functions like external service lookups.
 */
@Slf4j
@Component
public class AdminMenuController {

    /**
     * Opens the Sesame Name Resolver dialog to look up astronomical objects.
     */
    public void findInSesame(ActionEvent actionEvent) {
        try {
            SesameNameResolverDialog dialog = new SesameNameResolverDialog();
            Optional<List<String>> resultOpt = dialog.showAndWait();
            if (resultOpt.isPresent()) {
                log.info("Sesame lookup completed with {} results", resultOpt.get().size());
            }
        } catch (Exception e) {
            log.error("Error opening Sesame Name Resolver dialog", e);
            showErrorAlert("Sesame Lookup", "Failed to open dialog: " + e.getMessage());
        }
    }
}
