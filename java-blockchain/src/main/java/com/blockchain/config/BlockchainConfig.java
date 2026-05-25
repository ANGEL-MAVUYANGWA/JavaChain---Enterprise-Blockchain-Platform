// src/main/java/com/blockchain/config/BlockchainConfig.java
package com.blockchain.config;

/**
 * Configuration class for the blockchain network.
 * All adjustable parameters are centralized here for easy tuning.
 */
public class BlockchainConfig {

    private int difficulty;
    private double miningReward;
    private int maxTransactionsPerBlock;
    private long blockTimeTarget;
    private int p2pPort;
    private int apiPort;
    private int maxPeers;
    private int requiredConfirmations;
    private String dataDirectory;

    private static BlockchainConfig instance;

    private BlockchainConfig() {
        this.difficulty = 4;
        this.miningReward = 50.0;
        this.maxTransactionsPerBlock = 100;
        this.blockTimeTarget = 10000;
        this.p2pPort = 8888;
        this.apiPort = 8080;
        this.maxPeers = 30;
        this.requiredConfirmations = 6;
        this.dataDirectory = "./blockchain_data";
    }

    public static BlockchainConfig getInstance() {
        if (instance == null) {
            instance = new BlockchainConfig();
        }
        return instance;
    }

    public void loadFromEnvironment() {
        String difficultyEnv = System.getenv("BLOCKCHAIN_DIFFICULTY");
        if (difficultyEnv != null && !difficultyEnv.isEmpty()) {
            this.difficulty = Integer.parseInt(difficultyEnv);
        }

        String p2pPortEnv = System.getenv("P2P_PORT");
        if (p2pPortEnv != null && !p2pPortEnv.isEmpty()) {
            this.p2pPort = Integer.parseInt(p2pPortEnv);
        }

        String apiPortEnv = System.getenv("API_PORT");
        if (apiPortEnv != null && !apiPortEnv.isEmpty()) {
            this.apiPort = Integer.parseInt(apiPortEnv);
        }

        String dataDirEnv = System.getenv("BLOCKCHAIN_DATA_DIR");
        if (dataDirEnv != null && !dataDirEnv.isEmpty()) {
            this.dataDirectory = dataDirEnv;
        }
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getMiningReward() {
        return miningReward;
    }

    public void setMiningReward(double miningReward) {
        this.miningReward = miningReward;
    }

    public int getMaxTransactionsPerBlock() {
        return maxTransactionsPerBlock;
    }

    public long getBlockTimeTarget() {
        return blockTimeTarget;
    }

    public int getP2pPort() {
        return p2pPort;
    }

    public int getApiPort() {
        return apiPort;
    }

    public int getMaxPeers() {
        return maxPeers;
    }

    public int getRequiredConfirmations() {
        return requiredConfirmations;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }
}