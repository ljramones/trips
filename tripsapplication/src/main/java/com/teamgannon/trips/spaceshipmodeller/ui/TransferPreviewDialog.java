package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.integration.ManeuverNode;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferBody;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferCalculator;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferCategory;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferCost;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlan;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlanSink;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferSuitability;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

import static com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels.get;

/**
 * A mission-transfer preview for a ship between two named bodies.
 * <p>
 * The user picks a ship, an origin and a destination body (by name), a transfer type, and a central star
 * mass; the dialog shows the live {@link TransferPlan} from the {@link TransferPlannerBridge}: required vs
 * available delta-V, transfer time, propellant, a rough burn time, and feasibility. "Create Full Transfer
 * Plan" either hands the plan to a sink (save and open the planner) or shows the read-only plan dialog.
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
    private final ComboBox<TransferType> typeCombo = new ComboBox<>();
    private final TextField starMassField = new TextField();

    private final Label routeValue = new Label();
    private final Label requiredDvValue = new Label();
    private final Label shipDvValue = new Label();
    private final Label marginValue = new Label();
    private final Label timeValue = new Label();
    private final Label propellantValue = new Label();
    private final Label burnValue = new Label();
    private final Label feasibleValue = new Label();
    private final Label feasibilityMessage = new Label();
    private final Label typeDescription = new Label();
    private final Button createPlanButton = new Button(get("transfer.createPlan", "Create Full Transfer Plan"));

    /** Optional: when set, "Create Full Transfer Plan" hands the plan off to be saved/opened. */
    private TransferPlanSink onCreate;
    private String solarSystemId;

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

        typeCombo.getItems().setAll(orderedTypes());
        typeCombo.setButtonCell(typeCell());
        typeCombo.setCellFactory(lv -> typeCell());
        typeCombo.setValue(ships.isEmpty()
                ? TransferType.HOHMANN : TransferCalculator.defaultTypeFor(ships.get(0)));

        shipCombo.valueProperty().addListener((o, a, b) -> {
            if (b != null) {
                typeCombo.setValue(TransferCalculator.defaultTypeFor(b));
            }
            recompute();
        });
        originCombo.valueProperty().addListener((o, a, b) -> recompute());
        destCombo.valueProperty().addListener((o, a, b) -> recompute());
        typeCombo.valueProperty().addListener((o, a, b) -> recompute());
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
        inputs.add(new Label(get("transfer.type", "Transfer type") + ":"), 0, r);
        inputs.add(typeCombo, 1, r++);
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

        feasibilityMessage.setWrapText(true);
        feasibilityMessage.setMaxWidth(460);

        createPlanButton.setOnAction(e -> onCreatePlan());
        HBox planRow = new HBox(8, createPlanButton);
        planRow.setPadding(new Insets(4, 0, 0, 0));

        typeDescription.setWrapText(true);
        typeDescription.setMaxWidth(470);
        typeDescription.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");

        VBox box = new VBox(10, inputs, typeDescription, new Separator(), results, feasibilityMessage, planRow);
        box.setPadding(new Insets(14));
        return box;
    }

    private static List<TransferType> orderedTypes() {
        List<TransferType> ordered = new ArrayList<>();
        for (TransferCategory category : TransferCategory.values()) {
            ordered.addAll(TransferType.byCategory(category));
        }
        return ordered;
    }

    private ListCell<TransferType> typeCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(TransferType type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                    return;
                }
                setText("[" + type.category().label() + "] " + type.label());
                setTextFill(costColor(type.cost()));
                SpaceshipDesign ship = shipCombo.getValue();
                boolean ok = ship == null || TransferSuitability.suitable(type, ship);
                setDisable(!ok);
                setStyle(ok ? "" : "-fx-opacity: 0.45;");
            }
        };
    }

    private static Color costColor(TransferCost cost) {
        return switch (cost) {
            case EFFICIENT -> Color.web("#1e8449");
            case EXPENSIVE_FAST -> Color.web("#d68910");
            case EXOTIC -> Color.web("#8e44ad");
        };
    }

    private void recompute() {
        SpaceshipDesign ship = shipCombo.getValue();
        TransferBody origin = originCombo.getValue();
        TransferBody dest = destCombo.getValue();
        TransferType type = typeCombo.getValue();
        if (ship == null || origin == null || dest == null || type == null) {
            routeValue.setText(get("transfer.noShips", "Select a ship and bodies"));
            for (Label l : List.of(requiredDvValue, shipDvValue, marginValue, timeValue,
                    propellantValue, burnValue, feasibleValue)) {
                l.setText("");
            }
            typeDescription.setText("");
            feasibilityMessage.setText("");
            createPlanButton.setDisable(true);
            return;
        }
        double starMass = parse(starMassField, 1.0);
        TransferPlan plan = bridge.createTransferPlan(origin, dest, starMass, ship, type);
        boolean suitable = TransferSuitability.suitable(type, ship);

        typeDescription.setText(type.description());
        routeValue.setText(origin.name() + " → " + dest.name() + "  (" + type.label() + ")");
        requiredDvValue.setText("%.2f km/s".formatted(plan.totalDeltaVKmps()));
        shipDvValue.setText(formatDeltaV(plan.shipDeltaVKmps()));
        marginValue.setText(formatDeltaV(plan.shipDeltaVKmps() - plan.totalDeltaVKmps()));
        timeValue.setText("%.0f days".formatted(plan.transferTimeDays()));
        propellantValue.setText(formatTons(plan.totalPropellantTons()) + " / "
                + formatTons(ship.massBudget().propellantMassTons()));
        burnValue.setText(formatDuration(totalBurnSeconds(plan)));

        if (!suitable) {
            feasibleValue.setText("Unavailable for this drive");
            feasibleValue.setTextFill(Color.web("#c0392b"));
            feasibilityMessage.setText("This ship's drive cannot perform a " + type.label()
                    + ". Choose a drive suited to it (greyed types are unavailable for this ship).");
            feasibilityMessage.setTextFill(Color.web("#c0392b"));
            createPlanButton.setDisable(true);
            return;
        }
        if (plan.feasible()) {
            feasibleValue.setText(get("transfer.feasible.yes", "Feasible"));
            feasibleValue.setTextFill(Color.web("#1e8449"));
        } else {
            feasibleValue.setText(get("transfer.feasible.no", "Insufficient Δv"));
            feasibleValue.setTextFill(Color.web("#c0392b"));
        }
        feasibilityMessage.setText(feasibilityText(plan));
        feasibilityMessage.setTextFill(plan.feasible() ? Color.web("#1e8449") : Color.web("#c0392b"));
        createPlanButton.setDisable(!plan.feasible());
    }

    private static double totalBurnSeconds(TransferPlan plan) {
        double sum = 0;
        boolean any = false;
        for (ManeuverNode n : plan.nodes()) {
            if (!Double.isNaN(n.burnTimeSeconds())) {
                sum += n.burnTimeSeconds();
                any = true;
            }
        }
        return any ? sum : Double.NaN;
    }

    /**
     * Registers a sink for "Create Full Transfer Plan" (e.g. save and open the planner). Without a sink,
     * the button just shows a read-only {@link TransferPlanDialog}.
     *
     * @param sink          handler for the created plan
     * @param solarSystemId solar system id to record with the plan (may be {@code null})
     * @return this dialog
     */
    public TransferPreviewDialog onCreate(TransferPlanSink sink, String solarSystemId) {
        this.onCreate = sink;
        this.solarSystemId = solarSystemId;
        return this;
    }

    private void onCreatePlan() {
        SpaceshipDesign ship = shipCombo.getValue();
        TransferBody origin = originCombo.getValue();
        TransferBody dest = destCombo.getValue();
        if (ship == null || origin == null || dest == null) {
            return;
        }
        double starMass = parse(starMassField, 1.0);
        TransferType type = typeCombo.getValue() == null ? TransferType.HOHMANN : typeCombo.getValue();
        TransferPlan plan = bridge.createTransferPlan(origin, dest, starMass, ship, type);
        if (onCreate != null) {
            TransferPlanSink sink = onCreate;
            String sysId = solarSystemId;
            close();
            // open after this modal dialog has closed, so the planner window is interactive
            Platform.runLater(() -> sink.accept(plan, ship, sysId, starMass));
        } else {
            new TransferPlanDialog(plan).showAndWait();
        }
    }

    private static String feasibilityText(TransferPlan plan) {
        String typeLabel = plan.type().label();
        if (Double.isNaN(plan.shipDeltaVKmps())) {
            return "This drive carries no reaction mass, so a " + typeLabel
                    + " can't be planned from its Δv budget.";
        }
        if (!plan.feasible()) {
            return "Insufficient Δv: this %s needs %.1f km/s but the ship has only %.1f km/s."
                    .formatted(typeLabel, plan.totalDeltaVKmps(), plan.shipDeltaVKmps());
        }
        if (!plan.propellantSufficient() && !Double.isNaN(plan.totalPropellantTons())) {
            return "Enough Δv, but this %s needs %.0f t of propellant — more than the ship carries."
                    .formatted(typeLabel, plan.totalPropellantTons());
        }
        double ratio = plan.shipDeltaVKmps() / Math.max(plan.totalDeltaVKmps(), 1e-9);
        if (ratio >= 3) {
            return "This ship can comfortably perform this %s (%.0f× the required Δv)."
                    .formatted(typeLabel, ratio);
        }
        return "This ship can perform this " + typeLabel + ", with limited margin.";
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
