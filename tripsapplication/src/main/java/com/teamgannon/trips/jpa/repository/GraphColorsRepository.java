package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.GraphColorsPersist;
import org.springframework.data.repository.CrudRepository;

public interface GraphColorsRepository extends CrudRepository<GraphColorsPersist, String> {
}
