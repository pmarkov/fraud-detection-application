package com.student.fraudsters.simulation;

import com.google.common.collect.Lists;
import com.student.fraudsters.logging.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.neo4j.driver.Values;
import org.neo4j.driver.summary.SummaryCounters;
import org.paysim.IteratingPaySim;
import org.paysim.PaySimState;
import org.paysim.actors.SuperActor;
import org.paysim.base.Transaction;
import org.paysim.parameters.Parameters;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.student.fraudsters.simulation.SimulationConstants.*;
import static java.lang.String.format;

public class SimulationEngine {

    private final Logger logger;
    private final QueryExecutor queryExecutor;

    public final String PAYSIM_PROPERTIES_FILE;

    private final int batchSize;
    private final int queueDepth;
    private final Driver driver;

    public SimulationEngine(Driver driver, Logger logger, int batchSize, int queueDepth) throws FileNotFoundException {
        this.driver = driver;
        this.logger = logger;
        this.batchSize = batchSize;
        this.queueDepth = queueDepth;
        this.queryExecutor = new QueryExecutor(logger);
        PAYSIM_PROPERTIES_FILE = ResourceUtils.getFile("classpath:PaySim.properties").getAbsolutePath();
    }

    public SimulationEngine(Driver driver, Logger logger, int batchSize) throws FileNotFoundException {
        this(driver, logger, batchSize, DEFAULT_SIM_QUEUE_DEPTH);
    }

    public void load() {
        IteratingPaySim sim = new IteratingPaySim(new Parameters(PAYSIM_PROPERTIES_FILE), queueDepth);

        sim.run();
        logger.log(format("Simulation started using PaySim v%f", PaySimState.PAYSIM_VERSION));

        try {
            runSimulationLoadSteps(sim);
            logger.log("Simulation completed successfully.");
        } catch (Exception e) {
            handleExceptionWhileRunningSimulationSteps(sim, e);
        }
    }

    private void handleExceptionWhileRunningSimulationSteps(IteratingPaySim sim, Exception e) {
        logger.log("Exception while loading data: " + e.getMessage());
        try {
            sim.abort();
        } catch (IllegalStateException ise) {
            logger.log("Sim already aborted!");
        }
    }

    private void runSimulationLoadSteps(IteratingPaySim sim) {
        cleanupDatabase();
        enforcePaySimSchema();
        loadTransactions(sim);
        labelMulesAsClients();
        createClientIdentities(sim);
        setIdentityPropertiesForMerchantsAndBanks(sim);
    }

    public void cleanupDatabase() {
        logger.log("Cleaning up database...");
        deletePaySimSchema();
        deleteNodesAndRelationships();
    }

    private void deletePaySimSchema() {
        logger.log("Deleting PaySim schema.");
        queryExecutor.executeWriteQuery(driver, new Query(CypherConstants.DELETE_SCHEMA));
    }

    private void deleteNodesAndRelationships() {
        logger.log("Deleting nodes and relationships.");
        queryExecutor.executeWriteQuery(driver,
                new Query(CypherConstants.DELETE_NODES_AND_RELATIONSHIPS,
                        Values.parameters("batchSize", batchSize)));
    }

    private void enforcePaySimSchema() {
        logger.log("Configuring PaySim schema...");
        queryExecutor.enforcePaySimSchema(driver);
        logger.log("Schema configured.");
    }

    private void loadTransactions(IteratingPaySim sim) {
        logger.log("Loading simulation transactions...");
        final ZonedDateTime start = ZonedDateTime.now();
        final List<Transaction> batch = new ArrayList<>(batchSize);
        final AtomicInteger atom = new AtomicInteger(0);
        sim.forEachRemaining(transaction -> addTransactionToBatch(transaction, batch, atom));

        if (batch.size() > 0) {
            loadTransactionsInDb(batch, atom);
        }

        logger.log(format("[loaded %d PaySim transactions]", atom.get()));
        logger.log(format("[estimated load rate: %.2f PaySim-transactions/second]",
                (float) atom.get() / Duration.between(start, ZonedDateTime.now()).getSeconds()));
    }

    private void addTransactionToBatch(Transaction tx, List<Transaction> batch, AtomicInteger atom) {
        batch.add(tx);
        if (batch.size() >= batchSize) {
            loadTransactionsInDb(batch, atom);
            batch.clear();
        }
    }

    private void loadTransactionsInDb(List<Transaction> batch, AtomicInteger atom) {
        SummaryCounters counters = queryExecutor.executeWriteQuery(driver, Util.compileBulkTransactionQuery(batch));
        logger.log(format("Created %d relationships.", counters.relationshipsCreated()));
        atom.addAndGet(batch.size());
    }

    private void labelMulesAsClients() {
        logger.log("Labeling all Mules as Clients...");
        SummaryCounters counters = queryExecutor.executeWriteQuery(driver, new Query(CypherConstants.MAKE_MULES_CLIENTS));
        logger.log(format("Labeled %d nodes.", counters.labelsAdded()));
    }

    private void createClientIdentities(IteratingPaySim sim) {
        logger.log("Setting identities for Clients...");
        Lists.partition(sim.getClients(), batchSize)
                .forEach(
                        chunk -> {
                            List<Query> queries = chunk.stream()
                                    .map(client -> Util.compileClientIdentityQuery(client.getClientIdentity()))
                                    .collect(Collectors.toList());
                            queryExecutor.executeBatch(driver, queries);
                        });
    }

    private void setIdentityPropertiesForMerchantsAndBanks(IteratingPaySim sim) {
        logger.log("Setting any extra node properties for Merchants and Banks...");
        List<SuperActor> allActors = Stream.concat(sim.getBanks().stream(), sim.getMerchants().stream())
                .collect(Collectors.toList());

        Lists.partition(allActors, batchSize)
                .forEach(chunk -> {
                    List<Query> queries = chunk.stream()
                            .map(Util::compilePropertyUpdateQuery)
                            .collect(Collectors.toList());
                    queryExecutor.executeBatch(driver, queries);
                });
    }
}























