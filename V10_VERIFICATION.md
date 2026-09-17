# Archvialia V10 Verification

## What V10 changes

- Rebuilt from the V9 application baseline without changing the microservice topology.
- Replaced the dark authentication screen with a light academic-library design.
- Added a CSS-generated animated library environment: shelves, books, windows, lamps, readers, plant, light beams and floating dust are DOM/CSS elements rather than a pasted still image.
- Preserved responsive Student, Faculty and Librarian/Admin flows.
- Frontend API error parsing now handles `message`, `detail`, `error`, `title` and short text responses.
- Registration mode clears the demo credentials and starts with a blank form.
- Legacy `USER` accounts are normalized to `STUDENT` when a JWT is issued.
- Restored the JWT filter's production behavior after the temporary debugging experiment.
- Fixed the PowerShell smoke-test variable interpolation bug (`${root}:...`).

## Static verification performed in the build workspace

- React JSX parsed with the TypeScript compiler parser with zero parse diagnostics.
- `backend/pom.xml`, `backend/security-lib/pom.xml`, and `frontend/package.json` parse successfully.
- Book/Auth/security source paths and dependencies were checked against the V9 baseline.
- The JWT filter is restored to its original exception handling; no diagnostic `System.err` logging remains.

## Runtime verification status

The assistant workspace does not have access to the user's Docker Desktop daemon or Maven installation, so a real Docker Compose build cannot honestly be claimed as executed here. The V10 archive is therefore packaged for the user's Docker Desktop environment, with the runtime smoke test included.

## Runtime verification

From the extracted V10 project folder:

```powershell
docker compose config --quiet
docker compose build --progress=plain
docker compose up -d
docker compose ps
.\scripts\smoke-test.ps1
```

The smoke test covers Eureka, gateway health, frontend reachability, Student login/catalogue/borrowing/fines, Faculty registration/catalogue access, and Admin login/user/loan/fine access.

### Authentication request hardening

- Authentication endpoints (`/api/auth/login`, `/api/auth/register`) do not receive a stale `Authorization` header from the frontend.
- Auth Service explicitly permits HTTP `OPTIONS` requests so browser CORS preflight cannot be rejected by Spring Security.
