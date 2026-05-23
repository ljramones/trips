package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferBody;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferEstimate;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.List;

import static com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels.get;

/**
 * A mission-transfer preview for a ship between two named bodies.
 * <p>
 * The user picks a ship, an origin and a destination body (by name), and a central star mass; the dialog
 * shows a live {@link TransferEstimate} from the {@link TransferPlannerBridge}: required vs available
 * delta-V, transfer time, propellant, a rough burn time, and feasibility. It is informational only
 * ({@code Dialog<Void>} with a Close button).
 * <p>
 * Two entry points share one implementation: the Spaceship Modeller opens it for a single ship against
 * Solar-System presets; the Solar System view opens it with the system's planets as the body list and the
 * clicked body pre-selected as origin.
 */
public class TransferPreviewDialog extends Dialog<Void> {

    private static final List<TransferBody> SOL_BODIES = List.of(
            new TransferBody("Mercury", 0.39), new TransferBody("Venus", 0.72), new TransferBody("Earth", 1.0),
            new TransferBody("Mars", 1.52), new TransferBody("Jupiter", 5.20), new TransferBody("Saturn", 9.58),
            new TransferBody("Uranus", 19.20), new TransferBody("Neptune", 30.10));

    private final TransferPlannerBridge bridge;

    private final ComboBox<SpaceshipDesign> shipCombo = new ComboBox<>();
    private final ComboBox<TransferBody> originCombo = new ComboBox<>();
    private final ComboBox<TransferBody> destCombo = new ComboBox<>();
    private final TextField starMassField = new TextField();

    private final Label routeValue = new Label();
    private final Label requiredDvValue = new Label();
    private final Label shipDvValue = new Label();
    private final Label marginValue = new Label();
    private final Label timeValue = new Label();
    private final Label propellantValue = new Label();
    private final Label burnValue = new Label();
    private final Label feasibleValue = new Label();

    /**
     * Ship-centric entry point (Spaceship Modeller): one fixed ship against Solar-System presets.
     *
     * @param ship   the ship to plan for
     * @param bridge the transfer bridge
     */
    public TransferPreviewDialog(SpaceshipDesign ship, TransferPlannerBridge bridge) {
        this(bridge, List.of(ship), SOL_BODIES, new TransferBody("Earth", 1.0), 1.0);
    }

    /**
     * Full entry point (Solar System view): pick from a ship library and a body list.
     *
     * @param bridge               the transfer bridge
     * @param ships                ships to choose from (first is pre-selected)
     * @param bodies               candidate origin/destination bodies (falls back to Solar presets if empty)
     * @param origin               body to pre-select as origin (may be {@code null})
     * @param centralStarMassSolar central star mass, in solar masses
     */
    public TransferPreviewDialog(TransferPlannerBridge bridge,
                                 List<SpaceshipDesign> ships,
                                 List<TransferBody> bodies,
                                 TransferBody origin,
                                 double centralStarMassSolar) {
        this.bridge = bridge;
        List<TransferBody> bodyList = (bodies == null || bodies.isEmpty()) ? SOL_BODIES : bodies;

        setTitle(get("transfer.title", "Plan Mission Transfer"));
        getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);

        shipCombo.getItems().setAll(ships);
        shipCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SpaceshipDesign d) {
                return d == null ? "" : d.name() + " (" + d.driveType().name() + ")";
            }

            @Override
            public SpaceshipDesign fromString(String s) {
                return null;
            }
        });
        if (!ships.isEmpty()) {
            shipCombo.setValue(ships.get(0));
        }

        originCombo.getItems().setAll(bodyList);
        destCombo.getItems().setAll(bodyList);
        starMassField.setText(Double.toString(centralStarMassSolar));

        TransferBody originSel = (origin != null && bodyList.contains(origin)) ? origin : bodyList.get(0);
        originCombo.setValue(originSel);
        destCombo.setValue(firstDifferent(bodyList, originSel));

        shipCombo.valueProperty().addListener((o, a, b) -> recompute());
        originCombo.valueProperty().addListener((o, a, b) -> recompute());
        destCombo.valueProperty().addListener((o, a, b) -> recompute());
        starMassField.textProperty().addListener((o, a, b) -> recompute());

        getDialogPane().setContent(buildContent());
        getDialogPane().setPrefWidth(500);
        recompute();
        setResultConverter(bt -> null);
    }

    private static TransferBody firstDifferent(List<TransferBody> list, TransferBody origin) {
        for (TransferBody b : list) {
            if (!b.equals(origin)) {
                return b;
            }
        }
        return origin;
    }

    private VBox buildContent() {
        GridPane inputs = grid();
        int r = 0;
        inputs.add(new Label(get("transfer.shipPicker", "Ship") + ":"), 0, r);
        inputs.add(shipCombo, 1, r++);
        inputs.add(new Label(get("transfer.origin", "Origin") + ":"), 0, r);
        inputs.add(originCombo, 1, r++);
        inputs.add(new Label(get("transfer.destination", "Destination") + ":"), 0, r);
        inputs.add(destCombo, 1, r++);
        inputs.add(new Label(get("transfer.starMass", "Central star mass (Msun)") + ":"), 0, r);
        inputs.add(starMassField, 1, r++);

        GridPane results = grid();
        int q = 0;
        addResult(results, q++, get("transfer.route", "Route"), routeValue);
        addResult(results, q++, get("transfer.requiredDv", "Required Δv"), requiredDvValue);
        addResult(results, q++, get("transfer.shipDv", "Ship Δv"), shipDvValue);
        addResult(results, q++, get("transfer.margin", "Δv margin"), marginValue);
        addResult(results, q++, get("transfer.time", "Transfer time"), timeValue);
        addResult(results, q++, get("transfer.propellant", "Propellant (req / avail)"), propellantValue);
        addResult(results, q++, get("transfer.burn", "Approx. burn time"), burnValue);
        feasibleValue.setStyle("-fx-font-weight: bold;");
        addResult(results, q++, get("transfer.feasible", "Feasibility"), feasibleValue);

        VBox box = new VBox(10, inputs, new Separator(), results);
        box.setPadding(new Insets(14));
        return box;
    }

    private void recompute() {
        SpaceshipDesign ship = shipCombo.getValue();
        TransferBody origin = originCombo.getValue();
        TransferBody dest = destCombo.getValue();
        if (ship == null || origin == null || dest == null) {
            routeValue.setText(get("transfer.noShips", "Select a ship and bodies"));
            for (Label l : List.of(requiredDvValue, shipDvValue, marginValue, timeValue,
                    propellantValue, burnValue, feasibleValue)) {
                l.setText("");
            }
            return;
        }
        double starMass = parse(starMassField, 1.0);
        TransferEstimate e = bridge.estimateTransfer(
                origin.semiMajorAxisAu(), dest.semiMajorAxisAu(), starMass, ship);

        routeValue.setText(origin.name() + " → " + dest.name());
        requiredDvValue.setText("%.2f km/s".formatted(e.requiredDeltaVKmps()));
        shipDvValue.setText(formatDeltaV(e.shipDeltaVKmps()));
        marginValue.setText(formatDeltaV(e.deltaVMarginKmps()));
        timeValue.setText("%.0f days".formatted(e.transferTimeDays()));
        propellantValue.setText(formatTons(e.propellantRequiredTons()) + " / "
                + formatTons(e.propellantAvailableTons()));
        burnValue.setText(formatDuration(e.burnTimeSeconds()));

        if (e.feasible()) {
            feasibleValue.setText(get("transfer.feasible.yes", "Feasible"));
            feasibleValue.setTextFill(Color.web("#1e8449"));
        } else {
            feasibleValue.setText(get("transfer.feasible.no", "Insufficient Δv"));
            feasibleValue.setTextFill(Color.web("#c0392b"));
        }
    }

    // -------------------------------------------------------------- helpers

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(6);
        return g;
    }

    private static void addResult(GridPane g, int row, String label, Label value) {
        Label l = new Label(label + ":");
        l.setStyle("-fx-text-fill: #555;");
        g.add(l, 0, row);
        g.add(value, 1, row);
    }

    private static String formatDeltaV(double kmps) {
        return Double.isNaN(kmps) ? "n/a" : "%.0f km/s".formatted(kmps);
    }

    private static String formatTons(double tons) {
        return Double.isNaN(tons) ? "n/a" : "%.0f t".formatted(tons);
    }

    private static String formatDuration(double seconds) {
        if (Double.isNaN(seconds)) {
            return "n/a";
        }
        double days = seconds / 86_400.0;
        if (days >= 1) {
            return "%.1f days".formatted(days);
        }
        double hours = seconds / 3_600.0;
        if (hours >= 1) {
            return "%.1f hours".formatted(hours);
        }
        return "%.0f s".formatted(seconds);
    }

    private static double parse(TextField f, double fallback) {
        try {
            String t = f.getText();
            return (t == null || t.isBlank()) ? fallback : Double.parseDouble(t.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
