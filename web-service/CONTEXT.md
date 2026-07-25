# Aura — Contexto del Backend (para el equipo)

## 1. Qué es Aura

Aura es una plataforma de seguridad personal multimodal: al activar una alerta, el celular graba cámara frontal, cámara trasera y audio ambiental de forma encubierta, sube esos archivos al backend, y un modelo de IA (Gemma 4) los analiza para generar un reporte estructurado del incidente y sugerir a qué entidad legal acudir (Línea 100, Comisaría de la Mujer, etc.). La usuaria revisa y aprueba ese borrador antes de exportarlo o compartirlo.

## 2. Arquitectura general

El backend es un **modular monolith** (un solo deployable, no microservicios) organizado en **8 bounded contexts** siguiendo DDD + arquitectura hexagonal (ports & adapters). Usamos **Spring Modulith** para forzar los límites entre módulos en tiempo de test: nadie puede importar clases internas de otro contexto directamente.

**Regla de oro:** los bounded contexts nunca se llaman entre sí directamente. Toda comunicación cruzada es vía **eventos de dominio** (`ApplicationEventPublisher` publica, `@ApplicationModuleListener` escucha). Esto significa que cada módulo se puede leer y entender de forma aislada, y que el día que necesitemos separar algo en un microservicio (candidato más probable: `analysis`, por su dependencia externa de Gemma 4), no hay que reescribir lógica de negocio, solo cambiar el transporte del evento.

**Convención de carpetas, igual en los 8 contextos:**
```
com.aura.{context}
├── domain
│   ├── model          → Aggregates, Entities, Value Objects (records inmutables, sin anotaciones de Spring/Mongo)
│   ├── event          → Domain Events
│   └── repository     → Interfaces de repositorio (ports)
├── application
│   ├── usecase        → Application Services (orquestan el dominio)
│   ├── dto            → Commands/Queries
│   └── mapper         → Dominio ↔ DTOs
├── infrastructure
│   ├── persistence    → @Document de Mongo + implementación de los repository ports
│   └── config
└── interfaces/rest/{controller,request,response}
```

## 3. Stack técnico

| Componente | Elección |
|---|---|
| Lenguaje | Java 21 (LTS) — records para VOs/DTOs, sealed interfaces para estados cerrados, Virtual Threads habilitados |
| Framework | Spring Boot **4.1.x** (Spring Framework 7) |
| Build tool | Maven |
| Base de datos | MongoDB Atlas (Spring Data MongoDB) |
| Seguridad | Spring Security 7 + JWT (access + refresh token), BCrypt |
| Modularidad | Spring Modulith (`ApplicationModularityTests` verifica los límites) |
| Documentación API | springdoc-openapi → Swagger UI en `/swagger-ui/index.html` |
| Testing | JUnit 5, Mockito, Testcontainers (MongoDB real en los integration tests) |

## 4. Estado actual de los 8 bounded contexts

| Contexto | Estado | Qué hace |
|---|---|---|
| `iam` | ✅ Completo | Identidad, autenticación, sesiones |
| `emergencyactivation` | ✅ Completo | Máquina de estados del incidente |
| `evidence` | ✅ Completo | Ingesta y storage de archivos multimedia |
| `analysis` | 🔨 En progreso | Integración con Gemma 4 |
| `reporting` | 🔨 En progreso | Borrador, edición, aprobación y export del reporte |
| `trustnetwork` | ⏳ Pendiente | Contactos SOS y alertas |
| `directory` | ⏳ Pendiente | Catálogo de entidades institucionales |
| `privacy` | ⏳ Pendiente | Políticas de retención/autodestrucción |
| `shared` | ✅ Completo | Excepciones base, `ApiResponse<T>`, `GlobalExceptionHandler`, auditoría |

**Orden de prioridad decidido:** `analysis` + `reporting` primero porque sin ellos el pipeline se queda "muerto" en estado `UPLOADED` — son el corazón real del pitch (la IA analiza, la usuaria aprueba). `trustnetwork` es la siguiente feature de mayor impacto visual para la demo. `directory` y `privacy` quedan al final o directamente mockeados (ninguno es visible en una demo de 5 minutos).

## 5. Detalle de lo ya implementado

### 5.1 `iam` — Identity & Access Management

**Aggregates:** `User` (email, hashedPassword, nombre, teléfono, active), `Credential` (pinHash, biometricEnabled), `DeviceSession` (refreshTokenHash, deviceId, expiresAt, revoked).

**Eventos:** `UserRegisteredEvent`, `UserAuthenticatedEvent`, `PinConfiguredEvent`, `AccountDeletionRequestedEvent`.

**Endpoints:**
- `POST /api/v1/auth/register`, `/login`, `/refresh`, `/logout`
- `POST /api/v1/auth/pin`, `/pin/validate`
- `GET|PATCH|DELETE /api/v1/users/me`

**Mongo:** `users`, `device_sessions` (índice único en `email`).

### 5.2 `emergencyactivation` — Incident Lifecycle

**Aggregate:** `Incident`, con estado modelado como `sealed interface IncidentStatus`:
`ACTIVATED → RECORDING → UPLOADED → PROCESSING → DRAFT_READY → APPROVED → EXPORTED → CLOSED` (+ estado lateral `CANCELLED`). Las transiciones son métodos del propio aggregate (`incident.markUploaded()`, etc.) que validan que el salto de estado sea legal.

**Eventos:** `IncidentActivatedEvent`, `IncidentUploadedEvent`, `IncidentProcessingStartedEvent`, `IncidentApprovedEvent`, `IncidentClosedEvent`, `IncidentCancelledEvent`.

**Endpoints:**
- `POST /api/v1/incidents/activate` (toma GPS + userId del JWT)
- `POST /api/v1/incidents/{id}/cancel`
- `GET /api/v1/incidents/{id}` y `GET /api/v1/incidents` (lista paginada)
- `POST /api/v1/incidents/{id}/transition` — atajo temporal solo para demo/testing manual, documentado como tal en el código.

**Mongo:** `incidents`, índice compuesto `(userId, status)`.

### 5.3 `evidence` — Media Evidence Ingestion

**Aggregate:** `EvidenceAsset` (type: `FRONT_CAMERA`/`BACK_CAMERA`/`AMBIENT_AUDIO`, storageReference, integrityHash SHA-256, sizeBytes, contentType, retentionStatus, purgeAt).

**Decisión de storage:** **MongoDB GridFS**, no S3 — evitamos configurar una cuenta AWS bajo presión de tiempo de hackathon. El binario nunca vive en el documento `EvidenceAsset`, solo la referencia.

**Eventos:** `MediaReceivedEvent`, `MediaValidatedEvent`, `AllEvidenceUploadedEvent` (cuando llegaron los 3 streams), `MediaPurgedEvent`.

**Endpoints:**
- `POST /api/v1/incidents/{incidentId}/evidence` — multipart con `frontCamera`, `backCamera`, `ambientAudio` (usa `@RequestPart`, no `@RequestParam`, para que Swagger genere bien el campo de archivo)
- `GET /api/v1/incidents/{incidentId}/evidence` (metadata) y `GET /api/v1/evidence/{id}/download`
- `DELETE /api/v1/evidence/{id}`

**Mongo:** `evidence_assets` + buckets nativos de GridFS.

### 5.4 Cómo se conectan `emergencyactivation` y `evidence` (el primer ejemplo real de choreography)

1. `evidence` recibe los 3 archivos → publica `AllEvidenceUploadedEvent(incidentId)`.
2. `emergencyactivation` tiene un listener (`@ApplicationModuleListener`) que escucha ese evento y llama a `incident.markUploaded()` — sin conocer nada de la clase `EvidenceAsset`.

Este es el patrón exacto que `analysis` y `reporting` (en progreso) están replicando para conectarse con lo ya construido.

## 6. Variables de entorno

**No están en git** (`.gitignore`) — pide los valores reales a quien las tenga, nunca generes credenciales nuevas de Atlas por tu cuenta.

```
MONGODB_URI=
MONGODB_DATABASE=
JWT_SECRET=
JWT_ACCESS_TOKEN_EXPIRATION_MINUTES=15
JWT_REFRESH_TOKEN_EXPIRATION_DAYS=30
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
CORS_ALLOWED_ORIGINS=
LOG_LEVEL=INFO
GEMMA_API_URL=          # reservado para `analysis`
GEMMA_API_KEY=          # reservado para `analysis`
FIELD_ENCRYPTION_KEY=   # reservado para `privacy`
```

## 7. Cómo correr el proyecto localmente

1. Clona el repo y verifica JDK 21 + Maven instalados.
2. Pide el archivo `.env`/`application-dev.yml` con los secretos reales al equipo.
3. `mvn clean install` — confirma que pasan los tests de `iam`, `emergencyactivation` y `evidence` antes de tocar nada.
4. `mvn spring-boot:run` con perfil `dev`.
5. Swagger UI en `http://localhost:8080/swagger-ui/index.html` — usa el botón "Authorize" con el JWT que te devuelve `/auth/login` para probar los endpoints protegidos.

## 8. Convención de branches

`feature/kebab-case-en-inglés`, agrupando bounded contexts que se integran entre sí en la misma tarea:

- `feature/emergency-activation-evidence-ingestion` (ya mergeado)
- `feature/ai-analysis-incident-reporting` (en progreso ahora)

## 9. Roadmap pendiente

`trustnetwork` → `directory` → `privacy`, en ese orden de prioridad para la demo. `directory` y `privacy` pueden mockearse (un string fijo en vez de catálogo real; una config que nunca se ejecuta en vivo durante la demo) sin que el jurado lo note.
