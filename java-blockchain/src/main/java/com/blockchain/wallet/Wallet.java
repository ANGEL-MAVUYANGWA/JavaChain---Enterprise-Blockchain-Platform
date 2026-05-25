package com.blockchain.wallet;

import com.blockchain.crypto.CryptoUtils;
import com.blockchain.transaction.Transaction;
import com.blockchain.transaction.TransactionInput;
import com.blockchain.transaction.TransactionOutput;
import com.blockchain.core.Blockchain;

import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Wallet {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Blockchain blockchain;  // Add blockchain reference

    public Wallet() {
        generateKeyPair();
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

    public double getBalance() {
        if (blockchain == null) {
            return 0;
        }

        double total = 0;
        Map<String, TransactionOutput> utxoSet = blockchain.getUtxoSet();

        for (Map.Entry<String, TransactionOutput> item : utxoSet.entrySet()) {
            TransactionOutput utxo = item.getValue();
            if (utxo.isMinedTo(publicKey)) {
                total += utxo.getAmount();
            }
        }
        return total;
    }

    public Transaction sendFunds(Object recipient, double value) {
        if (getBalance() < value) {
            System.out.println("Insufficient funds to send " + value);
            return null;
        }

        List<TransactionInput> inputs = new ArrayList<>();
        double total = 0;
        Map<String, TransactionOutput> utxoSet = blockchain.getUtxoSet();

        for (Map.Entry<String, TransactionOutput> item : utxoSet.entrySet()) {
            TransactionOutput utxo = item.getValue();
            if (utxo.isMinedTo(publicKey)) {
                total += utxo.getAmount();
                inputs.add(new TransactionInput(utxo.getId()));
                if (total > value) {
                    break;
                }
            }
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);
        newTransaction.setBlockchain(blockchain);

        return newTransaction;
    }

    public String getPublicKeyString() {
        return CryptoUtils.getStringFromKey(publicKey);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}