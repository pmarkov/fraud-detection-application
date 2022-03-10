package com.student.fraudsters.detection.execution.task;

import com.student.fraudsters.detection.execution.task.algorith_execution.CentralityAlgorithmTask;
import com.student.fraudsters.detection.execution.task.algorith_execution.CommunityDetectionAlgorithmTask;
import com.student.fraudsters.detection.execution.task.algorith_execution.SimilarityAlgorithmTask;
import com.student.fraudsters.detection.execution.CypherConstants;
import com.student.fraudsters.detection.execution.task.algorith_execution.ExecuteGdsAlgorithmTask;

import java.util.Map;

public class TaskFactory {

    public static final String SECOND_PARTY_FRAUD_GROUP_PROPERTY = "secondPartyFraudGroup";
    public static final String FIRST_PARTY_FRAUD_GROUP_PROPERTY = "firstPartyFraudGroup";

    public static CreateRelationshipsTask getCreateSharedIdentifiersRelationshipTask(String relationshipType) {
        return new CreateRelationshipsTask(
                relationshipType,
                CypherConstants.CREATE_RELATIONSHIP_BETWEEN_CLIENTS_WITH_MUTUAL_IDENTIFIERS,
                Map.of("relationshipType", relationshipType)
        );
    }

    public static CreateGraphProjectionTask getCreateGraphProjectionForClientsWithSharedIdentifiersTask(String graphProjectionName) {
        return new CreateGraphProjectionTask(
                graphProjectionName,
                CypherConstants.GRAPH_PROJECTION_FOR_CLIENTS_WITH_SHARED_IDENTIFIERS_TEMPLATE,
                Map.of("projectionName", graphProjectionName)
        );
    }

    public static ExecuteGdsAlgorithmTask getCommunityDetectionForClientClustersTask(String algorithmId, String graphProjectionName) {
        String algorithmName = CypherConstants.getCommunityDetectionAlgorithms().get(algorithmId).get("name");
        String algorithmReturnType = CypherConstants.getCommunityDetectionAlgorithms().get(algorithmId).get("returnType");
        String algorithmProcedureInvocation = CypherConstants.getAlgorithmQualityTierForIdMap().get(algorithmId).procedureInvocation;
        return new CommunityDetectionAlgorithmTask(
                algorithmName,
                CypherConstants.RUN_COMMUNITY_DETECTION_ALGORITHM_FOR_CLIENT_CLUSTERS_TEMPLATE,
                Map.of("algorithmType", algorithmProcedureInvocation,
                        "algorithmId", algorithmId,
                        "returnType", algorithmReturnType,
                        "projectionName", graphProjectionName,
                        "writeProperty", FIRST_PARTY_FRAUD_GROUP_PROPERTY)
        );
    }

    public static ExecuteGdsAlgorithmTask getSimilarityAlgorithmForClientsInFraudRingsTask(String algorithmId, String graphProjectionName) {
        String algorithmName = CypherConstants.getSimilarityAlgorithms().get(algorithmId).get("name");
        String algorithmProcedureInvocation = CypherConstants.getAlgorithmQualityTierForIdMap().get(algorithmId).procedureInvocation;
        return new SimilarityAlgorithmTask(
                algorithmName,
                CypherConstants.RUN_SIMILARITY_ALGORITHM_FOR_CLIENTS_IN_FRAUD_RINGS_TEMPLATE,
                Map.of("algorithmType", algorithmProcedureInvocation,
                        "algorithmId", algorithmId,
                        "projectionName", graphProjectionName)
        );
    }

    public static ExecuteGdsAlgorithmTask getCentralityAlgorithmForSimilarClientsInFraudRingsTask(String algorithmId) {
        String algorithmName = CypherConstants.getCentralityAlgorithms().get(algorithmId).get("name");
        String algorithmProcedureInvocation = CypherConstants.getAlgorithmQualityTierForIdMap().get(algorithmId).procedureInvocation;
        return new CentralityAlgorithmTask(
                algorithmName,
                true,
                CypherConstants.RUN_CENTRALITY_ALGORITHM_FOR_CLIENTS_IN_FRAUD_RINGS_TEMPLATE,
                Map.of("algorithmType", algorithmProcedureInvocation,
                        "algorithmId", algorithmId)
        );
    }

    public static CreateGraphProjectionTask getCreateGraphProjectionForFirstPartyFraudstersTask(String graphProjectionName) {
        return new CreateGraphProjectionTask(
                graphProjectionName,
                CypherConstants.GRAPH_PROJECTION_FOR_FIRST_PARTY_FRAUDSTERS_SIMILARITY_TEMPLATE,
                Map.of("projectionName", graphProjectionName, "fraudGroupProperty", FIRST_PARTY_FRAUD_GROUP_PROPERTY)
        );
    }

    public static DropGraphProjectionTask getDropGraphProjectionTask(String graphProjectionName) {
        return new DropGraphProjectionTask(
                graphProjectionName,
                CypherConstants.DROP_GRAPH_PROJECTION_TEMPLATE,
                Map.of("projectionName", graphProjectionName)
        );
    }

    public static AddNodeLabelsTask getAddNodeLabelsBasedOnFirstPartyFraudScoreTask(double percentileThreshold) {
        String labelName = "FirstPartyFraudster";
        return new AddNodeLabelsTask(
                labelName,
                CypherConstants.ADD_NODE_LABELS_BASED_ON_FIRST_FRAUD_SCORE,
                Map.of("label", labelName, "percentileThreshold", String.valueOf(percentileThreshold))
        );
    }

    public static CreateRelationshipsTask getCreateRelationshipsFromFraudstersToClientsTask(String relationshipType) {
        String propertyName = "amount";
        return new CreateRelationshipsTask(
                relationshipType,
                CypherConstants.ADD_TRANSFER_RELATIONSHIP_FROM_FRAUDSTER_TO_CLIENT,
                Map.of("firstPartyFraudsterLabel", "FirstPartyFraudster",
                       "propertyName", propertyName, "relationshipType", relationshipType)
        );
    }

    public static CreateRelationshipsTask getCreateRelationshipsFromClientsToFraudsters(String relationshipType) {
        String propertyName = "amount";
        return new CreateRelationshipsTask(
                relationshipType,
                CypherConstants.ADD_TRANSFER_RELATIONSHIP_FROM_CLIENT_TO_FRAUDSTER,
                Map.of("firstPartyFraudsterLabel", "FirstPartyFraudster",
                        "propertyName", propertyName, "relationshipType", relationshipType)
        );
    }

    public static CreateGraphProjectionTask getCreateSecondPartyFraudNetworkGraphProjectionTask(String graphProjectionName, String relationshipType) {
        return new CreateGraphProjectionTask(
                graphProjectionName,
                CypherConstants.CREATE_SECOND_PARTY_FRAUD_NETWORK_GRAPH_PROJECTION,
                Map.of("projectionName", graphProjectionName, "relationshipType", relationshipType)
        );
    }

    public static CommunityDetectionAlgorithmTask getCommunityDetectionAlgorithmForSecondPartyFraudGroupingTask(String algorithmId, String graphProjectionName) {
        String algorithmName = CypherConstants.getCommunityDetectionAlgorithms().get(algorithmId).get("name");
        String algorithmReturnType = CypherConstants.getCommunityDetectionAlgorithms().get(algorithmId).get("returnType");
        String algorithmProcedureInvocation = CypherConstants.getAlgorithmQualityTierForIdMap().get(algorithmId).procedureInvocation;
        return new CommunityDetectionAlgorithmTask(
                algorithmName,
                CypherConstants.RUN_COMMUNITY_DETECTION_ALGORITHM_FOR_CLIENT_CLUSTERS_TEMPLATE,
                Map.of("algorithmType", algorithmProcedureInvocation,
                        "algorithmId", algorithmId,
                        "returnType", algorithmReturnType,
                        "projectionName", graphProjectionName,
                        "writeProperty", SECOND_PARTY_FRAUD_GROUP_PROPERTY)
        );
    }

    public static CentralityAlgorithmTask getCentralityAlgorithmForSecondPartyFraudScore(String algorithmId, String graphProjectionName, double minimumScore) {
        String algorithmName = CypherConstants.getCentralityAlgorithms().get(algorithmId).get("name");
        String algorithmProcedureInvocation = CypherConstants.getAlgorithmQualityTierForIdMap().get(algorithmId).procedureInvocation;
        return new CentralityAlgorithmTask(
                algorithmName,
                false,
                CypherConstants.RUN_CENTRALITY_ALGORITHM_FOR_SECOND_PARTY_FRAUD_SCORE,
                Map.of("algorithmType", algorithmProcedureInvocation,
                       "algorithmId", algorithmId,
                       "projectionName", graphProjectionName,
                       "firstPartyFraudsterLabel", "FirstPartyFraudster",
                       "minimumScore", String.valueOf(minimumScore),
                       "fraudGroupProperty", SECOND_PARTY_FRAUD_GROUP_PROPERTY)
        );
    }
}
