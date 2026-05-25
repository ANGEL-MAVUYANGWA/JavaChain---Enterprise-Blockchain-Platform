# JavaChain - Enterprise Blockchain Platform

## Overview

JavaChain is a complete, production-ready blockchain implementation written in Java. It features a proof-of-work consensus mechanism, cryptographic security, peer-to-peer networking, and a RESTful API. This project demonstrates core blockchain concepts including immutable ledger, transaction signing, mining, and distributed consensus.

---

## Features

### Core Blockchain
- Proof-of-Work (PoW) consensus algorithm
- SHA-256 cryptographic hashing
- Block validation and chain integrity checking
- Dynamic difficulty adjustment based on network hashrate
- Genesis block initialization with predefined coin distribution

### Transaction System
- UTXO (Unspent Transaction Output) model for balance tracking
- ECDSA digital signatures for transaction authentication
- Transaction fee mechanism
- Double-spending prevention
- Transaction pool (mempool) management

### Wallet Management
- ECDSA key pair generation (secp256r1 curve)
- Balance inquiry and transaction creation
- Public key as wallet address
- Secure private key storage

### Peer-to-Peer Network
- WebSocket-based P2P communication
- Automatic peer discovery and connection
- Block and transaction broadcasting
- Chain synchronization between nodes

### REST API
- HTTP-based API for external integration
- JSON request/response format
- CORS support for web applications
- Real-time blockchain queries

### Persistent Storage
- Local disk storage for blockchain data
- Automatic recovery after node restart
- Block-level indexing for efficient retrieval

---

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 11+ |
| Build Tool | Maven | 3.6+ |
| Cryptography | Bouncy Castle | 1.70 |
| WebSocket | Java-WebSocket | 1.5.4 |
| HTTP Server | Spark Java | 2.9.4 |
| JSON Processing | Gson | 2.10.1 |
| Logging | SLF4J + Simple | 2.0.9 |

---

## Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                    JavaChain Application                     │
├─────────────┬─────────────┬─────────────┬───────────────────┤
│   Wallet    │  Blockchain │    Node     │     API Server    │
│  - Keys     │  - Blocks   │  - P2P      │  - REST Endpoints │
│  - Balance  │  - UTXO Set │  - Peers    │  - JSON Responses │
│  - Send     │  - Mining   │  - Broadcast│  - CORS Support   │
├─────────────┼─────────────┼─────────────┼───────────────────┤
│                     Core Components                          │
│  - Transaction Processing  - Merkle Trees                    │
│  - Cryptographic Utils     - Storage Manager                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

- Java 11 or higher (JDK 17+ recommended)
- Maven 3.6 or higher
- Internet connection for dependency download
- Ports 8080 (API) and 8888 (P2P) available

---

## Installation

### Clone the Repository

```bash
git clone https://github.com/yourusername/javachain.git
cd javachain
```

### Build the Project

```bash
mvn clean package
```

The compiled JAR file will be created at:

```text
target/javachain-1.0.0.jar
```

### Run the Application

```bash
java -jar target/javachain-1.0.0.jar
```

---

## Configuration

JavaChain can be configured using environment variables:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `BLOCKCHAIN_DIFFICULTY` | Mining difficulty (1-10) | 4 |
| `P2P_PORT` | P2P network listening port | 8888 |
| `API_PORT` | HTTP API listening port | 8080 |
| `BLOCKCHAIN_DATA_DIR` | Data storage directory | ./blockchain_data |

### Example Configuration

```bash
# Linux/Mac
export BLOCKCHAIN_DIFFICULTY=3
export P2P_PORT=9000
export API_PORT=9001
java -jar target/javachain-1.0.0.jar
```

```powershell
# Windows PowerShell
$env:BLOCKCHAIN_DIFFICULTY="3"
$env:P2P_PORT="9000"
$env:API_PORT="9001"
java -jar target/javachain-1.0.0.jar
```

---

## Usage Guide

### Interactive Console Commands

| Command | Description | Example |
|---------|-------------|---------|
| `balance` | Check wallet balance | `balance` |
| `send <address> <amount>` | Send coins | `send 0x7d4e... 25` |
| `mine` | Mine pending transactions | `mine` |
| `info` | Show blockchain information | `info` |
| `blocks` | Display all blocks | `blocks` |
| `validate` | Validate blockchain integrity | `validate` |
| `peers` | Show connected peers | `peers` |
| `help` | Show available commands | `help` |
| `exit` | Exit the application | `exit` |

---

## REST API Documentation

### Base URL

```text
http://localhost:8080
```

### Blockchain Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/chain` | Get entire blockchain |
| GET | `/api/chain/latest` | Get latest block |
| GET | `/api/info` | Get blockchain statistics |
| GET | `/api/validate` | Validate blockchain |

### Wallet Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallet/balance` | Get wallet balance |
| GET | `/api/wallet/address` | Get wallet address |

### Network Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/peers` | Get connected peers |
| POST | `/api/peers/connect` | Connect to a peer |

---

## API Usage Examples

```bash
# Get blockchain information
curl http://localhost:8080/api/info

# Get wallet balance
curl http://localhost:8080/api/wallet/balance

# Mine pending transactions
curl -X POST http://localhost:8080/api/mine
```

---

## Project Structure

```text
javachain/
├── pom.xml
├── README.md
└── src/main/java/com/blockchain/
    ├── JavaChainApplication.java
    ├── config/
    │   └── BlockchainConfig.java
    ├── core/
    │   ├── Block.java
    │   └── Blockchain.java
    ├── transaction/
    │   ├── Transaction.java
    │   ├── TransactionInput.java
    │   └── TransactionOutput.java
    ├── wallet/
    │   └── Wallet.java
    ├── crypto/
    │   └── CryptoUtils.java
    ├── network/
    │   └── Node.java
    ├── api/
    │   └── ApiServer.java
    ├── db/
    │   └── StorageManager.java
    └── utils/
        └── HashUtil.java
```

---

## How It Works

### Block Structure
Each block contains:
- `index` – Position in the chain
- `timestamp` – Creation time
- `transactions` – Validated transactions
- `previousHash` – Link to previous block
- `hash` – Current block hash
- `nonce` – Proof-of-work value
- `difficulty` – Mining difficulty

### Mining Process
1. Collect pending transactions
2. Create a new candidate block
3. Solve the proof-of-work puzzle
4. Reward the miner with coins
5. Broadcast the new block to peers

### Security Features
- SHA-256 cryptographic hashing
- ECDSA digital signatures
- Proof-of-Work consensus
- Immutable chain validation
- UTXO-based double-spending prevention

---

## Testing

Run the test suite:

```bash
mvn test
```

---

## Running in Development Mode

```bash
mvn compile exec:java -Dexec.mainClass="com.blockchain.JavaChainApplication"
```

---

## Troubleshooting

### Port Already in Use

```text
Error: Address already in use
```

Solution:

```bash
export P2P_PORT=8889
export API_PORT=8081
```

### Slow Mining

Reduce difficulty:

```bash
export BLOCKCHAIN_DIFFICULTY=2
```

---

## Performance Considerations

- Difficulty level 4 mines blocks in approximately 0.5–2 seconds on modern hardware
- Maximum of 100 transactions per block
- In-memory UTXO set for faster performance
- Lightweight P2P communication

---

## Future Enhancements

- [ ] Merkle tree optimization
- [ ] SPV support
- [ ] Persistent peer discovery
- [ ] Dynamic transaction fee market
- [ ] Multi-threaded mining
- [ ] Smart contract support
- [ ] Wallet encryption
- [ ] Blockchain explorer UI

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push your branch
5. Open a Pull Request

---

## License

This project is licensed under the MIT License.

---

## Acknowledgments

- Bouncy Castle
- Java-WebSocket
- Spark Java
- Gson

---

## Disclaimer

This project is intended for educational and research purposes only. It should not be used in production financial systems without extensive security auditing and testing.

---

## Version History

### v1.0.0 — 2026-05-25
- Initial release
- Blockchain core implementation
- P2P networking support
- REST API integration
- Persistent storage system
- Wallet management

---

## Contact

- Repository: `https://github.com/yourusername/javachain`
- Issues: `https://github.com/yourusername/javachain/issues`

---

**Built with Java and ❤️**

