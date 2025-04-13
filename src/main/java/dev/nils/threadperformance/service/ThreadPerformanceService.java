package dev.nils.threadperformance.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
public class ThreadPerformanceService {

    private static final int PLATFORM_THREAD_POOL_SIZE = 200;

    public Map<String, Object> runPlatformThreadTest(int requests, int delayMs) {
        System.gc(); // Attempt to clean up before test
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        ExecutorService executorService = Executors.newFixedThreadPool(PLATFORM_THREAD_POOL_SIZE);

        Instant start = Instant.now();

        try {
            CompletableFuture<?>[] futures = IntStream.range(0, requests)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> simulateWork(delayMs), executorService))
                    .toArray(CompletableFuture<?>[]::new);

            CompletableFuture.allOf(futures).join();
        } finally {
            executorService.shutdown();
        }

        Instant end = Instant.now();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return collectMetrics(requests, delayMs, start, end, startMemory, endMemory, "Platform Threads");
    }

    public Map<String, Object> runVirtualThreadTest(int requests, int delayMs) {
        System.gc(); // Attempt to clean up before test
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        Instant start = Instant.now();

        try {
            CompletableFuture<?>[] futures = IntStream.range(0, requests)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> simulateWork(delayMs), executorService))
                    .toArray(CompletableFuture<?>[]::new);

            CompletableFuture.allOf(futures).join();
        } finally {
            executorService.shutdown();
        }

        Instant end = Instant.now();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return collectMetrics(requests, delayMs, start, end, startMemory, endMemory, "Virtual Threads");
    }

    private void simulateWork(int delayMs) {
        try {
            // Simulate I/O-bound work (like a network call or database query)
            Thread.sleep(delayMs);

            // Simulate some CPU work
            double result = 0;
            for (int i = 0; i < 100_000; i++) {
                result += Math.sqrt(i);
            }

            // Prevent JIT optimization from removing the loop
            if (result < 0) {
                throw new IllegalStateException("This should never happen");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<String, Object> collectMetrics(
            int requests,
            int delayMs,
            Instant start,
            Instant end,
            long startMemory,
            long endMemory,
            String threadType) {

        long totalTimeMs = Duration.between(start, end).toMillis();
        long memoryUsedBytes = endMemory - startMemory;

        return Map.of(
                "threadType", threadType,
                "requests", requests,
                "delayMs", delayMs,
                "totalTimeMs", totalTimeMs,
                "avgTimePerRequestMs", (double) totalTimeMs / requests,
                "throughputRPS", (double) requests * 1000 / totalTimeMs,
                "memoryUsedBytes", memoryUsedBytes,
                "memoryUsedMB", memoryUsedBytes / (1024.0 * 1024.0)
        );
    }
}
