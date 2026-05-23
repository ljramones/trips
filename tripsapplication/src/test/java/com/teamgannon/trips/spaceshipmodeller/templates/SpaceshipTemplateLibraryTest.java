package com.teamgannon.trips.spaceshipmodeller.templates;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationEngine;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the seeded {@link SpaceshipTemplateLibrary}. */
class SpaceshipTemplateLibraryTest {

    private final SpaceshipTemplateLibrary library = new SpaceshipTemplateLibrary();
    private final ValidationEngine engine = new ValidationEngine();

    @Test
    @DisplayName("library provides between 8 and 10 templates")
    void hasEightToTenTemplates() {
        int n = library.getAllTemplates().size();
        assertTrue(n >= 8 && n <= 10, "expected 8-10 templates, got " + n);
    }

    @Test
    @DisplayName("template names are unique")
    void templateNamesAreUnique() {
        List<SpaceshipDesign> templates = library.getAllTemplates();
        long distinct = templates.stream().map(SpaceshipDesign::name).distinct().count();
        assertEquals(templates.size(), distinct);
    }

    @Test
    @DisplayName("every template passes validation with no errors")
    void everyTemplateIsValid() {
        for (SpaceshipDesign d : library.getAllTemplates()) {
            ValidationResult result = engine.validate(d);
            assertTrue(result.isValid(),
                    () -> d.name() + " should be valid but had errors: " + result.errors());
        }
    }

    @Test
    @DisplayName("each call builds fresh instances with new ids")
    void freshInstancesEachCall() {
        assertNotEquals(library.getAllTemplates().get(0).id(), library.getAllTemplates().get(0).id());
    }
}
