# Variables
DOCKER_COMPOSE = docker compose

.PHONY: help run serve test _stop _build-backend

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Main targets:'
	@echo '  run          Run backend and frontend in Docker Compose (foreground, Ctrl+C to stop)'
	@echo '  run-bg       Run backend and frontend in Docker Compose (background)'
	@echo '  serve        Run backend in Docker, frontend with hot reload'
	@echo '  test         Run backend tests locally with Maven (uses Testcontainers)'
	@echo '  kill-webpack Kill webpack dev server process running on port 3000'

run: ## Run backend and frontend in Docker Compose (foreground)
	@echo "Starting backend and frontend in Docker Compose..."
	@echo ""
	@echo "MongoDB: http://localhost:27017"
	@echo "Backend API: http://localhost:8080"
	@echo "Frontend: http://localhost:3000"
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"
	@echo ""
	@echo "Press Ctrl+C to stop all services"
	@echo ""
	$(DOCKER_COMPOSE) up --build

run-bg: ## Run backend and frontend in Docker Compose (foreground)
	@echo "Starting backend and frontend in Docker Compose..."
	@echo ""
	@echo "MongoDB: http://localhost:27017"
	@echo "Backend API: http://localhost:8080"
	@echo "Frontend: http://localhost:3000"
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"
	@echo ""
	@echo "Press Ctrl+C to stop all services"
	@echo ""
	$(DOCKER_COMPOSE) up -d --build

serve: _stop _build-backend ## Stop containers, run backend in Docker, frontend with hot reload
	@echo "Starting backend in Docker Compose..."
	$(DOCKER_COMPOSE) up -d mongodb app
	@echo ""
	@echo "✓ MongoDB started on localhost:27017"
	@echo "✓ Backend API started on localhost:8080"
	@echo ""
	@echo "Starting frontend dev server with hot reload..."
	@echo "Press Ctrl+C to stop frontend (backend will keep running)"
	@echo ""
	@cd frontend && npm install --silent && npm start

test: ## Run backend tests with Maven (local)
	@echo "Running backend tests with Maven..."
	@echo ""
	@echo "Note: Tests use Testcontainers for isolated MongoDB"
	@echo "Docker must be running for test execution"
	@echo ""
	mvn test

kill-webpack: ## Kill webpack dev server process
	@echo "Killing webpack dev server..."
	@-pkill -f "webpack serve" 2>/dev/null || true
	@-pkill -f "webpack-dev-server" 2>/dev/null || true
	@-lsof -ti:3000 | xargs kill -9 2>/dev/null || true
	@echo "✓ Webpack dev server stopped"

# Internal targets (not meant to be called directly)
_stop:
	@echo "Stopping all Docker containers..."
	@$(DOCKER_COMPOSE) down 2>/dev/null || true
	@-pkill -f "webpack serve" 2>/dev/null || true
	@-pkill -f "webpack-dev-server" 2>/dev/null || true

_build-backend:
	@echo "Building backend if needed..."
	$(DOCKER_COMPOSE) build app
