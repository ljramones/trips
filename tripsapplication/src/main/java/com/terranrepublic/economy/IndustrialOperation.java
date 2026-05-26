package com.terranrepublic.economy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable process recipe. MINING may have empty inputs; outputsPerTick represents extraction from the
 * referenced ResourceDeposit when sourceDepositId is set.
 */
public record IndustrialOperation(
        String id,
        OperationType type,
        String hostAssetId,
        Map<String, Double> inputsPerTick,
        Map<String, Double> outputsPerTick,
        double efficiency,
        String sourceDepositId,
        String notes
) {

    /**
     * Backwards-compatible constructor for operations that predate typed resource deposits.
     */
    public IndustrialOperation(
            String id,
            OperationType type,
            String hostAssetId,
            Map<String, Double> inputsPerTick,
            Map<String, Double> outputsPerTick,
            double efficiency,
            String notes
    ) {
        this(id, type, hostAssetId, inputsPerTick, outputsPerTick, efficiency, null, notes);
    }

    public IndustrialOperation {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("IndustrialOperation id must be provided");
        }
        if (hostAssetId == null || hostAssetId.isBlank()) {
            throw new IllegalArgumentException("IndustrialOperation hostAssetId must be provided");
        }
        if (efficiency < 0 || efficiency > 1) {
            throw new IllegalArgumentException("IndustrialOperation efficiency must be in [0, 1]");
        }
        type = type == null ? OperationType.FABRICATION : type;
        inputsPerTick = copyRates(inputsPerTick, "inputsPerTick");
        outputsPerTick = copyRates(outputsPerTick, "outputsPerTick");
        sourceDepositId = sourceDepositId == null || sourceDepositId.isBlank() ? null : sourceDepositId;
        notes = notes == null ? "" : notes;
    }

    private static Map<String, Double> copyRates(Map<String, Double> rates, String fieldName) {
        LinkedHashMap<String, Double> copy = new LinkedHashMap<>();
        if (rates != null) {
            for (Map.Entry<String, Double> entry : rates.entrySet()) {
                String commodityId = entry.getKey();
                double rate = entry.getValue() == null ? 0 : entry.getValue();
                if (commodityId == null || commodityId.isBlank()) {
                    throw new IllegalArgumentException(fieldName + " commodity ids must be provided");
                }
                if (rate < 0) {
                    throw new IllegalArgumentException(fieldName + " rates must not be negative");
                }
                if (rate > 0) {
                    copy.put(commodityId, rate);
                }
            }
        }
        return Map.copyOf(copy);
    }
}
