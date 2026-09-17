up:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f

backend-test:
	cd backend && mvn clean verify

frontend-build:
	cd frontend && npm install && npm run build
