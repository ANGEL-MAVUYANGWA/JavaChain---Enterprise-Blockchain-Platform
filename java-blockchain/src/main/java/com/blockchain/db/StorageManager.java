// src/main/java/com/blockchain/db/StorageManager.java
package com.blockchain.db;

import com.blockchain.core.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages persistent storage of the blockchain to disk.
 * Allows the blockchain to be recovered after node restarts.
 */
public class StorageManager {
    private static final Logger logger = LoggerFactory.getLogger(StorageManager.class);

    private final String dataDirectory;

    public StorageManager() {
        this.dataDirectory = "./blockchain_data";
        createDataDirectory();
    }

    /**
     * Creates the data directory if it doesn't exist.
     */
    private void createDataDirectory() {
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("Created data directory: {}", dataDirectory);
            }
        }
    }

    /**
     * Saves a block to disk.
     *
     * @param block The block to save
     */
    public void saveBlock(Block block) {
        String filename = dataDirectory + "/block_" + block.getIndex() + ".dat";

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(block);
            logger.debug("Saved block {} to disk", block.getIndex());
        } catch (IOException e) {
            logger.error("Failed to save block " + block.getIndex(), e);
        }
    }

    /**
     * Loads a block from disk by index.
     *
     * @param index The block index
     * @return The loaded block, or null if not found
     */
    public Block loadBlock(int index) {
        String filename = dataDirectory + "/block_" + index + ".dat";
        File file = new File(filename);

        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Block) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load block " + index, e);
            return null;
        }
    }

    /**
     * Loads all blocks from disk.
     *
     * @return List of all blocks found
     */
    public List<Block> loadAllBlocks() {
        List<Block> blocks = new ArrayList<>();
        File dir = new File(dataDirectory);
        File[] files = dir.listFiles((d, name) -> name.startsWith("block_") && name.endsWith(".dat"));

        if (files != null) {
            for (File file : files) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    Block block = (Block) ois.readObject();
                    blocks.add(block);
                } catch (IOException | ClassNotFoundException e) {
                    logger.error("Failed to load block from file: " + file.getName(), e);
                }
            }
        }

        // Sort by index
        blocks.sort((b1, b2) -> Integer.compare(b1.getIndex(), b2.getIndex()));

        logger.info("Loaded {} blocks from disk", blocks.size());
        return blocks;
    }

    /**
     * Checks if a block exists on disk.
     *
     * @param index The block index
     * @return true if the block exists
     */
    public boolean blockExists(int index) {
        String filename = dataDirectory + "/block_" + index + ".dat";
        return new File(filename).exists();
    }
}