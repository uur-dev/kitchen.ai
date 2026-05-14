# kitchen.ai

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture Summary](#architecture-summary)
- [Repository Structure](#repository-structure)
- [Microservices](#microservices)
  - [API Gateway](#1-api-gateway)
  - [Auth Service](#2-auth-service)
  - [Recipe Service](#3-recipe-service)
  - [AI Service](#4-ai-service)
  - [Notification Service](#5-notification-service)
- [Event Flow & Saga Pattern](#event-flow--saga-pattern)
- [Data Stores](#data-stores)
- [Mobile Apps](#mobile-apps)
- [Getting Started](#getting-started)
- [Scaling Services](#scaling-services)
- [Environment Variables](#environment-variables)
- [Tech Stack](#tech-stack)
- [Contributing](#contributing)

---

## Overview

**kitchen.ai** is a cloud-native, event-driven recipe generation platform built on a Spring Boot microservice backend and native iOS/Android clients. Users describe the ingredients available in their kitchen via text, voice, or a photo and select a target cuisine (Pakistani, Chinese, Italian, etc.). The platform routes the request through an AI pipeline backed by **Google Gemini**, generates a detailed recipe with timing and step-by-step instructions and delivers results both in-app and via push/email notification.

The entire platform spins up with a **single Docker Compose command** and is designed to scale each service independently using Docker replica counts, no Kubernetes required for development or staging environments.

---

## Key Features

| Feature | Detail |
|---|---|
|  **Multimodal Input** | Text prompts, audio transcription and image ingredient detection via Gemini AI |
|  **Cuisine Selection** | Filterable cuisine list: Pakistani, Chinese, Italian, Mexican, Japanese and more |
|  **Gemini Multimodal AI** | Single-call image+text recipe generation — no separate Vision API needed |
|  **Async Event Pipeline** | Full CQRS + Saga pattern over RabbitMQ for non-blocking recipe processing |
|  **Real-Time Notifications** | Push (FCM/APNs) and email notifications when recipe generation completes |
|  **Native Mobile Clients** | React Native app for Android & iOS in same monorepo |
|  **Secure Auth** | JWT + OAuth 2.0 (Google Sign-In, Apple Sign-In) |
|  **One-Command Deployment** | `docker compose up` brings the entire stack online — no manual config |
|  **Horizontal Scaling** | Each service scales independently via `docker compose up --scale` |

---

## Architecture Summary

```
Mobile App [React Native] (iOS / Android)
        │
        ▼
  [ API Gateway ]  ◄──── JWT / OAuth Validation via Auth Service
        │
        ├──────────────────────────────────────────────┐
        ▼                                              ▼
 [ Auth Service ]                           [ Recipe Service ]
  PostgreSQL DB                              PostgreSQL DB
                                                    │
                                          Publishes: recipe.request.created
                                                    │
                                                    ▼
                                           [ RabbitMQ Bus ]
                                                    │
                                          Consumes: recipe.request.created
                                                    │
                                                    ▼
                                           [ AI Service ]
                                            MongoDB DB
                                                    │
                                     Calls Gemini API (text + image — single multimodal call)
                                                    │
                                       Process: recipe request
                                                    │
                              ┌─────────────────────┤
                              ▼                     ▼
                    [ Recipe Service ]    [ Notification Service ]
                    Updates status:        MongoDB DB
                     COMPLETED             Sends: Push
```

> For the full visual architecture diagram see [`/docs/architecture.png`](./docs/architecture.png) or view the interactive diagram in the project wiki.

---

## Repository Structure

```
kitchen.ai/
│
├── backend/
│   ├── api-gateway/               # Spring Cloud Gateway — routing, auth filter, rate limiting
│   ├── auth-service/              # JWT issuance, OAuth2 (Google/Apple), user management
│   ├── recipe-service/            # Recipe CRUD, status tracking, cuisine catalogue
│   ├── ai-service/                # Gemini integration, prompt engineering, async processor
│   └── notification-service/      # Push (FCM/APNs) + email dispatch, delivery tracking
│
├── mobile_app/
│   ├── iOS/                       # Swift / SwiftUI native app
│   └── Android/                   # Kotlin / Jetpack Compose native app
│
├── infrastructure/
│   ├── docker-compose.yml         # 🚀 Single-command full stack orchestration
│   ├── docker-compose.override.yml
│   └── rabbitmq/
│       └── definitions.json       # Pre-configured exchanges, queues, bindings
│
├── docs/
│   ├── architecture.png
│   ├── api-spec.yaml              # OpenAPI 3.0 specification
│   └── event-contracts/           # RabbitMQ event schema definitions
│
└── README.md
```

---

## Microservices

### 1. API Gateway

**Path:** `backend/api-gateway/`  
**Technology:** Spring Cloud Gateway, Spring Security

The single entry point for all client traffic. Responsibilities:

- **Request Routing** — Routes to downstream services based on path prefix (`/auth/**`, `/recipes/**`, `/ai/**`)
- **JWT Validation** — Validates Bearer tokens on every request before forwarding
- **OAuth Token Introspection** — Delegates to Auth Service for token verification
- **Rate Limiting** — Per-user and per-IP throttling using Redis token bucket
- **CORS Policy** — Centralised CORS management for mobile and web clients
- **Request Tracing** — Injects `X-Correlation-ID` headers for distributed tracing

**Port:** `8080`

---

### 2. Auth Service

**Path:** `backend/auth-service/`  
**Technology:** Spring Boot, Spring Security, OAuth2, PostgreSQL, Redis

Manages the complete identity lifecycle:

- **User Registration & Login** — Email/password with BCrypt hashing
- **JWT Issuance** — Short-lived access tokens (15 min) + long-lived refresh tokens (7 days)
- **Token Refresh** — Secure rotation of refresh tokens stored in Redis
- **Session Invalidation** — Logout and token blacklisting via Redis TTL

**Port:** `0 - random`  
**Database:** PostgreSQL (users, sessions, oauth_accounts)  
**Cache:** Redis (token blacklist, refresh token store)

---

### 3. Recipe Service

**Path:** `backend/recipe-service/`  
**Technology:** Spring Boot, Spring Data JPA, PostgreSQL, Redis

The core domain service:

- **Recipe Request Submission** — Accepts multimodal input payloads (text, audio transcript, image URL)
- **Cuisine Catalogue API** — Returns the list of supported cuisines for the mobile dropdowns
- **Status Lifecycle** — Manages recipe state machine: `PROCESSING → COMPLETED / FAILED`
- **Redis Cache** — Caches completed recipes and cuisine list to reduce DB read load
- **Event Publisher** — Publishes `recipe.request.created` event to RabbitMQ after persisting the request
- **Interal Private API** — API to update recipe status `COMPLETED` called from AI Service, when processed.
- **Recipe History** — User-scoped recipe history with pagination

**Port:** `0 (random)`  
**Database:** PostgreSQL (recipes, cuisines, recipe_steps)  
**Cache:** Redis (recipe cache, cuisine list cache)

---

### 4. AI Service

**Path:** `backend/ai-service/`  
**Technology:** Spring Boot, MongoDB, Google Gemini API (Gemini 2.5 Flash-Lite)

The intelligence layer — fully event-driven, no synchronous HTTP exposure:

- **Event Consumer** — Subscribes to `recipe.request.created` queue from RabbitMQ
- **Prompt Engineering** — Constructs structured Gemini prompts embedding cuisine context, available ingredients, dietary notes, and a strict JSON output schema
- **Gemini Integration** — Calls Gemini 2.5 Flash-Lite, handles streaming response, retry with exponential backoff, and token budget management
- **Response Parsing** — Extracts structured recipe: title, description, total duration, difficulty, ingredient list, ordered steps
- **Save Processed Recipe** — Call interal (not public) API of recipe-service to save compelted recipe with the structured recipe payload
- **Audit Logging** — Persists all AI requests/responses in MongoDB for debugging and fine-tuning data collection
- **Image Understanding** — When a photo is submitted, the image is passed directly to Gemini as a multimodal payload (no separate Vision API call). Gemini identifies visible ingredients and reasons about quantities in a **single API call**, then returns a structured recipe. This eliminates the latency and cost of a two-step Vision → LLM pipeline.

**Port:** `0 (random)` (internal only — not exposed via API Gateway)  
**Database:** MongoDB (ai_requests, ai_responses, prompt_templates)

---

### 5. Notification Service

**Path:** `backend/notification-service/`  
**Technology:** Spring Boot, MongoDB, Firebase Push Notification

Handles all outbound user communications:

- **Event Consumer** — Subscribes to `notification.recipe.processed` events from RabbitMQ
- **Push Notifications** — Sends FCM notifications (Android or iOS) with recipe title and basic details in JSON payload
- **User Preferences** — Respects per-user notification preferences (push enabled)
- **Delivery Tracking** — Persists notification delivery status in MongoDB
- **Retry Logic** — Dead-letter queue handling for failed notification deliveries

**Port:** `0 (random)` (internal only — not exposed via API Gateway)  
**Database:** MongoDB (notifications, delivery_logs, user_preferences)

---

## Event Flow & Saga Pattern

kitchen.ai implements the **Saga Choreography pattern** — no central orchestrator. Each service reacts to events and emits new events, forming a fully decoupled async pipeline.

### Happy Path

```
1.  User submits recipe request via mobile app
2.  API Gateway validates JWT, routes to Recipe Service
3.  Recipe Service persists record (status: PROCESSING)
4.  Recipe Service publishes → [recipe.request.created] → RabbitMQ Exchange
5.  AI Service consumes [recipe.request.created]
6.  AI Service process record internally (status: PROCESSING)
7.  AI Service calls Gemini API (sync HTTP to external)
8.  AI Service call interal API to update processed recipe → [recipe-service] → updates status: COMPLETED
10. Notification Service consumes [notification.recipe.processed] → sends push
11. Mobile app receives push notification with json payload for completed recipe
```

### Failure Handling

```
IF Gemini API fails after max retries:
  AI Service store error in mongo-db for tracking
  Recipe recived API call to update reason → updates status: FAILED
  Notification Service consumes [notification.recipe.processed] → sends failure notification
  Dead-letter queue captures the original message for replay
```

### RabbitMQ Topology


| Exchange | Type | Routing Key | Queue / Consumer |
|:---|:---|:---|:---|
| `recipe.exchange` | Topic | `recipe.request.created` | `recipe.ai.queue` (AI Service) |
| `notification.exchange` | Topic | `notification.recipe.processed` | `notification.queue` (Notification Service) |
| `recipe.dlx.exchange` | Direct | `recipe.failed` | `recipe.dlx.queue` (Ops / Manual Replay) |
| `notification.dlx.exchange` | Direct | `notification.process.failed` | `notification.dlx.queue` (Ops / Manual Replay) |

---

### Logic Summary

*   **Primary Flow:** The AI Service listens specifically for `recipe.request.created`.
*   **Decoupling:** moved notifications to their own exchange (`notification.exchange`), which is a cleaner separation of concerns than the previous "all-in-one" topic exchange.
*   **Granular Error Handling:** have two distinct Dead Letter Queues (DLQs). This allows you to differentiate between a failure in **generating** a recipe versus a failure in **notifying** the user.

### Routing Key Mappings

| Constant | Value |
|:---|:---|
| `RECIPE_ROUTING_KEY` | `recipe.request.created` |
| `RECIPE_DLX_ROUTING_KEY` | `recipe.failed` |
| `NOTIFICATION_ROUTING_KEY` | `notification.recipe.processed` |
| `NOTIFICATION_DLX_ROUTING_KEY` | `notification.process.failed` |

---

## Data Stores

| Service | Database | Why |
|---|---|---|
| Auth Service | **PostgreSQL** | Relational integrity for users, sessions, OAuth accounts |
| Recipe Service | **PostgreSQL** | Structured recipe data, strong consistency for status updates |
| AI Service | **MongoDB** | Flexible schema for varied AI request/response payloads |
| Notification Service | **MongoDB** | Document model suits delivery logs and preference objects |
| API Gateway + Auth | **Redis** | Low-latency token cache, rate limit counters, session blacklist |

---

## Mobile Apps

The mobile presence is unified under a single cross-platform codebase, ensuring feature parity and faster development cycles:

### Cross-Platform — `mobile_app/`

- **Framework:** React Native (Latest Architecture)
- **Language:** TypeScript
- **UI Framework:** React Native / Tailwind CSS (NativeWind)
- **Architecture:** Redux Toolkit
- **Networking:** Axios with Interceptors for JWT management
- **Push Notifications:** React Native Firebase (FCM for Android or iOS)
- **Media Input:** 
    - **Camera/Images:** React Native Vision Camera & Image Picker
    - **Audio:** React Native Audio Recorder Player
    - **Processing:** Custom Native Modules for high-performance media tasks
---

### Key Technical Benefits
*   **Code Sharing:** Over 90% shared logic between iOS and Android.
*   **Hot Reloading:** Faster UI iteration and debugging.
*   **Native Modules:** Direct access to iOS (Swift) and Android (Kotlin) APIs for specialized tasks like heavy audio/image processing.

## Getting Started

### Prerequisites

- Docker Desktop 4.x+ (with Compose v2)
- Git

### Clone & Run

```bash
# 1. Clone the repository
git clone https://github.com/uur-dev/kitchen.ai/kitchen-ai.git
cd kitchen.ai

# 2. Copy environment template
cp infrastructure/.env.example infrastructure/.env

# 3. Add your Gemini API key or others configuration to .env
#    GEMINI_API_KEY=your_key_here

# 4. Start the entire platform
docker compose -f infrastructure/docker-compose.yml up --build
```

That's it. All services, databases, RabbitMQ, and Redis will be running.


## Scaling Services

Each service can be independently scaled by adjusting Docker Compose replica count — no infrastructure changes required:

```bash
# Scale AI Service to 3 instances (parallel recipe processing)
docker compose -f infrastructure/docker-compose.yml up --scale ai-service=3

# Scale Recipe Service to 2 instances
docker compose -f infrastructure/docker-compose.yml up --scale recipe-service=2

# Scale Notification Service to 2 instances
docker compose -f infrastructure/docker-compose.yml up --scale notification-service=2
```

RabbitMQ automatically distributes messages across all running instances of a consumer service (competing consumers pattern). Redis and PostgreSQL are shared stores — connection pooling is pre-configured via HikariCP.

---

## Environment Variables

Copy `infrastructure/.env.example` to `infrastructure/.env` and populate:

```env
# Postgres SQL
POSTGRES_USER=
POSTGRES_PASSWORD=
DB_HOST="localhost"
DB_PORT=5432
AUTH_DB_NAME=
RECIPE_DB_NAME=
POSTGRES_DB="postgres-db"

# PG Admin
PGADMIN_EMAIL=
PGADMIN_PASSWORD=

# Redis
REDIS_HOST="localhost"
REDIS_PORT=6379
REDIS_PASSWORD=

# Mongo
MONGO_USER=
MONGO_PASSWORD=
# Mongo Express
MONGO_EXPRESS_USER=
MONGO_EXPRESS_PASSWORD=

# RabbitMQ
RABBITMQ_HOST="localhost"
RABBITMQ_PORT=5672
RABBITMQ_USER=
RABBITMQ_PASSWORD=

# Gemini AI Key
GEMINI_API_KEY=

# JWT
JWT_SECRET="
MAX_SIGNATURE_AGE_SECONDS=3600

# Client IDs
ANDROID_APP_ID=
ANDROID_APP_SECRET=

IOS_APP_ID=
IOS_APP_SECRET=

WEB_APP_ID=
WEB_APP_SECRET=

DESKTOP_APP_ID=
DESKTOP_APP_SECRET=

MACOS_APP_ID=
MACOS_APP_SECRET=

# GOOGLE/Firebase
FIREBASE_SERVICE_ACCOUNT_JSON=
FIREBASE_STORAGE_BUCKET=
FIREBASE_STORAGE_FOLDER=
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| **API Gateway** | Spring Cloud Gateway 4.x |
| **Microservices** | Spring Boot 3.x, Java 21 |
| **Event Bus** | RabbitMQ 3.12 (AMQP) |
| **AI Provider** | Google Gemini 1.5 Pro / 2.0 Flash (multimodal — text + image in single call) |
| **Relational DB** | PostgreSQL 16 |
| **Document DB** | MongoDB 7 |
| **Cache / Session** | Redis 7 |
| **iOS / Android** | React Native |
| **Containerisation** | Docker, Docker Compose v2 |
| **API Spec** | OpenAPI 3.0 (Springdoc) |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

<p align="center">Built with ❤️ by Ubaid ur Rahman [UuR](http://uur-dev.com/)
