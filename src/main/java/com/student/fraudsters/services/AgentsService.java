package com.student.fraudsters.services;

import com.student.fraudsters.data.repositories.agents.BankRepository;
import com.student.fraudsters.data.repositories.agents.ClientRepository;
import com.student.fraudsters.data.repositories.agents.MerchantRepository;
import com.student.fraudsters.data.entity.agents.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgentsService {

    private final ClientRepository clientRepository;
    private final BankRepository bankRepository;
    private final MerchantRepository merchantRepository;

    public AgentsService(ClientRepository clientRepository, BankRepository bankRepository, MerchantRepository merchantRepository) {
        this.clientRepository = clientRepository;
        this.bankRepository = bankRepository;
        this.merchantRepository = merchantRepository;
    }

    public Client getClient(String clientId) {
        return clientRepository.findClientById(clientId);
    }

    public Map<String, Long> getClientCount() {
        return Map.of("result", clientRepository.count());
    }

    public Bank getBank(String bankId) {
        return bankRepository.findBankById(bankId);
    }

    public Map<String, Long> getBankCount() {
        return Map.of("result", bankRepository.count());
    }

    public Merchant getMerchant(String merchantId) {
        return merchantRepository.findMerchantById(merchantId);
    }

    public Map<String, Long> getMerchantCount() {
        return Map.of("result", merchantRepository.count());
    }

    public Map<String, Long> getCountOfClientsWithSharedIdentifiers() {
        return Map.of("result", clientRepository.countClientsWithSharedIdentifiers());
    }

    public List<FraudRingDto> getFraudRingsOrderedBySize(int minimumSize) {
        return clientRepository.getFraudRingsOrderedBySize(minimumSize);
    }

    public List<ClientDto> getClientsInFirstPartyFraudRing(int fraudRingId) {
        return clientRepository.findClientsByFirstPartyFraudGroup(fraudRingId);
    }

    public List<ClientDto> getAllClientsInFirstPartyFraudRing() {
        return clientRepository.findClientsInFraudRings();
    }

    public List<ClientDto> getFirstPartyFraudsters() {
        return clientRepository.findFirstPartyFraudsters();
    }

    public List<ClientDto> getSecondPartyFraudSuspects() {
        return clientRepository.findSecondPartyFraudSuspects();
    }

    public List<ClientDto> getSecondPartyFraudstersByFraudScore(double percentileThreshold) {
        return clientRepository.findSecondPartyFraudstersByFraudScore(percentileThreshold);
    }
}
