package com.student.fraudsters.services;

import com.student.fraudsters.detection.exception.NotAvailableAlgorithmException;
import com.student.fraudsters.detection.execution.CypherConstants;
import com.student.fraudsters.detection.execution.FraudDetectionEngine;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;

@Service
public class FraudDetectionService {

    private FraudDetectionEngine fraudDetectionEngine;

    public FraudDetectionService(FraudDetectionEngine fraudDetectionEngine) {
        this.fraudDetectionEngine = fraudDetectionEngine;
    }

    public void executeFirstPartyFraudDetection(ResponseBodyEmitter emitter,
                                                String communityDetectionAlgorithm,
                                                String similarityAlgorithm,
                                                String centralityAlgorithm,
                                                double percentileThreshold) {
        verifyExistingAlgorithmEntries(emitter, communityDetectionAlgorithm, CypherConstants.getCommunityDetectionAlgorithms());
        verifyExistingAlgorithmEntries(emitter, similarityAlgorithm, CypherConstants.getSimilarityAlgorithms());
        verifyExistingAlgorithmEntries(emitter, centralityAlgorithm, CypherConstants.getCentralityAlgorithms());
        fraudDetectionEngine.detectFirstPartyFraud(emitter,
                                                   communityDetectionAlgorithm,
                                                   similarityAlgorithm,
                                                   centralityAlgorithm,
                                                   percentileThreshold);
        emitter.complete();
    }

    public void executeSecondPartyFraudDetection(ResponseBodyEmitter emitter,
                                                 String communityDetectionAlgorithm,
                                                 String centralityAlgorithm,
                                                 double minimumScoreForCentralityAlgorithm) {
        verifyExistingAlgorithmEntries(emitter, communityDetectionAlgorithm, CypherConstants.getCommunityDetectionAlgorithms());
        verifyExistingAlgorithmEntries(emitter, centralityAlgorithm, CypherConstants.getCentralityAlgorithms());
        fraudDetectionEngine.detectSecondPartyFraud(emitter, communityDetectionAlgorithm, centralityAlgorithm, minimumScoreForCentralityAlgorithm);
        emitter.complete();
    }

    protected void verifyExistingAlgorithmEntries(ResponseBodyEmitter emitter, String algorithmId, Map<String, Map<String, String>> availableAlgorithms) {
        if (!availableAlgorithms.containsKey(algorithmId)) {
            NotAvailableAlgorithmException exception = new NotAvailableAlgorithmException(algorithmId);
            emitter.completeWithError(exception);
            throw exception;
        }
    }
}
