package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.planner.SavedTransferPlan;

/**
 * Read-only JavaBean view of a {@link SavedTransferPlan} for the planner's {@code TableView}
 * (via {@code PropertyValueFactory}).
 */
public class TransferPlanRow {

    private final SavedTransferPlan plan;

    public TransferPlanRow(SavedTransferPlan plan) {
        this.plan = plan;
    }

    public SavedTransferPlan getPlan() {
        return plan;
    }

    public String getRoute() {
        return plan.route();
    }

    public String getShip() {
        return plan.shipName();
    }

    public String getDeltaV() {
        return "%.2f km/s".formatted(plan.totalDeltaVKmps());
    }

    public String getStatus() {
        return plan.status().label();
    }

    public String getTime() {
        return "%.0f days".formatted(plan.transferTimeDays());
    }
}
