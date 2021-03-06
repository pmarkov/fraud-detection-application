package com.student.fraudsters.simulation;

import org.apache.commons.text.StringSubstitutor;
import org.neo4j.driver.Query;
import org.neo4j.driver.Values;
import org.paysim.actors.SuperActor;
import org.paysim.base.Transaction;
import org.paysim.identity.ClientIdentity;
import org.paysim.identity.Properties;

import java.util.*;
import java.util.stream.Collectors;

public class Util {

    protected static String capitalize(String string) {
        return Arrays.stream(string.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(""));
    }

    public static Query compileBulkTransactionQuery(List<Transaction> transactions) {
        Map<String, Object> propMap = new HashMap<>();
        List<Map<String, Object>> unwindList = new ArrayList<>(transactions.size());
        for (Transaction transaction : transactions) {
            unwindList.add(propsFromTx(transaction));
        }
        propMap.put("txs", unwindList);
        return new Query(CypherConstants.BULK_TRANSACTION_QUERY_STRING, propMap);
    }

    protected static Map<String, Object> propsFromTx(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("amount", t.getAmount());
        map.put("fraud", t.isFraud());
        map.put("flaggedFraud", t.isFlaggedFraud());
        map.put("senderId", t.getIdOrig());
        map.put("receiverId", t.getIdDest());
        map.put("senderName", t.getNameOrig());
        map.put("receiverName", t.getNameDest());
        map.put("id", String.format("tx-%s", t.getGlobalStep()));
        map.put("ts", t.getStep());
        map.put("step", t.getStep());
        map.put("globalStep", t.getGlobalStep());
        map.put("senderLabel", capitalize(t.getOrigType().toString()));
        map.put("receiverLabel", capitalize(t.getDestType().toString()));
        map.put("label", capitalize(t.getAction()));
        return map;
    }

    public static Query compilePropertyUpdateQuery(SuperActor actor) {
        final String label = capitalize(actor.getType().toString());

        final Map<String, Object> props = actor.getIdentityAsMap();
        props.remove(Properties.ID);

        StringSubstitutor stringSubstitutor = new StringSubstitutor(Map.of("label", label));

        return new Query(
                stringSubstitutor.replace(CypherConstants.UPDATE_NODE_PROPS),
                Values.parameters("id", actor.getId(), "props", props));
    }

    public static Query compileClientIdentityQuery(ClientIdentity identity) {
        return new Query(
                CypherConstants.CREATE_IDENTITY,
                Values.parameters(
                        "ssn", identity.ssn,
                        "email", identity.email,
                        "name", identity.name,
                        "phoneNumber", identity.phoneNumber,
                        "clientId", identity.id));
    }
}
