package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.GraphColor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GraphColorRepository extends CrudRepository<GraphColor, String> {
}
