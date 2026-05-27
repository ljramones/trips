package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.integration.Feasibility;
import com.teamgannon.trips.spaceshipmodeller.integration.ManeuverNode;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferFeasibility;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlan;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Displays a concrete {@link TransferPlan}: its maneuver nodes (burns) and the mission totals.
 * <p>
 * This is the "View Plan" result of "Create Full Transfer Plan". Informational ({@code Dialog<Void>} with a
 * Close button). When a richer mission-planner view exists, this is the natural hand-off point.
 */
public class TransferPlanDialog extends Dialog<Void> {

    public TransferPlanDialog(TransferPlan plan) {
        setTitle("Transfer Plan — " + plan.shipName());
        getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        getDialogPane().setContent(buildContent(plan));
        getDialogPane().setPrefWidth(560);
        setResultConverter(bt -> null);
    }

    private VBox buildContent(TransferPlan plan) {
        Label header = new Label(plan.type().label() + ":  "
                + plan.origin().name() + " → " + plan.destination().name());
        header.setFont(Font.font(header.getFont().getFamily(), 15));
        header.getStyleClass().add("trips-bold"); // Issue 50 / Bucket A

        TableView<ManeuverNode> table = new TableView<>(FXCollections.observableArrayList(plan.nodes()));
        table.setPrefHeight(140);

        TableColumn<ManeuverNode, String> nameCol = new TableColumn<>("Maneuver");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        nameCol.setPrefWidth(160);
        TableColumn<ManeuverNode, String> dvCol = new TableColumn<>("Δv");
        dvCol.setCellValueFactory(c -> new SimpleStringProperty("%.2f km/s".formatted(c.getValue().deltaVKmps())));
        TableColumn<ManeuverNode, String> timeCol = new TableColumn<>("T+ (days)");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty("%.0f".formatted(c.getValue().timeFromStartDays())));
        TableColumn<ManeuverNode, String> propCol = new TableColumn<>("Propellant");
        propCol.setCellValueFactory(c -> new SimpleStringProperty(tons(c.getValue().propellantTons())));
        TableColumn<ManeuverNode, String> burnCol = new TableColumn<>("Burn");
        burnCol.setCellValueFactory(c -> new SimpleStringProperty(duration(c.getValue().burnTimeSeconds())));
        table.getColumns().setAll(java.util.List.of(nameCol, dvCol, timeCol, propCol, burnCol));

        Label totals = new Label(
                "Total Δv: %.2f km/s   |   Ship Δv: %s   |   Transfer time: %.0f days   |   Propellant: %s"
                        .formatted(plan.totalDeltaVKmps(), kmps(plan.shipDeltaVKmps()),
                                plan.transferTimeDays(), tons(plan.totalPropellantTons())));

        Feasibility f = plan.feasibility();
        Label verdict = new Label(verdictText(plan, f));
        verdict.getStyleClass().add("trips-bold"); // Issue 50 / Bucket A
        verdict.setWrapText(true);
        verdict.setTextFill(switch (f) {
            case FEASIBLE -> Color.web("#1e8449");
            case MARGINAL -> Color.web("#d68910");
            case INSUFFICIENT -> Color.web("#c0392b");
        });

        VBox box = new VBox(10, header, table, new Separator(), totals, verdict);
        box.setPadding(new Insets(14));
        return box;
    }

    private static String verdictText(TransferPlan plan, Feasibility f) {
        Feasibility dv = TransferFeasibility.deltaVStatus(plan.totalDeltaVKmps(), plan.shipDeltaVKmps());
        Feasibility prop = TransferFeasibility.propellantStatus(
                plan.totalPropellantTons(), plan.availablePropellantTons());
        if (dv == Feasibility.INSUFFICIENT) {
            return "Not feasible: the ship's Δv budget (%s) is below the %.2f km/s required."
                    .formatted(kmps(plan.shipDeltaVKmps()), plan.totalDeltaVKmps());
        }
        if (prop == Feasibility.INSUFFICIENT) {
            return "Not feasible: needs %,.0f t of propellant but the ship carries only %,.0f t."
                    .formatted(plan.totalPropellantTons(), plan.availablePropellantTons());
        }
        if (f == Feasibility.MARGINAL) {
            return "Marginal: the ship can barely perform this transfer (just enough Δv / propellant).";
        }
        return "Feasible: the ship has comfortable Δv and propellant for these burns.";
    }

    private static String kmps(double v) {
        return Double.isNaN(v) ? "n/a" : "%.0f km/s".formatted(v);
    }

    private static String tons(double t) {
        return Double.isNaN(t) ? "n/a" : "%.0f t".formatted(t);
    }

    private static String duration(double seconds) {
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
}
