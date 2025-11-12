# Variables
DOCKER_COMPOSE = docker compose

.PHONY: help build run stop clean logs restart test

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

build: ## Build the Docker images
	$(DOCKER_COMPOSE) build

run: ## Start the application and MongoDB
	$(DOCKER_COMPOSE) up -d
	@echo "Application is starting..."
	@echo "MongoDB: http://localhost:27017"
	@echo "API: http://localhost:8080"
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"

stop: ## Stop the application and MongoDB
	$(DOCKER_COMPOSE) down

clean: ## Stop and remove containers, networks, and volumes
	$(DOCKER_COMPOSE) down -v
	mvn clean

logs: ## Show application logs
	$(DOCKER_COMPOSE) logs -f app

logs-mongo: ## Show MongoDB logs
	$(DOCKER_COMPOSE) logs -f mongodb

restart: stop run ## Restart the application

test: ## Run tests
	mvn test

dev: ## Run MongoDB only (for local development)
	$(DOCKER_COMPOSE) up -d mongodb
	@echo "MongoDB started on localhost:27017"
	@echo "Run your application locally with: mvn spring-boot:run"
