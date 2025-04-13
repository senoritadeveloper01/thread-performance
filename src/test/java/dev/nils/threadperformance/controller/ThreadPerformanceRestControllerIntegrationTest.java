package dev.nils.threadperformance.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for comparing platform threads and virtual threads performance
 * in a Spring Boot application.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ThreadPerformanceRestControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testPlatformThreads() {
        // Test platform threads with moderate load
        Map<String, Object> result = restTemplate.getForObject(
                createUrl("/api/threads/platform?requests=500&delayMs=50"),
                Map.class
        );

        assertNotNull(result);
        assertEquals("Platform Threads", result.get("threadType"));
        assertTrue((int) result.get("totalTimeMs") > 0);
        assertTrue((double) result.get("throughputRPS") > 0);
    }

    @Test
    void testVirtualThreads() {
        // Test virtual threads with moderate load
        Map<String, Object> result = restTemplate.getForObject(
                createUrl("/api/threads/virtual?requests=500&delayMs=50"),
                Map.class
        );

        assertNotNull(result);
        assertEquals("Virtual Threads", result.get("threadType"));
        assertTrue((int) result.get("totalTimeMs") > 0);
        assertTrue((double) result.get("throughputRPS") > 0);
    }

    @Test
    void compareThreadsPerformance() {
        // Compare performance between virtual and platform threads
        Map<String, Object> result = restTemplate.getForObject(
                createUrl("/api/threads/compare?requests=1000&delayMs=50"),
                Map.class
        );

        assertNotNull(result);
        Map<String, Object> platformResults = (Map<String, Object>) result.get("platformThreads");
        Map<String, Object> virtualResults = (Map<String, Object>) result.get("virtualThreads");
        Map<String, Object> comparison = (Map<String, Object>) result.get("comparison");

        assertNotNull(platformResults);
        assertNotNull(virtualResults);
        assertNotNull(comparison);

        // Extract and convert values properly
        int platformTime = (int) platformResults.get("totalTimeMs");
        int virtualTime = (int) virtualResults.get("totalTimeMs");
        String timeRatio = (String) comparison.get("timeRatio");

        // Virtual threads should be faster for I/O bound tasks
        assertTrue(platformTime >= virtualTime,
                "Platform threads took " + platformTime + "ms while virtual threads took " + virtualTime + "ms");

        // Log the comparison results
        System.out.println("Platform threads time: " + platformTime + "ms");
        System.out.println("Virtual threads time: " + virtualTime + "ms");
        System.out.println("Time ratio: " + timeRatio);
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 500, 1000, 2000})
    void testScalingPerformance(int requests) {
        // Test how both thread types scale with increasing concurrency
        Map<String, Object> result = restTemplate.getForObject(
                createUrl("/api/threads/compare?requests=" + requests + "&delayMs=50"),
                Map.class
        );

        assertNotNull(result);
        Map<String, Object> platformResults = (Map<String, Object>) result.get("platformThreads");
        Map<String, Object> virtualResults = (Map<String, Object>) result.get("virtualThreads");

        double platformThroughput = (double) platformResults.get("throughputRPS");
        double virtualThroughput = (double) virtualResults.get("throughputRPS");
        int platformTime = (int) platformResults.get("totalTimeMs");
        int virtualTime = (int) virtualResults.get("totalTimeMs");

        System.out.println("=== Test with " + requests + " concurrent requests ===");
        System.out.println("Platform threads throughput: " + platformThroughput + " req/s");
        System.out.println("Virtual threads throughput: " + virtualThroughput + " req/s");
        System.out.println("Platform threads time: " + platformTime + "ms");
        System.out.println("Virtual threads time: " + virtualTime + "ms");

        // The performance gap should widen with increased concurrency
        if (requests > 200) {
            assertTrue(virtualThroughput > platformThroughput,
                    "Virtual thread throughput should exceed platform thread throughput at high concurrency");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 50, 100, 200})
    void testIOSensitivity(int delayMs) {
        // Test how both thread types handle different I/O wait times
        int requests = 1000; // Fixed number of requests

        Map<String, Object> result = restTemplate.getForObject(
                createUrl("/api/threads/compare?requests=" + requests + "&delayMs=" + delayMs),
                Map.class
        );

        assertNotNull(result);
        Map<String, Object> platformResults = (Map<String, Object>) result.get("platformThreads");
        Map<String, Object> virtualResults = (Map<String, Object>) result.get("virtualThreads");

        int platformTime = (int) platformResults.get("totalTimeMs");
        int virtualTime = (int) virtualResults.get("totalTimeMs");

        System.out.println("=== Test with " + delayMs + "ms I/O delay ===");
        System.out.println("Platform threads time: " + platformTime + "ms");
        System.out.println("Virtual threads time: " + virtualTime + "ms");
        System.out.println("Advantage ratio: " + (double)platformTime / virtualTime + "x");

        // The advantage of virtual threads should increase with longer I/O delays
        if (delayMs >= 50) {
            assertTrue((double)platformTime / virtualTime >= 1.5,
                    "Virtual threads should have at least 1.5x advantage with significant I/O delay");
        }
    }

    @Test
    void testMemoryEfficiency() throws InterruptedException {
        // Test the memory efficiency of both thread types with high concurrency
        int requests = 5000;
        int delayMs = 100;

        // Force garbage collection before test
        System.gc();
        TimeUnit.SECONDS.sleep(1);

        Map<String, Object> result = restTemplate.getForObject(
                createUrl("/api/threads/compare?requests=" + requests + "&delayMs=" + delayMs),
                Map.class
        );

        assertNotNull(result);
        Map<String, Object> platformResults = (Map<String, Object>) result.get("platformThreads");
        Map<String, Object> virtualResults = (Map<String, Object>) result.get("virtualThreads");
        Map<String, Object> comparison = (Map<String, Object>) result.get("comparison");

        double platformMemoryMB = (double) platformResults.get("memoryUsedMB");
        double virtualMemoryMB = (double) virtualResults.get("memoryUsedMB");
        String memoryDifference = (String) comparison.get("memoryDifferenceMB");

        System.out.println("Platform threads memory: " + platformMemoryMB + " MB");
        System.out.println("Virtual threads memory: " + virtualMemoryMB + " MB");
        System.out.println("Memory difference: " + memoryDifference);

        // This might be environmentally dependent, so we're just logging values
        // rather than making hard assertions about memory usage
    }

    @Test
    void benchmarkHighConcurrencyPerformance() {
        // Test with very high concurrency to stress the system
        int requests = 10000;
        int delayMs = 50;

        Map<String, Object> result = restTemplate.getForObject(
                createUrl("/api/threads/compare?requests=" + requests + "&delayMs=" + delayMs),
                Map.class
        );

        assertNotNull(result);
        Map<String, Object> platformResults = (Map<String, Object>) result.get("platformThreads");
        Map<String, Object> virtualResults = (Map<String, Object>) result.get("virtualThreads");

        double platformThroughput = (double) platformResults.get("throughputRPS");
        double virtualThroughput = (double) virtualResults.get("throughputRPS");

        System.out.println("=== High Concurrency Benchmark (" + requests + " requests) ===");
        System.out.println("Platform threads throughput: " + platformThroughput + " req/s");
        System.out.println("Virtual threads throughput: " + virtualThroughput + " req/s");
        System.out.println("Performance ratio: " + virtualThroughput / platformThroughput + "x");

        // At very high concurrency, virtual threads should greatly outperform platform threads
        assertTrue(virtualThroughput > platformThroughput * 2,
                "Virtual threads should provide at least 2x throughput at high concurrency");
    }

    private String createUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
