package com.teamgannon.trips.service;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.dialogs.dataset.model.ExportTaskComplete;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.export.CSVDataSetDataExportService;
import com.teamgannon.trips.service.export.CSVQueryExporterService;
import com.teamgannon.trips.service.export.ExportResult;
import com.teamgannon.trips.service.model.ExportFileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DataExportService}.
 *
 * Note: JavaFX UI components (Label, ProgressBar, Button) are passed as null
 * because they cannot be mocked without JavaFX runtime initialization.
 * The mocked CSV services don't actually use these components.
 */
class DataExportServiceTest {

    @Mock
    private DatabaseManagementService databaseManagementService;

    @Mock
    private StarService starService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CSVQueryExporterService csvQueryExporterService;

    @Mock
    private CSVDataSetDataExportService csvDataSetDataExportService;

    @Mock
    private ExportTaskComplete exportTaskComplete;

    @Mock
    private SearchContext searchContext;

    private DataExportService dataExportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataExportService = new DataExportService(
                databaseManagementService,
                starService,
                eventPublisher,
                csvQueryExporterService,
                csvDataSetDataExportService
        );
    }

    @Test
    @DisplayName("Should create service with all dependencies")
    void shouldCreateServiceWithAllDependencies() {
        assertNotNull(dataExportService);
    }

    // === exportDataset Tests ===

    @Test
    @DisplayName("Should successfully start CSV export")
    void exportDataset_shouldSuccessfullyStartCsvExport() {
        // Given
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        // When
        ExportResult result = dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);  // JavaFX components as null

        // Then
        assertTrue(result.isSuccess());
        verify(csvDataSetDataExportService).exportAsCSV(
                eq(options), eq(databaseManagementService), eq(starService),
                eq(eventPublisher), eq(exportTaskComplete),
                isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("Should return failure when CSV export fails to queue")
    void exportDataset_shouldReturnFailureWhenCsvExportFailsToQueue() {
        // Given
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);

        // When
        ExportResult result = dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // Then
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("failed to start"));
    }

    @Test
    @DisplayName("Should block concurrent exports when already running")
    void exportDataset_shouldBlockConcurrentExportsWhenAlreadyRunning() {
        // Given - start first export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(csvDataSetDataExportService.whoAmI()).thenReturn("CSV Dataset Export");

        dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // When - try to start second export (service is now "running")
        ExportResult result = dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // Then - second export should be blocked
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("please wait"));
    }

    @Test
    @DisplayName("Should allow new export after previous completes")
    void exportDataset_shouldAllowNewExportAfterPreviousCompletes() {
        // Given - start and complete first export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // Complete the export
        dataExportService.complete(true, options.getDataset(), null);

        // When - start new export
        ExportResult result = dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // Then
        assertTrue(result.isSuccess());
    }

    // === exportDatasetOnQuery Tests ===

    @Test
    @DisplayName("Should successfully start CSV query export")
    void exportDatasetOnQuery_shouldSuccessfullyStartCsvQueryExport() {
        // Given
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvQueryExporterService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        // When
        ExportResult result = dataExportService.exportDatasetOnQuery(
                options, searchContext, eventPublisher, exportTaskComplete,
                null, null, null);

        // Then
        assertTrue(result.isSuccess());
        verify(csvQueryExporterService).exportAsCSV(
                eq(options), eq(searchContext), eq(databaseManagementService),
                eq(starService), eq(eventPublisher), eq(exportTaskComplete),
                isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("Should return failure when CSV query export fails to queue")
    void exportDatasetOnQuery_shouldReturnFailureWhenCsvQueryExportFailsToQueue() {
        // Given
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvQueryExporterService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);

        // When
        ExportResult result = dataExportService.exportDatasetOnQuery(
                options, searchContext, eventPublisher, exportTaskComplete,
                null, null, null);

        // Then
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("failed to start"));
    }

    @Test
    @DisplayName("Should block concurrent query exports when already running")
    void exportDatasetOnQuery_shouldBlockConcurrentQueryExportsWhenAlreadyRunning() {
        // Given - start first export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvQueryExporterService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(csvQueryExporterService.whoAmI()).thenReturn("CSV Query Export");

        dataExportService.exportDatasetOnQuery(
                options, searchContext, eventPublisher, exportTaskComplete,
                null, null, null);

        // When - try to start second export
        ExportResult result = dataExportService.exportDatasetOnQuery(
                options, searchContext, eventPublisher, exportTaskComplete,
                null, null, null);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("please wait"));
    }

    // === cancelCurrent Tests ===

    @Test
    @DisplayName("Should cancel running dataset export")
    void cancelCurrent_shouldCancelRunningDatasetExport() {
        // Given - start an export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(csvDataSetDataExportService.cancelExport()).thenReturn(true);

        dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // When
        dataExportService.cancelCurrent();

        // Then
        verify(csvDataSetDataExportService).cancelExport();
    }

    @Test
    @DisplayName("Should cancel running query export")
    void cancelCurrent_shouldCancelRunningQueryExport() {
        // Given - start a query export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvQueryExporterService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(csvQueryExporterService.cancelExport()).thenReturn(true);

        dataExportService.exportDatasetOnQuery(
                options, searchContext, eventPublisher, exportTaskComplete,
                null, null, null);

        // When
        dataExportService.cancelCurrent();

        // Then
        verify(csvQueryExporterService).cancelExport();
    }

    @Test
    @DisplayName("Should do nothing when no export is running")
    void cancelCurrent_shouldDoNothingWhenNoExportIsRunning() {
        // When
        dataExportService.cancelCurrent();

        // Then - no exceptions, no interactions with cancel methods
        verify(csvDataSetDataExportService, never()).cancelExport();
        verify(csvQueryExporterService, never()).cancelExport();
    }

    // === complete Tests ===

    @Test
    @DisplayName("Should reset state on successful completion")
    void complete_shouldResetStateOnSuccessfulCompletion() {
        // Given - start an export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // When
        dataExportService.complete(true, options.getDataset(), null);

        // Then - should be able to start new export
        ExportResult result = dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should reset state on failed completion")
    void complete_shouldResetStateOnFailedCompletion() {
        // Given - start an export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // When
        dataExportService.complete(false, options.getDataset(), "Some error");

        // Then - should be able to start new export
        ExportResult result = dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle null dataset in completion")
    void complete_shouldHandleNullDatasetInCompletion() {
        // When/Then - should not throw
        assertDoesNotThrow(() -> dataExportService.complete(true, null, null));
    }

    // === Concurrency Tests ===

    @Test
    @DisplayName("Should block dataset export when query export is running")
    void concurrency_shouldBlockDatasetExportWhenQueryExportIsRunning() {
        // Given - start query export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvQueryExporterService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(csvQueryExporterService.whoAmI()).thenReturn("CSV Query Export");

        dataExportService.exportDatasetOnQuery(
                options, searchContext, eventPublisher, exportTaskComplete,
                null, null, null);

        // When - try to start dataset export
        ExportResult result = dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("CSV Query Export"));
    }

    @Test
    @DisplayName("Should block query export when dataset export is running")
    void concurrency_shouldBlockQueryExportWhenDatasetExportIsRunning() {
        // Given - start dataset export
        ExportOptions options = createExportOptions(ExportFileType.CSV);
        when(csvDataSetDataExportService.exportAsCSV(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(csvDataSetDataExportService.whoAmI()).thenReturn("CSV Dataset Export");

        dataExportService.exportDataset(
                options, exportTaskComplete, eventPublisher,
                null, null, null);

        // When - try to start query export
        ExportResult result = dataExportService.exportDatasetOnQuery(
                options, searchContext, eventPublisher, exportTaskComplete,
                null, null, null);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("CSV Dataset Export"));
    }

    private ExportOptions createExportOptions(ExportFileType fileType) {
        DataSetDescriptor dataset = new DataSetDescriptor();
        dataset.setDataSetName("TestDataset");

        return ExportOptions.builder()
                .doExport(true)
                .exportFormat(fileType)
                .fileName("test-export.csv")
                .dataset(dataset)
                .build();
    }
}
