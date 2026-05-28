package com.teamgannon.trips.service.performance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HibernateCacheBenchmarkServiceTest {

    @Test
    void resultFormatsTimingAndHitRate() {
        HibernateCacheBenchmarkService.CacheBenchmarkResult result =
                new HibernateCacheBenchmarkService.CacheBenchmarkResult(
                        "DataSetDescriptor",
                        "nearby",
                        4,
                        2_000_000,
                        4_000_000,
                        3,
                        1,
                        1);

        assertThat(result.coldReadMillis()).isEqualTo(2.0);
        assertThat(result.averageWarmReadMillis()).isEqualTo(1.0);
        assertThat(result.hitRatePercent()).isEqualTo(75.0);
        assertThat(result.summary())
                .contains("DataSetDescriptor cache benchmark id=nearby")
                .contains("cold=2.000 ms")
                .contains("warm avg=1.000 ms")
                .contains("hitRate=75.0%");
    }
}
