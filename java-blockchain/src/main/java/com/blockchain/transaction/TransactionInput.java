// src/main/java/com/blockchain/transaction/TransactionInput.java
package com.blockchain.transaction;

/**
 * Represents an input to a transaction, which references a previous
 * unspent transaction output (UTXO). This is like referencing a
 * specific coin or bill that you are spending.
 */
public class TransactionInput {

    private String transactionOutputId;
    private TransactionOutput utxo;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public TransactionOutput getUtxo() {
        return utxo;
    }

    public void setUtxo(TransactionOutput utxo) {
        this.utxo = utxo;
    }
}