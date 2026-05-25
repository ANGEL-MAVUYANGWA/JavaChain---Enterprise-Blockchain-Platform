// src/main/java/com/blockchain/crypto/CryptoUtils.java
package com.blockchain.crypto;

import java.security.*;
import java.util.Base64;

/**
 * Utility class for cryptographic operations used throughout the blockchain.
 * Provides hashing, signing, and verification functions.
 */
public class CryptoUtils {

    /**
     * Applies SHA-256 hashing to a string input.
     *
     * @param input The string to hash
     * @return The hexadecimal hash string
     */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply SHA-256", e);
        }
    }

    /**
     * Creates an ECDSA signature for the given data using the private key.
     *
     * @param privateKey The private key to sign with
     * @param input The data to sign
     * @return The signature as a byte array
     */
    public static byte[] applyEcdsaSig(PrivateKey privateKey, String input) {
        try {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            dsa.update(input.getBytes());
            return dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ECDSA signature", e);
        }
    }

    /**
     * Verifies an ECDSA signature for the given data using the public key.
     *
     * @param publicKey The public key to verify with
     * @param signature The signature to verify
     * @param data The original signed data
     * @return true if the signature is valid
     */
    public static boolean verifyEcdsaSig(PublicKey publicKey, byte[] signature, String data) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts a key to a Base64 encoded string for storage or transmission.
     *
     * @param key The key to convert
     * @return The Base64 encoded string
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}