package com.teamgannon.trips.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CodebaseConventionTest {

    private static final Path MAIN_JAVA = Path.of("src/main/java");
    private static final Path MAIN_RESOURCES = Path.of("src/main/resources");
    private static final Path EVENTS_DIR = MAIN_JAVA.resolve("com/teamgannon/trips/events");
    private static final Path EVENT_CATALOG = EVENTS_DIR.resolve("EVENT_CATALOG.md");

    private static final Set<String> RAW_FXML_LOADER_ALLOWLIST = Set.of(
            "src/main/java/com/teamgannon/trips/controller/TransitFilterPane.java",
            "src/main/java/com/teamgannon/trips/objects/MeshViewShapeFactory.java",
            "src/main/java/com/teamgannon/trips/screenobjects/StarEditDialog.java",
            "src/main/java/com/teamgannon/trips/screenobjects/StarPropertiesPane.java",
            "src/main/java/com/teamgannon/trips/search/SearchPane.java",
            "src/main/java/com/teamgannon/trips/search/components/BasePane.java"
    );

    private static final Pattern EVENT_HEADING = Pattern.compile("^### (\\w+Event)$", Pattern.MULTILINE);
    private static final Pattern PLACEHOLDER_TEXT = Pattern.compile("(?i)\\b(TBD|coming soon|not implemented)\\b");

    private static final Set<String> USER_VISIBLE_PLACEHOLDER_ALLOWLIST = Set.of(
            "src/main/java/com/teamgannon/trips/solarsystem/rendering/BodyRenderer.java :: // Companion star: simple horizontal offset for now (binary-orbit math TBD).",
            "src/main/java/com/teamgannon/trips/spaceshipmodeller/ui/SpaceshipDesignerPanel.java :: \"Exporting \\\"\" + selected.name() + \"\\\" to a mission is coming soon.\");",
            "src/main/resources/com/teamgannon/trips/spaceshipmodeller/spaceshipmodeller.properties :: tooltip.export=Export the selected design to a mission (coming soon)"
    );

    @Test
    @DisplayName("raw FXMLLoader usage stays on the known allowlist")
    void rawFxmlLoaderUsageStaysOnKnownAllowlist() throws IOException {
        Set<String> actual = new TreeSet<>();
        try (Stream<Path> files = mainJavaFiles()) {
            for (Path file : files.toList()) {
                String text = Files.readString(file);
                if (text.contains("new FXMLLoader") || text.contains("FXMLLoader(")) {
                    actual.add(normalize(file));
                }
            }
        }

        assertEquals(new TreeSet<>(RAW_FXML_LOADER_ALLOWLIST), actual);
    }

    @Test
    @DisplayName("event catalog headings match current event classes")
    void eventCatalogHeadingsMatchCurrentEventClasses() throws IOException {
        Set<String> sourceEvents = new TreeSet<>();
        try (Stream<Path> files = Files.list(EVENTS_DIR)) {
            files.filter(path -> path.getFileName().toString().endsWith("Event.java"))
                    .map(path -> path.getFileName().toString().replace(".java", ""))
                    .forEach(sourceEvents::add);
        }

        String catalog = Files.readString(EVENT_CATALOG);
        Set<String> catalogEvents = new TreeSet<>();
        Matcher matcher = EVENT_HEADING.matcher(catalog);
        while (matcher.find()) {
            catalogEvents.add(matcher.group(1));
        }

        assertEquals(sourceEvents, catalogEvents);
        assertFalse(catalog.contains("DEAD EVENT"));
    }

    @Test
    @DisplayName("placeholder UI strings stay on the known allowlist")
    void placeholderUiStringsStayOnKnownAllowlist() throws IOException {
        Set<String> actual = new TreeSet<>();
        try (Stream<Path> files = mainCodeAndResourceFiles()) {
            for (Path file : files.toList()) {
                for (String line : Files.readAllLines(file)) {
                    if (PLACEHOLDER_TEXT.matcher(line).find()) {
                        actual.add(normalize(file) + " :: " + line.trim());
                    }
                }
            }
        }

        assertEquals(new TreeSet<>(USER_VISIBLE_PLACEHOLDER_ALLOWLIST), actual);
    }

    private static Stream<Path> mainJavaFiles() throws IOException {
        return Files.walk(MAIN_JAVA)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"));
    }

    private static Stream<Path> mainCodeAndResourceFiles() throws IOException {
        return Stream.concat(
                Files.walk(MAIN_JAVA)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java")),
                Files.walk(MAIN_RESOURCES)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".fxml")
                                || path.toString().endsWith(".properties"))
        );
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }
}
