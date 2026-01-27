package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CivilizationDisplayPreferencesRepository extends JpaRepository<CivilizationDisplayPreferences, String> {

    @NotNull
    Optional<CivilizationDisplayPreferences> findByStorageTag(String s);

}
