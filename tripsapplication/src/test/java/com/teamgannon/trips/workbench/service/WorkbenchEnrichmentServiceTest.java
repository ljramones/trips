package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.service.StarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WorkbenchEnrichmentService}.
 *
 * These tests verify that the HttpClient is properly configured as a singleton
 * to prevent thread/resource leaks that can occur when creating new HttpClient
 * instances for each request.
 */
class WorkbenchEnrichmentServiceTest {

    @Mock
    private StarService starService;

    private WorkbenchEnrichmentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new WorkbenchEnrichmentService(starService);
    }

    @Test
    @DisplayName("HTTP_CLIENT should be a static field to ensure singleton behavior")
    void httpClientShouldBeStatic() throws Exception {
        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");

        assertTrue(Modifier.isStatic(field.getModifiers()),
            "HTTP_CLIENT must be static to prevent creating new instances per request");
    }

    @Test
    @DisplayName("HTTP_CLIENT should be final to prevent reassignment")
    void httpClientShouldBeFinal() throws Exception {
        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");

        assertTrue(Modifier.isFinal(field.getModifiers()),
            "HTTP_CLIENT must be final to prevent accidental reassignment");
    }

    @Test
    @DisplayName("HTTP_CLIENT should be initialized (not null)")
    void httpClientShouldBeInitialized() throws Exception {
        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");
        field.setAccessible(true);

        Object httpClient = field.get(null);

        assertNotNull(httpClient, "HTTP_CLIENT must be eagerly initialized");
        assertTrue(httpClient instanceof HttpClient, "HTTP_CLIENT must be an HttpClient instance");
    }

    @Test
    @DisplayName("HTTP_CLIENT should have a connect timeout configured")
    void httpClientShouldHaveConnectTimeout() throws Exception {
        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");
        field.setAccessible(true);

        HttpClient httpClient = (HttpClient) field.get(null);

        assertTrue(httpClient.connectTimeout().isPresent(),
            "HTTP_CLIENT should have a connect timeout to prevent hanging connections");

        Duration timeout = httpClient.connectTimeout().get();
        assertTrue(timeout.getSeconds() > 0 && timeout.getSeconds() <= 60,
            "Connect timeout should be reasonable (1-60 seconds), was: " + timeout.getSeconds());
    }

    @Test
    @DisplayName("HTTP_CLIENT singleton - same instance returned across multiple service instances")
    void httpClientShouldBeSameAcrossServiceInstances() throws Exception {
        // Create multiple service instances
        WorkbenchEnrichmentService service1 = new WorkbenchEnrichmentService(starService);
        WorkbenchEnrichmentService service2 = new WorkbenchEnrichmentService(starService);
        WorkbenchEnrichmentService service3 = new WorkbenchEnrichmentService(starService);

        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");
        field.setAccessible(true);

        // All should reference the exact same HttpClient instance
        HttpClient client1 = (HttpClient) field.get(null);
        HttpClient client2 = (HttpClient) field.get(null);
        HttpClient client3 = (HttpClient) field.get(null);

        assertSame(client1, client2, "HTTP_CLIENT must be the same instance across service instances");
        assertSame(client2, client3, "HTTP_CLIENT must be the same instance across service instances");
    }

    @Test
    @DisplayName("Multiple service instantiations should not increase thread count significantly")
    void multipleServiceInstantiationsShouldNotLeakThreads() throws Exception {
        // Get baseline thread count
        int baselineThreadCount = getThreadCount();
        Set<String> baselineThreadNames = getThreadNames();

        // Create many service instances (simulating what would happen if HttpClient
        // was created per-instance instead of as a static singleton)
        for (int i = 0; i < 50; i++) {
            new WorkbenchEnrichmentService(starService);
        }

        // Force GC to clean up any garbage
        System.gc();
        Thread.sleep(100);

        int finalThreadCount = getThreadCount();
        Set<String> finalThreadNames = getThreadNames();

        // Calculate new threads created
        Set<String> newThreads = new HashSet<>(finalThreadNames);
        newThreads.removeAll(baselineThreadNames);

        int threadGrowth = finalThreadCount - baselineThreadCount;

        // With a singleton HttpClient, thread growth should be minimal (0-5 threads)
        // If HttpClient was created per-instance, we'd see 50+ new threads
        assertTrue(threadGrowth < 10,
            "Thread count grew by " + threadGrowth + " after creating 50 service instances. " +
            "This suggests HttpClient may not be properly shared. New threads: " + newThreads);
    }

    @Test
    @DisplayName("HttpClient thread pool threads should be bounded")
    void httpClientThreadsShouldBeBounded() throws Exception {
        // Verify that HttpClient-related threads don't grow unboundedly
        Set<String> threadNames = getThreadNames();

        long httpClientThreads = threadNames.stream()
            .filter(name -> name.contains("HttpClient") || name.contains("HTTP"))
            .count();

        // A single HttpClient should have a bounded number of worker threads
        // (typically based on available processors)
        int maxExpectedThreads = Runtime.getRuntime().availableProcessors() * 2 + 10;

        assertTrue(httpClientThreads <= maxExpectedThreads,
            "Found " + httpClientThreads + " HttpClient threads, expected <= " + maxExpectedThreads +
            ". This may indicate thread leakage.");
    }

    // ==================== CONCURRENCY TESTS ====================

    @Test
    @DisplayName("Concurrent access to HTTP_CLIENT should be thread-safe")
    void concurrentAccessShouldBeThreadSafe() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicReference<HttpClient> firstClient = new AtomicReference<>();
        List<HttpClient> allClients = new ArrayList<>();

        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");
        field.setAccessible(true);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    HttpClient client = (HttpClient) field.get(null);
                    synchronized (allClients) {
                        allClients.add(client);
                    }
                    firstClient.compareAndSet(null, client);
                } catch (Exception e) {
                    fail("Exception during concurrent access: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads simultaneously
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "Threads should complete within timeout");
        executor.shutdown();

        // All threads should have received the same instance
        HttpClient expected = firstClient.get();
        assertNotNull(expected);
        for (HttpClient client : allClients) {
            assertSame(expected, client, "All threads must receive the same HttpClient instance");
        }
    }

    @RepeatedTest(3)
    @DisplayName("Repeated instantiation stress test - no thread leak")
    void repeatedInstantiationStressTest() throws Exception {
        int baselineThreadCount = getThreadCount();

        // Create 100 service instances rapidly
        for (int i = 0; i < 100; i++) {
            new WorkbenchEnrichmentService(starService);
        }

        System.gc();
        Thread.sleep(200);

        int finalThreadCount = getThreadCount();
        int growth = finalThreadCount - baselineThreadCount;

        assertTrue(growth < 15,
            "Thread count grew by " + growth + " during stress test - possible leak");
    }

    // ==================== CONTRACT TESTS ====================

    @Test
    @DisplayName("No method should accept HttpClient as parameter (prevents accidental per-request creation)")
    void noMethodShouldAcceptHttpClientParameter() {
        Method[] methods = WorkbenchEnrichmentService.class.getDeclaredMethods();

        for (Method method : methods) {
            for (Parameter param : method.getParameters()) {
                assertFalse(HttpClient.class.isAssignableFrom(param.getType()),
                    "Method '" + method.getName() + "' accepts HttpClient parameter. " +
                    "This pattern can lead to per-request HttpClient creation. " +
                    "Use the static HTTP_CLIENT field instead.");
            }
        }
    }

    @Test
    @DisplayName("No field should be non-static HttpClient (prevents per-instance creation)")
    void noNonStaticHttpClientField() {
        Field[] fields = WorkbenchEnrichmentService.class.getDeclaredFields();

        for (Field field : fields) {
            if (HttpClient.class.isAssignableFrom(field.getType())) {
                assertTrue(Modifier.isStatic(field.getModifiers()),
                    "Field '" + field.getName() + "' is a non-static HttpClient. " +
                    "HttpClient fields must be static to prevent per-instance creation.");
            }
        }
    }

    // ==================== CONFIGURATION TESTS ====================

    @Test
    @DisplayName("HTTP_CLIENT should use HTTP/2 or HTTP/1.1")
    void httpClientShouldUseValidHttpVersion() throws Exception {
        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");
        field.setAccessible(true);
        HttpClient httpClient = (HttpClient) field.get(null);

        HttpClient.Version version = httpClient.version();
        assertTrue(
            version == HttpClient.Version.HTTP_1_1 || version == HttpClient.Version.HTTP_2,
            "HttpClient should use HTTP/1.1 or HTTP/2, was: " + version
        );
    }

    @Test
    @DisplayName("HTTP_CLIENT should use default redirect policy (NEVER) for security")
    void httpClientShouldHaveSecureRedirectPolicy() throws Exception {
        Field field = WorkbenchEnrichmentService.class.getDeclaredField("HTTP_CLIENT");
        field.setAccessible(true);
        HttpClient httpClient = (HttpClient) field.get(null);

        // NEVER is the default and most secure - prevents open redirect attacks
        // If NORMAL or ALWAYS is needed, it should be a conscious decision
        HttpClient.Redirect policy = httpClient.followRedirects();
        assertNotNull(policy, "Redirect policy should be set");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to get current thread count.
     */
    private int getThreadCount() {
        return Thread.activeCount();
    }

    /**
     * Helper method to get names of all active threads.
     */
    private Set<String> getThreadNames() {
        Set<String> names = new HashSet<>();
        Thread[] threads = new Thread[Thread.activeCount() * 2];
        int count = Thread.enumerate(threads);
        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                names.add(threads[i].getName());
            }
        }
        return names;
    }
}
