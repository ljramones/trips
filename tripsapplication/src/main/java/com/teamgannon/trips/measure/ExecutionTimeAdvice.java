package com.teamgannon.trips.measure;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAdvice {

    private final MeterRegistry meterRegistry;

    public ExecutionTimeAdvice(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Around advice which measures the time for an object to execute.
     *
     * @param point the advice crosscut point to measure
     * @return return the crosscut point for further execution
     * @throws Throwable if there is an exception thrown
     */
    @Around("@annotation(TrackExecutionTime)")
    public Object executionTime(ProceedingJoinPoint point) throws Throwable {
        long startNanos = System.nanoTime();
        try {
            return point.proceed();
        } finally {
            long durationNanos = System.nanoTime() - startNanos;

            String clazzName = point.getSignature().getDeclaringTypeName();
            String methodName = point.getSignature().getName();

            // Metric name + tags (Micrometer style)
            Timer.builder("trips.method.execution.time")
                    .description("Execution time of methods annotated with @TrackExecutionTime")
                    .tag("class", clazzName)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .record(durationNanos, TimeUnit.NANOSECONDS);

            log.info("Metrics:: Class Name: {}. Method Name: {}. execution time is : {}ms",
                    clazzName, methodName, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        }
    }
}
