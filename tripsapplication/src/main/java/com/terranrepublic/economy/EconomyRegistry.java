package com.terranrepublic.economy;

import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.infrastructure.TransportNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable validation registry for strong economy references. Loose lore/provenance strings are not checked.
 */
public record EconomyRegistry(
        Map<String, SpaceAsset> assetsById,
        Map<String, TransportNode> nodesById,
        Map<String, Commodity> commoditiesById,
        Map<String, ResourceDeposit> depositsById,
        Map<String, Stockpile> stockpilesById,
        Map<String, IndustrialOperation> operationsById,
        Map<String, SupplyRoute> routesById
) {

    public EconomyRegistry {
        assetsById = copyById(assetsById == null ? List.of() : assetsById.values(), SpaceAsset::id, "asset");
        nodesById = copyById(nodesById == null ? List.of() : nodesById.values(), TransportNode::id, "node");
        commoditiesById = copyById(commoditiesById == null ? List.of() : commoditiesById.values(), Commodity::id,
                "commodity");
        depositsById = copyById(depositsById == null ? List.of() : depositsById.values(), ResourceDeposit::id,
                "deposit");
        stockpilesById = copyById(stockpilesById == null ? List.of() : stockpilesById.values(), Stockpile::id,
                "stockpile");
        operationsById = copyById(operationsById == null ? List.of() : operationsById.values(),
                IndustrialOperation::id, "operation");
        routesById = copyById(routesById == null ? List.of() : routesById.values(), SupplyRoute::id, "route");
    }

    public static EconomyRegistry of(
            Collection<SpaceAsset> assets,
            Collection<TransportNode> nodes,
            Collection<Commodity> commodities,
            Collection<ResourceDeposit> deposits,
            Collection<Stockpile> stockpiles,
            Collection<IndustrialOperation> operations,
            Collection<SupplyRoute> routes
    ) {
        return new EconomyRegistry(
                copyById(assets, SpaceAsset::id, "asset"),
                copyById(nodes, TransportNode::id, "node"),
                copyById(commodities, Commodity::id, "commodity"),
                copyById(deposits, ResourceDeposit::id, "deposit"),
                copyById(stockpiles, Stockpile::id, "stockpile"),
                copyById(operations, IndustrialOperation::id, "operation"),
                copyById(routes, SupplyRoute::id, "route"));
    }

    public static EconomyRegistry of(
            Collection<SpaceAsset> assets,
            Collection<TransportNode> nodes,
            Collection<Commodity> commodities,
            Collection<Stockpile> stockpiles,
            Collection<IndustrialOperation> operations,
            Collection<SupplyRoute> routes
    ) {
        return of(assets, nodes, commodities, List.of(), stockpiles, operations, routes);
    }

    public List<String> validate() {
        ArrayList<String> errors = new ArrayList<>();
        validateDeposits(errors);
        validateStockpiles(errors);
        validateOperations(errors);
        validateRoutes(errors);
        return List.copyOf(errors);
    }

    public void requireValid() {
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join("; ", errors));
        }
    }

    public EconomyRegistry withStockpiles(Map<String, Stockpile> stockpiles) {
        return new EconomyRegistry(assetsById, nodesById, commoditiesById, depositsById, stockpiles, operationsById,
                routesById);
    }

    private void validateDeposits(ArrayList<String> errors) {
        for (ResourceDeposit deposit : depositsById.values()) {
            // Celestial-body ids live in existing JPA/TRIPS models; this registry does not perform entity lookup.
            if (deposit.bodyId() == null || deposit.bodyId().isBlank()) {
                errors.add("ResourceDeposit " + deposit.id() + " has blank bodyId");
            }
            for (String commodityId : deposit.abundanceByCommodityId().keySet()) {
                if (!commoditiesById.containsKey(commodityId)) {
                    errors.add("ResourceDeposit " + deposit.id() + " has unknown commodityId " + commodityId);
                }
            }
        }
    }

    private void validateStockpiles(ArrayList<String> errors) {
        for (Stockpile stockpile : stockpilesById.values()) {
            if (!assetsById.containsKey(stockpile.ownerAssetId())) {
                errors.add("Stockpile " + stockpile.id() + " has unknown ownerAssetId " + stockpile.ownerAssetId());
            }
            for (String commodityId : stockpile.quantitiesByCommodityId().keySet()) {
                if (!commoditiesById.containsKey(commodityId)) {
                    errors.add("Stockpile " + stockpile.id() + " has unknown commodityId " + commodityId);
                }
            }
        }
    }

    private void validateOperations(ArrayList<String> errors) {
        for (IndustrialOperation operation : operationsById.values()) {
            if (!assetsById.containsKey(operation.hostAssetId())) {
                errors.add("IndustrialOperation " + operation.id() + " has unknown hostAssetId "
                        + operation.hostAssetId());
            }
            if (operation.sourceDepositId() != null && !depositsById.containsKey(operation.sourceDepositId())) {
                errors.add("IndustrialOperation " + operation.id() + " has unknown sourceDepositId "
                        + operation.sourceDepositId());
            }
            operation.inputsPerTick().keySet().stream()
                    .filter(commodityId -> !commoditiesById.containsKey(commodityId))
                    .forEach(commodityId -> errors.add("IndustrialOperation " + operation.id()
                            + " has unknown input commodityId " + commodityId));
            operation.outputsPerTick().keySet().stream()
                    .filter(commodityId -> !commoditiesById.containsKey(commodityId))
                    .forEach(commodityId -> errors.add("IndustrialOperation " + operation.id()
                            + " has unknown output commodityId " + commodityId));
        }
    }

    private void validateRoutes(ArrayList<String> errors) {
        for (SupplyRoute route : routesById.values()) {
            if (!nodesById.containsKey(route.fromNodeId())) {
                errors.add("SupplyRoute " + route.id() + " has unknown fromNodeId " + route.fromNodeId());
            }
            if (!nodesById.containsKey(route.toNodeId())) {
                errors.add("SupplyRoute " + route.id() + " has unknown toNodeId " + route.toNodeId());
            }
            if (!commoditiesById.containsKey(route.commodityId())) {
                errors.add("SupplyRoute " + route.id() + " has unknown commodityId " + route.commodityId());
            }
        }
    }

    private static <T> Map<String, T> copyById(Collection<T> values, IdAccessor<T> idAccessor, String label) {
        LinkedHashMap<String, T> copy = new LinkedHashMap<>();
        if (values != null) {
            for (T value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("EconomyRegistry cannot contain null " + label + " entries");
                }
                String id = idAccessor.id(value);
                if (copy.put(id, value) != null) {
                    throw new IllegalArgumentException("Duplicate " + label + " id: " + id);
                }
            }
        }
        return Map.copyOf(copy);
    }

    @FunctionalInterface
    private interface IdAccessor<T> {
        String id(T value);
    }
}
