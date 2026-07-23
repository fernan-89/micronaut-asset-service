# ADR 001: Architectural Foundation of the IT Asset Management Service (Hexagonal, Reactive, and Forensic Patterns)

## Status
Accepted

## Context
The IT Asset Management (ITAM) context is a mission-critical operational microservice responsible for the authoritative inventory and lifecycle governance of hardware, servers, and cloud resources [cite: 1434, 1438]. Due to high-compliance requirements and the need for global scale, the system requires an architecture capable of handling high-throughput asynchronous operations while maintaining absolute data integrity and an irrefutable forensic audit trail [cite: 1, 1424, 1426].

To achieve "NASA-level" reliability and prevent technical debt, the service must avoid monolithic coupling, weak domain typing, and proxy-based dependency injection conflicts that hinder GraalVM native image compatibility [cite: 1, 1040, 1218].

## Decision
We have decided to implement **Hexagonal Architecture (Ports and Adapters)** combined with a **Fully Reactive Stack** (Java 21, Micronaut 4.4.2, Project Reactor, and MongoDB Reactive Streams) [cite: 2, 1424].

### 1. Domain Sovereignty and Immutability
*   **Aggregate Root:** The core `Asset` model is implemented as a **Java Record**, ensuring absolute immutability and thread-safety in concurrent reactive pipelines [cite: 967, 1032, 1360].
*   **Finite State Machine (FSM):** Asset lifecycles (Provisioned, Ready, Deployed, Maintenance, Decommissioned) are strictly governed by an internal FSM within the `AssetStatus` value object, preventing illegal state transitions [cite: 972, 1224, 1426].
*   **Deterministic Identity:** Asset identifiers are generated using Name-based UUIDs (v3) to ensure idempotency and prevent duplicate provisioning across multi-tenant environments [cite: 967, 969, 1434].

### 2. Infrastructure Resilience
*   **Strong Typing:** Native use of `java.util.UUID` across all layers (Domain, Application, and Persistence) is mandatory to eliminate injection risks and parsing overhead [cite: 864, 1052, 1068].
*   **Explicit Dependency Injection:** To ensure reliable Ahead-of-Time (AOT) proxy generation, we mandate the use of explicit constructors with `@Inject` on all Micronaut components, prohibiting the use of Lombok’s `@RequiredArgsConstructor` on `@Singleton` or `@Controller` beans [cite: 1040, 1270].
*   **BSON Representation:** The MongoDB driver is configured with `uuid-representation: STANDARD` to ensure BSON Binary Subtype 4 compliance for high-performance indexing [cite: 1064, 1068].

### 3. Forensic Governance and Communication
*   **Append-Only Audit Trail:** Every state mutation must atomically register an immutable record in the `AssetAudit` ledger, capturing the executor identity and business justification [cite: 1150, 1438].
*   **RFC 7807 Compliance:** All boundary errors are intercepted by a `GlobalExceptionHandler` and projected as "Problem Details" to prevent internal implementation leakage [cite: 1347, 1348].
*   **Output Projections:** External-facing data is strictly wrapped in explicit Response DTOs using the **Aggregate Root Projection** pattern to shield the core domain [cite: 1041, 1048, 1218].

## Consequences

### Positive:
*   **Zero-Blocking I/O:** Reactive streams ensure maximum hardware utilization and minimal latency [cite: 1, 1106, 1424].
*   **Strict Decoupling:** Core business logic remains portable and framework-agnostic, facilitating 100% test coverage with JUnit 5 and Mockito [cite: 1106, 1218].
*   **Forensic Accountability:** Guaranteed traceability for all hardware lifecycle events [cite: 1347, 1438].
*   **Production Readiness:** Optimized for ultra-fast startup and low memory footprint via AOT [cite: 1090, 1218].

### Negative:
*   **Implementation Overhead:** The requirement for explicit DTOs and Mappers increases initial boilerplate [cite: 1035, 1218].
*   **Paradigm Complexity:** Requires a high level of discipline and expertise in reactive programming and functional mutations [cite: 1106, 1218].

## Compliance
*   All new code must adhere to the `com.thinklab` package structure [cite: 862, 868].
*   CI/CD pipelines must reject any Micronaut component using `@RequiredArgsConstructor` [cite: 865, 868].
*   Every state mutation in the `Asset` aggregate must invoke the corresponding `AssetAudit` persistence port [cite: 1150, 1347].
*   Direct exposure of Domain Entities in the Web layer is strictly prohibited [cite: 1041, 1218].

---
*Built with high-assurance engineering standards by Thinklab Staff Engineering [cite: 1, 1476].*