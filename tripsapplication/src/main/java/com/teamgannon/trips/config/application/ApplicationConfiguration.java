package com.teamgannon.trips.config.application;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public @NotNull TripsContext getTripsContext() {
        return new TripsContext();
    }

}
