package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvancedQueryServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void buildPlanScopesQueryToDatasetAndMarksEmptyWhereAsUnfiltered() {
        AdvancedQueryService service = new AdvancedQueryService(mock(StarService.class));

        AdvancedQueryService.QueryPlan plan = service.buildPlan("O'Brien", " ");

        assertThat(plan.queryToRun()).isEqualTo("SELECT * FROM STAR_OBJ WHERE DATA_SET_NAME='O''Brien'");
        assertThat(plan.unfiltered()).isTrue();
    }

    @Test
    void buildPlanAppendsWhereClauseWhenProvided() {
        AdvancedQueryService service = new AdvancedQueryService(mock(StarService.class));

        AdvancedQueryService.QueryPlan plan = service.buildPlan("nearby", "DISTANCE < 10");

        assertThat(plan.queryToRun()).isEqualTo("SELECT * FROM STAR_OBJ WHERE DATA_SET_NAME='nearby' AND DISTANCE < 10");
        assertThat(plan.unfiltered()).isFalse();
    }

    @Test
    void runInteractiveRequestsSentinelRowAndTrimsToInteractiveLimit() {
        StarService starService = mock(StarService.class);
        AdvancedQueryService service = new AdvancedQueryService(starService);
        String query = "SELECT * FROM STAR_OBJ WHERE DATA_SET_NAME='nearby'";
        List<StarObject> rows = IntStream.rangeClosed(1, AdvancedQueryService.INTERACTIVE_RESULT_LIMIT + 1)
                .mapToObj(index -> new StarObject())
                .toList();
        when(starService.runNativeQuery(query, AdvancedQueryService.INTERACTIVE_RESULT_LIMIT + 1)).thenReturn(rows);

        AdvancedQueryService.InteractiveResult result = service.runInteractive(query);

        assertThat(result.truncated()).isTrue();
        assertThat(result.stars()).hasSize(AdvancedQueryService.INTERACTIVE_RESULT_LIMIT);
        verify(starService).runNativeQuery(query, AdvancedQueryService.INTERACTIVE_RESULT_LIMIT + 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportCsvStreamsNativeQueryToFile() throws Exception {
        StarService starService = mock(StarService.class);
        AdvancedQueryService service = new AdvancedQueryService(starService);
        String query = "SELECT * FROM STAR_OBJ WHERE DATA_SET_NAME='nearby'";
        Path target = tempDir.resolve("advanced.trips.csv");
        StarObject alpha = new StarObject();
        alpha.setDisplayName("Alpha, Centauri");
        alpha.setDataSetName("nearby");

        when(starService.processNativeQueryStream(eq(query), any())).thenAnswer(invocation -> {
            Consumer<StarObject> consumer = invocation.getArgument(1);
            consumer.accept(alpha);
            return 1L;
        });
        List<Long> progress = new ArrayList<>();

        AdvancedQueryService.ExportResult result = service.exportCsv(query, target, progress::add);

        assertThat(result.file()).isEqualTo(target);
        assertThat(result.rowsExported()).isEqualTo(1);
        assertThat(Files.readString(target))
                .startsWith("id,dataSetName,displayName")
                .contains("Alpha~ Centauri");
        assertThat(progress).containsExactly(1L);
        verify(starService).processNativeQueryStream(eq(query), any());
    }
}
