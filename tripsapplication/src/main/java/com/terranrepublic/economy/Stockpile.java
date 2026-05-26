package com.terranrepublic.economy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable inventory owned by a cataloged space asset. Quantities are stored in tons.
 */
public record Stockpile(
        String id,
        String ownerAssetId,
        Map<String, Double> quantitiesByCommodityId,
        double capacityTons
) {

    public Stockpile {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Stockpile id must be provided");
        }
        if (ownerAssetId == null || ownerAssetId.isBlank()) {
            throw new IllegalArgumentException("Stockpile ownerAssetId must be provided");
        }
        if (capacityTons < 0) {
            throw new IllegalArgumentException("Stockpile capacity must not be negative");
        }
        quantitiesByCommodityId = copyQuantities(quantitiesByCommodityId);
    }

    public Stockpile withDelta(String commodityId, double amount) {
        if (commodityId == null || commodityId.isBlank() || amount == 0) {
            return this;
        }
        double current = quantitiesByCommodityId.getOrDefault(commodityId, 0.0);
        double totalWithoutCommodity = totalTons() - current;
        double maxForCommodity = Math.max(0, capacityTons - totalWithoutCommodity);
        double updated = Math.max(0, Math.min(maxForCommodity, current + amount));

        LinkedHashMap<String, Double> next = new LinkedHashMap<>(quantitiesByCommodityId);
        if (updated == 0) {
            next.remove(commodityId);
        } else {
            next.put(commodityId, updated);
        }
        return new Stockpile(id, ownerAssetId, next, capacityTons);
    }

    public double quantity(String commodityId) {
        return quantitiesByCommodityId.getOrDefault(commodityId, 0.0);
    }

    public double totalTons() {
        return quantitiesByCommodityId.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double availableCapacityTons() {
        return Math.max(0, capacityTons - totalTons());
    }

    private static Map<String, Double> copyQuantities(Map<String, Double> quantities) {
        LinkedHashMap<String, Double> copy = new LinkedHashMap<>();
        if (quantities != null) {
            for (Map.Entry<String, Double> entry : quantities.entrySet()) {
                String commodityId = entry.getKey();
                double quantity = entry.getValue() == null ? 0 : entry.getValue();
                if (commodityId == null || commodityId.isBlank()) {
                    throw new IllegalArgumentException("Stockpile commodity ids must be provided");
                }
                if (quantity < 0) {
                    throw new IllegalArgumentException("Stockpile quantities must not be negative");
                }
                if (quantity > 0) {
                    copy.put(commodityId, quantity);
                }
            }
        }
        return Map.copyOf(copy);
    }
}
