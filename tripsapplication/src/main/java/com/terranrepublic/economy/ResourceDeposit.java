package com.terranrepublic.economy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured resource inventory attached to an existing celestial body by id.
 */
public record ResourceDeposit(
        String id,
        String bodyId,
        BodyKind bodyKind,
        Map<String, Double> abundanceByCommodityId,
        double extractionDifficulty,
        String notes
) {

    public ResourceDeposit {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ResourceDeposit id must be provided");
        }
        if (extractionDifficulty < 0 || extractionDifficulty > 1) {
            throw new IllegalArgumentException("ResourceDeposit extractionDifficulty must be in [0, 1]");
        }
        bodyId = bodyId == null ? "" : bodyId;
        bodyKind = bodyKind == null ? BodyKind.OTHER : bodyKind;
        abundanceByCommodityId = copyAbundances(abundanceByCommodityId);
        notes = notes == null ? "" : notes;
    }

    public ResourceDeposit withAbundanceDelta(String commodityId, double amount) {
        if (commodityId == null || commodityId.isBlank() || amount == 0) {
            return this;
        }
        double current = abundanceByCommodityId.getOrDefault(commodityId, 0.0);
        double updated = Math.max(0, current + amount);

        LinkedHashMap<String, Double> next = new LinkedHashMap<>(abundanceByCommodityId);
        if (updated == 0) {
            next.remove(commodityId);
        } else {
            next.put(commodityId, updated);
        }
        return new ResourceDeposit(id, bodyId, bodyKind, next, extractionDifficulty, notes);
    }

    private static Map<String, Double> copyAbundances(Map<String, Double> abundances) {
        LinkedHashMap<String, Double> copy = new LinkedHashMap<>();
        if (abundances != null) {
            for (Map.Entry<String, Double> entry : abundances.entrySet()) {
                String commodityId = entry.getKey();
                double abundance = entry.getValue() == null ? 0 : entry.getValue();
                if (commodityId == null || commodityId.isBlank()) {
                    throw new IllegalArgumentException("ResourceDeposit commodity ids must be provided");
                }
                if (abundance < 0) {
                    throw new IllegalArgumentException("ResourceDeposit abundances must not be negative");
                }
                if (abundance > 0) {
                    copy.put(commodityId, abundance);
                }
            }
        }
        return Map.copyOf(copy);
    }
}
