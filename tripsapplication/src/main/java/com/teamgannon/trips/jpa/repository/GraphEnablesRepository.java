package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import org.springframework.data.repository.CrudRepository;

public interface GraphEnablesRepository extends CrudRepository<GraphEnablesPersist, String> {
}
