# ====== Project shortcuts ======
# Variables
COMPOSE ?= docker compose
PROFILE ?= dev

.PHONY: help db-up db-down db-clean run-dev logs status psql

help: ## Affiche cette aide
	@echo "Commandes disponibles :"
	@awk 'BEGIN {FS":.*##"} /^[a-zA-Z0-9_.-]+:.*##/ {printf "  \033[36m%-14s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

db-up: ## Démarre Postgres (5433) + Adminer (8081)
	$(COMPOSE) up -d
	@$(COMPOSE) ps

db-down: ## Stoppe et supprime les conteneurs (conserve les données)
	$(COMPOSE) down

db-clean: ## Stoppe et supprime conteneurs + volumes (réinitialise la DB)
	$(COMPOSE) down -v

run-dev: ## Lance l'app Spring Boot avec le profil $(PROFILE)
	./mvnw spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

logs: ## Affiche les logs des services Docker en live
	$(COMPOSE) logs -f

status: ## Affiche l'état des conteneurs
	$(COMPOSE) ps

psql: ## Ouvre un shell psql sur la base Docker (mdp: postgres)
	docker exec -it client-contract-postgres psql -U postgres -d client_contract_db