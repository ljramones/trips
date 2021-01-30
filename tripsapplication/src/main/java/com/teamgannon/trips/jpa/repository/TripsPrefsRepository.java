package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.TripsPrefs;
import org.springframework.data.repository.CrudRepository;

public interface TripsPrefsRepository extends CrudRepository<TripsPrefs, String> {
}
