package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 * Used to manage all the database interactions
 * <p>
 * Created by larrymitchell on 2017-01-20.
 */
@Slf4j
@Service("dbservice")
public class DatabaseManagementService {

    private static final int MAX_REQUEST_SIZE = 9999;

    /**
     * storage of data sets in DB
     */
    private final DataSetDescriptorRepository dataSetDescriptorRepository;

    /**
     * storage of astrographic objects in DB
     */
    private final StarObjectRepository starObjectRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * constructor
     *
     * @param dataSetDescriptorRepository the data descriptor repo
     * @param starObjectRepository        the astrographic objects
     */
    public DatabaseManagementService(DataSetDescriptorRepository dataSetDescriptorRepository,
                                     StarObjectRepository starObjectRepository) {

        this.dataSetDescriptorRepository = dataSetDescriptorRepository;
        this.starObjectRepository = starObjectRepository;
    }

}
