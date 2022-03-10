package com.student.fraudsters.simulation;

public class CypherConstants {

    public static final String DELETE_SCHEMA = "CALL apoc.schema.assert({},{},true)";

    public static final String DELETE_NODES_AND_RELATIONSHIPS = "CALL apoc.periodic.iterate(\"MATCH (n) RETURN n\", \"DETACH DELETE n\", {batchSize: $batchSize})";

    public static final String[] SCHEMA_QUERIES = {
            // Core Types
            "CREATE CONSTRAINT ON (c:Client) ASSERT c.id IS UNIQUE",
            "CREATE CONSTRAINT ON (b:Bank) ASSERT b.id IS UNIQUE",
            "CREATE CONSTRAINT ON (m:Merchant) ASSERT m.id IS UNIQUE",
            "CREATE CONSTRAINT ON (m:Mule) ASSERT m.id IS UNIQUE",

            // Transaction Types
            "CREATE CONSTRAINT ON (c:CashIn) ASSERT c.id IS UNIQUE",
            "CREATE CONSTRAINT ON (c:CashOut) ASSERT c.id IS UNIQUE",
            "CREATE CONSTRAINT ON (d:Debit) ASSERT d.id IS UNIQUE",
            "CREATE CONSTRAINT ON (p:Payment) ASSERT p.id IS UNIQUE",
            "CREATE CONSTRAINT ON (t:Transfer) ASSERT t.id IS UNIQUE",
            "CREATE CONSTRAINT ON (tx:Transaction) ASSERT tx.id IS UNIQUE",

            // Identity Types
            "CREATE CONSTRAINT ON (e:Email) ASSERT e.email IS UNIQUE",
            "CREATE CONSTRAINT ON (s:SSN) ASSERT s.ssn IS UNIQUE",
            "CREATE CONSTRAINT ON (p:Phone) ASSERT p.phoneNumber IS UNIQUE",

            // Various Indices
            "CREATE INDEX ON :Transaction(globalStep)",
            "CREATE INDEX ON :CashIn(globalStep)",
            "CREATE INDEX ON :CashOut(globalStep)",
            "CREATE INDEX ON :Debit(globalStep)",
            "CREATE INDEX ON :Payment(globalStep)",
            "CREATE INDEX ON :Transfer(globalStep)",
            "CREATE INDEX ON :Merchant(highRisk)",
            "CREATE INDEX ON :Transaction(fraud)",
    };

    public static final String BULK_TRANSACTION_QUERY_STRING = String.join(
            "\n",
            new String[] {
                    "UNWIND $txs AS tx",
                    "  CALL apoc.merge.node([ tx.senderLabel ], {id: tx.senderId }) YIELD node AS s",
                    "  CALL apoc.merge.node([ tx.receiverLabel ], { id: tx.receiverId }) YIELD node AS r",
                    "  CALL apoc.create.node([ 'Transaction', tx.label ], { id: tx.id, ts: tx.ts, amount: tx.amount, fraud: tx.fraud, step: tx.step, globalStep: tx.globalStep}) YIELD node AS t",
                    "  CREATE (s)-[:PERFORMED]->(t)-[:TO]->(r)",
            });

    public static final String CREATE_IDENTITY =
            String.join(
                    "\n",
                    new String[] {
                            "MERGE (c:Client {id: $clientId}) ON MATCH SET c.name = $name",
                            "MERGE (s:SSN {ssn: $ssn})",
                            "MERGE (e:Email {email: $email})",
                            "MERGE (p:Phone {phoneNumber: $phoneNumber})",
                            "MERGE (c)-[:HAS_SSN]->(s)",
                            "MERGE (c)-[:HAS_EMAIL]->(e)",
                            "MERGE (c)-[:HAS_PHONE]->(p)",
                    });

    public static final String MAKE_MULES_CLIENTS = "MATCH (m:Mule) WHERE NOT m:Client SET m:Client";

    public static final String UPDATE_NODE_PROPS = "MATCH (n:${label} {id: $id}) SET n += $props";
}
