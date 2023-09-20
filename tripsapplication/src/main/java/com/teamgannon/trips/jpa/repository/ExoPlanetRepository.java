package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ExoPlanetRepository extends PagingAndSortingRepository<ExoPlanet, String> {
}
