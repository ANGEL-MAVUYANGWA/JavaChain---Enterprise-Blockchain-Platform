package com.blockchain.transaction;

import com.blockchain.core.Blockchain;
import com.blockchain.crypto.CryptoUtils;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Transaction {

    private String transactionId;
    private PublicKey sender;
    private Object recipient;
    private double amount;
    private double fee;
    private byte[] signature;
    private long timestamp;
    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;
    private Blockchain blockchain;

    public Transaction(PublicKey sender, Object recipient, double amount, List<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.inputs = inputs != null ? inputs : new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.fee = 0.01;
        this.transactionId = calculateHash();
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public String calculateHash() {
        // Fix null recipient for genesis transaction
        String senderStr = sender != null ? CryptoUtils.getStringFromKey(sender) : "null";
        String recipientStr = recipient != null ? recipient.toString() : "genesis";
        String data = senderStr + recipientStr + amount + timestamp + fee;
        return CryptoUtils.applySha256(data);
    }

    public void generateSignature(PrivateKey privateKey) {
        if (privateKey == null || sender == null) {
            return;
        }
        String senderStr = CryptoUtils.getStringFromKey(sender);
        String recipientStr = recipient != null ? recipient.toString() : "genesis";
        String data = senderStr + recipientStr + amount + fee;
        signature = CryptoUtils.applyEcdsaSig(privateKey, data);
    }

    public boolean verifySignature() {
        if (sender == null) {
            return true; // Genesis transaction or coinbase transaction
        }

        if (signature == null) {
            return false;
        }

        String senderStr = CryptoUtils.getStringFromKey(sender);
        String recipientStr = recipient != null ? recipient.toString() : "genesis";
        String data = senderStr + recipientStr + amount + fee;
        return CryptoUtils.verifyEcdsaSig(sender, signature, data);
    }

    public boolean isValid() {
        if (!verifySignature()) {
            return false;
        }

        // Genesis transaction is always valid
        if (sender == null) {
            return true;
        }

        double totalInput = getInputsValue();

        if (totalInput < amount + fee) {
            System.out.println("Insufficient funds: " + totalInput + " < " + (amount + fee));
            return false;
        }

        return true;
    }

    public boolean processTransaction(Map<String, TransactionOutput> utxoSet) {
        if (!verifySignature()) {
            System.out.println("Transaction signature verification failed");
            return false;
        }

        // Genesis transaction handling
        if (sender == null) {
            for (TransactionOutput output : outputs) {
                utxoSet.put(output.getId(), output);
            }
            return true;
        }

        double leftOver = getInputsValue() - (amount + fee);

        outputs.add(new TransactionOutput(recipient, amount, transactionId));
        if (leftOver > 0.001) { // Small threshold to avoid dust
            outputs.add(new TransactionOutput(sender, leftOver, transactionId));
        }

        for (TransactionOutput output : outputs) {
            utxoSet.put(output.getId(), output);
        }

        for (TransactionInput input : inputs) {
            utxoSet.remove(input.getTransactionOutputId());
        }

        return true;
    }

    public double getInputsValue() {
        double total = 0;
        for (TransactionInput input : inputs) {
            if (input.getUtxo() != null) {
                total += input.getUtxo().getAmount();
            }
        }
        return total;
    }

    public double getOutputsValue() {
        double total = 0;
        for (TransactionOutput output : outputs) {
            total += output.getAmount();
        }
        return total;
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public PublicKey getSender() { return sender; }
    public Object getRecipient() { return recipient; }
    public double getAmount() { return amount; }
    public double getFee() { return fee; }
    public List<TransactionInput> getInputs() { return inputs; }
    public List<TransactionOutput> getOutputs() { return outputs; }

    public void setFee(double fee) { this.fee = fee; }
}