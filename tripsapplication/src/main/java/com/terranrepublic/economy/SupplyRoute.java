package com.terranrepublic.economy;

/**
 * Declared flow between two transport nodes for one strong-key commodity.
 */
public record SupplyRoute(
        String id,
        String fromNodeId,
        String toNodeId,
        String commodityId,
        double tonsPerTick,
        String routeLore
) {

    public SupplyRoute {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("SupplyRoute id must be provided");
        }
        if (fromNodeId == null || fromNodeId.isBlank() || toNodeId == null || toNodeId.isBlank()) {
            throw new IllegalArgumentException("SupplyRoute endpoints must be provided");
        }
        if (commodityId == null || commodityId.isBlank()) {
            throw new IllegalArgumentException("SupplyRoute commodityId must be provided");
        }
        if (tonsPerTick < 0) {
            throw new IllegalArgumentException("SupplyRoute tonsPerTick must not be negative");
        }
        routeLore = routeLore == null ? "" : routeLore;
    }
}
