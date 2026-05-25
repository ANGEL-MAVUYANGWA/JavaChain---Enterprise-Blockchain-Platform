package com.blockchain.core;

import com.blockchain.config.BlockchainConfig;
import com.blockchain.transaction.Transaction;
import com.blockchain.transaction.TransactionInput;
import com.blockchain.transaction.TransactionOutput;
import com.blockchain.wallet.Wallet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Blockchain {
    private static final Logger logger = Logger.getLogger(Blockchain.class.getName());

    private final List<Block> chain;
    private final Map<String, TransactionOutput> utxoSet;
    private final List<Transaction> pendingTransactions;
    private final BlockchainConfig config;
    private long totalTransactions;
    private double totalFeesCollected;

    public Blockchain(BlockchainConfig config) {
        this.config = config;
        this.chain = new ArrayList<>();
        this.utxoSet = new ConcurrentHashMap<>();
        this.pendingTransactions = new ArrayList<>();
        this.totalTransactions = 0;
        this.totalFeesCollected = 0;

        if (chain.isEmpty()) {
            createGenesisBlock();
        }

        logger.info("Blockchain initialized with " + chain.size() + " blocks");
    }

    public Map<String, TransactionOutput> getUtxoSet() {
        return utxoSet;
    }

    private void createGenesisBlock() {
        logger.info("Creating genesis block...");

        Block genesisBlock = new Block(0, "0", config.getDifficulty());

        Transaction genesisTransaction = createGenesisTransaction();
        genesisBlock.addTransaction(genesisTransaction);
        genesisBlock.mineBlock();

        chain.add(genesisBlock);

        for (TransactionOutput output : genesisTransaction.getOutputs()) {
            utxoSet.put(output.getId(), output);
        }

        logger.info("Genesis block created with hash: " + genesisBlock.getHash());
    }

    private Transaction createGenesisTransaction() {
        // Create genesis transaction with null sender and null recipient (special case)
        Transaction genesis = new Transaction(null, null, 0, null);
        genesis.getOutputs().clear();

        // Add initial coin distribution to specific addresses
        // In a real blockchain, these would be actual public keys
        String[] initialAddresses = {
                "network_foundation",
                "development_fund",
                "community_rewards",
                "mining_pool"
        };

        double[] initialAmounts = {1000000, 500000, 500000, 2000000};

        for (int i = 0; i < initialAddresses.length; i++) {
            TransactionOutput output = new TransactionOutput(
                    initialAddresses[i],
                    initialAmounts[i],
                    genesis.getTransactionId()
            );
            genesis.getOutputs().add(output);
        }

        return genesis;
    }

    public Block minePendingTransactions(Wallet minerWallet) {
        if (pendingTransactions.isEmpty()) {
            logger.info("No pending transactions to mine");
            return null;
        }

        logger.info("Mining block with " + pendingTransactions.size() + " transactions");

        Block newBlock = new Block(
                chain.size(),
                getLatestBlock().getHash(),
                calculateDynamicDifficulty()
        );

        int transactionsToAdd = Math.min(pendingTransactions.size(), config.getMaxTransactionsPerBlock());
        List<Transaction> transactionsForBlock = new ArrayList<>(pendingTransactions.subList(0, transactionsToAdd));

        Transaction rewardTransaction = createMiningRewardTransaction(minerWallet);
        newBlock.addTransaction(rewardTransaction);

        for (Transaction tx : transactionsForBlock) {
            if (tx.isValid()) {
                newBlock.addTransaction(tx);
            }
        }

        newBlock.buildMerkleTree();
        newBlock.mineBlock();

        if (isValidNewBlock(newBlock, getLatestBlock())) {
            chain.add(newBlock);
            updateUtxoSet(newBlock);
            pendingTransactions.removeAll(transactionsForBlock);
            totalTransactions += transactionsForBlock.size();
            totalFeesCollected += calculateBlockFees(newBlock);

            logger.info("Block " + newBlock.getIndex() + " mined successfully. Hash: " + newBlock.getHash());

            return newBlock;
        } else {
            logger.info("Invalid block rejected");
            return null;
        }
    }

    private int calculateDynamicDifficulty() {
        if (chain.size() < 10) {
            return config.getDifficulty();
        }

        List<Block> recentBlocks = chain.subList(chain.size() - 10, chain.size());
        long totalTime = 0;

        for (int i = 1; i < recentBlocks.size(); i++) {
            Block current = recentBlocks.get(i);
            Block previous = recentBlocks.get(i - 1);
            totalTime += (current.getTimestamp() - previous.getTimestamp());
        }

        long averageTime = totalTime / (recentBlocks.size() - 1);
        long targetTime = config.getBlockTimeTarget();

        int newDifficulty = config.getDifficulty();

        if (averageTime < targetTime / 2) {
            newDifficulty = Math.min(config.getDifficulty() + 1, 10);
        } else if (averageTime > targetTime * 2) {
            newDifficulty = Math.max(config.getDifficulty() - 1, 1);
        }

        return newDifficulty;
    }

    private Transaction createMiningRewardTransaction(Wallet minerWallet) {
        double totalFees = calculatePendingTransactionFees();

        Transaction rewardTx = new Transaction(
                null,  // Coinbase transaction has no sender
                minerWallet.getPublicKey(),
                config.getMiningReward() + totalFees,
                null
        );

        rewardTx.generateSignature(minerWallet.getPrivateKey());
        rewardTx.setBlockchain(this);

        return rewardTx;
    }

    private double calculatePendingTransactionFees() {
        return pendingTransactions.stream()
                .mapToDouble(Transaction::getFee)
                .limit(config.getMaxTransactionsPerBlock())
                .sum();
    }

    private double calculateBlockFees(Block block) {
        return block.getTransactions().stream()
                .skip(1)
                .mapToDouble(Transaction::getFee)
                .sum();
    }

    private void updateUtxoSet(Block block) {
        for (Transaction transaction : block.getTransactions()) {
            for (TransactionInput input : transaction.getInputs()) {
                utxoSet.remove(input.getTransactionOutputId());
            }

            for (TransactionOutput output : transaction.getOutputs()) {
                utxoSet.put(output.getId(), output);
            }
        }
    }

    private boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (newBlock.getIndex() != previousBlock.getIndex() + 1) {
            return false;
        }

        if (!newBlock.getPreviousHash().equals(previousBlock.getHash())) {
            return false;
        }

        if (!newBlock.getHash().equals(newBlock.calculateHash())) {
            return false;
        }

        String target = "0".repeat(newBlock.getDifficulty());
        if (newBlock.getHash().length() < newBlock.getDifficulty() ||
                !newBlock.getHash().substring(0, newBlock.getDifficulty()).equals(target)) {
            return false;
        }

        for (Transaction transaction : newBlock.getTransactions()) {
            if (!transaction.isValid()) {
                return false;
            }
        }

        return true;
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            if (!isValidNewBlock(currentBlock, previousBlock)) {
                logger.info("Block " + i + " is invalid");
                return false;
            }
        }

        logger.info("Blockchain validation passed. All " + chain.size() + " blocks are valid.");
        return true;
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            logger.info("Transaction is null");
            return false;
        }

        if (!transaction.verifySignature()) {
            logger.info("Transaction signature invalid");
            return false;
        }

        if (!transaction.isValid()) {
            logger.info("Transaction is invalid");
            return false;
        }

        // Skip balance check for coinbase transactions
        if (transaction.getSender() != null && transaction.getAmount() > getBalance(transaction.getSender())) {
            logger.info("Insufficient balance for transaction");
            return false;
        }

        pendingTransactions.add(transaction);
        logger.info("Transaction added to pending pool: " + transaction.getTransactionId());

        return true;
    }

    public double getBalance(Object address) {
        return utxoSet.values().stream()
                .filter(utxo -> utxo.isMinedTo(address))
                .mapToDouble(TransactionOutput::getAmount)
                .sum();
    }

    public double getBalance(Wallet wallet) {
        return getBalance(wallet.getPublicKey());
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public List<Block> getChain() {
        return new ArrayList<>(chain);
    }

    public List<Transaction> getPendingTransactions() {
        return new ArrayList<>(pendingTransactions);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBlocks", chain.size());
        stats.put("totalTransactions", totalTransactions);
        stats.put("totalFeesCollected", totalFeesCollected);
        stats.put("pendingTransactions", pendingTransactions.size());
        stats.put("utxoCount", utxoSet.size());
        stats.put("chainValid", isChainValid());
        stats.put("currentDifficulty", config.getDifficulty());
        if (!chain.isEmpty()) {
            stats.put("latestBlock", getLatestBlock().getHash());
        }
        return stats;
    }
}