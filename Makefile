C7_PROCESS_ENGINE_API_DIR := apps/c7-process-engine-api
C7_VANILLA_DIR := apps/c7-vanilla
FRONTEND_DIR := apps/frontend
STACK_DIR := stack

C7_PROCESS_ENGINE_API_COMPOSE := $(STACK_DIR)/docker-compose.c7-process-engine-api.yml
C7_VANILLA_COMPOSE := $(STACK_DIR)/docker-compose.c7-vanilla.yml
LOCAL_IMAGES := miravelo-process-automation-example-c7-process-engine-api:local \
	miravelo-process-automation-example-c7-vanilla:local \
	miravelo-process-automation-example-frontend:local

.DEFAULT_GOAL := help

.PHONY: help
help: ## Show this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z0-9_.-]+:.*?##/ { printf "  \033[36m%-42s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Build

.PHONY: setup
setup: ## Install frontend dependencies and verify both backends without tests.
	cd $(FRONTEND_DIR) && npm ci
	./mvnw clean verify -DskipTests

.PHONY: build-app
build-app: build-backend build-frontend ## Build and test all application artifacts.

.PHONY: build-backend
build-backend: ## Build and test both backend variations.
	./mvnw clean verify

.PHONY: build-backend-c7-process-engine-api
build-backend-c7-process-engine-api: ## Build and test the Process Engine API backend.
	./mvnw -pl $(C7_PROCESS_ENGINE_API_DIR) clean verify

.PHONY: build-backend-c7-vanilla
build-backend-c7-vanilla: ## Build and test the vanilla Camunda 7 backend.
	./mvnw -pl $(C7_VANILLA_DIR) clean verify

.PHONY: build-frontend
build-frontend: ## Build, lint, and test the frontend.
	cd $(FRONTEND_DIR) && npm ci && npm run lint && npm run test:coverage && VITE_API_BASE_URL=/ npm run build

.PHONY: build-docker
build-docker: docker-build-backends docker-build-frontend ## Build all local Docker images.

.PHONY: docker-build-backends
docker-build-backends: docker-build-c7-process-engine-api docker-build-c7-vanilla ## Build both backend images.

.PHONY: docker-build-c7-process-engine-api
docker-build-c7-process-engine-api: build-backend-c7-process-engine-api ## Build the Process Engine API backend image.
	docker build -t miravelo-process-automation-example-c7-process-engine-api:local $(C7_PROCESS_ENGINE_API_DIR)

.PHONY: docker-build-c7-vanilla
docker-build-c7-vanilla: build-backend-c7-vanilla ## Build the vanilla Camunda 7 backend image.
	docker build -t miravelo-process-automation-example-c7-vanilla:local $(C7_VANILLA_DIR)

.PHONY: docker-build-frontend
docker-build-frontend: build-frontend ## Build the frontend image.
	docker build -t miravelo-process-automation-example-frontend:local $(FRONTEND_DIR)

.PHONY: docker-prune-images
docker-prune-images: ## Remove this project's local Docker images.
	@docker info >/dev/null
	@for image in $(LOCAL_IMAGES); do \
		if docker image inspect "$$image" >/dev/null 2>&1; then \
			docker image rm "$$image" || exit; \
		fi; \
	done

##@ Infrastructure Only

.PHONY: infrastructure-up
infrastructure-up: ## Start PostgreSQL and MailHog for host-based development.
	docker compose -f $(STACK_DIR)/docker-compose.yml up -d

.PHONY: infrastructure-down
infrastructure-down: ## Stop the host-development infrastructure.
	docker compose -f $(STACK_DIR)/docker-compose.yml down

.PHONY: infrastructure-logs
infrastructure-logs: ## Follow the host-development infrastructure logs.
	docker compose -f $(STACK_DIR)/docker-compose.yml logs --follow

##@ Process Engine API Stack

.PHONY: full-c7-process-engine-api
full-c7-process-engine-api: docker-build-c7-process-engine-api docker-build-frontend ## Build and start the Process Engine API stack.
	$(MAKE) full-c7-process-engine-api-up

.PHONY: full-c7-process-engine-api-up
full-c7-process-engine-api-up: ## Start the Process Engine API stack from existing images.
	docker compose -f $(C7_PROCESS_ENGINE_API_COMPOSE) up -d

.PHONY: full-c7-process-engine-api-down
full-c7-process-engine-api-down: ## Stop the Process Engine API stack.
	docker compose -f $(C7_PROCESS_ENGINE_API_COMPOSE) down

.PHONY: full-c7-process-engine-api-logs
full-c7-process-engine-api-logs: ## Follow Process Engine API backend and frontend logs.
	docker compose -f $(C7_PROCESS_ENGINE_API_COMPOSE) logs --follow backend frontend

.PHONY: full-c7-process-engine-api-reset
full-c7-process-engine-api-reset: ## Stop the Process Engine API stack and delete its database.
	docker compose -f $(C7_PROCESS_ENGINE_API_COMPOSE) down --volumes

##@ Vanilla Camunda 7 Stack

.PHONY: full-c7-vanilla
full-c7-vanilla: docker-build-c7-vanilla docker-build-frontend ## Build and start the vanilla Camunda 7 stack.
	$(MAKE) full-c7-vanilla-up

.PHONY: full-c7-vanilla-up
full-c7-vanilla-up: ## Start the vanilla Camunda 7 stack from existing images.
	docker compose -f $(C7_VANILLA_COMPOSE) up -d

.PHONY: full-c7-vanilla-down
full-c7-vanilla-down: ## Stop the vanilla Camunda 7 stack.
	docker compose -f $(C7_VANILLA_COMPOSE) down

.PHONY: full-c7-vanilla-logs
full-c7-vanilla-logs: ## Follow vanilla Camunda 7 backend and frontend logs.
	docker compose -f $(C7_VANILLA_COMPOSE) logs --follow backend frontend

.PHONY: full-c7-vanilla-reset
full-c7-vanilla-reset: ## Stop the vanilla Camunda 7 stack and delete its database.
	docker compose -f $(C7_VANILLA_COMPOSE) down --volumes
