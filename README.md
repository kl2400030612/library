## V13 admin circulation live sync

The Admin workspace now refreshes circulation, fines, and user data automatically every 5 seconds while the Admin page is open. A manual **Refresh** control is also available. This keeps the **All circulation** table current when students or faculty borrow/return resources from another browser/session.

# Archvialia V10 — Enterprise Academic E-Library & Digital Resource Circulation System

A deployable reference implementation for **PS038 – Enterprise Academic E-Library & Digital Resource Circulation System**.

## What is included

- Spring Boot microservices backend
  - Eureka Server
  - API Gateway
  - Auth Service (JWT)
  - Book Service
  - Borrow Service
  - Fine Service
- React + Vite frontend
- PostgreSQL persistence
- Eureka-based discovery
- Load-balanced inter-service calls
- STUDENT / FACULTY / ADMIN authorization
- Search, borrow, return, availability and fine calculation APIs
- Automated backend tests
- Docker Compose deployment
- GitHub Actions CI

## Architecture

```text
Browser
   |
   v
React/Vite Frontend
   |
   v
API Gateway :8080
   |
   +--> AUTH-SERVICE  :8081
   +--> BOOK-SERVICE  :8082
   +--> BORROW-SERVICE:8083
   +--> FINE-SERVICE  :8084
             |
             +--> Book Service
             +--> Fine Service

All services register with Eureka :8761. Gateway and clients refresh the Eureka registry every 5 seconds to reduce startup discovery races.
PostgreSQL is shared for this academic reference implementation; each service owns distinct tables.
```

## Technology

- Java 21
- Spring Boot 4.1.x
- Spring Cloud 2025.1.x
- Spring Security 7
- JWT (JJWT)
- Spring Cloud Gateway
- Netflix Eureka
- Spring Cloud LoadBalancer
- Spring Data JPA
- PostgreSQL
- React + Vite

## Quick start with Docker

1. Copy `.env.example` to `.env` and change secrets.
2. Run:

```bash
docker compose up --build
```

3. Open the frontend at `http://localhost:5173`.
4. API Gateway is at `http://localhost:8080`.
5. Eureka dashboard is at `http://localhost:8761`.

The frontend defaults to the API Gateway URL from `VITE_API_URL`.

## Demo accounts

When `SEED_DEMO_DATA=true`, the auth service creates:

- Admin: `admin@archvialia.local` / `Admin@12345`
- User: `user@archvialia.local` / `User@12345`

Change these before any real deployment. They are for demonstrations only.

## Local development

Prerequisites:

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16+

Start backend:

```bash
cd backend
mvn clean verify
```

Start services in this order:

1. Eureka Server
2. Auth Service
3. Book Service
4. Fine Service
5. Borrow Service
6. API Gateway

Then start frontend:

```bash
cd frontend
npm install
npm run dev
```

## Important API endpoints

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Books

- `GET /api/books`
- `GET /api/books/search?q=java`
- `GET /api/books/{id}`
- `GET /api/books/{id}/availability`
- `POST /api/books` (ADMIN)
- `PUT /api/books/{id}` (ADMIN)
- `DELETE /api/books/{id}` (ADMIN)

### Borrow

- `POST /api/borrow`
- `GET /api/borrow/me`
- `GET /api/borrow/{id}`
- `PUT /api/borrow/{id}/return`
- `GET /api/borrow/admin/all` (ADMIN)

### Fines

- `GET /api/fines/me`
- `GET /api/fines/{borrowId}`
- `POST /api/fines/calculate/{borrowId}`
- `GET /api/fines/admin/all` (ADMIN)

## Example requests

Login:

```json
POST /api/auth/login
{
  "email": "user@archvialia.local",
  "password": "User@12345"
}
```

Borrow:

```json
POST /api/borrow
Authorization: Bearer <JWT>
{
  "bookId": 1
}
```

Return:

```text
PUT /api/borrow/1/return
Authorization: Bearer <JWT>
```

## Deployment

The included `docker-compose.yml` is suitable for a single-host demo deployment. For production, put the gateway/frontend behind TLS, use managed PostgreSQL, rotate the JWT secret, remove demo credentials, and use a proper secret manager.

## Evaluation-ready documentation

- Architecture diagram: `docs/architecture.svg`
- PS038 requirement traceability: `docs/requirements-matrix.md`
- API quick reference: `docs/api-reference.md`
- Evaluation checklist: `docs/evaluation-checklist.md`
- 5-minute demo script: `docs/demo-script.md`

The recommended evaluation flow is:
`Login → Search → Availability → Borrow → Active loan → Return → Fine → Admin → Eureka`

## Requirement mapping

| Problem statement | Implementation |
|---|---|
| Book Service | `backend/book-service` |
| Borrow Service | `backend/borrow-service` |
| Fine Service | `backend/fine-service` |
| JWT authentication | `backend/auth-service` + `security-lib` |
| API Gateway | `backend/api-gateway` |
| Eureka | `backend/eureka-server` |
| Load balancing | Spring Cloud LoadBalancer + Eureka |
| Borrow → Book → Fine | Borrow service orchestrates both calls |
| Search | Book Service search endpoint |
| Borrow / Return | Borrow Service |
| Fine calculation | Fine Service |
| Testing | Maven tests in each service |
| Deployment | Dockerfiles + Docker Compose |


## Preflight check

From the project root, verify Docker first:

```powershell
docker --version
docker compose version
docker run hello-world
```

Then validate the Compose file before building:

```powershell
docker compose config --quiet
```

If validation succeeds, launch the full stack:

```powershell
docker compose up --build
```

Open the frontend at http://localhost:5173 and Eureka at http://localhost:8761. The API Gateway is available at http://localhost:8080.

## Role model

Archvialia supports three user types:
- **Student** (`STUDENT`) — search, borrow, return, access library resources, and view personal loans/fines.
- **Faculty** (`FACULTY`) — search, borrow and return academic/research resources, and view personal loans/fines.
- **Librarian/Admin** (`ADMIN`) — manage resources and availability, review all users, monitor all loans/returns, and review all fines.

Public registration can create Student or Faculty accounts. Librarian/Admin accounts are controlled by the seeded admin account or by an existing Librarian/Admin through the Admin workspace.

## V10 reliability notes

- Light, warm academic-library visual system with a CSS-built animated library scene on the authentication screen; no static bookshelf image is required.
- Authentication error handling reads Spring Problem Details (`detail`) as well as legacy `message`/`error`, so duplicate emails and validation failures are shown clearly.
- Registration fields start clean instead of reusing the demo student credentials.
- Legacy `USER` accounts receive `STUDENT` in newly issued JWTs for consistent role handling across services.

- Public registration accepts Student and Faculty only; Admin access is controlled by an existing Admin/seed account.
- Legacy `USER` rows are treated as Student rows for backward compatibility.
- The auth role CHECK constraint is repaired idempotently at auth-service startup, so older databases do not require a manual SQL migration.
- Frontend authentication requests retry transient 502/503/504 responses and temporary network failures; invalid credentials and validation errors are not retried.
- React authentication transitions keep Hook order stable, so sign-in/sign-out does not depend on a browser refresh.
- Docker image builds use `-Dmaven.test.skip=true` so test-source compilation cannot break production image builds.

## V12 browser/CORS hardening
The frontend now calls the API through the same-origin `/api/...` path. Nginx inside the frontend container proxies those requests to the API Gateway. This removes browser cross-origin/preflight dependency while preserving the API Gateway as the centralized entry point.


## V14 fix
Admin circulation, fines, and user data are refreshed independently. A failure in one admin endpoint no longer prevents successful circulation records from appearing in the Admin panel.
"# library" 
