package com.teamgannon.trips.spaceshipmodeller.planner;

import com.teamgannon.trips.spaceshipmodeller.integration.ManeuverNode;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferBody;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlan;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link TransferPlanService} with a mocked repository and a real mapper. */
@ExtendWith(MockitoExtension.class)
class TransferPlanServiceTest {

    @Mock
    private TransferPlanRepository repository;

    private final TransferPlanMapper mapper = new TransferPlanMapper();
    private TransferPlanService service;

    @BeforeEach
    void setUp() {
        service = new TransferPlanService(repository, mapper);
    }

    private TransferPlan computed() {
        return new TransferPlan("Roci", TransferType.HOHMANN,
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52),
                List.of(new ManeuverNode("Departure", 2.5, 0.0, 10.0, 100.0)),
                5.0, 18.0, 259.0, 3000.0, true, true);
    }

    @Test
    @DisplayName("findAll maps entities back to domain objects")
    void findAllMaps() {
        SavedTransferPlan saved = SavedTransferPlan.fromComputed(computed(), "ship-1", "sys-1", 1.0);
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(mapper.toEntity(saved)));
        assertEquals(1, service.findAll().size());
    }

    @Test
    @DisplayName("saveComputed builds, persists and returns the plan")
    void saveComputedPersists() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SavedTransferPlan saved = service.saveComputed(computed(), "ship-1", "sys-1", 1.0);
        assertEquals("sys-1", saved.solarSystemId());
        assertEquals(TransferPlanStatus.FEASIBLE, saved.status());
        verify(repository).save(any(TransferPlanEntity.class));
    }

    @Test
    @DisplayName("delete removes the entity when present")
    void deleteRemovesWhenPresent() {
        SavedTransferPlan saved = SavedTransferPlan.fromComputed(computed(), "ship-1", "sys-1", 1.0);
        TransferPlanEntity entity = mapper.toEntity(saved);
        when(repository.findById(saved.id())).thenReturn(Optional.of(entity));
        service.delete(saved.id());
        verify(repository).delete(entity);
    }
}
