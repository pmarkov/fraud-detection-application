package com.student.fraudsters.services;

import com.student.fraudsters.logging.SseLogger;
import com.student.fraudsters.simulation.SimulationEngine;
import org.neo4j.driver.Driver;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SimulationService {

    Driver driver;

    public SimulationService(Driver driver) {
        this.driver = driver;
    }

    public void executeRunAndLoadSimulation(int batchSize, int queueDepth, SseEmitter emitter) {
        try {
            SimulationEngine simulationEngine = new SimulationEngine(driver, new SseLogger(emitter), batchSize, queueDepth);
            simulationEngine.load();
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
            e.printStackTrace();
        }
    }

    public void cleanupDatabaseFromSimulationData(int batchSize, SseEmitter emitter) {
        try {
            SimulationEngine simulationEngine = new SimulationEngine(driver, new SseLogger(emitter), batchSize);
            simulationEngine.cleanupDatabase();
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
            e.printStackTrace();
        }
    }
}
