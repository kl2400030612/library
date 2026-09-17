# Architecture

## Components

- **Eureka Server**: service registry.
- **API Gateway**: single client entry point and load-balanced routing.
- **Auth Service**: user registration, login, password hashing and JWT issuance.
- **Book Service**: catalogue, search and copy availability.
- **Borrow Service**: loan lifecycle and orchestration.
- **Fine Service**: overdue fine calculation and history.
- **Frontend**: React/Vite dashboard for users and admins.

## Main request flow

```text
Browser → Gateway → Borrow Service → Book Service
                             |
                             +----→ Fine Service
```

Borrow Service passes the user's JWT to downstream services. Each servlet-based service validates the token independently. This keeps service boundaries explicit while allowing the API Gateway to remain a lightweight routing layer.

## Fine formula

`fine = max(0, returnDate - dueDate) × dailyFineRate`

## Deployment

Docker Compose starts PostgreSQL, Eureka, the services, Gateway and frontend as one network. For production, use TLS, managed database infrastructure and external secret management.
