package com.blockchain.network;

import com.blockchain.core.Blockchain;
import com.blockchain.core.Block;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Node extends WebSocketServer {

    private final Blockchain blockchain;
    private final List<WebSocketClient> peers;
    private final String nodeId;

    public Node(int port, Blockchain blockchain) {
        super(new java.net.InetSocketAddress(port));
        this.blockchain = blockchain;
        this.peers = new CopyOnWriteArrayList<>();
        this.nodeId = java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
        System.out.println("[P2P] New connection from: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[P2P] Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[P2P] Received: " + message);

        if (message.startsWith("REQUEST_CHAIN:")) {
            conn.send("CHAIN_DATA:" + blockchain.getChain().size());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("[P2P] Node started on port: " + getPort());
        System.out.println("[P2P] Node ID: " + nodeId);
    }

    public void connectToPeer(String host, int port) {
        try {
            WebSocketClient client = new WebSocketClient(new URI("ws://" + host + ":" + port)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("[P2P] Connected to peer: " + host + ":" + port);
                    send("REQUEST_CHAIN:");
                    peers.add(this);
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("[P2P] Peer message: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("[P2P] Disconnected from peer: " + host + ":" + port);
                    peers.remove(this);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("[P2P] Peer error: " + ex.getMessage());
                    peers.remove(this);
                }
            };

            client.connect();
        } catch (URISyntaxException e) {
            System.err.println("[P2P] Invalid peer address: " + host + ":" + port);
        }
    }

    public void broadcastBlock(Block block) {
        String message = "NEW_BLOCK:" + block.getHash();
        for (WebSocket conn : getConnections()) {
            conn.send(message);
        }
    }

    public int getPeerCount() {
        return getConnections().size() + peers.size();
    }
}