package com.student.fraudsters.simulation;

import com.student.fraudsters.logging.Logger;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.driver.summary.SummaryCounters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class QueryExecutor {

    private final Logger logger;

    private static final short MAX_RETRIES = 3;

    public QueryExecutor(Logger logger) {
        this.logger = logger;
    }

    SummaryCounters executeWriteQuery(Driver driver, Query query) {
        try (Session session = driver.session()) {
            return executeWriteQueryInternally(query, session, 1);
        }
    }

    private SummaryCounters executeWriteQueryInternally(Query query, Session session, int currentAttempt) {
        try {
            return session.writeTransaction(tx -> executeWriteQueryByTransaction(query, tx));
        } catch (TransientException e) {
            handleTransientExceptionDuringWriteQuery(query, session, currentAttempt);
        }
        return null;
    }

    private SummaryCounters executeWriteQueryByTransaction(Query query, Transaction tx) {
        return tx.run(query).consume().counters();
    }

    private void handleTransientExceptionDuringWriteQuery(Query query, Session session, int currentAttempt) {
        if (currentAttempt > MAX_RETRIES) {
            logger.log("Caught Transient exception, aborting...");
            return;
        }
        logger.log("Caught Transient exception, retrying...");
        executeWriteQueryInternally(query, session, currentAttempt + 1);
    }


    void executeBatch(Driver driver, List<Query> queries) {
        try (Session session = driver.session()) {
            final AtomicInteger nodeCnt = new AtomicInteger();
            final AtomicInteger relCnt = new AtomicInteger();
            int cnt = executeBatchInternally(queries, session, nodeCnt, relCnt, 1);
            logger.log(format("batch executed %d queries, created %d nodes and %d relationships", cnt, nodeCnt.get(), relCnt.get()));
        }
    }

    private Integer executeBatchInternally(List<Query> queries, Session session, AtomicInteger nodeCnt, AtomicInteger relCnt, int currentAttempt) {
        try {
            return session.writeTransaction(tx -> executeBatchQueriesByTransaction(queries, nodeCnt, relCnt, tx));
        } catch (TransientException e) {
            return handleTransientExceptionDuringBatchExecution(queries, session, nodeCnt, relCnt, currentAttempt);
        }
    }

    private Integer executeBatchQueriesByTransaction(List<Query> queries, AtomicInteger nodeCnt, AtomicInteger relCnt, Transaction tx) {
        queries.forEach(
                q -> {
                    SummaryCounters results = tx.run(q).consume().counters();
                    nodeCnt.addAndGet(results.nodesCreated());
                    relCnt.addAndGet(results.relationshipsCreated());
                });
        return queries.size();
    }

    private Integer handleTransientExceptionDuringBatchExecution(List<Query> queries, Session session,AtomicInteger nodeCnt, AtomicInteger relCnt, int currentAttempt) {
        if (currentAttempt > MAX_RETRIES) {
            logger.log("Caught Transient exception, aborting...");
            return 0;
        }
        logger.log("Caught Transient exception, retrying...");
        return executeBatchInternally(queries, session, nodeCnt, relCnt, currentAttempt + 1);
    }

    void enforcePaySimSchema(Driver driver) {
        Arrays.stream(CypherConstants.SCHEMA_QUERIES)
                .forEach(
                        q -> {
                            try (Session session = driver.session()) {
                                session.run(q);
                            } catch (ClientException ce) {
                                logger.log(format("Constraint provided by '%s' might already exist.", q));
                            }
                        });

    }
}
