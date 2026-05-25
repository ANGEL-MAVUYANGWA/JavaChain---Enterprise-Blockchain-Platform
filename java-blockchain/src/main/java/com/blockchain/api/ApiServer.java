package com.blockchain.api;

import com.blockchain.core.Blockchain;
import com.blockchain.core.Block;
import com.blockchain.network.Node;
import com.blockchain.wallet.Wallet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.Spark;

import static spark.Spark.*;

public class ApiServer {

    private final Blockchain blockchain;
    private final Node node;
    private final Wallet wallet;
    private final Gson gson;
    private boolean isRunning = false;

    public ApiServer(Blockchain blockchain, Node node, Wallet wallet) {
        this.blockchain = blockchain;
        this.node = node;
        this.wallet = wallet;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // Set blockchain reference in wallet
        wallet.setBlockchain(blockchain);
    }

    public void start() {
        if (isRunning) {
            return;
        }

        port(8080);

        enableCors();

        get("/api/chain", (req, res) -> {
            res.type("application/json");
            return gson.toJson(blockchain.getChain());
        });

        get("/api/chain/latest", (req, res) -> {
            res.type("application/json");
            return gson.toJson(blockchain.getLatestBlock());
        });

        get("/api/transactions/pending", (req, res) -> {
            res.type("application/json");
            return gson.toJson(blockchain.getPendingTransactions());
        });

        post("/api/mine", (req, res) -> {
            Block block = blockchain.minePendingTransactions(wallet);
            res.type("application/json");
            if (block != null) {
                return gson.toJson(block);
            } else {
                return "{\"error\": \"No transactions to mine\"}";
            }
        });

        get("/api/wallet/balance", (req, res) -> {
            res.type("application/json");
            double balance = blockchain.getBalance(wallet);
            return "{\"balance\": " + balance + "}";
        });

        get("/api/wallet/address", (req, res) -> {
            res.type("application/json");
            return "{\"address\": \"" + wallet.getPublicKeyString() + "\"}";
        });

        get("/api/info", (req, res) -> {
            res.type("application/json");
            return gson.toJson(blockchain.getStatistics());
        });

        get("/api/validate", (req, res) -> {
            res.type("application/json");
            boolean isValid = blockchain.isChainValid();
            return "{\"valid\": " + isValid + "}";
        });

        get("/api/peers", (req, res) -> {
            res.type("application/json");
            int peerCount = node != null ? node.getPeerCount() : 0;
            return "{\"peers\": " + peerCount + "}";
        });

        System.out.println("[API] Server started on port 8080");
        isRunning = true;
    }

    public void stop() {
        if (isRunning) {
            Spark.stop();
            isRunning = false;
            System.out.println("[API] Server stopped");
        }
    }

    private void enableCors() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
    }
}