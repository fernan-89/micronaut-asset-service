# Thinklab IT Asset Management Service

#### Overview
A mission-critical, high-assurance microservice responsible for the authoritative lifecycle management of IT Assets. Engineered with Hexagonal Architecture (Ports and Adapters) and a Fully Reactive Stack, this service ensures zero-blocking I/O, deterministic state transitions, and strict multi-tenant isolation. Built to standards of high reliability to govern global hardware inventory.

#### Tech Stack
*   Language: Java 21 (Leveraging Records, Sealed Interfaces, and Pattern Matching).
*   Framework: Micronaut 4.4.2 (AOT Compilation for ultra-fast startup and GraalVM readiness).
*   Reactive Engine: Project Reactor (Mono/Flux) for non-blocking execution pipelines.
*   Persistence: Reactive MongoDB (Micronaut Data) with specialized UUID BSON encoding.
*   Security & Validation: Jakarta Bean Validation & Defense-in-Depth constructor sanitization.
*   API Documentation: OpenAPI 3.0 / Swagger (Generated at compile-time).
*   Testing: JUnit 5, Mockito, and StepVerifier for reactive stream validation.

#### Core Features
*   Forensic Audit Trail: Every lifecycle mutation (PROVISIONING, DEPLOYMENT, MAINTENANCE, DECOMMISSIONING) is recorded as an immutable forensic event with mandatory executor tracking and business justification.
*   FSM-Driven Lifecycle: Hardware states are governed by a Finite State Machine (FSM) implemented in the Domain Layer, preventing illegal transitions (e.g., reactivating a decommissioned asset).
*   Tier 3 Governance: Integrated 360-degree views (AssetFullView) providing the current state consolidated with the complete history in a single parallelized reactive call.
*   Standardized Error Handling: Full compliance with RFC 7807 (Problem Details), ensuring semantic error signaling (409 Conflict for duplicates, 422 for logic violations).

#### Getting Started
##### Prerequisites
*   Java 21 JDK
*   Docker (for local MongoDB instance and Testcontainers)
*   Gradle 8.x

##### Running the Application
1.  Clean and Build:
    ```bash
    ./gradlew clean build
    ```
2.  Start the Service:
    ```bash
    ./gradlew run
    ```
    *The service will be available at http://localhost:8083*

#### API Documentation (OpenAPI)
The contract is generated automatically during the build process. Once the service is running, access:
*   Swagger UI: http://localhost:8083/swagger/views/swagger-ui
*   OpenAPI Specification: http://localhost:8083/swagger/it-asset-management-api-1.0.0.yml

#### Postman Collection
A complete automated E2E testing suite is available in the docs/postman directory. It covers:
1. Happy Path: Full lifecycle from Provisioning to Decommissioning.
2. Negative Testing: Duplicate serial number detection and illegal state transitions.
3. Observability: Health, Liveness, and Readiness probes validation.

---
*Built with high-assurance engineering standards by Thinklab Staff Engineering.*