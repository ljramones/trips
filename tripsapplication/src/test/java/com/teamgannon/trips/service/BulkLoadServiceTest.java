package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BulkLoadServiceTest {

    @Test
    void removeDataSetUsesBulkStarDeleteBeforeDescriptorDelete() {
        StarObjectRepository starObjectRepository = mock(StarObjectRepository.class);
        DataSetDescriptorRepository dataSetDescriptorRepository = mock(DataSetDescriptorRepository.class);
        BulkLoadService service = new BulkLoadService(
                mock(StarService.class),
                mock(DatasetService.class),
                mock(DatabaseManagementService.class),
                dataSetDescriptorRepository,
                starObjectRepository
        );
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName("test-dataset");
        when(starObjectRepository.deleteByDataSetName("test-dataset")).thenReturn(42);

        service.removeDataSet(descriptor);

        InOrder orderedDeletes = inOrder(starObjectRepository, dataSetDescriptorRepository);
        orderedDeletes.verify(starObjectRepository).deleteByDataSetName("test-dataset");
        orderedDeletes.verify(dataSetDescriptorRepository).delete(descriptor);
    }
}
