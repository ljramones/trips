package com.teamgannon.trips.spaceshipmodeller.planner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TransferPlanEntity}.
 */
public interface TransferPlanRepository extends JpaRepository<TransferPlanEntity, String> {

    List<TransferPlanEntity> findAllByOrderByCreatedAtDesc();

    List<TransferPlanEntity> findByShipId(String shipId);

    List<TransferPlanEntity> findBySolarSystemId(String solarSystemId);
}
