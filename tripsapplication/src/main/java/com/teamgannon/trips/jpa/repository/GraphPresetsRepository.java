package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.GraphPersistValues;
import org.springframework.data.repository.CrudRepository;

public interface GraphPresetsRepository extends CrudRepository<GraphPersistValues, String> {
}
