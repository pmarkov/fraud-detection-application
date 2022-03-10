package com.student.fraudsters.controllers;

import com.student.fraudsters.services.SimulationService;
import com.student.fraudsters.simulation.SimulationConstants;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/simulation")
public class SimulationController {

    private final SimulationService simulationService;
    private ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/load")
    public SseEmitter runAndLoadSimulationWithParams(@RequestParam(defaultValue = SimulationConstants.DEFAULT_BATCH_SIZE_STRING) Integer batchSize,
                                                     @RequestParam(defaultValue = SimulationConstants.DEFAULT_QUEUE_DEPTH_STRING) Integer queueDepth) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(SimulationConstants.SIMULATION_LOAD_TIMEOUT_MINUTES));
        nonBlockingService.execute(() -> simulationService.executeRunAndLoadSimulation(batchSize, queueDepth, emitter));
        return emitter;
    }

    @DeleteMapping("/cleanup")
    public SseEmitter cleanupDatabase(@RequestParam(defaultValue = SimulationConstants.DEFAULT_BATCH_SIZE_STRING) Integer batchSize) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(SimulationConstants.SIMULATION_CLEANUP_TIMEOUT_MINUTES));
        nonBlockingService.execute(() -> simulationService.cleanupDatabaseFromSimulationData(batchSize, emitter));
        return emitter;
    }
}
