package com.student.fraudsters.detection.execution;

import com.student.fraudsters.detection.execution.task.algorith_execution.GdsAlgorithmTier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CypherConstants {

    public static final String CREATE_RELATIONSHIP_BETWEEN_CLIENTS_WITH_MUTUAL_IDENTIFIERS = "" +
            "MATCH (c1:Client)-[rel:HAS_EMAIL|HAS_PHONE|HAS_SSN]->(info)\n" +
            "<-[:HAS_EMAIL|HAS_PHONE|HAS_SSN]-(c2:Client)\n" +
            "WHERE c1.id<>c2.id\n" +
            "WITH c1, c2, count(rel) AS sharedIdentifiers\n" +
            "MERGE (c1)-[:${relationshipType} { count: sharedIdentifiers }]-(c2)";

    public static final String GRAPH_PROJECTION_FOR_CLIENTS_WITH_SHARED_IDENTIFIERS_TEMPLATE = "" +
            "CALL gds.graph.create('${projectionName}', 'Client',\n" +
            "{\n" +
            "    SHARED_IDENTIFIERS:{\n" +
            "        type: 'SHARED_IDENTIFIERS',\n" +
            "        properties: {\n" +
            "            count: {\n" +
            "                property: 'count'\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "   }\n" +
            ")";

    public static final String GRAPH_PROJECTION_FOR_FIRST_PARTY_FRAUDSTERS_SIMILARITY_TEMPLATE = "" +
            "CALL gds.graph.create.cypher('${projectionName}',\n" +
            "'MATCH(c:Client)\n" +
            "    WHERE exists(c.${fraudGroupProperty})\n" +
            "    RETURN id(c) AS id,labels(c) AS labels\n" +
            "UNION\n" +
            "MATCH(n)\n" +
            "    WHERE n:Email OR n:Phone OR n:SSN\n" +
            "    RETURN id(n) AS id,labels(n) AS labels',\n" +
            "'MATCH(c:Client)\n" +
            "-[:HAS_EMAIL|HAS_PHONE|HAS_SSN]->(ids)\n" +
            "WHERE exists(c.${fraudGroupProperty})\n" +
            "RETURN id(c) AS source, id(ids) AS target')\n";

    public static final String EXISTS_GRAPH_PROJECTION_TEMPLATE = "CALL gds.graph.exists($projectionName)";

    public static final String DROP_GRAPH_PROJECTION_TEMPLATE = "CALL gds.graph.drop('${projectionName}')";

    public static final String RUN_COMMUNITY_DETECTION_ALGORITHM_FOR_CLIENT_CLUSTERS_TEMPLATE = "" +
            "CALL gds${algorithmType}${algorithmId}.stream('${projectionName}')\n" +
            "YIELD ${returnType}, nodeId\n" +
            "WITH ${returnType} AS cluster, gds.util.asNode(nodeId) AS client\n" +
            "WITH cluster, collect(client.id) AS clients\n" +
            "WITH *, size(clients) AS clusterSize\n" +
            "WHERE clusterSize>1\n" +
            "UNWIND clients AS client\n" +
            "MATCH(c:Client)\n" +
            "WHERE c.id=client\n" +
            "SET c.${writeProperty}=cluster;";

    public static final String RUN_SIMILARITY_ALGORITHM_FOR_CLIENTS_IN_FRAUD_RINGS_TEMPLATE = "" +
            "CALL gds${algorithmType}${algorithmId}.write('${projectionName}', {\n" +
            "    topK: 15,\n" +
            "    writeProperty: 'similarityScore',\n" +
            "    writeRelationshipType: 'SIMILAR_TO'\n" +
            "});";

    public static final String RUN_CENTRALITY_ALGORITHM_FOR_CLIENTS_IN_FRAUD_RINGS_TEMPLATE = "" +
            "CALL gds${algorithmType}${algorithmId}.write(\n" +
            "    {\n" +
            "        nodeProjection: 'Client',\n" +
            "        relationshipProjection: 'SIMILAR_TO',\n" +
            "        relationshipProperties: 'similarityScore',\n" +
            "        relationshipWeightProperty: 'similarityScore',\n" +
            "        writeProperty: 'firstPartyFraudScore'" +
            "    })\n";

    public static final String ADD_NODE_LABELS_BASED_ON_FIRST_FRAUD_SCORE = "" +
            "MATCH(c:Client)\n" +
            "WHERE exists(c.firstPartyFraudScore)\n" +
            "WITH percentileCont(c.firstPartyFraudScore, ${percentileThreshold})\n" +
            "    AS firstPartyFraudThreshold\n" +
            "MATCH(c:Client)\n" +
            "WHERE c.firstPartyFraudScore>firstPartyFraudThreshold\n" +
            "SET c:${label};";

    public static final String ADD_TRANSFER_RELATIONSHIP_FROM_FRAUDSTER_TO_CLIENT = "" +
            "MATCH (c1:Client:${firstPartyFraudsterLabel})-[]->(t:Transaction)-[]->(c2:Client)\n" +
            "WHERE NOT c2:${firstPartyFraudsterLabel}\n" +
            "WITH c1,c2,sum(t.amount) AS totalAmount\n" +
            "SET c2:SecondPartyFraudSuspect\n" +
            "CREATE (c1)-[:${relationshipType} {${propertyName}:totalAmount}]->(c2);";

    public static final String ADD_TRANSFER_RELATIONSHIP_FROM_CLIENT_TO_FRAUDSTER = "" +
            "MATCH (c1:Client:${firstPartyFraudsterLabel})<-[]-(t:Transaction)<-[]-(c2:Client)\n" +
            "WHERE NOT c2:${firstPartyFraudsterLabel}\n" +
            "WITH c1,c2,sum(t.amount) AS totalAmount\n" +
            "SET c2:SecondPartyFraudSuspect\n" +
            "CREATE (c1)<-[:${relationshipType} {${propertyName}:totalAmount}]-(c2);";

    public static final String CREATE_SECOND_PARTY_FRAUD_NETWORK_GRAPH_PROJECTION = "" +
            "CALL gds.graph.create('${projectionName}', 'Client', '${relationshipType}', {relationshipProperties:'amount'});";

    public static final String RUN_CENTRALITY_ALGORITHM_FOR_SECOND_PARTY_FRAUD_SCORE = "" +
            "CALL gds${algorithmType}${algorithmId}.stream('${projectionName}',{relationshipWeightProperty:'amount'})\n" +
            "YIELD nodeId,score\n" +
            "WITH gds.util.asNode(nodeId) AS client,score\n" +
            "WHERE exists(client.${fraudGroupProperty})\n" +
            "AND score > ${minimumScore} AND NOT client:${firstPartyFraudsterLabel}\n" +
            "MATCH(c:Client {id:client.id})\n" +
            "SET c.secondPartyFraudScore=score;";

    private final static Map<String, GdsAlgorithmTier> algorithmQualityTierForId;
    private final static Map<String, Map<String, String>> communityDetectionAlgorithms;
    private final static Map<String, Map<String, String>> centralityAlgorithms;
    private final static Map<String, Map<String, String>> similarityAlgorithms;

    private static final String COMMUNITY_DETECTION_LOUVAIN_DESCRIPTION = "" +
            "The Louvain method is an algorithm to detect communities in large networks. " +
            "It maximizes a modularity score for each community, where the modularity quantifies the quality of an assignment of nodes to communities. " +
            "This means evaluating how much more densely connected the nodes within a community are, compared to how connected they would be in a random network.";
    private static final GdsAlgorithmTier COMMUNITY_DETECTION_LOUVAIN_QUALITY_TIER = GdsAlgorithmTier.PRODUCTION;
    private static final String COMMUNITY_DETECTION_LOUVAIN_NAME = "Louvain";
    private static final String COMMUNITY_DETECTION_LOUVAIN_RETURN_TYPE = "communityId";

    private static final String COMMUNITY_DETECTION_WCC_DESCRIPTION = "" +
            "The WCC algorithm finds sets of connected nodes in an undirected graph, " +
            "where all nodes in the same set form a connected component. WCC is often used early in an analysis to understand the structure " +
            "of a graph. Using WCC to understand the graph structure enables running other algorithms independently on an identified cluster. " +
            "As a preprocessing step for directed graphs, it helps quickly identify disconnected groups.";
    private static final GdsAlgorithmTier COMMUNITY_DETECTION_WCC_QUALITY_TIER = GdsAlgorithmTier.PRODUCTION;
    private static final String COMMUNITY_DETECTION_WCC_NAME = "Weakly Connected Components";
    private static final String COMMUNITY_DETECTION_WCC_RETURN_TYPE = "componentId";

    private static final String COMMUNITY_DETECTION_LABEL_PROPAGATION_DESCRIPTION = "" +
            "LPA works by propagating labels throughout the network and forming communities based on this process of label propagation.\n" +
            "The intuition behind the algorithm is that a single label can quickly become dominant in a densely connected group of nodes, but will have trouble crossing a sparsely connected region. " +
            "Labels will get trapped inside a densely connected group of nodes, and those nodes that end up with the same label when the algorithms finish can be considered part of the same community.";
    private static final GdsAlgorithmTier COMMUNITY_DETECTION_LABEL_PROPAGATION_QUALITY_TIER = GdsAlgorithmTier.PRODUCTION;
    private static final String COMMUNITY_DETECTION_LABEL_PROPAGATION_NAME = "Label Propagation";
    private static final String COMMUNITY_DETECTION_LABEL_PROPAGATION_RETURN_TYPE = "communityId";

    private static final String CENTRALITY_PAGE_RANK_DESCRIPTION = "" +
            "The PageRank algorithm measures the importance of each node within the graph, " +
            "based on the number incoming relationships and the importance of the corresponding source nodes. " +
            "The underlying assumption roughly speaking is that a page is only as important as the pages that link to it.";
    private static final GdsAlgorithmTier CENTRALITY_PAGE_RANK_QUALITY_TIER = GdsAlgorithmTier.PRODUCTION;
    private static final String CENTRALITY_PAGE_RANK_NAME = "Page Rank";

    private static final String CENTRALITY_ARTICLE_RANK_DESCRIPTION = "" +
            "ArticleRank is a variant of the Page Rank algorithm, which measures the transitive influence of nodes. " +
            "Page Rank follows the assumption that relationships originating from low-degree nodes have a higher influence than relationships from high-degree nodes. " +
            "Article Rank lowers the influence of low-degree nodes by lowering the scores being sent to their neighbors in each iteration.";
    private static final GdsAlgorithmTier CENTRALITY_ARTICLE_RANK_QUALITY_TIER = GdsAlgorithmTier.ALPHA;
    private static final String CENTRALITY_ARTICLE_RANK_NAME = "Article Rank";

    private static final String CENTRALITY_DEGREE_DESCRIPTION = "" +
            "The Degree Centrality algorithm can be used to find popular nodes within a graph. " +
            "Degree centrality measures the number of incoming or outgoing (or both) relationships from a node, " +
            "depending on the orientation of a relationship projection. It can be applied to either weighted or unweighted graphs. " +
            "In the weighted case the algorithm computes the sum of all positive weights of adjacent relationships of a node, " +
            "for each node in the graph. Non-positive weights are ignored";
    private static final GdsAlgorithmTier CENTRALITY_DEGREE_QUALITY_TIER = GdsAlgorithmTier.ALPHA;
    private static final String CENTRALITY_DEGREE_NAME = "Degree Centrality";

    private static final String SIMILARITY_NODE_SIMILARITY_DESCRIPTION = "" +
            "The Node Similarity algorithm compares a set of nodes based on the nodes they are connected to. " +
            "Two nodes are considered similar if they share many of the same neighbors. " +
            "Node Similarity computes pair-wise similarities based on the Jaccard metric, also known as the Jaccard Similarity Score.";
    private static final GdsAlgorithmTier SIMILARITY_NODE_SIMILARITY_QUALITY_TIER = GdsAlgorithmTier.PRODUCTION;
    private static final String SIMILARITY_NODE_SIMILARITY_NAME = "Node Similarity";



    static {
        algorithmQualityTierForId = new HashMap<>();
        initAlgorithmQualityTierForIdMap();
        communityDetectionAlgorithms = new HashMap<>();
        centralityAlgorithms = new HashMap<>();
        similarityAlgorithms = new HashMap<>();
        initCommunityDetectionAlgorithms();
        initCentralityAlgorithms();
        initSimilarityAlgorithms();
    }

    public static void initAlgorithmQualityTierForIdMap() {
        algorithmQualityTierForId.put("louvain", COMMUNITY_DETECTION_LOUVAIN_QUALITY_TIER);
        algorithmQualityTierForId.put("wcc", COMMUNITY_DETECTION_WCC_QUALITY_TIER);
        algorithmQualityTierForId.put("labelPropagation", COMMUNITY_DETECTION_LABEL_PROPAGATION_QUALITY_TIER);
        algorithmQualityTierForId.put("pageRank", CENTRALITY_PAGE_RANK_QUALITY_TIER);
        algorithmQualityTierForId.put("articleRank", CENTRALITY_ARTICLE_RANK_QUALITY_TIER);
        algorithmQualityTierForId.put("degree", CENTRALITY_DEGREE_QUALITY_TIER);
        algorithmQualityTierForId.put("nodeSimilarity", SIMILARITY_NODE_SIMILARITY_QUALITY_TIER);
    }

    public static Map<String, GdsAlgorithmTier> getAlgorithmQualityTierForIdMap() {
        return Collections.unmodifiableMap(algorithmQualityTierForId);
    }

    public static Map<String, Map<String, String>> getCommunityDetectionAlgorithms() {
        return Collections.unmodifiableMap(communityDetectionAlgorithms);
    }

    public static Map<String, Map<String, String>> getCentralityAlgorithms() {
        return Collections.unmodifiableMap(centralityAlgorithms);
    }

    public static Map<String, Map<String, String>> getSimilarityAlgorithms() {
        return Collections.unmodifiableMap(similarityAlgorithms);
    }

    private static void initCommunityDetectionAlgorithms() {
        var louvainMap = Map.of(
                "description", COMMUNITY_DETECTION_LOUVAIN_DESCRIPTION,
                "quality-tier", COMMUNITY_DETECTION_LOUVAIN_QUALITY_TIER.label,
                "name", COMMUNITY_DETECTION_LOUVAIN_NAME,
                "returnType", COMMUNITY_DETECTION_LOUVAIN_RETURN_TYPE);
        communityDetectionAlgorithms.put("louvain", louvainMap);

        var wccMap = Map.of(
                "description", COMMUNITY_DETECTION_WCC_DESCRIPTION,
                "quality-tier", COMMUNITY_DETECTION_WCC_QUALITY_TIER.label,
                "name", COMMUNITY_DETECTION_WCC_NAME,
                "returnType", COMMUNITY_DETECTION_WCC_RETURN_TYPE);
        communityDetectionAlgorithms.put("wcc", wccMap);

        var labelPropagationMap = Map.of(
                "description", COMMUNITY_DETECTION_LABEL_PROPAGATION_DESCRIPTION,
                "quality-tier", COMMUNITY_DETECTION_LABEL_PROPAGATION_QUALITY_TIER.label,
                "name", COMMUNITY_DETECTION_LABEL_PROPAGATION_NAME,
                "returnType", COMMUNITY_DETECTION_LABEL_PROPAGATION_RETURN_TYPE);
        communityDetectionAlgorithms.put("labelPropagation", labelPropagationMap);
    }

    private static void initCentralityAlgorithms() {
        var pageRankMap = Map.of(
                "description", CENTRALITY_PAGE_RANK_DESCRIPTION,
                "quality-tier", CENTRALITY_PAGE_RANK_QUALITY_TIER.label,
                "name", CENTRALITY_PAGE_RANK_NAME);
        centralityAlgorithms.put("pageRank", pageRankMap);

        var articleRankMap = Map.of(
                "description", CENTRALITY_ARTICLE_RANK_DESCRIPTION,
                "quality-tier", CENTRALITY_ARTICLE_RANK_QUALITY_TIER.label,
                "name", CENTRALITY_ARTICLE_RANK_NAME);
        centralityAlgorithms.put("articleRank", articleRankMap);

        var degreeCentralityMap = Map.of(
                "description", CENTRALITY_DEGREE_DESCRIPTION,
                "quality-tier", CENTRALITY_DEGREE_QUALITY_TIER.label,
                "name", CENTRALITY_DEGREE_NAME);
        centralityAlgorithms.put("degree", degreeCentralityMap);
    }

    private static void initSimilarityAlgorithms() {
        var nodeSimilarityMap = Map.of(
                "description", SIMILARITY_NODE_SIMILARITY_DESCRIPTION,
                "quality-tier", SIMILARITY_NODE_SIMILARITY_QUALITY_TIER.label,
                "name", SIMILARITY_NODE_SIMILARITY_NAME);
        similarityAlgorithms.put("nodeSimilarity", nodeSimilarityMap);
    }
}
