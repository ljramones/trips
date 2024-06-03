package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface CivilizationDisplayPreferencesRepository extends PagingAndSortingRepository<CivilizationDisplayPreferences, String> {

    @NotNull
    Optional<CivilizationDisplayPreferences> findByStorageTag(String s);

}
