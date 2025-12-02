# IoT-Enabled Carbon Credit Trading Platform

<p>
  <strong>Real-time carbon emission monitoring via ESP sensors with integrated credit marketplace</strong>
</p>

---

## 📋 Table of Contents

- [Introduction](#-introduction)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Platform Workflows](#-platform-workflows)
- [IoT Integration](#-iot-integration)
- [Database Schema](#-database-schema)
- [Security Architecture](#-security-architecture)
- [Technology Stack](#️-technology-stack)
- [Installation \& Setup](#️-installation--setup)
- [User Manual](#-user-manual)
- [API Documentation](#-api-documentation)
- [Troubleshooting](#-troubleshooting)
- [License](#-license)

---

## 🌍 Introduction

### What is a Carbon Credit?

A **Carbon Credit** represents 1 tonne of CO₂ that has been removed from or prevented from entering the atmosphere.

- **1 Carbon Credit = 1 Tonne of CO₂ removed or avoided**

### Why This Platform?

**EcoSense** is a comprehensive marketplace that bridges environmental sustainability with economic incentives:

- **Producers** (factories, industries) can purchase credits to offset their emissions
- **Reducers** (solar farms, wind projects, reforestation) earn credits for environmental impact
- **Real-time Monitoring** through IoT sensors (ESP32/ESP8266) for accurate carbon tracking
- **Transparent Trading** with automated order matching and secure transactions

---

## ✨ Key Features

### 🔐 User Management
- **Role-Based Access**: Users, Producers, and Admins
- **JWT Authentication**: Secure token-based authentication
- **Digital Wallet**: Integrated cash and credit balance management

### 🌱 Project Management
- **Project Submission**: Create and submit environmental projects
- **Admin Verification**: Rigorous verification process before credit issuance
- **Automated Credit Generation**: Credits automatically issued upon project approval
- **Project Types**:
  - **Reducing**: Solar, Wind, Reforestation (earn credits)
  - **Producing**: Manufacturing, Industrial (buy credits)

### 📡 IoT Sensor Integration
- **ESP32/ESP8266 Support**: Real-time carbon emission monitoring
- **MQTT Protocol**: Secure, lightweight messaging
- **HiveMQ Integration**: Cloud MQTT broker support
- **Automated Data Collection**: Continuous usage tracking from sensors
- **Per-User Configuration**: Custom broker, topic, and authentication settings

### 💹 Marketplace Trading
- **Order Matching Engine**: Automated buyer-seller matching
- **Dynamic Pricing**: Prices within ±10% of last traded price
- **Order Types**: Buy and Sell orders
- **Order Status**: Pending, Partial, Executed, Cancelled
- **Real-time Execution**: Instant trade completion when orders match

### 📊 Dashboard & Analytics
- **Portfolio Valuation**: Real-time portfolio value based on market prices
- **Usage History**: Cumulative CO₂ emission tracking from IoT sensors
- **Price Charts**: Historical price trends with trading chart visualization
- **Market Depth**: Real-time order book visualization
- **Transaction History**: Complete audit trail of all transactions

### 🔄 Credit Lifecycle
- **Generation**: Automated upon project verification
- **Trading**: Buy/sell in the marketplace
- **Retirement**: Permanent removal to claim carbon neutrality

---

## 🏗️ System Architecture

```mermaid
graph TB
    subgraph "IoT Layer"
        ESP[ESP32/ESP8266 Sensors]
        MQTT[MQTT Broker - HiveMQ]
    end
    subgraph "Frontend Layer"
        USER[User Browser]
        REACT[React Application]
    end
    subgraph "Backend Layer"
        API[Spring Boot REST API]
        AUTH[Authentication Service]
        PROJ[Project Service]
        MARKET[Marketplace Engine]
        WALLET[Wallet Service]
        MQTT_SUB[MQTT Subscriber Service]
        USAGE[Usage Service]
    end
    subgraph "Data Layer"
        MONGO[(MongoDB Database)]
    end
    ESP -->|Publishes CO2 Data| MQTT
    MQTT -->|Subscribe| MQTT_SUB
    MQTT_SUB -->|Process Messages| USAGE
    USAGE -->|Store Usage Data| MONGO
    USER -->|HTTPS| REACT
    REACT -->|REST API Calls| API
    API --> AUTH
    API --> PROJ
    API --> MARKET
    API --> WALLET
    API --> USAGE
    AUTH -->|JWT Tokens| REACT
    PROJ -->|CRUD Operations| MONGO
    MARKET -->|Order Matching| MONGO
    WALLET -->|Transactions| MONGO
```

### Component Interaction Diagram

```mermaid
graph LR
    subgraph Client
        UI[React Components]
        STATE[Local State]
        API_CLIENT[API Client]
    end
    subgraph Security
        JWT_FILTER[JWT Filter]
        AUTH_MANAGER[Auth Manager]
    end
    subgraph Controllers
        AUTH_CTRL[Auth Controller]
        PROJ_CTRL[Project Controller]
        MARKET_CTRL[Marketplace Controller]
        WALLET_CTRL[Wallet Controller]
        USAGE_CTRL[Usage Controller]
    end
    subgraph Services
        USER_SVC[User Service]
        PROJ_SVC[Project Service]
        MARKET_SVC[Marketplace Service]
        WALLET_SVC[Wallet Service]
        MQTT_SVC[MQTT Service]
        USAGE_SVC[Usage Service]
    end
    subgraph Repositories
        USER_REPO[(User Repository)]
        PROJ_REPO[(Project Repository)]
        ORDER_REPO[(Order Repository)]
        WALLET_REPO[(Wallet Repository)]
        USAGE_REPO[(Usage Repository)]
    end
    UI --> API_CLIENT
    API_CLIENT -->|HTTP + JWT| JWT_FILTER
    JWT_FILTER --> AUTH_MANAGER
    AUTH_MANAGER --> AUTH_CTRL
    AUTH_MANAGER --> PROJ_CTRL
    AUTH_MANAGER --> MARKET_CTRL
    AUTH_MANAGER --> WALLET_CTRL
    AUTH_MANAGER --> USAGE_CTRL
    AUTH_CTRL --> USER_SVC
    PROJ_CTRL --> PROJ_SVC
    MARKET_CTRL --> MARKET_SVC
    WALLET_CTRL --> WALLET_SVC
    USAGE_CTRL --> USAGE_SVC
    USER_SVC --> USER_REPO
    PROJ_SVC --> PROJ_REPO
    PROJ_SVC --> WALLET_SVC
    MARKET_SVC --> ORDER_REPO
    MARKET_SVC --> WALLET_SVC
    WALLET_SVC --> WALLET_REPO
    MQTT_SVC --> USAGE_SVC
    USAGE_SVC --> USAGE_REPO
```

### Architecture Highlights

- **Three-Tier Architecture**: Separation of presentation, business logic, and data
- **RESTful API**: Standard HTTP methods for all operations
- **JWT Security**: Stateless authentication with role-based access control
- **Event-Driven IoT**: Asynchronous MQTT message processing
- **NoSQL Database**: Flexible schema for diverse data types
- **Microservice-Ready**: Service-oriented design for scalability

---

## 📊 Platform Workflows

### 1. Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant React as React Frontend
    participant API as Spring Boot API
    participant Auth as Auth Service
    participant DB as MongoDB
    participant JWT as JWT Util
    
    User->>React: Enter Credentials
    React->>API: POST /api/auth/signin
    API->>Auth: authenticate(email, password)
    Auth->>DB: findByEmail(email)
    DB-->>Auth: User Document
    Auth->>Auth: Compare Password Hash
    
    alt Authentication Success
        Auth->>JWT: generateToken(user)
        JWT-->>Auth: JWT Token
        Auth-->>API: Authentication Token
        API-->>React: {token, user info}
        React->>React: Store Token in localStorage
        React-->>User: Redirect to Dashboard
    else Authentication Failed
        Auth-->>API: Invalid Credentials
        API-->>React: 401 Unauthorized
        React-->>User: Show Error Message
    end
```

### 2. Project Verification Workflow

```mermaid
stateDiagram-v2
    [*] --> Draft: User Creates Project
    Draft --> Pending: Submit Project
    Pending --> UnderReview: Admin Opens Project
    
    UnderReview --> Verified: Admin Approves
    UnderReview --> Rejected: Admin Rejects
    
    Verified --> CreditGenerated: Auto-Generate Credits
    CreditGenerated --> Active: Credits in Wallet
    
    Rejected --> [*]: Project Closed
    Active --> [*]: Project Lifecycle Complete
    
    note right of CreditGenerated
        Credits = CO2Reduction tonnes
        Automatically added to wallet
    end note
```

#### Detailed Project Verification Sequence

```mermaid
sequenceDiagram
    participant Owner as Project Owner
    participant UI as React UI
    participant API as Backend API
    participant ProjSvc as Project Service
    participant WalletSvc as Wallet Service
    participant DB as MongoDB
    
    Owner->>UI: Submit Project Details
    UI->>API: POST /api/projects
    API->>ProjSvc: createProject(data)
    ProjSvc->>DB: Save Project (status=PENDING)
    DB-->>ProjSvc: Project Created
    ProjSvc-->>API: Project ID
    API-->>UI: Success Response
    UI-->>Owner: "Project Submitted"
    
    Note over Owner,DB: ... Waiting for Admin Review ...
    
    participant Admin
    Admin->>UI: Review Project
    Admin->>UI: Click "Verify"
    UI->>API: PUT /api/projects/{id}/verify
    API->>ProjSvc: verifyProject(id)
    
    ProjSvc->>DB: Update Status to VERIFIED
    ProjSvc->>ProjSvc: Calculate Credits
    Note right of ProjSvc: credits = project.co2Reduction
    
    ProjSvc->>WalletSvc: addCredits(ownerId, credits)
    WalletSvc->>DB: Update Wallet
    WalletSvc->>DB: Add Transaction Record
    WalletSvc-->>ProjSvc: Credits Added
    
    ProjSvc-->>API: Verification Complete
    API-->>UI: Success
    UI-->>Admin: "Project Verified"
    
    Note over Owner: Owner receives notification
    Owner->>UI: Check Wallet
    UI->>API: GET /api/wallet
    API-->>UI: Wallet with new credits
    UI-->>Owner: Display Updated Balance
```

### 3. Order Matching & Trade Execution

```mermaid
flowchart TD
    Start([User Places Order]) --> CheckType{Order Type?}
    
    CheckType -->|BUY| ValidateCash[Validate Cash Balance]
    CheckType -->|SELL| ValidateCredits[Validate Credit Balance]
    
    ValidateCash --> HasCash{Sufficient Cash?}
    ValidateCredits --> HasCredits{Sufficient Credits?}
    
    HasCash -->|No| ErrorFunds[Error: Insufficient Funds]
    HasCredits -->|No| ErrorCredits[Error: Insufficient Credits]
    
    HasCash -->|Yes| CheckPrice[Validate Price Range]
    HasCredits -->|Yes| CheckPrice
    
    CheckPrice --> PriceValid{Within Range?}
    PriceValid -->|No| ErrorPrice[Error: Price Out of Range]
    
    PriceValid -->|Yes| LockFunds[Lock Cash/Credits]
    LockFunds --> CreateOrder[Create Order Record]
    CreateOrder --> FindMatch[Search for Matching Orders]
    
    FindMatch --> MatchFound{Match Found?}
    
    MatchFound -->|No| OrderPending[Order Status: PENDING]
    OrderPending --> WaitMatch[Wait in Order Book]
    
    MatchFound -->|Yes| ExecuteTrade[Execute Trade]
    ExecuteTrade --> TransferCredits[Transfer Credits to Buyer]
    TransferCredits --> TransferCash[Transfer Cash to Seller]
    TransferCash --> RecordTrade[Record Trade in Database]
    RecordTrade --> UpdateOrders[Update Order Status: EXECUTED]
    UpdateOrders --> NotifyUsers[Notify Both Parties]
    NotifyUsers --> Success([Trade Complete])
    
    WaitMatch -.->|New Matching Order| MatchFound
    WaitMatch -.->|User Cancels| CancelOrder[Cancel Order]
    CancelOrder --> UnlockFunds[Unlock Cash/Credits]
    UnlockFunds --> Cancelled([Order Cancelled])
    
    ErrorFunds --> End([Error Response])
    ErrorCredits --> End
    ErrorPrice --> End
    
    style ExecuteTrade fill:#90EE90
    style Success fill:#87CEEB
    style ErrorFunds fill:#FFB6C1
    style ErrorCredits fill:#FFB6C1
    style ErrorPrice fill:#FFB6C1
```

#### Order State Transitions

```mermaid
stateDiagram-v2
    [*] --> PENDING: Order Placed
    
    PENDING --> PARTIAL: Partially Matched
    PENDING --> EXECUTED: Fully Matched
    PENDING --> CANCELLED: User Cancels
    
    PARTIAL --> EXECUTED: Remaining Quantity Matched
    PARTIAL --> CANCELLED: User Cancels Remaining
    
    EXECUTED --> [*]
    CANCELLED --> [*]
    
    note right of PENDING
        Funds/Credits Locked
        Waiting in Order Book
    end note
    
    note right of PARTIAL
        Some quantity traded
        Remaining in order book
    end note
    
    note right of EXECUTED
        Fully traded
        Funds/Credits transferred
    end note
```

### 4. Credit Lifecycle Diagram

```mermaid
graph TB
    Start([CO2 Reduction Activity]) --> Project[Environmental Project]
    Project -->|Submit| Pending[Project Status: PENDING]
    Pending -->|Admin Review| Verify{Verification}
    
    Verify -->|❌ Rejected| End1([No Credits Generated])
    Verify -->|✅ Approved| Generate[Generate Credits]
    
    Generate --> Wallet[Credits Added to Wallet]
    Wallet --> Available[Available for Trading]
    
    Available --> Choice{Owner Decision}
    
    Choice -->|Sell| Marketplace[List on Marketplace]
    Choice -->|Hold| Portfolio[Hold in Portfolio]
    Choice -->|Retire| Retire[Retire Credits]
    
    Marketplace --> OrderBook[Order Book]
    OrderBook -->|Match Found| Trade[Execute Trade]
    Trade --> BuyerWallet[Transfer to Buyer Wallet]
    
    BuyerWallet --> BuyerChoice{Buyer Decision}
    BuyerChoice -->|Resell| Marketplace
    BuyerChoice -->|Retire| Retire
    BuyerChoice -->|Hold| Portfolio2[Buyer Portfolio]
    
    Portfolio -->|Later Decision| Choice
    Portfolio2 -->|Later Decision| BuyerChoice
    
    Retire --> Permanent[Permanently Removed]
    Permanent --> Claim[Claim Carbon Neutrality]
    Claim --> End2([Credit Lifecycle Complete])
    
    style Generate fill:#90EE90
    style Trade fill:#87CEEB
    style Retire fill:#FFD700
    style Claim fill:#FF6347
```

### 5. Wallet Transaction Flow

```mermaid
sequenceDiagram
    participant User
    participant UI
    participant WalletCtrl as Wallet Controller
    participant WalletSvc as Wallet Service
    participant DB as MongoDB
    
    User->>UI: Initiate Transaction
    
    alt Add Funds
        UI->>WalletCtrl: POST /api/wallet/add
        WalletCtrl->>WalletSvc: addFunds(userId, amount)
        WalletSvc->>DB: Update balance
        WalletSvc->>DB: Record transaction
        DB-->>WalletSvc: Success
        WalletSvc-->>WalletCtrl: Updated Wallet
        WalletCtrl-->>UI: New Balance
    
    else Buy Credits (Order Execution)
        Note over UI,DB: Happens during trade execution
        WalletSvc->>WalletSvc: Validate buyer balance
        WalletSvc->>DB: Deduct cash from buyer
        WalletSvc->>DB: Add cash to seller
        WalletSvc->>DB: Add credits to buyer
        WalletSvc->>DB: Deduct credits from seller
        WalletSvc->>DB: Record both transactions
    
    else Retire Credits
        UI->>WalletCtrl: POST /api/retirement/retire
        WalletCtrl->>WalletSvc: retireCredits(userId, amount)
        WalletSvc->>WalletSvc: Validate credit balance
        WalletSvc->>DB: Deduct credits (permanent)
        WalletSvc->>DB: Record retirement transaction
        DB-->>WalletSvc: Success
        WalletSvc-->>WalletCtrl: Updated Wallet
        WalletCtrl-->>UI: Credits Retired
    end
    
    UI-->>User: Display Updated Wallet
```

### 6. Complete User Journey Map

```mermaid
journey
    title Carbon Credit Platform - User Journey
    section Registration
      Visit Platform: 5: User
      Sign Up: 4: User
      Verify Email: 4: User
      Login: 5: User
    
    section Project Owner Path
      Submit Green Project: 5: Owner
      Wait for Verification: 3: Owner
      Receive Credits: 5: Owner
      List Credits for Sale: 5: Owner
      Receive Payment: 5: Owner
    
    section Company Path
      Add Funds to Wallet: 4: Company
      Browse Marketplace: 4: Company
      Place Buy Order: 4: Company
      Receive Credits: 5: Company
      Retire Credits: 5: Company
      Claim Carbon Neutrality: 5: Company
    
    section Admin Path
      Review Pending Projects: 4: Admin
      Verify Projects: 5: Admin
      Monitor Trades: 4: Admin
      Platform Analytics: 5: Admin
```

---

## 📡 IoT Integration

### Overview

The platform integrates with IoT sensors to monitor carbon emissions in real-time. Users can connect ESP32/ESP8266 devices that publish CO₂ data to an MQTT broker.

```mermaid
sequenceDiagram
    participant ESP as ESP32 Sensor
    participant MQTT as MQTT Broker
    participant SUB as MQTT Subscriber
    participant USAGE as Usage Service
    participant DB as MongoDB
    participant UI as Dashboard

    ESP->>MQTT: Publish CO2 Reading
    Note over ESP,MQTT: Topic: user/{userId}/carbon
    
    MQTT->>SUB: Message Notification
    SUB->>USAGE: Process Message(payload, userId)
    USAGE->>USAGE: Parse CO2 Value
    USAGE->>DB: Save UsageEntry
    
    UI->>USAGE: GET /api/usage
    USAGE->>DB: Fetch Usage History
    DB->>USAGE: Return Data
    USAGE->>UI: Display Charts
```

### Configuration Steps

1. **Navigate to Dashboard** → **Usage Tab**
2. **Configure MQTT Settings**:
   - **Broker URL**: `ssl://your-broker:8883` (e.g., HiveMQ Cloud)
   - **Topic**: `user/{userId}/carbon` or custom topic
   - **Username**: MQTT broker username
   - **Password**: MQTT broker password
3. **Save Configuration** - Backend automatically subscribes to the topic
4. **ESP32 Setup**: Program your ESP device to publish CO₂ readings

### Sample ESP32 Code

```cpp
#include <WiFi.h>
#include <PubSubClient.h>

const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";
const char* mqtt_server = "your-broker.hivemq.cloud";
const int mqtt_port = 8883;
const char* mqtt_user = "your_username";
const char* mqtt_pass = "your_password";
const char* topic = "user/YOUR_USER_ID/carbon";

WiFiClientSecure espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) delay(500);
  
  client.setServer(mqtt_server, mqtt_port);
  espClient.setInsecure(); // For testing only
}

void loop() {
  if (!client.connected()) reconnect();
  client.loop();
  
  // Simulate CO2 reading (replace with actual sensor)
  float co2Value = readCO2Sensor();
  String payload = String(co2Value, 2);
  
  client.publish(topic, payload.c_str());
  delay(60000); // Send every minute
}
```

### Data Format

The ESP device should publish numeric CO₂ values in tonnes:

```
Payload Examples:
- "1.25" → 1.25 tonnes of CO₂
- "0.5" → 0.5 tonnes of CO₂
- "2.75" → 2.75 tonnes of CO₂
```

---

## 💾 Database Schema

### Entity Relationship Overview

```mermaid
erDiagram
    USER ||--o{ PROJECT : owns
    USER ||--|| WALLET : has
    USER ||--o{ ORDER : places
    USER ||--o{ USAGE_CONFIG : configures
    USER ||--o{ USAGE_ENTRY : generates
    
    PROJECT ||--o{ CREDIT_GENERATION : triggers
    
    ORDER ||--o{ TRADE : participates_in
    
    WALLET ||--o{ TRANSACTION : contains
    
    USAGE_CONFIG ||--o{ USAGE_ENTRY : produces
    
    USER {
        string id PK
        string name
        string email UK
        string password
        array roles
        datetime createdAt
    }
    
    PROJECT {
        string id PK
        string ownerId FK
        string name
        enum type
        string location
        double co2Reduction
        enum status
        string description
        datetime createdAt
        datetime verifiedAt
    }
    
    WALLET {
        string id PK
        string userId FK
        double balance
        double creditBalance
        datetime lastUpdated
    }
    
    ORDER {
        string id PK
        string userId FK
        enum type
        double price
        int quantity
        int remainingQuantity
        enum status
        datetime createdAt
        datetime executedAt
    }
    
    TRADE {
        string id PK
        string buyOrderId FK
        string sellOrderId FK
        string buyerId FK
        string sellerId FK
        double price
        int quantity
        datetime timestamp
    }
    
    TRANSACTION {
        string id PK
        string walletId FK
        enum type
        double cashAmount
        double creditAmount
        string referenceId
        datetime timestamp
    }
    
    USAGE_CONFIG {
        string id PK
        string userId FK
        string broker
        string topic
        string username
        string password
        datetime createdAt
    }
    
    USAGE_ENTRY {
        string id PK
        string userId FK
        double co2KgDelta
        string rawPayload
        datetime timestamp
    }
```

### Collection Details

#### Users Collection
```json
{
  "_id": "ObjectId",
  "name": "John Doe",
  "email": "john@example.com",
  "password": "$2a$10$hashed...",
  "roles": ["ROLE_USER"],
  "createdAt": "2024-11-28T10:00:00Z"
}
```

#### Projects Collection
```json
{
  "_id": "ObjectId",
  "ownerId": "user-id",
  "name": "Rajasthan Solar Park",
  "type": "SOLAR",
  "location": "Rajasthan, India",
  "co2Reduction": 1000.0,
  "status": "VERIFIED",
  "description": "1MW Solar Installation",
  "createdAt": "2024-11-28T10:00:00Z",
  "verifiedAt": "2024-11-28T12:00:00Z"
}
```

#### Wallets Collection
```json
{
  "_id": "ObjectId",
  "userId": "user-id",
  "balance": 10000.50,
  "creditBalance": 150.0,
  "transactions": [
    {
      "type": "BUY_CREDITS",
      "cashAmount": -1000.0,
      "creditAmount": 10.0,
      "referenceId": "trade-id",
      "timestamp": "2024-11-28T14:00:00Z"
    }
  ],
  "lastUpdated": "2024-11-28T14:00:00Z"
}
```

#### Orders Collection
```json
{
  "_id": "ObjectId",
  "userId": "user-id",
  "type": "BUY",
  "price": 100.0,
  "quantity": 10,
  "remainingQuantity": 3,
  "status": "PARTIAL",
  "createdAt": "2024-11-28T15:00:00Z",
  "executedAt": null
}
```

#### Trades Collection
```json
{
  "_id": "ObjectId",
  "buyOrderId": "order-id-1",
  "sellOrderId": "order-id-2",
  "buyerId": "buyer-user-id",
  "sellerId": "seller-user-id",
  "price": 100.0,
  "quantity": 7,
  "timestamp": "2024-11-28T15:30:00Z"
}
```

#### Usage Entries Collection (IoT Data)
```json
{
  "_id": "ObjectId",
  "userId": "user-id",
  "co2KgDelta": 1.25,
  "rawPayload": "1.25",
  "timestamp": "2024-11-28T16:00:00Z"
}
```

### Data Flow Between Collections

```mermaid
flowchart LR
    subgraph Input
        REG[User Registration]
        PROJ_SUB[Project Submission]
        ORDER_PLACE[Order Placement]
        IOT_DATA[IoT Sensor Data]
    end
    
    subgraph Collections
        USERS[(Users)]
        PROJECTS[(Projects)]
        WALLETS[(Wallets)]
        ORDERS[(Orders)]
        TRADES[(Trades)]
        USAGE[(Usage Entries)]
    end
    
    subgraph Processing
        VERIFY[Admin Verification]
        MATCH[Order Matching Engine]
        MQTT[MQTT Processor]
    end
    
    REG --> USERS
    USERS --> WALLETS
    
    PROJ_SUB --> PROJECTS
    PROJECTS --> VERIFY
    VERIFY -->|Generate Credits| WALLETS
    
    ORDER_PLACE --> ORDERS
    ORDERS --> MATCH
    MATCH -->|Execute| TRADES
    TRADES -->|Update Balances| WALLETS
    MATCH -->|Update Status| ORDERS
    
    IOT_DATA --> MQTT
    MQTT --> USAGE
    
    style VERIFY fill:#90EE90
    style MATCH fill:#87CEEB
    style MQTT fill:#FFD700
```

---

## 🔒 Security Architecture

### Authentication & Authorization Flow

```mermaid
sequenceDiagram
    participant Client
    participant Filter as JWT Filter
    participant Auth as Auth Manager
    participant Service
    participant DB
    
    Client->>Filter: HTTP Request + JWT
    
    alt No Token
        Filter->>Filter: Check if public endpoint
        alt Public Endpoint (/auth/*)
            Filter->>Service: Allow Request
        else Protected Endpoint
            Filter-->>Client: 401 Unauthorized
        end
    else Has Token
        Filter->>Filter: Extract JWT from Header
        Filter->>Filter: Validate Token Signature
        
        alt Invalid Token
            Filter-->>Client: 401 Unauthorized
        else Valid Token
            Filter->>Filter: Extract userId from JWT
            Filter->>Auth: Load User Details
            Auth->>DB: findById(userId)
            DB-->>Auth: User + Roles
            Auth->>Auth: Create Authentication Object
            Auth->>Filter: Set Security Context
            Filter->>Service: Forward Request
            
            Service->>Service: Check Role-Based Access
            alt Has Required Role
                Service->>DB: Execute Business Logic
                DB-->>Service: Result
                Service-->>Client: 200 OK + Data
            else Missing Role
                Service-->>Client: 403 Forbidden
            end
        end
    end
```

### Role-Based Access Control (RBAC)

```mermaid
graph TB
    subgraph Roles
        USER[ROLE_USER]
        ADMIN[ROLE_ADMIN]
    end
    
    subgraph User Permissions
        U1[View Own Projects]
        U2[Create Projects]
        U3[Place Orders]
        U4[Manage Own Wallet]
        U5[Retire Own Credits]
        U6[Configure IoT Settings]
        U7[View Marketplace]
    end
    
    subgraph Admin Permissions
        A1[Verify Projects]
        A2[View All Users]
        A3[View All Projects]
        A4[Platform Analytics]
        A5[Manage System Settings]
    end
    
    USER --> U1
    USER --> U2
    USER --> U3
    USER --> U4
    USER --> U5
    USER --> U6
    USER --> U7
    
    ADMIN --> A1
    ADMIN --> A2
    ADMIN --> A3
    ADMIN --> A4
    ADMIN --> A5
    ADMIN -.->|Inherits| USER
```

### Security Layers

```mermaid
graph TD
    Client[Client Application]
    
    Client --> HTTPS[HTTPS Layer]
    HTTPS --> CORS[CORS Filter]
    CORS --> JWT_Filter[JWT Authentication Filter]
    JWT_Filter --> Auth[Spring Security Auth]
    Auth --> RBAC[Role-Based Access Control]
    RBAC --> Service[Service Layer]
    Service --> Validation[Input Validation]
    Validation --> Sanitization[Data Sanitization]
    Sanitization --> Business[Business Logic]
    Business --> Encryption[Password Encryption]
    Encryption --> DB[(Database)]
    
    style HTTPS fill:#90EE90
    style JWT_Filter fill:#87CEEB
    style RBAC fill:#FFD700
    style Encryption fill:#FF6347
```

### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-id",
    "email": "user@example.com",
    "roles": ["ROLE_USER"],
    "iat": 1701177600,
    "exp": 1701264000
  },
  "signature": "HMACSHA256(base64(header) + '.' + base64(payload), secret)"
}
```

---

## 🛠️ Technology Stack

### Frontend
- **React 18** - Modern UI library with hooks
- **Vite** - Fast build tool and dev server
- **Tailwind CSS** - Utility-first styling
- **Chart.js** - Data visualization
- **Axios** - HTTP client for API calls
- **React Router** - Client-side routing

### Backend
- **Java 17** - LTS version with modern features
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication & authorization
- **JWT** - JSON Web Tokens for stateless auth
- **Eclipse Paho** - MQTT client library
- **Lombok** - Boilerplate reduction

### Database
- **MongoDB** - NoSQL document database
- **MongoDB Atlas** - Cloud database option

### IoT
- **MQTT Protocol** - Lightweight pub/sub messaging
- **HiveMQ** - MQTT broker (cloud or self-hosted)
- **ESP32/ESP8266** - Microcontroller platforms

### Deployment Architecture

```mermaid
graph TB
    subgraph "Client Tier - Vercel"
        CDN["CDN (Cloudflare)"]
        REACT["React App (Static)"]
    end
    
    subgraph "API Tier - Render/AWS"
        LB["Load Balancer"]
        API1["Spring Boot Instance 1"]
        API2["Spring Boot Instance 2"]
    end
    
    subgraph "Data Tier"
        MONGO_PRIMARY[("MongoDB Primary")]
        MONGO_SECONDARY[("MongoDB Secondary")]
    end
    
    subgraph "IoT Tier"
        MQTT_BROKER["HiveMQ Cloud"]
        ESP_DEVICES["ESP32 Devices"]
    end
    
    subgraph "External Services"
        AUTH_SERVICE["JWT Auth"]
    end
    
    Users -->|HTTPS| CDN
    CDN --> REACT
    REACT -->|API Calls| LB
    LB --> API1
    LB --> API2
    
    API1 --> MONGO_PRIMARY
    API2 --> MONGO_PRIMARY
    MONGO_PRIMARY -.->|Replication| MONGO_SECONDARY
    
    ESP_DEVICES -->|MQTT/SSL| MQTT_BROKER
    MQTT_BROKER -->|Subscribe| API1
    MQTT_BROKER -->|Subscribe| API2
    
    API1 --> AUTH_SERVICE
    API2 --> AUTH_SERVICE
    
    style CDN fill:#90EE90
    style LB fill:#87CEEB
    style MONGO_PRIMARY fill:#FFD700
    style MQTT_BROKER fill:#FF6347
```

---

## ⚙️ Installation & Setup

### Prerequisites

Ensure you have the following installed:

- **Node.js** v18+ ([Download](https://nodejs.org/))
- **Java JDK** 17+ ([Download](https://adoptium.net/))
- **Maven** 3.6+ ([Download](https://maven.apache.org/))
- **MongoDB** ([Atlas](https://www.mongodb.com/cloud/atlas) or [Local](https://www.mongodb.com/try/download/community))

### 1. Clone the Repository

```bash
git clone https://github.com/Harmit485/carbon-credit-platform.git
cd carbon-credit-platform
```

### 2. Backend Setup

#### Navigate to Server Directory
```bash
cd server
```

#### Configure Environment Variables

Create or update `src/main/resources/application.properties`:

```properties
# MongoDB Configuration
spring.data.mongodb.uri=${MONGODB_URI}

# JWT Configuration
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000

# Server Port
server.port=${PORT:8080}

# CORS Configuration
app.cors.allowed-origins=http://localhost:5173,https://your-frontend-domain.com
```

**Required Environment Variables:**

| Variable | Description | Example |
|----------|-------------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/carbonDB` |
| `JWT_SECRET` | Secret key for JWT signing | `your-super-secret-key-min-256-bits` |
| `PORT` | Server port (optional) | `8080` |

#### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

#### Verify Backend

```bash
curl http://localhost:8080/api/test/health
```

Expected response: `OK`

### 3. Frontend Setup

#### Navigate to Client Directory
```bash
cd ../client
```

#### Configure Environment

Create a `.env` file in the `client` directory:

```env
VITE_API_URL=http://localhost:8080/api
```

For production deployment:
```env
VITE_API_URL=https://your-backend-domain.com/api
```

#### Install Dependencies and Run

```bash
# Install packages
npm install

# Start development server
npm run dev
```

The frontend will start on `http://localhost:5173`

#### Build for Production

```bash
npm run build
```

### 4. Database Initialization

The application will automatically create collections on first run. For testing purposes, you may want to create an admin user:

1. Register a new user through the UI
2. Manually update the user in MongoDB:

```javascript
db.users.updateOne(
  { email: "admin@example.com" },
  { $set: { roles: ["ROLE_ADMIN"] } }
)
```

---

## 📖 User Manual

### Getting Started

#### 1. Registration

1. Navigate to the **Sign Up** page
2. Fill in your details:
   - **Name**: Your full name
   - **Email**: Valid email address
   - **Password**: Minimum 6 characters
   - **Role**: Select "User" (Admin accounts are created separately)
3. Click **Sign Up**
4. Redirected to **Login** page

#### 2. Login

1. Enter your registered **Email** and **Password**
2. Click **Sign In**
3. You will receive a JWT token (stored automatically)
4. Redirected to **Dashboard**

### For Project Owners (Credit Earners)

#### Step 1: Submit an Environmental Project

1. Navigate to **Projects** → **Create New Project**
2. Fill in project details:
   - **Project Name**: e.g., "Rajasthan Solar Park"
   - **Type**: Select from Solar, Wind, Reforestation, etc.
   - **Location**: Project location
   - **CO₂ Reduction**: Estimated tonnes of CO₂ saved (e.g., 1000)
   - **Description**: Brief project description
3. Click **Submit Project**
4. Project status will be **PENDING**

#### Step 2: Wait for Verification

- Admin will review your project
- Once verified, credits are **automatically generated** and added to your wallet
- Check your **Wallet** to see credit balance

#### Step 3: Sell Credits in Marketplace

1. Go to **Trading** or **Marketplace**
2. Select **Sell** tab
3. Enter:
   - **Quantity**: Number of credits to sell
   - **Price**: Price per credit (must be within ±10% of last traded price)
4. Click **Place Sell Order**
5. Your credits are locked until the order is executed or cancelled

### For Companies (Credit Buyers)

#### Step 1: Add Funds to Wallet

1. Navigate to **Wallet**
2. View your current **Cash Balance**
3. *(In production, integrate with payment gateway. For testing, you can simulate adding funds)*

#### Step 2: Buy Credits

1. Go to **Trading** or **Marketplace**
2. Select **Buy** tab
3. Enter:
   - **Quantity**: Number of credits to purchase
   - **Price**: Price per credit (within ±10% of market price)
4. Click **Place Buy Order**
5. If a matching sell order exists, trade executes instantly
6. Credits are added to your wallet; cash is debited

#### Step 3: Retire Credits (Optional)

1. Navigate to **Retirement** page
2. Enter number of credits to retire
3. Click **Retire Credits**
4. Credits are permanently removed from circulation
5. You can claim carbon neutrality for the retired amount

### For Admins

#### Verify Projects

1. Login with Admin credentials
2. Navigate to **Projects** or **Dashboard**
3. View list of **PENDING** projects
4. Review project details
5. Click **Verify** to approve
6. System automatically:
   - Changes project status to **VERIFIED**
   - Calculates credits based on CO₂ reduction
   - Issues credits to project owner's wallet

#### Monitor Platform

- View all users, projects, and transactions
- Access comprehensive analytics
- Manage platform settings

### IoT Sensor Configuration

#### Configure MQTT Connection

1. Navigate to **Dashboard** → **Usage** tab
2. Click **Configure MQTT Settings**
3. Enter your MQTT broker details:
   - **Broker URL**: e.g., `ssl://broker.hivemq.com:8883`
   - **Topic**: e.g., `user/your-user-id/carbon`
   - **Username**: Your MQTT username
   - **Password**: Your MQTT password
4. Click **Save Configuration**
5. Backend subscribes to the topic automatically

#### View Usage Data

1. Check **Dashboard** → **Usage History**
2. View cumulative CO₂ emissions over time
3. Data updates automatically as ESP sensor publishes readings

---

## 🔄 Complete User Flow Diagram

```mermaid
graph TD
    A[Register Account] -->|Login| B[Dashboard]
    B --> C{User Type?}
    
    C -->|Project Owner| D[Submit Green Project]
    C -->|Company| E[Add Funds to Wallet]
    C -->|Admin| F[Verify Pending Projects]
    
    D --> G[Project Status: PENDING]
    G --> F
    F -->|Approve| H[Generate Credits and Add to Wallet]
    
    H --> I[Sell Credits in Marketplace]
    E --> J[Buy Credits in Marketplace]
    
    I --> K{Matching Order?}
    J --> K
    
    K -->|Yes| L[Execute Trade]
    K -->|No| M[Order Status: PENDING]
    
    L --> N[Update Wallet Balances]
    M --> O[Wait for Match or Cancel]
    
    N --> P[Retire Credits - Optional]
    
    style H fill:#90EE90
    style L fill:#87CEEB
    style P fill:#FFB6C1
```

---

## 📡 API Documentation

### Authentication

#### Register User
```http
POST /api/auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": ["user"]
}
```

**Response:**
```json
{
  "message": "User registered successfully"
}
```

#### Login
```http
POST /api/auth/signin
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "id": "user-id",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### Projects

#### Get All Projects
```http
GET /api/projects
Authorization: Bearer {token}
```

#### Create Project
```http
POST /api/projects
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Solar Farm Project",
  "type": "SOLAR",
  "location": "Rajasthan, India",
  "co2Reduction": 1000,
  "description": "1MW Solar Installation"
}
```

#### Verify Project (Admin Only)
```http
PUT /api/projects/{projectId}/verify
Authorization: Bearer {token}
```

### Marketplace

#### Get All Orders
```http
GET /api/marketplace/orders
Authorization: Bearer {token}
```

#### Place Order
```http
POST /api/marketplace/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "type": "BUY",
  "quantity": 10,
  "price": 100
}
```

#### Cancel Order
```http
PUT /api/marketplace/orders/{orderId}/cancel
Authorization: Bearer {token}
```

### Wallet

#### Get Wallet Details
```http
GET /api/wallet
Authorization: Bearer {token}
```

**Response:**
```json
{
  "userId": "user-id",
  "balance": 10000.50,
  "creditBalance": 150.0,
  "transactions": [
    {
      "type": "BUY_CREDITS",
      "amount": -100,
      "credits": 10,
      "timestamp": "2024-11-28T10:30:00Z"
    }
  ]
}
```

#### Add Funds (Simulation)
```http
POST /api/wallet/add
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 5000
}
```

### Usage (IoT Data)

#### Get Usage History
```http
GET /api/usage
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "userId": "user-id",
    "co2Value": 1.25,
    "timestamp": "2024-11-28T10:00:00Z"
  },
  {
    "userId": "user-id",
    "co2Value": 0.85,
    "timestamp": "2024-11-28T11:00:00Z"
  }
]
```

#### Configure MQTT
```http
POST /api/usage/config
Authorization: Bearer {token}
Content-Type: application/json

{
  "broker": "ssl://broker.hivemq.com:8883",
  "topic": "user/my-user-id/carbon",
  "username": "mqtt-user",
  "password": "mqtt-password"
}
```

### Dashboard

#### Get Dashboard Data
```http
GET /api/dashboard
Authorization: Bearer {token}
```

**Response:**
```json
{
  "portfolioValue": 15000.0,
  "creditBalance": 150.0,
  "pendingOrders": 2,
  "completedTrades": 5,
  "usageHistory": [...],
  "priceHistory": [...]
}
```

---

## 🐛 Troubleshooting

### Common Issues

| Error | Cause | Solution |
|-------|-------|----------|
| **401 Unauthorized** | Token expired or invalid | Logout and login again to refresh token |
| **Insufficient Funds** | Wallet cash balance too low | Add funds to wallet before placing buy order |
| **Price out of range** | Order price outside ±10% of last traded price | Adjust price to be within acceptable range |
| **Project not verifying** | Not logged in as admin | Login with admin credentials |
| **MQTT connection failed** | Incorrect broker details | Verify broker URL, username, and password |
| **No usage data** | ESP not publishing or wrong topic | Check ESP code and MQTT configuration |

### Backend Not Starting

**Check MongoDB Connection:**
```bash
# Verify MONGODB_URI is set correctly
echo $MONGODB_URI
```

**Check Logs:**
```bash
# Look for errors in Maven output
mvn spring-boot:run
```

### Frontend Not Loading

**Verify API URL:**
```bash
# Check .env file
cat client/.env
# Should show: VITE_API_URL=http://localhost:8080/api
```

**Clear Cache:**
```bash
cd client
rm -rf node_modules
npm install
```

### MQTT Issues

**Test MQTT Connection:**
Use an MQTT client like [MQTT Explorer](http://mqtt-explorer.com/) to verify:
1. Broker is accessible
2. Topic is correct
3. Credentials are valid

**Check Backend Logs:**
```bash
# Look for MQTT connection messages
# Should see: "Connected to MQTT broker for user {userId}"
```

---

## 🔒 Security Best Practices

1. **Never commit `.env` files** - They contain sensitive credentials
2. **Use strong JWT secrets** - Minimum 256 bits, cryptographically random
3. **Enable HTTPS in production** - Encrypt all network traffic
4. **Secure MQTT with SSL/TLS** - Use `ssl://` or `wss://` protocols
5. **Implement rate limiting** - Prevent API abuse
6. **Regular security audits** - Keep dependencies updated

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2024 EcoSense Platform

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📞 Support

For questions or issues:

- **Email**: support@ecosense-platform.com
- **GitHub Issues**: [Create an issue](https://github.com/Harmit485/carbon-credit-platform/issues)
- **Documentation**: [Wiki](https://github.com/Harmit485/carbon-credit-platform/wiki)

---

<p align="center">
  Made with 💚 for a sustainable future
</p>
