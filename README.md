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
| 🎙️ **Multimodal Input** | Text prompts, audio transcription and image ingredient detection via Gemini AI |
| 🌍 **Cuisine Selection** | Filterable cuisine list: Pakistani, Chinese, Italian, Mexican, Japanese and more |
| 🤖 **Gemini Multimodal AI** | Single-call image+text recipe generation — no separate Vision API needed |
| ⚡ **Async Event Pipeline** | Full CQRS + Saga pattern over RabbitMQ for non-blocking recipe processing |
| 🔔 **Real-Time Notifications** | Push (FCM/APNs) and email notifications when recipe generation completes |
| 📱 **Native Mobile Clients** | Swift (iOS) and Kotlin (Android) apps in the same monorepo |
| 🔐 **Secure Auth** | JWT + OAuth 2.0 (Google Sign-In, Apple Sign-In) |
| 🐳 **One-Command Deployment** | `docker compose up` brings the entire stack online — no manual config |
| 📈 **Horizontal Scaling** | Each service scales independently via `docker compose up --scale` |

---

## Architecture Summary

```
Mobile App (iOS / Android)
        │
        ▼
  [ API Gateway ]  ◄──── JWT / OAuth Validation via Auth Service
        │
        ├──────────────────────────────────────────────┐
        ▼                                              ▼
 [ Auth Service ]                           [ Recipe Service ]
  PostgreSQL DB                              PostgreSQL DB
                                                    │
                                          Publishes: recipe.created
                                                    │
                                                    ▼
                                           [ RabbitMQ Bus ]
                                                    │
                                          Consumes: recipe.created
                                                    │
                                                    ▼
                                           [ AI Service ]
                                            MongoDB DB
                                                    │
                                     Calls Gemini API (text + image — single multimodal call)
                                                    │
                                       Publishes: recipe.completed
                                                    │
                              ┌─────────────────────┤
                              ▼                     ▼
                    [ Recipe Service ]    [ Notification Service ]
                    Updates status:        MongoDB DB
                     COMPLETED             Sends: Push + Email
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
- **Google Sign-In** — OAuth2 authorization code flow via Google Identity Services
- **Apple Sign-In** — Sign In with Apple (SIWA) using Apple's identity token
- **Token Refresh** — Secure rotation of refresh tokens stored in Redis
- **Session Invalidation** — Logout and token blacklisting via Redis TTL

**Port:** `8081`  
**Database:** PostgreSQL (users, sessions, oauth_accounts)  
**Cache:** Redis (token blacklist, refresh token store)

---

### 3. Recipe Service

**Path:** `backend/recipe-service/`  
**Technology:** Spring Boot, Spring Data JPA, PostgreSQL, Redis

The core domain service:

- **Recipe Submission** — Accepts multimodal input payloads (text, audio transcript, image URL)
- **Cuisine Catalogue API** — Returns the list of supported cuisines for the mobile dropdowns
- **Status Lifecycle** — Manages recipe state machine: `PENDING → PROCESSING → COMPLETED / FAILED`
- **Redis Cache** — Caches completed recipes and cuisine list to reduce DB read load
- **Event Publisher** — Publishes `recipe.created` event to RabbitMQ after persisting the request
- **Event Consumer** — Listens for `recipe.completed` event from AI Service; updates recipe record and marks status as `COMPLETED`
- **Recipe History** — User-scoped recipe history with pagination

**Port:** `8082`  
**Database:** PostgreSQL (recipes, cuisines, recipe_steps)  
**Cache:** Redis (recipe cache, cuisine list cache)

---

### 4. AI Service

**Path:** `backend/ai-service/`  
**Technology:** Spring Boot, MongoDB, Google Gemini API (Gemini 1.5 Pro / 2.0 Flash)

The intelligence layer — fully event-driven, no synchronous HTTP exposure:

- **Event Consumer** — Subscribes to `recipe.created` queue from RabbitMQ
- **Prompt Engineering** — Constructs structured Gemini prompts embedding cuisine context, available ingredients, dietary notes, and a strict JSON output schema
- **Gemini Integration** — Calls Gemini 1.5 Pro / 2.0 Flash API; handles streaming response, retry with exponential backoff, and token budget management
- **Response Parsing** — Extracts structured recipe: title, description, total duration, difficulty, ingredient list, ordered steps
- **Event Publisher** — Publishes `recipe.completed` event with the structured recipe payload
- **Audit Logging** — Persists all AI requests/responses in MongoDB for debugging and fine-tuning data collection
- **Image Understanding** — When a photo is submitted, the image is passed directly to Gemini as a multimodal payload (no separate Vision API call). Gemini identifies visible ingredients and reasons about quantities in a **single API call**, then returns a structured recipe. This eliminates the latency and cost of a two-step Vision → LLM pipeline.

**Port:** `8083` (internal only — not exposed via API Gateway)  
**Database:** MongoDB (ai_requests, ai_responses, prompt_templates)

---

### 5. Notification Service

**Path:** `backend/notification-service/`  
**Technology:** Spring Boot, MongoDB, Firebase Push Notification, JavaMail

Handles all outbound user communications:

- **Event Consumer** — Subscribes to `recipe.completed` and `recipe.failed` events from RabbitMQ
- **Push Notifications** — Sends FCM notifications (Android) and APNs notifications (iOS) with recipe title and deep-link payload
- **Email Notifications** — Sends formatted HTML recipe emails via SMTP (configurable: SendGrid, SES, or self-hosted)
- **User Preferences** — Respects per-user notification preferences (push enabled, email enabled, quiet hours)
- **Delivery Tracking** — Persists notification delivery status in MongoDB
- **Retry Logic** — Dead-letter queue handling for failed notification deliveries

**Port:** `8084` (internal only — not exposed via API Gateway)  
**Database:** MongoDB (notifications, delivery_logs, user_preferences)

---

## Event Flow & Saga Pattern

kitchen.ai implements the **Saga Choreography pattern** — no central orchestrator. Each service reacts to events and emits new events, forming a fully decoupled async pipeline.

### Happy Path

```
1.  User submits recipe request via mobile app
2.  API Gateway validates JWT, routes to Recipe Service
3.  Recipe Service persists record (status: PENDING)
4.  Recipe Service publishes → [recipe.created] → RabbitMQ Exchange
5.  AI Service consumes [recipe.created]
6.  AI Service updates record internally (status: PROCESSING)
7.  AI Service calls Gemini API (sync HTTP to external)
8.  AI Service publishes → [recipe.completed] → RabbitMQ Exchange
9.  Recipe Service consumes [recipe.completed] → updates status: COMPLETED
10. Notification Service consumes [recipe.completed] → sends push + email
11. Mobile app receives push notification with deep-link to completed recipe
```

### Failure Handling

```
IF Gemini API fails after max retries:
  AI Service publishes → [recipe.failed] → RabbitMQ Exchange
  Recipe Service consumes [recipe.failed] → updates status: FAILED
  Notification Service consumes [recipe.failed] → sends failure notification
  Dead-letter queue captures the original message for replay
```

### RabbitMQ Topology

| Exchange | Type | Routing Key | Consumer |
|---|---|---|---|
| `recipe.exchange` | Topic | `recipe.created` | AI Service |
| `recipe.exchange` | Topic | `recipe.completed` | Recipe Service, Notification Service |
| `recipe.exchange` | Topic | `recipe.failed` | Recipe Service, Notification Service |
| `recipe.dlx` | Direct | `recipe.dead` | Ops / Manual Replay |

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

Both apps are native, sharing no cross-platform runtime:

### iOS — `mobile_app/iOS/`

- **Language:** Swift 5.9+
- **UI Framework:** SwiftUI
- **Architecture:** MVVM + Combine
- **Auth:** Sign In with Apple, Google Sign-In SDK
- **Networking:** URLSession with async/await
- **Push:** APNs via Firebase Cloud Messaging
- **Media Input:** AVFoundation (audio capture), PhotosUI (image picker), CoreML (on-device preprocessing)

### Android — `mobile_app/Android/`

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM + StateFlow + Hilt DI
- **Auth:** Google Sign-In, Custom JWT login
- **Networking:** Retrofit + OkHttp
- **Push:** Firebase Cloud Messaging (FCM)
- **Media Input:** CameraX, SpeechRecognizer, MediaRecorder

---

## Getting Started

### Prerequisites

- Docker Desktop 4.x+ (with Compose v2)
- Git

### Clone & Run

```bash
# 1. Clone the repository
git clone https://github.com/your-org/kitchen-ai.git
cd kitchen.ai

# 2. Copy environment template
cp infrastructure/.env.example infrastructure/.env

# 3. Add your Gemini API key to .env
#    GEMINI_API_KEY=your_key_here

# 4. Start the entire platform
docker compose -f infrastructure/docker-compose.yml up --build
```

That's it. All services, databases, RabbitMQ, and Redis will be running.

### Service Endpoints (after startup)

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| RabbitMQ Management UI | http://localhost:15672 (guest / guest) |
| Auth Service (direct) | http://localhost:8081 |
| Recipe Service (direct) | http://localhost:8082 |

### Health Checks

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

---

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
# Gemini AI
GEMINI_API_KEY=your_gemini_api_key

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Apple OAuth
APPLE_CLIENT_ID=com.yourorg.kitchen.ai
APPLE_TEAM_ID=YOUR_TEAM_ID
APPLE_KEY_ID=YOUR_KEY_ID

# JWT
JWT_SECRET=your_256bit_secret_here
JWT_ACCESS_TTL_MINUTES=15
JWT_REFRESH_TTL_DAYS=7

# Email (SMTP)
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=your_sendgrid_api_key

# Firebase (Push Notifications)
FIREBASE_SERVICE_ACCOUNT_PATH=/secrets/firebase-service-account.json

# Postgres (auto-created by Docker Compose)
POSTGRES_USER=kitchen.ai
POSTGRES_PASSWORD=kitchen.ai_secret

# RabbitMQ (auto-created by Docker Compose)
RABBITMQ_USER=kitchen.ai
RABBITMQ_PASSWORD=kitchen.ai_rabbit
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
| **iOS** | Swift 5.9, SwiftUI, Combine |
| **Android** | Kotlin 1.9, Jetpack Compose, Hilt |
| **Containerisation** | Docker, Docker Compose v2 |
| **Observability** | Spring Actuator, Micrometer, Distributed Tracing (Correlation IDs) |
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

<p align="center">Built with ❤️ — Spring Boot · RabbitMQ · Gemini · Swift · Kotlin · Docker</p>
