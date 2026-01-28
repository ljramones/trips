package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.AppRegistration;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for AppRegistration entity.
 * Since there's only one registration record, use findById(AppRegistration.REGISTRATION_ID).
 */
public interface AppRegistrationRepository extends CrudRepository<AppRegistration, String> {
}
