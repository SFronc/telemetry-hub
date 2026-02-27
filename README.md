## Overview

TelemetryHub is a comptact but production-oriented Java system form collecting, storing and exposing telemetry data from multiple clients or devices (agents). In practice, it is a simplified version of the kind of backend used in monitoring and observability platform - for example, systems that gather CPU, memory, temperature, or other operational metrics. 

## What Problem It Solves

In real-world software environments, there are often many data sources - servers, containers, applications, or IoT devices - that contiuously send operational metrics. This creates server practical engineering challenges:

- Many clients may connect at the same time, so the server must handle concurrent traffic efficiently.
- Incoming data must be validated, authenticated, and protected against malformed or oversized request.
- Telemetry must be stored reliably and later retrieved quickly by device and time range.
- The system should remain stable even when dependencies, such as the database, become temporarily unreliable.
- Other tools and services should be able to integrate with it throigh a clear API.

TelemetryHub addresses these needs by acing as a central ingestion and query hub for telemetry data.

## What the Project Does

### 1. Accepts telemetry from clients through two channels

TelemetryHub allows data ingestion in two ways:

- **TCP (Java NIO / Reactor pattern):** clients maintain a connection and send 'INGEST' messages using a framed protocol (4-byte length prefix followed by JSON).
- **HTTP:** clients can also submit a telemetry through 'POST /ingest' with a JSON payload.

### 2. Validates and authenticates requests

This system use a shared authentication token:

- For TCP, the token is included in the 'authToken' field of the 'MessageEnvelope'.
- FOr HTTP, the token is sent in the 'Authorization: Bearer <token>' header.

If the token is missing or invalid, the request is rejected with an unauthorized response.

### 3. Stores telemetry in a database

Telemetry is persisted through **JDBC** into an **H2** database. Each record includes:

- 'deviceId'
- 'timestamp'
- a JSON map of metrics

This allows the system to keep durable history of telemetry readings and later query them efficiently.

### 4. Exposes telemetry and operational data through HTTP

The project provides several HTTP endpoints:

- 'GET /health' 0 returns whether the service is alive
- 'GET /metrics' - returns internal service counters such as ingested messages, errors, and active connections
- 'GET /devices/{id}/readings?...' - returns telemetry readings for a given device within a selected time range
- 'POST /ingest' - accepts telemetry over HTTP

### 5. Handles infrastructure failures in a controlled way

The repository layer is wrapped with resilience mechanism:

- **RETRY** with exponential backoff and jitter
- **Circuit Breaker** with 'CLOSED', 'OPEN', and 'HALF_OPEN' states

If the database starts failing repeatedly, the system avoids endlessly retrying expensive operations and instead returns a fast 'SERVICE_UNAVAILABLE' response.
