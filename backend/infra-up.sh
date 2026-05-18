#!/bin/bash

# Sirf Infrastructure services ki list
INFRA_SERVICES="postgres-db pgadmin-web mongodb mongo-express redis-server redis-insight rabbitmq"

echo "------------------------------------------------------"
echo "🚀 Kitchen-AI: Starting Infrastructure Only..."
echo "------------------------------------------------------"

# Docker compose up command (Detached mode)
docker compose up -d $INFRA_SERVICES

echo ""
echo "------------------------------------------------------"
echo "📊 Services Status:"
echo "------------------------------------------------------"
docker compose ps $INFRA_SERVICES

echo ""
echo "🔗 Management UIs available at:"
echo "------------------------------------------------------"
echo "🐘 pgAdmin:       http://localhost:5050"
echo "🍃 Mongo Express: http://localhost:8082"
echo "🔴 Redis Insight: http://localhost:8001"
echo "🐇 RabbitMQ:      http://localhost:15672"
echo "------------------------------------------------------"
echo "✅ Infrastructure is live! Microservices are NOT started."