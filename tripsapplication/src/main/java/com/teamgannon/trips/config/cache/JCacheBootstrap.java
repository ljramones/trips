package com.teamgannon.trips.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.io.IOException;
import java.net.URI;

/**
 * Hands a fully-built JCache {@link CacheManager} (using EhCache 3.10 as the
 * JSR-107 provider, configured by {@code ehcache.xml} on the classpath) to
 * Hibernate's L2 cache machinery (Phase 7 / Issue 53).
 * <p>
 * Why this exists: JCache 1.0's URI-based config discovery
 * ({@code URI.create("classpath:ehcache.xml")}) doesn't resolve Spring's
 * classpath prefix, so the simpler {@code hibernate.javax.cache.uri}
 * property doesn't work. Instead we read the classpath resource ourselves,
 * pre-build the {@code CacheManager}, and hand it to Hibernate via the
 * {@link ConfigSettings#CACHE_MANAGER} property at SessionFactory build
 * time. Hibernate then uses our instance instead of creating its own with
 * the provider-default URI.
 *
 * <h2>Verification</h2>
 * On startup look for one of:
 * <ul>
 *   <li>{@code "Bootstrapping Hibernate L2 cache via JCache provider ..."} — happy path</li>
 *   <li>{@code "Hibernate L2 cache config 'ehcache.xml' not found on classpath"} — falls back to JSR-107 defaults</li>
 * </ul>
 */
@Slf4j
@Configuration
public class JCacheBootstrap {

    private static final String EHCACHE_CLASSPATH = "ehcache.xml";
    private static final String DEFAULT_PROVIDER = "org.ehcache.jsr107.EhcacheCachingProvider";

    private final ApplicationContext applicationContext;

    public JCacheBootstrap(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Build the JCache {@link CacheManager} from {@code ehcache.xml} on the
     * classpath. The bean is also handed to Hibernate via the customiser
     * below, so the same instance is shared everywhere.
     */
    @Bean
    public CacheManager jakartaCacheManager() throws IOException {
        CachingProvider provider = Caching.getCachingProvider(DEFAULT_PROVIDER);
        Resource resource = applicationContext.getResource("classpath:" + EHCACHE_CLASSPATH);
        if (!resource.exists()) {
            log.warn("Hibernate L2 cache config '{}' not found on classpath; "
                    + "using provider-default cache regions.", EHCACHE_CLASSPATH);
            return provider.getCacheManager();
        }
        URI uri = resource.getURI();
        log.info("Bootstrapping Hibernate L2 cache via JCache provider {} with config {}",
                DEFAULT_PROVIDER, uri);
        return provider.getCacheManager(uri, getClass().getClassLoader());
    }

    /**
     * Hand our pre-built {@link CacheManager} to Hibernate so it doesn't
     * spin up its own with the provider-default URI (which would ignore
     * {@code ehcache.xml}).
     */
    @Bean
    public HibernatePropertiesCustomizer hibernateJCachePropertiesCustomizer(CacheManager cacheManager) {
        return properties -> properties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }
}
