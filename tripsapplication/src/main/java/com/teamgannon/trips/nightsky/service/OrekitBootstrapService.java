package com.teamgannon.trips.nightsky.service;

import lombok.extern.slf4j.Slf4j;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One-time Orekit data initialization on application startup.
 * Idempotent - safe to call multiple times.
 */
@Slf4j
@Service
public class OrekitBootstrapService {

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Value("${orekit.data.path:#{systemProperties['user.home']}/orekit-data}")
    private String orekitDataPath;

    /**
     * Initialize Orekit data providers on application startup.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initializeOrekit() {
        if (initialized.compareAndSet(false, true)) {
            try {
                File orekitData = new File(orekitDataPath);
                if (!orekitData.exists()) {
                    log.warn("""
                            Orekit data directory not found at: {}. \
                            Download from https://gitlab.orekit.org/orekit/orekit-data\
                            """,
                            orekitDataPath);
                    return;
                }

                DataProvidersManager manager = DataContext.getDefault()
                        .getDataProvidersManager();
                manager.addProvider(new DirectoryCrawler(orekitData));

                log.info("Orekit initialized successfully from: {}", orekitDataPath);
            } catch (Exception e) {
                log.error("Failed to initialize Orekit: {}", e.getMessage(), e);
                initialized.set(false);
            }
        }
    }

    /**
     * Check if Orekit is properly initialized.
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Force re-initialization (for testing).
     */
    public void reinitialize() {
        initialized.set(false);
        initializeOrekit();
    }
}
