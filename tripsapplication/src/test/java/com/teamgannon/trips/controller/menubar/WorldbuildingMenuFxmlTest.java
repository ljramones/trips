package com.teamgannon.trips.controller.menubar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lightweight static check that the Worldbuilding menu FXML carries the Installations Designer entry
 * with the prompt-pinned mnemonic on the letter {@code I}. Reads the FXML resource as text rather
 * than constructing a JavaFX scene — that's enough to detect drift between the menu file and the
 * v2 Phase C plan, and it avoids the {@code Platform.startup} bootstrap.
 */
class WorldbuildingMenuFxmlTest {

    private static final Path WORLDBUILDING_MENU_FXML = Path.of(
            "src/main/resources/com/teamgannon/trips/controller/menubar/WorldbuildingMenu.fxml");

    @Test
    @DisplayName("WorldbuildingMenu.fxml contains the Installations Designer menu item with mnemonic I")
    void worldbuildingMenuHasInstallationDesignerItem() throws IOException {
        String fxml = Files.readString(WORLDBUILDING_MENU_FXML);
        assertTrue(fxml.contains("openInstallationDesigner"),
                "FXML must wire #openInstallationDesigner to the menu item");
        assertTrue(fxml.contains("_Installations Designer..."),
                "FXML must use mnemonic-prefixed text '_Installations Designer...' (mnemonic I)");
        assertTrue(fxml.contains("mnemonicParsing=\"true\""),
                "FXML must enable mnemonicParsing so the underscore registers as a mnemonic");
    }
}
