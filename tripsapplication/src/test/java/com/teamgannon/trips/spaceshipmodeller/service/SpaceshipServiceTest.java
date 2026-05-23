package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipRepository;
import com.teamgannon.trips.spaceshipmodeller.propulsion.Category;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link SpaceshipService} using a mocked repository and a real mapper. */
@ExtendWith(MockitoExtension.class)
class SpaceshipServiceTest {

    @Mock
    private SpaceshipRepository repository;

    private final SpaceshipDesignMapper mapper = new SpaceshipDesignMapper();
    private SpaceshipService service;

    @BeforeEach
    void setUp() {
        service = new SpaceshipService(repository, mapper);
    }

    private SpaceshipDesign design(String name, DriveType drive) {
        return SpaceshipBuilder.create(name)
                .shipClass(ShipClass.FRIGATE).driveType(drive)
                .structureTons(200).engineTons(150).propellantTons(300)
                .payloadTons(50).crewTons(20).radiatorTons(120).crew(4).build();
    }

    @Test
    @DisplayName("findAll maps entities back to domain objects")
    void findAllMapsEntities() {
        SpaceshipDesign d = design("A", DriveType.FUSION_TORCH);
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(d)));

        List<SpaceshipDesign> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).name());
    }

    @Test
    @DisplayName("save persists via the repository and returns the round-tripped design")
    void savePersists() {
        SpaceshipDesign d = design("B", DriveType.FUSION_TORCH);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SpaceshipDesign saved = service.save(d);
        assertEquals("B", saved.name());
        verify(repository).save(any(SpaceshipEntity.class));
    }

    @Test
    @DisplayName("delete removes the entity when it is present")
    void deleteRemovesWhenPresent() {
        SpaceshipDesign d = design("C", DriveType.FUSION_TORCH);
        SpaceshipEntity entity = mapper.toEntity(d);
        when(repository.findById(d.id())).thenReturn(Optional.of(entity));

        service.delete(d.id());
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("findByCategory filters by the drive's category")
    void findByCategoryFilters() {
        SpaceshipDesign fusion = design("F", DriveType.FUSION_TORCH);
        SpaceshipDesign chemical = design("Ch", DriveType.CHEMICAL_BIPROPELLANT);
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(fusion), mapper.toEntity(chemical)));

        List<SpaceshipDesign> result = service.findByCategory(Category.FUSION);
        assertEquals(1, result.size());
        assertEquals("F", result.get(0).name());
    }

    @Test
    @DisplayName("seedTemplates saves only designs whose name does not already exist")
    void seedTemplatesSkipsExisting() {
        SpaceshipDesign a = design("Alpha", DriveType.FUSION_TORCH);
        SpaceshipDesign b = design("Beta", DriveType.FUSION_TORCH);
        when(repository.existsByNameIgnoreCase("Alpha")).thenReturn(true);
        when(repository.existsByNameIgnoreCase("Beta")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int added = service.seedTemplates(List.of(a, b));
        assertEquals(1, added);
        verify(repository, times(1)).save(any(SpaceshipEntity.class));
    }

    @Test
    @DisplayName("validate delegates to the rules engine")
    void validateDelegatesToEngine() {
        SpaceshipDesign bad = SpaceshipBuilder.create("Bad")
                .shipClass(ShipClass.LANDER).driveType(DriveType.ION_GRIDDED)
                .structureTons(1).propellantTons(99).build();
        assertFalse(service.validate(bad).isValid());
    }
}
