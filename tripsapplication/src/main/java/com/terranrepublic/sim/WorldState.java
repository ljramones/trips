package com.terranrepublic.sim;

import com.terranrepublic.economy.EconomyRegistry;
import com.terranrepublic.economy.IndustrialOperation;
import com.terranrepublic.economy.ResourceDeposit;
import com.terranrepublic.economy.Stockpile;
import com.terranrepublic.economy.SupplyRoute;
import com.terranrepublic.infrastructure.GraphRegistry;
import com.terranrepublic.infrastructure.TransportNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable simulation snapshot. Holding a list of these records gives replay/history for free.
 */
public record WorldState(
        EconomyRegistry economyRegistry,
        GraphRegistry graphRegistry,
        Map<String, Stockpile> stockpilesById,
        Map<String, ResourceDeposit> depositsById,
        Map<String, IndustrialOperation> operationsById,
        Map<String, SupplyRoute> routesById,
        Map<String, TransportNode> nodesById,
        Map<String, String> stockpileIdByAssetId,
        Map<String, String> stockpileIdByNodeId,
        long tick
) {

    public WorldState {
        stockpilesById = copyMap(stockpilesById);
        depositsById = copyMap(depositsById);
        operationsById = copyMap(operationsById);
        routesById = copyMap(routesById);
        nodesById = copyMap(nodesById);
        stockpileIdByAssetId = copyStringMap(stockpileIdByAssetId);
        stockpileIdByNodeId = copyStringMap(stockpileIdByNodeId);
        if (economyRegistry != null) {
            economyRegistry = economyRegistry.withStockpiles(stockpilesById);
        }
    }

    public static WorldState from(
            EconomyRegistry economyRegistry,
            GraphRegistry graphRegistry,
            Map<String, String> stockpileIdByAssetId,
            Map<String, String> stockpileIdByNodeId,
            long tick
    ) {
        return new WorldState(
                economyRegistry,
                graphRegistry,
                economyRegistry == null ? Map.of() : economyRegistry.stockpilesById(),
                economyRegistry == null ? Map.of() : economyRegistry.depositsById(),
                economyRegistry == null ? Map.of() : economyRegistry.operationsById(),
                economyRegistry == null ? Map.of() : economyRegistry.routesById(),
                graphRegistry == null ? Map.of() : graphRegistry.nodesById(),
                stockpileIdByAssetId,
                stockpileIdByNodeId,
                tick);
    }

    public WorldState withStockpiles(Map<String, Stockpile> stockpiles, long nextTick) {
        return new WorldState(
                economyRegistry,
                graphRegistry,
                stockpiles,
                depositsById,
                operationsById,
                routesById,
                nodesById,
                stockpileIdByAssetId,
                stockpileIdByNodeId,
                nextTick);
    }

    private static <T> Map<String, T> copyMap(Map<String, T> map) {
        LinkedHashMap<String, T> copy = new LinkedHashMap<>();
        if (map != null) {
            copy.putAll(map);
        }
        return java.util.Collections.unmodifiableMap(copy);
    }

    private static Map<String, String> copyStringMap(Map<String, String> map) {
        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        if (map != null) {
            copy.putAll(map);
        }
        return java.util.Collections.unmodifiableMap(copy);
    }
}
