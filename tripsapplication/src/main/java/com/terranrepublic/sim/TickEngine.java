package com.terranrepublic.sim;

import com.terranrepublic.economy.IndustrialOperation;
import com.terranrepublic.economy.Stockpile;
import com.terranrepublic.economy.SupplyRoute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure functional economy tick engine. It never mutates the input {@link WorldState}.
 */
public final class TickEngine {

    private TickEngine() {
    }

    public static WorldState tick(WorldState state) {
        LinkedHashMap<String, Stockpile> stockpiles = new LinkedHashMap<>(state.stockpilesById());
        runOperations(state, stockpiles);
        runRoutes(state, stockpiles);
        return state.withStockpiles(stockpiles, state.tick() + 1);
    }

    public static List<WorldState> run(WorldState initial, int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks must not be negative");
        }
        ArrayList<WorldState> history = new ArrayList<>(ticks + 1);
        WorldState current = initial;
        history.add(current);
        for (int i = 0; i < ticks; i++) {
            current = tick(current);
            history.add(current);
        }
        return List.copyOf(history);
    }

    private static void runOperations(WorldState state, LinkedHashMap<String, Stockpile> stockpiles) {
        state.operationsById().values().stream()
                .sorted(Comparator.comparing(IndustrialOperation::id))
                .forEach(operation -> runOperation(state, stockpiles, operation));
    }

    private static void runOperation(
            WorldState state,
            LinkedHashMap<String, Stockpile> stockpiles,
            IndustrialOperation operation
    ) {
        String stockpileId = state.stockpileIdByAssetId().get(operation.hostAssetId());
        Stockpile stockpile = stockpiles.get(stockpileId);
        if (stockpile == null) {
            return;
        }

        double fraction = affordableFraction(stockpile, operation.inputsPerTick());
        if (fraction <= 0) {
            return;
        }
        for (Map.Entry<String, Double> input : operation.inputsPerTick().entrySet()) {
            stockpile = stockpile.withDelta(input.getKey(), -input.getValue() * fraction);
        }
        for (Map.Entry<String, Double> output : operation.outputsPerTick().entrySet()) {
            stockpile = stockpile.withDelta(output.getKey(), output.getValue() * fraction * operation.efficiency());
        }
        stockpiles.put(stockpile.id(), stockpile);
    }

    private static double affordableFraction(Stockpile stockpile, Map<String, Double> inputsPerTick) {
        double fraction = 1.0;
        for (Map.Entry<String, Double> input : inputsPerTick.entrySet()) {
            double required = input.getValue();
            if (required <= 0) {
                continue;
            }
            fraction = Math.min(fraction, stockpile.quantity(input.getKey()) / required);
        }
        return Math.max(0, Math.min(1, fraction));
    }

    private static void runRoutes(WorldState state, LinkedHashMap<String, Stockpile> stockpiles) {
        state.routesById().values().stream()
                .sorted(Comparator.comparing(SupplyRoute::id))
                .forEach(route -> runRoute(state, stockpiles, route));
    }

    private static void runRoute(WorldState state, LinkedHashMap<String, Stockpile> stockpiles, SupplyRoute route) {
        Stockpile source = stockpiles.get(state.stockpileIdByNodeId().get(route.fromNodeId()));
        Stockpile destination = stockpiles.get(state.stockpileIdByNodeId().get(route.toNodeId()));
        if (source == null || destination == null) {
            return;
        }
        double moved = Math.min(route.tonsPerTick(), source.quantity(route.commodityId()));
        moved = Math.min(moved, destination.availableCapacityTons());
        if (moved <= 0) {
            return;
        }
        source = source.withDelta(route.commodityId(), -moved);
        destination = destination.withDelta(route.commodityId(), moved);
        stockpiles.put(source.id(), source);
        stockpiles.put(destination.id(), destination);
    }
}
