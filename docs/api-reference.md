# Archvialia API Reference — Evaluation Quick Sheet

All browser/client requests go through the **API Gateway** at `http://localhost:8080`.

## Authentication

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create Student/Faculty account |
| POST | `/api/auth/login` | Public | Authenticate and receive JWT |

## Books

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/books` | Authenticated | List active resources |
| GET | `/api/books/search?q=...` | Authenticated | Search catalogue |
| GET | `/api/books/{id}` | Authenticated | Resource details |
| GET | `/api/books/{id}/availability` | Authenticated | Copy availability |
| POST | `/api/books` | ADMIN | Create resource |
| PUT | `/api/books/{id}` | ADMIN | Update resource |
| DELETE | `/api/books/{id}` | ADMIN | Deactivate resource |

## Borrowing

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/borrow` | Authenticated | Borrow a book |
| GET | `/api/borrow/me` | Authenticated | Current user's loans |
| GET | `/api/borrow/{id}` | Owner/Admin | Loan details |
| PUT | `/api/borrow/{id}/return` | Owner/Admin | Return a loan |
| GET | `/api/borrow/admin/all` | ADMIN | Review all loans |

## Fines

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/fines/me` | Authenticated | Current user's fines |
| GET | `/api/fines/{borrowId}` | Owner/Admin | Fine for a loan |
| POST | `/api/fines/calculate/{borrowId}` | Owner/Admin | Calculate/store fine |
| GET | `/api/fines/admin/all` | ADMIN | Review all fines |

## Security model

The client sends the JWT as:

`Authorization: Bearer <JWT>`

Public endpoints are limited to registration/login and health/actuator endpoints. Administrative catalogue/user/loan/fine views are protected with the `ADMIN` role.
