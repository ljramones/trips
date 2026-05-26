package com.terranrepublic.economy;

import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomyRegistryTest {

    @Test
    void validationPassesForCoherentMiniEconomy() {
        SpaceAsset troy = Catalog.TROY;
        TransportNode mineNode = node("node-mine");
        TransportNode refineryNode = node("node-refinery");
        Commodity ore = new Commodity("ore-ni-fe", "Nickel-Iron Ore", CommodityClass.RAW_ORE, 1, "Test ore");
        Commodity metal = new Commodity("metal-ni-fe", "Nickel-Iron Metal", CommodityClass.REFINED_METAL, 1,
                "Refined test metal");
        Stockpile stockpile = new Stockpile(
                "stockpile-troy",
                troy.id(),
                Map.of(ore.id(), 10.0),
                1_000);
        IndustrialOperation refining = new IndustrialOperation(
                "op-refine",
                OperationType.REFINING,
                troy.id(),
                Map.of(ore.id(), 2.0),
                Map.of(metal.id(), 1.5),
                0.8,
                "Test refining");
        SupplyRoute route = new SupplyRoute(
                "route-ore",
                mineNode.id(),
                refineryNode.id(),
                ore.id(),
                5,
                "Loose route lore");

        EconomyRegistry registry = EconomyRegistry.of(
                List.of(troy),
                List.of(mineNode, refineryNode),
                List.of(ore, metal),
                List.of(stockpile),
                List.of(refining),
                List.of(route));

        assertEquals(List.of(), registry.validate());
        assertDoesNotThrow(registry::requireValid);
        assertEquals(15, stockpile.withDelta(ore.id(), 5).quantity(ore.id()));
        assertEquals(0, stockpile.withDelta(ore.id(), -20).quantity(ore.id()));
    }

    @Test
    void validationFailsOnDanglingStrongReference() {
        SpaceAsset troy = Catalog.TROY;
        TransportNode node = node("node-mine");
        Commodity ore = new Commodity("ore-ni-fe", "Nickel-Iron Ore", CommodityClass.RAW_ORE, 1, "Test ore");
        IndustrialOperation badOperation = new IndustrialOperation(
                "op-bad",
                OperationType.REFINING,
                troy.id(),
                Map.of("missing-commodity", 1.0),
                Map.of(ore.id(), 1.0),
                1,
                "Loose notes are not validated");

        EconomyRegistry registry = EconomyRegistry.of(
                List.of(troy),
                List.of(node),
                List.of(ore),
                List.of(),
                List.of(badOperation),
                List.of(new SupplyRoute("route-bad", node.id(), "missing-node", ore.id(), 1, "Loose route lore")));

        List<String> errors = registry.validate();
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(error -> error.contains("missing-commodity")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("missing-node")));
        assertThrows(IllegalStateException.class, registry::requireValid);
    }

    @Test
    void miningOperationCanStrongReferenceResourceDeposit() {
        SpaceAsset troy = Catalog.TROY;
        TransportNode node = node("node-mine");
        Commodity ore = new Commodity("ore-ni-fe", "Nickel-Iron Ore", CommodityClass.RAW_ORE, 1, "Test ore");
        ResourceDeposit deposit = new ResourceDeposit(
                "deposit-asteroid-1",
                "body-asteroid-1",
                BodyKind.ASTEROID_BELT,
                Map.of(ore.id(), 50_000.0),
                0.8,
                "Fake body id; body existence is validated outside the economy registry");
        IndustrialOperation mining = new IndustrialOperation(
                "op-mine",
                OperationType.MINING,
                troy.id(),
                Map.of(),
                Map.of(ore.id(), 25.0),
                1,
                deposit.id(),
                "Extracts from typed deposit");

        EconomyRegistry registry = EconomyRegistry.of(
                List.of(troy),
                List.of(node),
                List.of(ore),
                List.of(deposit),
                List.of(new Stockpile("stockpile-troy", troy.id(), Map.of(), 1_000)),
                List.of(mining),
                List.of());

        assertEquals(List.of(), registry.validate());
        assertDoesNotThrow(registry::requireValid);
        assertEquals(49_990, deposit.withAbundanceDelta(ore.id(), -10).abundanceByCommodityId().get(ore.id()));
        assertEquals(0, deposit.withAbundanceDelta(ore.id(), -100_000).abundanceByCommodityId()
                .getOrDefault(ore.id(), 0.0));
    }

    @Test
    void validationFailsOnDanglingSourceDepositReference() {
        SpaceAsset troy = Catalog.TROY;
        TransportNode node = node("node-mine");
        Commodity ore = new Commodity("ore-ni-fe", "Nickel-Iron Ore", CommodityClass.RAW_ORE, 1, "Test ore");
        IndustrialOperation mining = new IndustrialOperation(
                "op-mine",
                OperationType.MINING,
                troy.id(),
                Map.of(),
                Map.of(ore.id(), 25.0),
                1,
                "missing-deposit",
                "Dangling source deposit should fail validation");

        EconomyRegistry registry = EconomyRegistry.of(
                List.of(troy),
                List.of(node),
                List.of(ore),
                List.of(),
                List.of(),
                List.of(mining),
                List.of());

        List<String> errors = registry.validate();
        assertEquals(1, errors.size());
        assertTrue(errors.getFirst().contains("missing-deposit"));
        assertThrows(IllegalStateException.class, registry::requireValid);
    }

    private static TransportNode node(String id) {
        Instant now = Instant.now();
        return new TransportNode(
                id,
                id,
                "Test",
                "Terran",
                false,
                "Test node",
                NodeType.RING_GATE,
                0,
                0,
                0,
                List.of(),
                1_000,
                true,
                0,
                now,
                now);
    }
}
