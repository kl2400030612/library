# Evaluation / Submission Checklist

## Before the demo

- [ ] Docker Desktop is running.
- [ ] `docker compose config --quiet` succeeds.
- [ ] All eight containers are running.
- [ ] Frontend opens at `http://localhost:5173`.
- [ ] Gateway health is UP at `http://localhost:8080/actuator/health`.
- [ ] Eureka dashboard opens at `http://localhost:8761`.
- [ ] Student login works.
- [ ] Faculty registration works.
- [ ] Admin login works.
- [ ] Search → borrow → return workflow works.
- [ ] Admin workspace loads.

## What to show the evaluator

1. **Problem:** centralized academic digital-resource circulation.
2. **Architecture:** Frontend → Gateway → microservices → PostgreSQL; Eureka provides discovery.
3. **Security:** JWT authentication + role-based authorization.
4. **Core workflow:** search → availability → borrow → loan → return → fine.
5. **Microservices:** Book, Borrow, Fine, Auth, Gateway, Eureka.
6. **Testing:** automated backend tests and the latest verified test result.
7. **Deployment:** Docker Compose with reproducible services.

## Avoid during the demo

- Do not manually edit the database.
- Do not expose JWT secrets or `.env` values.
- Do not make live source-code changes unless a reviewer explicitly asks for one.
- Do not spend the demo on implementation details before showing the working workflow.
