package com.teamgannon.trips.measure;

import com.codahale.metrics.MetricRegistry;
import com.teamgannon.trips.config.application.MetricsConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
//@ConditionalOnExpression("${aspect.enabled.true}")
public class ExecutionTimeAdvice {

    private MetricRegistry metricRegistry;

    public ExecutionTimeAdvice(MetricsConfiguration metricsConfiguration) {
        this.metricRegistry = metricsConfiguration.getMetricsRegistry();
    }

    /**
     * around advice which measures the time for an object to execute
     *
     * @param point the advice crosscut point to measure
     * @return return the crosscut point for further execution
     * @throws Throwable if there is an execution thrown
     */
    @Around("@annotation(TrackExecutionTime)")
    public Object executionTime(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object object = point.proceed();
        long endTime = System.currentTimeMillis();
        String clazzName = point.getSignature().getDeclaringTypeName();
        String methodName = point.getSignature().getName();
        long timeInMs = endTime - startTime;

        log.info("Metrics:: Class Name:" + clazzName +
                ". Method Name: " + methodName +
                ". execution time is : " +
                "%,d".formatted(timeInMs) + "ms"
        );
        return object;
    }
}
