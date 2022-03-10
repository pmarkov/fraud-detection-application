package com.student.fraudsters.detection.execution;

import com.student.fraudsters.detection.execution.task.TaskFactory;
import com.student.fraudsters.detection.execution.task.core.Task;
import com.student.fraudsters.detection.exception.FraudDetectionTaskFailedException;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FraudDetectionEngine {

    private static final String FIRST_PARTY_FRAUD_CLIENTS_COMMUNITY = "FraudRings";
    private static final String SHARED_IDENTIFIERS_RELATIONSHIP = "SHARED_IDENTIFIERS";
    private static final String FIRST_PARTY_SIMILAR_FRAUDSTERS = "SimilarFraudsters";
    private static final String TRANSFER_TO_RELATIONSHIP = "TRANSFER_TO";
    public static final String SECOND_PARTY_FRAUD_NETWORK = "SecondPartyFraudNetwork";

    private final Neo4jClient neo4jClient;

    public FraudDetectionEngine(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    /**
     *  Steps for detecting first-party fraud are:
     *    1. Identify clients with shared identifiers:
     *      - count them;
     *      - create SHARED_IDENTIFIERS relationship between them with property count.
     *    2. Execute community detection algorithm (e.g. WCC), based on the SHARED_IDENTIFIERS relationship:
     *      - create a monopartite graph projection for Client nodes;
     *      - execute the algorithm on this projection and write the community ID on the nodes, participating in a cluster with size > 1;
     *      - fraud rings can be visualized at this point.
     *    3. Finding similar clients within clusters with the help of similarity algorithm (e.g. Node Similarity)
     *      and writing the results in a SIMILAR_TO relationship between the clients:
     *      - create graph projection of bipartite graph with Client nodes as source and identification (SSN, Email, Phone) nodes as target;
     *      - execute the algorithm on this projection and write the new relationships and their weight property back to the database.
     *    4. Compute and assign fraud score (firstPartyFraudScore) to Client nodes in the clusters identified in previous steps,
     *      based on SIMILAR_TO relationships, using a centrality algorithm (e.g. Degree Centrality):
     *      - the graph projection from the previous execution can be run with Client nodes and SIMILAR_TO relationships;
     *      - execute the algorithm and write the score to the database;
     *      - potential first-party fraudsters can be visualized, based on some threshold percentile value.
     *    5. Clean up the graph catalog.
     *
     *  Steps for detecting second-party fraud are:
     *    Note: The potential first-party fraudsters should be detected beforehand, because the second-party fraudsters
     *      can be identified, based on their relationships with them.
     *    1. Identify transactions between first-party fraudsters and clients (what types of transaction, transferred amount etc.):
     *      - create TRANSFER_TO bidirectional relationship between first-party fraudsters and clients containing the transferred amount.
     *    2. Use a community detection algorithm (e.g. WCC) to identify networks of clients who are connected to first party fraudsters:
     *      - create a monopartite graph projection for Client nodes and TRANSFER_TO relationships;
     *      - execute the algorithm on this projection and write the community ID on the nodes, participating in a cluster with size > 1;
     *    3. Use a centrality algorithm (e.g. Page Rank) to compute a score based on how influential these clients are when relationships
     *      take into account the amount of money transferred to/from fraudsters:
     *      - the graph projection from the previous execution can be run with Client nodes and TRANSFER_TO relationships;
     *      - execute the algorithm and write the score to the database;
     *      - potential second-party fraudsters can be visualized, based on some threshold percentile value.
     *    4. Cleanup the graph catalog.
     */

    public void detectFirstPartyFraud(ResponseBodyEmitter emitter,
                                      String communityDetectionAlgorithm,
                                      String similarityAlgorithm,
                                      String centralityAlgorithm,
                                      double percentileThreshold) {
        List<Task> tasks = generateFirstPartyFraudDetectionTaskList(communityDetectionAlgorithm,
                                                                    similarityAlgorithm,
                                                                    centralityAlgorithm,
                                                                    percentileThreshold);
        executeTasks(tasks, emitter);
    }

    public void detectSecondPartyFraud(ResponseBodyEmitter emitter,
                                       String communityDetectionAlgorithm,
                                       String centralityAlgorithm,
                                       double minimumScoreForCentralityAlgorithm) {
        List<Task> tasks = generateSecondPartyFraudDetectionTaskList(communityDetectionAlgorithm,
                                                                     centralityAlgorithm,
                                                                     minimumScoreForCentralityAlgorithm);
        executeTasks(tasks, emitter);
    }

    private void executeTasks(List<Task> tasks, ResponseBodyEmitter emitter) {
        for (Task task : tasks) {
            if (!executeTask(task, emitter)) {
                return;
            }
        }
    }

    private boolean executeTask(Task task, ResponseBodyEmitter emitter) {
        try {
            emitter.send(task.getInitialMessage());
            if (!task.execute(neo4jClient)) {
                emitter.completeWithError(new FraudDetectionTaskFailedException(task));
                return false;
            }
            emitter.send(task.getSuccessMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private List<Task> generateFirstPartyFraudDetectionTaskList(String communityDetectionAlgorithm,
                                                                String similarityAlgorithm,
                                                                String centralityAlgorithm,
                                                                double percentileThreshold) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(TaskFactory.getCreateSharedIdentifiersRelationshipTask(SHARED_IDENTIFIERS_RELATIONSHIP));
        tasks.add(TaskFactory.getCreateGraphProjectionForClientsWithSharedIdentifiersTask(FIRST_PARTY_FRAUD_CLIENTS_COMMUNITY));
        tasks.add(TaskFactory.getCommunityDetectionForClientClustersTask(communityDetectionAlgorithm, FIRST_PARTY_FRAUD_CLIENTS_COMMUNITY));
        tasks.add(TaskFactory.getCreateGraphProjectionForFirstPartyFraudstersTask(FIRST_PARTY_SIMILAR_FRAUDSTERS));
        tasks.add(TaskFactory.getSimilarityAlgorithmForClientsInFraudRingsTask(similarityAlgorithm, FIRST_PARTY_SIMILAR_FRAUDSTERS));
        tasks.add(TaskFactory.getCentralityAlgorithmForSimilarClientsInFraudRingsTask(centralityAlgorithm));
        tasks.add(TaskFactory.getAddNodeLabelsBasedOnFirstPartyFraudScoreTask(percentileThreshold));
        tasks.add(TaskFactory.getDropGraphProjectionTask(FIRST_PARTY_FRAUD_CLIENTS_COMMUNITY));
        tasks.add(TaskFactory.getDropGraphProjectionTask(FIRST_PARTY_SIMILAR_FRAUDSTERS));
        return tasks;
    }

    private List<Task> generateSecondPartyFraudDetectionTaskList(String communityDetectionAlgorithm,
                                                                 String centralityAlgorithm,
                                                                 double minimumScoreForCentralityAlgorithm) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(TaskFactory.getCreateRelationshipsFromFraudstersToClientsTask(TRANSFER_TO_RELATIONSHIP));
        tasks.add(TaskFactory.getCreateRelationshipsFromClientsToFraudsters(TRANSFER_TO_RELATIONSHIP));
        tasks.add(TaskFactory.getCreateSecondPartyFraudNetworkGraphProjectionTask(SECOND_PARTY_FRAUD_NETWORK, TRANSFER_TO_RELATIONSHIP));
        tasks.add(TaskFactory.getCommunityDetectionAlgorithmForSecondPartyFraudGroupingTask(communityDetectionAlgorithm, SECOND_PARTY_FRAUD_NETWORK));
        tasks.add(TaskFactory.getCentralityAlgorithmForSecondPartyFraudScore(centralityAlgorithm, SECOND_PARTY_FRAUD_NETWORK, minimumScoreForCentralityAlgorithm));
        tasks.add(TaskFactory.getDropGraphProjectionTask(SECOND_PARTY_FRAUD_NETWORK));
        return tasks;
    }
}
