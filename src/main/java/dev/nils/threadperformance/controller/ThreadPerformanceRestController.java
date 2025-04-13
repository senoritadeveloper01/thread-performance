package dev.nils.threadperformance.controller;

import dev.nils.threadperformance.service.ThreadPerformanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/threads")
public class ThreadPerformanceRestController {

    private final ThreadPerformanceService threadPerformanceService;

    public ThreadPerformanceRestController(ThreadPerformanceService threadPerformanceService) {
        this.threadPerformanceService = threadPerformanceService;
    }

    @GetMapping("/platform")
    public Map<String, Object> testPlatformThreads(
            @RequestParam(defaultValue = "1000") int requests,
            @RequestParam(defaultValue = "100") int delayMs) {
        return threadPerformanceService.runPlatformThreadTest(requests, delayMs);
    }

    @GetMapping("/virtual")
    public Map<String, Object> testVirtualThreads(
            @RequestParam(defaultValue = "1000") int requests,
            @RequestParam(defaultValue = "100") int delayMs) {
        return threadPerformanceService.runVirtualThreadTest(requests, delayMs);
    }

    @GetMapping("/compare")
    public Map<String, Object> compareThreads(
            @RequestParam(defaultValue = "1000") int requests,
            @RequestParam(defaultValue = "100") int delayMs) {
        Map<String, Object> platformResults = threadPerformanceService.runPlatformThreadTest(requests, delayMs);
        Map<String, Object> virtualResults = threadPerformanceService.runVirtualThreadTest(requests, delayMs);

        // Fix: Convert Long to double properly using longValue()
        long platformTime = ((Number) platformResults.get("totalTimeMs")).longValue();
        long virtualTime = ((Number) virtualResults.get("totalTimeMs")).longValue();
        long platformMemory = ((Number) platformResults.get("memoryUsedBytes")).longValue();
        long virtualMemory = ((Number) platformResults.get("memoryUsedBytes")).longValue();

        return Map.of(
                "platformThreads", platformResults,
                "virtualThreads", virtualResults,
                "comparison", Map.of(
                        "timeRatio", String.format("%.2fx", (double) platformTime / virtualTime),
                        "memoryDifferenceMB", String.format("%.2f",
                                (platformMemory - virtualMemory) / (1024.0 * 1024.0))
                )
        );
    }
}
