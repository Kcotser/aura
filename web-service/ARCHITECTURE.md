# Architecture Decision Record — Aura Backend

## ADR-001: Modular Monolith over Microservices

**Date:** 2026-07-25  
**Status:** Accepted  
**Context:** Aura is a hackathon project that must be demo-ready within a tight timeline, while being designed to scale into a production system.

### Decision

We chose a **modular monolith** using **Spring Boot 4.1** and **Spring Modulith** rather than a microservices architecture from day one.

### Rationale

#### Why NOT microservices initially

1. **Operational complexity**: Microservices require container orchestration (Kubernetes), service discovery, distributed tracing, and inter-service authentication from day one — adding weeks of infrastructure work irrelevant to the hackathon demo.
2. **Premature distribution**: Martin Fowler's "MonolithFirst" pattern advises that getting the bounded context boundaries right takes time. Distributing before boundaries stabilize leads to chatty, tightly-coupled services — the worst of both worlds.
3. **Team size**: A small hackathon team benefits from a single codebase, single deployment artifact, and shared test suite.

#### Why Spring Modulith

Spring Modulith gives us **modular monolith discipline** with a clear path to microservices:

- **Enforced encapsulation**: `ApplicationModularityTests` fails the CI build if any module accesses internal classes of another — the same boundary discipline as microservices.
- **Domain event bus**: Inter-module communication via `ApplicationEventPublisher` + `@ApplicationModuleListener` mirrors async messaging (Kafka/RabbitMQ) without the infrastructure. Extracting a context to a microservice requires only replacing the listener with a message consumer.
- **Documented modules**: Each bounded context is a self-documenting Spring Modulith module with a `package-info.java`.

### Bounded Context Design

```
com.aura
├── shared          — Cross-cutting concerns (no business logic)
├── iam             — Identity & Access Management ✅
├── emergencyactivation — Incident state machine 🚧
├── evidence        — Multimedia evidence ingestion 🚧
├── analysis        — Gemma 4 AI integration 🚧
├── reporting       — Incident report lifecycle 🚧
├── trustnetwork    — SOS contacts & alerts 🚧
├── directory       — Institutional entities catalog 🚧
└── privacy         — Retention policies & encryption config 🚧
```

### Migration Path to Microservices

When a context needs to be extracted:
1. Deploy it as a separate Spring Boot application (the domain code moves as-is)
2. Replace `ApplicationEventPublisher` with a Kafka producer/consumer
3. Expose internal use cases as gRPC or REST endpoints
4. No rewrite of domain logic required

## ADR-002: First Candidate for Extraction — `analysis`

**Candidate:** `com.aura.analysis` (Gemma 4 integration)  
**Reason:** The analysis context is **compute-intensive** and has a hard external dependency on the Gemma 4 API. It has:
- A distinct scaling profile (needs GPU/accelerator instances, not the same fleet as IAM)
- Natural isolation (Anti-Corruption Layer already isolates Gemma 4's model from domain concepts)
- Async-only interaction pattern (evidence uploaded → analysis triggered → result event published), which maps cleanly to a message-driven microservice

## ADR-003: Hexagonal Architecture (Ports & Adapters)

Every bounded context follows the same layered structure:

```
domain/     — Pure business logic, zero framework dependencies
application/ — Use cases that orchestrate the domain
infrastructure/ — MongoDB documents, Spring Security config, external adapters
interfaces/ — REST controllers, request/response DTOs
```

**Rule:** The `domain` layer never imports Spring, MongoDB, or any infrastructure framework. This makes it trivially unit-testable and infrastructure-replaceable.

## ADR-004: MongoDB Atlas as Primary Store

- **Document model** fits the evolving schema of a hackathon project better than rigid SQL migrations
- **Atlas** provides managed replication, backups, and Atlas Search for potential full-text evidence search in the future
- **Aggregation pipelines** can power the reporting context's complex queries
