package com.teamgannon.trips.service.exoplanet;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExoPlanetService {

    private final ExoPlanetRepository exoPlanetRepository;

    public ExoPlanetService(ExoPlanetRepository exoPlanetRepository) {
        this.exoPlanetRepository = exoPlanetRepository;
    }

    public void save(ExoPlanet exoPlanet) {
        exoPlanetRepository.save(exoPlanet);
    }

    public void delete(ExoPlanet exoPlanet) {
        exoPlanetRepository.delete(exoPlanet);
    }

    public void deleteAll() {
        exoPlanetRepository.deleteAll();
    }

    public Iterable<ExoPlanet> findAll() {
        return exoPlanetRepository.findAll();
    }

    public Iterable<ExoPlanet> findAll(int page, int size) {
        return exoPlanetRepository.findAll();
    }

    public ExoPlanet findByName(String name) {
        return exoPlanetRepository.findByName(name);
    }

    public Iterable<ExoPlanet> findByStarName(String starName) {
        return exoPlanetRepository.findByStarName(starName);
    }

    public boolean existsByName(String name) {
        return exoPlanetRepository.existsByName(name);
    }

}
