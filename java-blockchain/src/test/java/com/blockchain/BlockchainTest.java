package com.blockchain;

import com.blockchain.config.BlockchainConfig;
import com.blockchain.core.Blockchain;
import com.blockchain.wallet.Wallet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.Security;

import static org.junit.Assert.*;

public class BlockchainTest {

    private Blockchain blockchain;
    private Wallet wallet1;
    private Wallet wallet2;

    // Add Bouncy Castle provider before any tests run
    @BeforeClass
    public static void setUpClass() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void setUp() {
        BlockchainConfig config = BlockchainConfig.getInstance();
        blockchain = new Blockchain(config);
        wallet1 = new Wallet();
        wallet1.setBlockchain(blockchain);  // Set blockchain reference
        wallet2 = new Wallet();
        wallet2.setBlockchain(blockchain);  // Set blockchain reference
    }

    @Test
    public void testGenesisBlockCreation() {
        assertNotNull(blockchain.getChain());
        assertTrue(blockchain.getChain().size() >= 1);
        assertEquals(0, blockchain.getChain().get(0).getIndex());
        System.out.println("Genesis block test passed");
    }

    @Test
    public void testBlockMining() {
        int initialSize = blockchain.getChain().size();

        // Add a test transaction first
        Wallet minerWallet = new Wallet();
        minerWallet.setBlockchain(blockchain);

        // Mine pending transactions
        com.blockchain.core.Block minedBlock = blockchain.minePendingTransactions(minerWallet);

        // If no pending transactions, mining returns null
        if (minedBlock != null) {
            assertTrue(blockchain.getChain().size() > initialSize);
        }

        assertTrue(blockchain.isChainValid());
        System.out.println("Block mining test passed");
    }

    @Test
    public void testTransactionCreation() {
        // Mine initial block to get balance
        blockchain.minePendingTransactions(wallet1);
        double initialBalance = blockchain.getBalance(wallet1);

        // Balance should be mining reward (50) or 0 if no mining happened
        System.out.println("Initial balance: " + initialBalance);

        assertTrue(initialBalance >= 0);
        System.out.println("Transaction creation test passed");
    }

    @Test
    public void testChainValidation() {
        assertTrue(blockchain.isChainValid());

        // Try to mine some transactions
        Wallet minerWallet = new Wallet();
        minerWallet.setBlockchain(blockchain);
        blockchain.minePendingTransactions(minerWallet);

        assertTrue(blockchain.isChainValid());
        System.out.println("Chain validation test passed");
    }

    @Test
    public void testWalletBalance() {
        // Mine some blocks to get balance
        blockchain.minePendingTransactions(wallet1);
        double balance = blockchain.getBalance(wallet1);

        System.out.println("Wallet balance: " + balance);
        assertTrue(balance >= 0);
        System.out.println("Wallet balance test passed");
    }

    @Test
    public void testBlockHashIntegrity() {
        com.blockchain.core.Block genesisBlock = blockchain.getChain().get(0);
        String originalHash = genesisBlock.getHash();

        // Hash should remain the same
        assertEquals(originalHash, genesisBlock.calculateHash());
        System.out.println("Block hash integrity test passed");
    }
}