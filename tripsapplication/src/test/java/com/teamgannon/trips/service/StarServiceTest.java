package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StarServiceTest {

    @Test
    void plotQueryStreamsAndKeepsNearestTwoThousandStars() {
        StarObjectRepository starObjectRepository = mock(StarObjectRepository.class);
        StarService starService = new StarService(starObjectRepository, mock(DataSetDescriptorRepository.class));
        SearchContext searchContext = new SearchContext();
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName("stream-test");
        searchContext.addDataSet(descriptor);
        AstroSearchQuery query = searchContext.getAstroSearchQuery();
        query.setUpperDistanceLimit(10_000);
        query.setCenterCoordinates(new double[]{0, 0, 0});
        List<StarObject> stars = IntStream.rangeClosed(1, 2_005)
                .mapToObj(distance -> star("star-" + distance, distance))
                .toList();
        when(starObjectRepository.findBySearchQueryStream(query)).thenReturn(stars.stream());

        List<StarObject> result = starService.getAstrographicObjectsOnQuery(searchContext);

        assertThat(result).hasSize(2_000);
        assertThat(result)
                .extracting(StarObject::getDisplayName)
                .doesNotContain("star-2001", "star-2002", "star-2003", "star-2004", "star-2005");
        verify(starObjectRepository).findBySearchQueryStream(query);
        verify(starObjectRepository, never()).findBySearchQuery(query);
    }

    private StarObject star(String name, double x) {
        StarObject star = new StarObject();
        star.setDisplayName(name);
        star.setCommonName(name);
        star.setDataSetName("stream-test");
        star.setX(x);
        star.setY(0);
        star.setZ(0);
        star.setDistance(x);
        return star;
    }
}
