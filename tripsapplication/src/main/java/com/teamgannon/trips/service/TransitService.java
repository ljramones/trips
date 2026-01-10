package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.TransitSettings;
import com.teamgannon.trips.jpa.repository.TransitSettingsRepository;
import com.teamgannon.trips.measure.TrackExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class TransitService {


    /**
     * transit settings
     */
    private final TransitSettingsRepository transitSettingsRepository;

    public TransitService(TransitSettingsRepository transitSettingsRepository) {
        this.transitSettingsRepository = transitSettingsRepository;
    }

    @TrackExecutionTime
    @Transactional
    public TransitSettings getTransitSettings() {
        Optional<TransitSettings> transitSettingsOptional = transitSettingsRepository.findById("main");

        if (transitSettingsOptional.isPresent()) {
            return transitSettingsOptional.get();
        } else {
            TransitSettings transitSettings = new TransitSettings();
            transitSettings.setId("main");
            transitSettingsRepository.save(transitSettings);
            return transitSettings;
        }
    }

    @Transactional
    public void setTransitSettings(TransitSettings transitSettings) {
        transitSettingsRepository.save(transitSettings);
    }

}
