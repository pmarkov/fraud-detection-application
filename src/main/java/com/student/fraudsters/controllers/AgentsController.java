package com.student.fraudsters.controllers;

import com.student.fraudsters.data.entity.agents.*;
import com.student.fraudsters.services.AgentsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agents")
public class AgentsController {

    private final AgentsService service;

    public AgentsController(AgentsService service) {
        this.service = service;
    }

    @GetMapping("/client")
    public Client getClientById(@RequestParam String id) {
        return service.getClient(id);
    }

    @GetMapping("/client-count")
    public Map<String, Long> getClientCount() {
        return service.getClientCount();
    }

    @GetMapping("/bank")
    public Bank getBankById(@RequestParam String id) {
        return service.getBank(id);
    }

    @GetMapping("/bank-count")
    public Map<String, Long> getBankCount() {
        return service.getBankCount();
    }

    @GetMapping("/merchant")
    public Merchant getMerchantById(@RequestParam String id) {
        return service.getMerchant(id);
    }

    @GetMapping("/merchant-count")
    public Map<String, Long> getMerchantCount() {
        return service.getMerchantCount();
    }

    @GetMapping("/first-party-fraudsters/clients-with-shared-identifiers-count")
    public Map<String, Long> getCountOfClientsWithSharedIdentifiers() {
        return service.getCountOfClientsWithSharedIdentifiers();
    }

    @GetMapping("/first-party-fraudsters/fraud-rings")
    public List<FraudRingDto> getFirstPartyFraudRings(@RequestParam int minimumSize) {
        return service.getFraudRingsOrderedBySize(minimumSize);
    }

    @GetMapping("/first-party-fraudsters/clients-in-fraud-ring")
    public List<ClientDto> getClientsInFirstPartyFraudRing(@RequestParam int fraudRingId) {
        return service.getClientsInFirstPartyFraudRing(fraudRingId);
    }

    @GetMapping("/first-party-fraudsters/clients-in-fraud-rings")
    public List<ClientDto> getAllClientsInFirstPartyFraudRings() {
        return service.getAllClientsInFirstPartyFraudRing();
    }

    @GetMapping("/first-party-fraudsters")
    public List<ClientDto> getFirstPartyFraudsters() {
        return service.getFirstPartyFraudsters();
    }

    @GetMapping("/second-party-fraudsters/suspects")
    public List<ClientDto> getSecondPartyFraudsterSuspects() {
        return service.getSecondPartyFraudSuspects();
    }

    @GetMapping("/second-party-fraudsters")
    public List<ClientDto> getSecondPartyFraudsters(@RequestParam double percentileThreshold) {
        return service.getSecondPartyFraudstersByFraudScore(percentileThreshold);
    }
}
