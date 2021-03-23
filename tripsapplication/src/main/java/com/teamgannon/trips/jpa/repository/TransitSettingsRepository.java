package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.TransitSettings;
import org.springframework.data.repository.CrudRepository;

public interface TransitSettingsRepository extends CrudRepository<TransitSettings, String> {
}
