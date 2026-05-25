package com.blockchain.core;

import com.blockchain.transaction.Transaction;
import com.blockchain.utils.HashUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Block {

    private final int index;
    private final long timestamp;
    private final List<Transaction> transactions;
    private final String previousHash;
    private String hash;
    private int nonce;
    private int difficulty;
    private String merkleRoot;

    public Block(int index, String previousHash, int difficulty) {
        this.index = index;
        this.timestamp = Instant.now().toEpochMilli();
        this.transactions = new ArrayList<>();
        this.previousHash = previousHash;
        this.difficulty = difficulty;
        this.nonce = 0;
        this.hash = "";
        this.merkleRoot = "";
    }

    public String calculateHash() {
        String dataToHash = index +
                Long.toString(timestamp) +
                transactions.stream()
                        .map(Transaction::calculateHash)
                        .collect(Collectors.joining()) +
                previousHash +
                Integer.toString(nonce) +
                merkleRoot;

        return HashUtil.applySha256(dataToHash);
    }

    public void mineBlock() {
        // Calculate initial hash
        hash = calculateHash();

        String target = "0".repeat(difficulty);
        long startTime = System.currentTimeMillis();

        System.out.println("Mining block " + index + " with difficulty " + difficulty);

        // Keep mining until we find a hash that starts with the required number of zeros
        while (!hash.substring(0, Math.min(difficulty, hash.length())).equals(target)) {
            nonce++;
            hash = calculateHash();

            if (nonce % 100000 == 0) {
                System.out.println("  Attempts: " + nonce + ", Current hash: " + hash.substring(0, Math.min(10, hash.length())) + "...");
            }
        }

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        System.out.println("Block mined successfully!");
        System.out.println("  Hash: " + hash);
        System.out.println("  Nonce: " + nonce);
        System.out.println("  Time: " + String.format("%.2f", seconds) + " seconds");
    }

    public void buildMerkleTree() {
        if (transactions.isEmpty()) {
            this.merkleRoot = "0";
            return;
        }

        List<String> hashes = transactions.stream()
                .map(Transaction::calculateHash)
                .collect(Collectors.toList());

        while (hashes.size() > 1) {
            List<String> nextLevel = new ArrayList<>();

            for (int i = 0; i < hashes.size(); i += 2) {
                if (i + 1 < hashes.size()) {
                    String combined = HashUtil.applySha256(hashes.get(i) + hashes.get(i + 1));
                    nextLevel.add(combined);
                } else {
                    nextLevel.add(hashes.get(i));
                }
            }
            hashes = nextLevel;
        }

        this.merkleRoot = hashes.get(0);
    }

    public boolean addTransaction(Transaction transaction) {
        if (index != 0) {
            if (!transaction.verifySignature()) {
                System.out.println("Transaction signature is invalid");
                return false;
            }

            if (!transaction.isValid()) {
                System.out.println("Transaction is invalid");
                return false;
            }
        }

        transactions.add(transaction);
        buildMerkleTree();
        return true;
    }

    public boolean isValid() {
        String calculatedHash = calculateHash();
        if (!calculatedHash.equals(hash)) {
            System.out.println("Block " + index + " hash mismatch");
            return false;
        }

        String target = "0".repeat(difficulty);
        if (hash.length() < difficulty || !hash.substring(0, difficulty).equals(target)) {
            System.out.println("Block " + index + " proof-of-work invalid");
            return false;
        }

        return true;
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public int getNonce() {
        return nonce;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }
}