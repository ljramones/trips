package com.teamgannon.trips.config.application;

import com.codahale.metrics.MetricRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

    /**
     * make sure we only have one metrics registry
     *
     * @return the metrics registry
     */
    @Bean
    public MetricRegistry getMetricsRegistry() {
        return new MetricRegistry();
    }

}
