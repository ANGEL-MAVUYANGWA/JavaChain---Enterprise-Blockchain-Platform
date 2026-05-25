package com.blockchain.transaction;

import com.blockchain.utils.HashUtil;
import java.security.PublicKey;

public class TransactionOutput {

    private String id;
    private Object recipient;
    private double amount;
    private String parentTransactionId;

    public TransactionOutput(Object recipient, double amount, String parentTransactionId) {
        this.recipient = recipient;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;

        String recipientStr = recipient != null ? recipient.toString() : "null";
        this.id = HashUtil.applySha256(
                recipientStr +
                        Double.toString(amount) +
                        parentTransactionId
        );
    }

    public boolean isMinedTo(Object address) {
        if (recipient == null || address == null) {
            return false;
        }

        if (recipient instanceof PublicKey && address instanceof PublicKey) {
            return recipient.equals(address);
        }

        return recipient.toString().equals(address.toString());
    }

    public String getId() {
        return id;
    }

    public Object getRecipient() {
        return recipient;
    }

    public double getAmount() {
        return amount;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }
}