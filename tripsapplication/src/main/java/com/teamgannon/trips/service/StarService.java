package com.teamgannon.trips.service;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.dialogs.db.DBReference;
import com.teamgannon.trips.dialogs.search.model.StarDistances;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class StarService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final int MAX_REQUEST_SIZE = 9999;

    private final StarObjectRepository starObjectRepository;
    private final DatasetService datasetService;

    public StarService(StarObjectRepository starObjectRepository,
                       DatasetService datasetService) {
        this.starObjectRepository = starObjectRepository;
        this.datasetService = datasetService;
    }

}
