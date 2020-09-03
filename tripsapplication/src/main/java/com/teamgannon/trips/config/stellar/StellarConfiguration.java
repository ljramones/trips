package com.teamgannon.trips.config.stellar;

import com.teamgannon.trips.stardata.StellarFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The stellar configuration
 * <p>
 * Created by larrymitchell on 2017-02-19.
 */
@Slf4j
@Configuration
public class StellarConfiguration {

    @Bean
    public StellarFactory stellarFactory() {
        StellarFactory factory = StellarFactory.getFactory();
        StellarFactory.createStellarTypes();

        return factory;
    }

}
