package com.teamgannon.trips.config.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TRIPSConfiguration {

    @Bean
    public TripsContext tripsContext() {
        return new TripsContext();
    }

}
