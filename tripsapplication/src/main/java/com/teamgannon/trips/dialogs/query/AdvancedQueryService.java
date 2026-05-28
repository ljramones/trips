package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.export.StarCsvFormatter;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

@Service
public class AdvancedQueryService {

    public static final int INTERACTIVE_RESULT_LIMIT = 2_000;

    private final StarService starService;

    public AdvancedQueryService(StarService starService) {
        this.starService = starService;
    }

    public @NotNull QueryPlan buildPlan(@NotNull String datasetName, String whereClause) {
        String trimmedDataset = datasetName.trim();
        if (trimmedDataset.isEmpty()) {
            throw new IllegalArgumentException("Dataset name is required.");
        }

        String trimmedWhere = whereClause == null ? "" : whereClause.trim();
        String query = "SELECT * FROM STAR_OBJ WHERE DATA_SET_NAME='%s'".formatted(escapeSqlLiteral(trimmedDataset));
        if (!trimmedWhere.isEmpty()) {
            query += " AND " + trimmedWhere;
        }
        return new QueryPlan(query, trimmedWhere.isEmpty());
    }

    public @NotNull List<String> validate(@NotNull String queryToRun) {
        Validation validation = new Validation(Collections.singletonList(DatabaseType.H2), queryToRun);
        List<ValidationError> errors = validation.validate();
        return errors.stream()
                .map(ValidationError::toString)
                .collect(Collectors.toList());
    }

    public @NotNull InteractiveResult runInteractive(@NotNull String queryToRun) {
        List<StarObject> stars = starService.runNativeQuery(queryToRun, INTERACTIVE_RESULT_LIMIT + 1);
        boolean truncated = stars.size() > INTERACTIVE_RESULT_LIMIT;
        if (truncated) {
            stars = List.copyOf(stars.subList(0, INTERACTIVE_RESULT_LIMIT));
        }
        return new InteractiveResult(stars, truncated);
    }

    public @NotNull ExportResult exportCsv(@NotNull String queryToRun,
                                           @NotNull Path targetFile,
                                           @NotNull LongConsumer progressConsumer) {
        AtomicLong progress = new AtomicLong(0);
        try (BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
            writer.write(StarCsvFormatter.headers());
            long exported = starService.processNativeQueryStream(queryToRun, star -> {
                try {
                    writer.write(StarCsvFormatter.format(star));
                    long current = progress.incrementAndGet();
                    if (current % 1_000 == 0) {
                        progressConsumer.accept(current);
                    }
                } catch (java.io.IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            writer.flush();
            progressConsumer.accept(exported);
            return new ExportResult(targetFile, exported);
        } catch (UncheckedIOException e) {
            throw new IllegalStateException("Failed to export Advanced Query CSV: " + e.getCause().getMessage(), e.getCause());
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to export Advanced Query CSV: " + e.getMessage(), e);
        }
    }

    private String escapeSqlLiteral(String value) {
        return value.replace("'", "''");
    }

    public record QueryPlan(String queryToRun, boolean unfiltered) {
    }

    public record InteractiveResult(List<StarObject> stars, boolean truncated) {
    }

    public record ExportResult(Path file, long rowsExported) {
    }
}
