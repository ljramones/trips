package com.terranrepublic.sim;

import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.economy.Commodity;
import com.terranrepublic.economy.CommodityClass;
import com.terranrepublic.economy.EconomyRegistry;
import com.terranrepublic.economy.IndustrialOperation;
import com.terranrepublic.economy.OperationType;
import com.terranrepublic.economy.Stockpile;
import com.terranrepublic.economy.SupplyRoute;
import com.terranrepublic.infrastructure.GraphRegistry;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickEngineTest {

    @Test
    void miningFeedsRefiningAndRouteWhileHistoryEvolves() {
        Scenario scenario = scenario(
                Map.of(),
                Map.of(),
                List.of(
                        new IndustrialOperation(
                                "op-1-mine",
                                OperationType.MINING,
                                Catalog.TROY.id(),
                                Map.of(),
                                Map.of("ore", 10.0),
                                1,
                                "MINING extracts declared outputs from the body with empty inputs"),
                        new IndustrialOperation(
                                "op-2-refine",
                                OperationType.REFINING,
                                Catalog.TROY.id(),
                                Map.of("ore", 5.0),
                                Map.of("metal", 2.0),
                                1,
                                "Refines ore into metal")),
                List.of(new SupplyRoute("route-metal", "node-source", "node-dest", "metal", 1.0, "Test route")));

        List<WorldState> history = TickEngine.run(scenario.world(), 20);
        WorldState initial = history.getFirst();
        WorldState finalState = history.getLast();
        Stockpile finalSource = finalState.stockpilesById().get("stockpile-source");
        Stockpile finalDest = finalState.stockpilesById().get("stockpile-dest");

        assertEquals(21, history.size());
        assertEquals(0, initial.tick());
        assertEquals(20, finalState.tick());
        assertNotSame(initial, finalState);
        assertEquals(0, initial.stockpilesById().get("stockpile-source").totalTons());
        assertTrue(finalSource.quantity("ore") > 0);
        assertTrue(finalDest.quantity("metal") > 0);
        assertTrue(history.get(10).stockpilesById().get("stockpile-source").totalTons()
                < finalSource.totalTons() + finalDest.totalTons());
        assertNoNegativeStockpiles(finalState);
        assertTrue(finalSource.quantity("ore") <= 200);
        assertTrue(finalSource.quantity("metal") + finalDest.quantity("metal") <= 40);
    }

    @Test
    void starvedOperationThrottlesInsteadOfGoingNegative() {
        Scenario scenario = scenario(
                Map.of("ore", 1.0),
                Map.of(),
                List.of(new IndustrialOperation(
                        "op-starved-refine",
                        OperationType.REFINING,
                        Catalog.TROY.id(),
                        Map.of("ore", 5.0),
                        Map.of("metal", 5.0),
                        1,
                        "Should run at 20 percent")),
                List.of());

        WorldState next = TickEngine.tick(scenario.world());
        Stockpile source = next.stockpilesById().get("stockpile-source");

        assertEquals(0, source.quantity("ore"));
        assertEquals(1, source.quantity("metal"));
        assertNoNegativeStockpiles(next);
    }

    private static Scenario scenario(
            Map<String, Double> sourceQuantities,
            Map<String, Double> destQuantities,
            List<IndustrialOperation> operations,
            List<SupplyRoute> routes
    ) {
        SpaceAsset sourceAsset = Catalog.TROY;
        SpaceAsset destAsset = Catalog.SHEVA_GUN;
        TransportNode sourceNode = node("node-source", List.of("node-dest"));
        TransportNode destNode = node("node-dest", List.of("node-source"));
        GraphRegistry graph = GraphRegistry.ofNodes(List.of(sourceNode, destNode));
        Commodity ore = new Commodity("ore", "Asteroid Ore", CommodityClass.RAW_ORE, 1, "Test ore");
        Commodity metal = new Commodity("metal", "Refined Metal", CommodityClass.REFINED_METAL, 1, "Test metal");
        Stockpile sourceStockpile = new Stockpile("stockpile-source", sourceAsset.id(), sourceQuantities, 1_000);
        Stockpile destStockpile = new Stockpile("stockpile-dest", destAsset.id(), destQuantities, 1_000);
        EconomyRegistry economy = EconomyRegistry.of(
                List.of(sourceAsset, destAsset),
                List.of(sourceNode, destNode),
                List.of(ore, metal),
                List.of(sourceStockpile, destStockpile),
                operations,
                routes);
        economy.requireValid();
        graph.requireValid();
        WorldState world = WorldState.from(
                economy,
                graph,
                Map.of(sourceAsset.id(), sourceStockpile.id(), destAsset.id(), destStockpile.id()),
                Map.of(sourceNode.id(), sourceStockpile.id(), destNode.id(), destStockpile.id()),
                0);
        return new Scenario(world);
    }

    private static TransportNode node(String id, List<String> connectedNodeIds) {
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
                connectedNodeIds,
                1_000,
                true,
                0,
                now,
                now);
    }

    private static void assertNoNegativeStockpiles(WorldState state) {
        for (Stockpile stockpile : state.stockpilesById().values()) {
            for (double quantity : stockpile.quantitiesByCommodityId().values()) {
                assertTrue(quantity >= 0, "negative stockpile quantity");
            }
        }
    }

    private record Scenario(WorldState world) {
    }
}
