package com.student.fraudsters.controllers;

import com.student.fraudsters.services.FraudDetectionService;
import com.student.fraudsters.detection.execution.CypherConstants;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/frauds")
public class FraudDetectionController {

    public static final int FRAUD_DETECTION_EXECUTION_TIMEOUT_MINUTES = 20;

    private ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/first-party/detection")
    public SseEmitter detectFirstPartyFraud(@RequestParam String communityDetectionAlgorithm,
                                            @RequestParam String similarityAlgorithm,
                                            @RequestParam String centralityAlgorithm,
                                            @RequestParam double percentileThreshold) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(FRAUD_DETECTION_EXECUTION_TIMEOUT_MINUTES));
        nonBlockingService.execute(() -> fraudDetectionService.executeFirstPartyFraudDetection(emitter, communityDetectionAlgorithm,
                                                                                               similarityAlgorithm, centralityAlgorithm,
                                                                                               percentileThreshold));
        return emitter;
    }

    @PostMapping("/second-party/detection")
    public SseEmitter detectSecondPartyFraud(@RequestParam String communityDetectionAlgorithm,
                                             @RequestParam String centralityAlgorithm,
                                             @RequestParam double minimumScoreForCentralityAlgorithm) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(FRAUD_DETECTION_EXECUTION_TIMEOUT_MINUTES));
        nonBlockingService.execute(() -> fraudDetectionService.executeSecondPartyFraudDetection(emitter,
                                                                                                communityDetectionAlgorithm,
                                                                                                centralityAlgorithm,
                                                                                                minimumScoreForCentralityAlgorithm));
        return emitter;
    }

    @GetMapping("/algorithms/community-detection")
    public Map<String, Map<String, String>> getAvailableCommunityDetectionAlgorithms() {
        return CypherConstants.getCommunityDetectionAlgorithms();
    }

    @GetMapping("/algorithms/centrality")
    public Map<String, Map<String, String>> getAvailableCentralityAlgorithms() {
        return CypherConstants.getCentralityAlgorithms();
    }

    @GetMapping("/algorithms/similarity")
    public Map<String, Map<String, String>> getAvailableSimilarityAlgorithms() {
        return CypherConstants.getSimilarityAlgorithms();
    }

}
