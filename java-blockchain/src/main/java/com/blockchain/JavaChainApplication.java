package com.blockchain;

import com.blockchain.config.BlockchainConfig;
import com.blockchain.core.Blockchain;
import com.blockchain.wallet.Wallet;
import com.blockchain.network.Node;
import com.blockchain.api.ApiServer;
import com.blockchain.transaction.Transaction;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.Scanner;

public class JavaChainApplication {

    private static Blockchain blockchain;
    private static Wallet wallet;
    private static Node node;
    private static ApiServer apiServer;

    public static void main(String[] args) {
        // Add Bouncy Castle security provider
        Security.addProvider(new BouncyCastleProvider());

        printBanner();

        try {
            // Load configuration
            BlockchainConfig config = BlockchainConfig.getInstance();
            config.loadFromEnvironment();

            System.out.println("Initializing blockchain...");
            blockchain = new Blockchain(config);

            System.out.println("Creating wallet...");
            wallet = new Wallet();
            wallet.setBlockchain(blockchain);
            System.out.println("Wallet created: " + wallet.getPublicKeyString());

            System.out.println("Starting P2P node on port " + config.getP2pPort() + "...");
            node = new Node(config.getP2pPort(), blockchain);
            node.start();

            System.out.println("Starting API server on port " + config.getApiPort() + "...");
            apiServer = new ApiServer(blockchain, node, wallet);
            apiServer.start();

            System.out.println("\nBlockchain ready!");
            System.out.println("Initial balance: " + wallet.getBalance());

            // Start interactive console
            startConsole();

        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("    ================================================");
        System.out.println("        J A V A   B L O C K C H A I N");
        System.out.println("        Enterprise Blockchain Platform v1.0.0");
        System.out.println("    ================================================");
        System.out.println();
    }

    private static void startConsole() {
        Scanner scanner = new Scanner(System.in);

        printHelp();

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("exit") || input.equals("quit")) {
                System.out.println("Shutting down...");
                shutdown();
                break;
            }

            switch (input) {
                case "balance":
                    double balance = wallet.getBalance();
                    System.out.println("Wallet balance: " + balance + " coins");
                    break;

                case "mine":
                    System.out.println("Mining pending transactions...");
                    com.blockchain.core.Block minedBlock = blockchain.minePendingTransactions(wallet);
                    if (minedBlock != null) {
                        System.out.println("Block mined successfully!");
                        System.out.println("Block hash: " + minedBlock.getHash());
                    } else {
                        System.out.println("No transactions to mine.");
                    }
                    break;

                case "info":
                    System.out.println("Blockchain Information:");
                    System.out.println("  Blocks: " + blockchain.getChain().size());
                    System.out.println("  Pending Transactions: " + blockchain.getPendingTransactions().size());
                    System.out.println("  Chain Valid: " + blockchain.isChainValid());
                    System.out.println("  Wallet Balance: " + wallet.getBalance());
                    break;

                case "blocks":
                    System.out.println("\n=== Blockchain ===");
                    for (com.blockchain.core.Block block : blockchain.getChain()) {
                        System.out.println("Block #" + block.getIndex());
                        System.out.println("  Hash: " + block.getHash());
                        System.out.println("  Previous: " + block.getPreviousHash());
                        System.out.println("  Transactions: " + block.getTransactions().size());
                        System.out.println("  Nonce: " + block.getNonce());
                        System.out.println();
                    }
                    break;

                case "validate":
                    boolean isValid = blockchain.isChainValid();
                    System.out.println("Blockchain is " + (isValid ? "VALID" : "INVALID"));
                    break;

                case "peers":
                    int peerCount = node.getPeerCount();
                    System.out.println("Connected peers: " + peerCount);
                    break;

                case "help":
                    printHelp();
                    break;

                default:
                    if (input.startsWith("send ")) {
                        handleSendCommand(input);
                    } else {
                        System.out.println("Unknown command. Type 'help' for available commands.");
                    }
                    break;
            }
        }

        scanner.close();
    }

    private static void handleSendCommand(String input) {
        String[] parts = input.split(" ");
        if (parts.length >= 3) {
            try {
                String recipient = parts[1];
                double amount = Double.parseDouble(parts[2]);
                Transaction tx = wallet.sendFunds(recipient, amount);
                if (tx != null) {
                    boolean added = blockchain.addTransaction(tx);
                    if (added) {
                        System.out.println("Transaction created and added to pending pool");
                        System.out.println("Transaction ID: " + tx.getTransactionId());
                    } else {
                        System.out.println("Failed to add transaction");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        } else {
            System.out.println("Usage: send <address> <amount>");
        }
    }

    private static void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  balance                    - Check your wallet balance");
        System.out.println("  send <address> <amount>    - Send coins to an address");
        System.out.println("  mine                       - Mine pending transactions");
        System.out.println("  info                       - Show blockchain information");
        System.out.println("  blocks                     - Show all blocks");
        System.out.println("  validate                   - Validate the blockchain");
        System.out.println("  peers                      - Show connected peers");
        System.out.println("  help                       - Show this help message");
        System.out.println("  exit                       - Exit the application");
    }

    private static void shutdown() {
        try {
            if (node != null) {
                node.stop();
            }
            if (apiServer != null) {
                apiServer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
        System.out.println("Goodbye!");
    }
}