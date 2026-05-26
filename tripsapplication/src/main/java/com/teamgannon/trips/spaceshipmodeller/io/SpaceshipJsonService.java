package com.teamgannon.trips.spaceshipmodeller.io;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import com.terranrepublic.assets.SpaceshipDesign;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;

/**
 * Imports and exports {@link SpaceshipDesign}s as JSON, for sharing designs between users or installations.
 * <p>
 * Designs are read/written through {@link SpaceshipDesignDto} so the JSON contains exactly the design's
 * state. The mapper owns a private {@link ObjectMapper} configured with a tiny ISO-8601 {@link Instant}
 * module (so no dependency on {@code jackson-datatype-jsr310} is assumed) and tolerant of unknown
 * properties on read (forward compatibility). Import accepts either a single JSON object or an array.
 */
@Component
public class SpaceshipJsonService {

    private final ObjectMapper mapper;

    public SpaceshipJsonService() {
        SimpleModule instantModule = new SimpleModule();
        instantModule.addSerializer(Instant.class, new InstantToIsoSerializer());
        instantModule.addDeserializer(Instant.class, new IsoToInstantDeserializer());

        this.mapper = JsonMapper.builder()
                .addModule(instantModule)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    // ----------------------------------------------------------- to JSON

    /**
     * @param design design to serialise
     * @return pretty-printed JSON for a single design
     */
    public String toJson(SpaceshipDesign design) {
        try {
            return mapper.writeValueAsString(SpaceshipDesignDto.fromDomain(design));
        } catch (JacksonException e) {
            throw new SpaceshipJsonException("Failed to serialise design '" + design.name() + "'", e);
        }
    }

    /**
     * @param designs designs to serialise
     * @return pretty-printed JSON array
     */
    public String toJson(List<SpaceshipDesign> designs) {
        try {
            List<SpaceshipDesignDto> dtos = designs.stream().map(SpaceshipDesignDto::fromDomain).toList();
            return mapper.writeValueAsString(dtos);
        } catch (JacksonException e) {
            throw new SpaceshipJsonException("Failed to serialise " + designs.size() + " design(s)", e);
        }
    }

    // --------------------------------------------------------- from JSON

    /**
     * @param json JSON for a single design
     * @return the parsed design
     */
    public SpaceshipDesign parseDesign(String json) {
        try {
            return mapper.readValue(json, SpaceshipDesignDto.class).toDomain();
        } catch (JacksonException e) {
            throw new SpaceshipJsonException("Failed to parse design JSON", e);
        }
    }

    /**
     * Parses one or many designs. Accepts either a JSON array or a single JSON object.
     *
     * @param json JSON text
     * @return the parsed designs (never {@code null})
     */
    public List<SpaceshipDesign> parseDesigns(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (root.isArray()) {
                List<SpaceshipDesignDto> dtos = mapper.readValue(json,
                        mapper.getTypeFactory().constructCollectionType(List.class, SpaceshipDesignDto.class));
                return dtos.stream().map(SpaceshipDesignDto::toDomain).toList();
            }
            return List.of(mapper.treeToValue(root, SpaceshipDesignDto.class).toDomain());
        } catch (JacksonException e) {
            throw new SpaceshipJsonException("Failed to parse designs JSON", e);
        }
    }

    // -------------------------------------------------------------- files

    /**
     * Writes a single design to a file.
     *
     * @param design design to export
     * @param file   target file
     */
    public void exportToFile(SpaceshipDesign design, File file) {
        writeFile(toJson(design), file);
    }

    /**
     * Writes a list of designs to a file as a JSON array.
     *
     * @param designs designs to export
     * @param file    target file
     */
    public void exportToFile(List<SpaceshipDesign> designs, File file) {
        writeFile(toJson(designs), file);
    }

    /**
     * Reads designs from a file (single object or array).
     *
     * @param file source file
     * @return the imported designs
     */
    public List<SpaceshipDesign> importFromFile(File file) {
        try {
            return parseDesigns(Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new SpaceshipJsonException("Failed to read " + file.getName(), e);
        }
    }

    private void writeFile(String json, File file) {
        try {
            Files.writeString(file.toPath(), json);
        } catch (IOException e) {
            throw new SpaceshipJsonException("Failed to write " + file.getName(), e);
        }
    }

    /** Serialises an {@link Instant} as an ISO-8601 string. */
    private static final class InstantToIsoSerializer extends ValueSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializationContext context) {
            gen.writeString(value.toString());
        }
    }

    /** Parses an {@link Instant} from an ISO-8601 string. */
    private static final class IsoToInstantDeserializer extends ValueDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext context) {
            return Instant.parse(p.getValueAsString());
        }
    }
}
