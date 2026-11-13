# Enterprise Railway AI (starter MVP)

A runnable Spring Boot starter demonstrating:
- PostgreSQL-backed trains, incidents, workflow, approvals, notifications and audit events
- Spring AI + Ollama for chat/tool calling
- PostgreSQL/pgvector RAG for policy documents
- MCP server tools for read-only railway lookups and policy search
- Multi-step notification workflow with approval before dispatch
- Mock notification delivery (does not send real email/SMS)

## Prerequisites
- Java 21
- Maven 3.9+
- Docker Desktop

## Start dependencies
```bash
docker compose up -d
docker exec -it railway-ollama ollama pull llama3.2:3b
docker exec -it railway-ollama ollama pull nomic-embed-text
```

## Run application
```bash
mvn spring-boot:run
```
App: http://localhost:8080  
Health: http://localhost:8080/actuator/health  
MCP endpoint is served by Spring AI MCP server configuration (Streamable HTTP/SSE transport according to the configured starter).

## Try it
Create an incident:
```bash
curl -X POST http://localhost:8080/api/incidents -H "Content-Type: application/json" -d "{\"trainId\":\"BNSF-7821\",\"delayMinutes\":45,\"cause\":\"TRACK_OBSTRUCTION\",\"description\":\"Track obstruction near milepost 42\"}"
```
Prepare workflow:
```bash
curl -X POST http://localhost:8080/api/notifications/workflows -H "Content-Type: application/json" -d "{\"incidentId\":1}"
```
Get workflow:
```bash
curl http://localhost:8080/api/notifications/workflows/1
```
Approve and dispatch:
```bash
curl -X POST http://localhost:8080/api/notifications/workflows/1/approve -H "Content-Type: application/json" -d "{\"approver\":\"ops-manager\"}"
```
Ask agent:
```bash
curl -X POST http://localhost:8080/api/agent/chat -H "Content-Type: application/json" -d "{\"message\":\"What is the policy for delays over 30 minutes?\"}"
```
Ingest a policy document:
```bash
curl -X POST http://localhost:8080/api/rag/ingest -H "Content-Type: application/json" -d "{\"title\":\"Delay Notification Policy\",\"content\":\"For delays of 30 minutes or more, notify the operations desk and affected customer contacts after the incident is verified. For uncertain impact, ask an operations manager to review before dispatch.\"}"
```
Search policy:
```bash
curl -X POST http://localhost:8080/api/rag/search -H "Content-Type: application/json" -d "{\"query\":\"delay of 30 minutes notification\",\"topK\":4}"
```


