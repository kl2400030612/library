# PS038 Requirement Traceability Matrix

Use this sheet during evaluation. Each requirement is mapped to the implementation area and a suggested proof point.

| PS038 requirement | Implementation | Demo / evidence |
|---|---|---|
| Search digital resources | Book Service `/api/books/search` | Search a title/category in the UI |
| Borrow resources | Borrow Service `POST /api/borrow` | Borrow an available book |
| Return resources | Borrow Service `PUT /api/borrow/{id}/return` | Return the active loan |
| E-books | Book model + `ebookUrl` | Show an e-book entry/link |
| Access permissions | JWT + Spring Security + Student/Faculty/Admin roles | Show Admin-only controls and protected APIs |
| Active loan periods | Borrow Service | Show due date in My Borrowings |
| Automatic fine calculation | Fine Service; calculated during return flow | Show fine record after an overdue return scenario |
| Copy availability | Book Service availability endpoint + reserve/release flow | Show copies before/after borrow/return |
| JWT authentication | Auth Service + shared security library | Login and use returned JWT for protected APIs |
| Centralized API access | Spring Cloud API Gateway | Frontend calls Gateway on port 8080 |
| Eureka registration | Eureka Server | Open Eureka dashboard and show registered services |
| Load balancing | Spring Cloud LoadBalancer + `lb://SERVICE` routes | Explain dynamic service resolution |
| Inter-service communication | Borrow Service → Book Service / Fine Service | Explain reserve, release and fine orchestration |
| Search/Borrow/Return/Fine APIs | REST controllers in each service | Show endpoint list / API calls |
| Testing | Maven test suites | Show the final automated test result: 164 passing in the latest verified batch |
| Deployment | Dockerfiles + Docker Compose | `docker compose up --build`; show running containers |

## Core evaluation story

The cleanest end-to-end story is:

`Login → Search → Check availability → Borrow → Active loan/due date → Return → Fine calculation → Availability restored`

Then switch to Admin:

`Admin login → Catalogue management → User/loan/fine visibility → Eureka dashboard`
