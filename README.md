> Note: this README.md file has been generated with Claude Code.
# user-service

User management microservice for the **Library Management System** — a portfolio project demonstrating a production-style microservice architecture across multiple languages and frameworks.

> The full system includes an auth-service (TypeScript/Node.js), catalog-service (Java/Spring Boot), loan-service (Kotlin/Spring Boot), notification-service (TypeScript/Node.js), and an API gateway (Spring Cloud Gateway). Each service owns its own database and communicates asynchronously via RabbitMQ or synchronously over REST.

## Overview

The user-service is the central authority for user accounts and roles in the system. It manages the full lifecycle of user accounts — creation, updates, deactivation, and deletion — and is the entry point for the invite-based onboarding flow. It is the primary event producer for the auth-service, which relies on these events to create credentials and issue invite tokens.

Role-based access is enforced at the service layer. The service trusts caller identity from headers injected by the API gateway (`X-User-Id`, `X-User-Role`) rather than validating JWTs directly.

## Features

- **User CRUD** — create, read, update, and delete user accounts with role and status tracking
- **Role enforcement** — hierarchical role checks ensure admins can only manage accounts at or below their own role level
- **Invite flow** — publishes a `user.created` event on account creation; the auth-service responds by generating and distributing an invite token
- **Invite resend** — revokes and regenerates an invite for a pending user on request
- **Deactivation** — explicit admin action to deactivate an account without deleting it
- **Role synchronisation** — publishes `user.role_updated` so the auth-service can keep future JWTs up to date
- **Event-driven status tracking** — consumes `auth.invite_token_generated` from the auth-service to confirm the invite was issued

## Tech Stack

- **Runtime:** Kotlin, Java 21
- **Framework:** Spring Boot 4
- **Database:** PostgreSQL (Spring Data JPA, Flyway migrations)
- **Messaging:** RabbitMQ (Spring AMQP)
- **Validation:** Jakarta Bean Validation
- **Containerisation:** Docker (multi-stage Maven build, non-root user)

## API

All endpoints require `X-User-Id` (UUID) and `X-User-Role` (role string) headers, injected by the API gateway after JWT validation.

| Method | Path | Roles | Description |
|---|---|---|---|
| `GET` | `/users` | `librarian`, `access-admin`, `super-admin` | List users; optional `role` and `status` query params |
| `GET` | `/users/{id}` | `access-admin`, `super-admin`, or self | Get a user by ID |
| `POST` | `/users` | `access-admin`, `super-admin` | Create a user (triggers invite flow) |
| `PATCH` | `/users/{id}` | `access-admin`, `super-admin` | Update name or role |
| `DELETE` | `/users/{id}` | `access-admin`, `super-admin` | Delete a user |
| `POST` | `/users/{id}/deactivate` | `access-admin`, `super-admin` | Deactivate a user account |
| `POST` | `/users/{id}/resend-invite` | `access-admin`, `super-admin` | Re-send the invite for a pending user |
| `GET` | `/health` | — | Actuator health check |

**Create user request**
```
POST /users
Content-Type: application/json
X-User-Id: <uuid>
X-User-Role: super-admin

{ "email": "user@example.com", "firstName": "Jane", "lastName": "Doe", "role": "member" }
```

**Update user request**
```
PATCH /users/{id}
Content-Type: application/json
X-User-Id: <uuid>
X-User-Role: access-admin

{ "firstName": "Jane", "role": "librarian" }
```
All fields are optional; omitted fields are left unchanged. Email cannot be updated.

### Role constraints

- `super-admin` can manage all roles including other `super-admin` accounts
- `access-admin` can manage `member`, `librarian`, and `access-admin` accounts
- Users cannot modify or delete their own account

## Messaging

The service publishes to the `user-service.events` RabbitMQ exchange and consumes from `auth-service.events`. Each queue has a corresponding dead-letter queue (DLQ) for messages that fail processing.

| Event published | Trigger | Description |
|---|---|---|
| `user.created` | `POST /users` | Signals the auth-service to create a credential record and issue an invite token |
| `user.invite_resent` | `POST /users/{id}/resend-invite` | Signals the auth-service to revoke the current invite token and issue a new one |
| `user.role_updated` | `PATCH /users/{id}` (role change only) | Signals the auth-service to update the role on the credential record |
| `user.deleted` | `DELETE /users/{id}` | Signals the auth-service to delete the credential record |

| Event consumed | Action |
|---|---|
| `auth.invite_token_generated` | Confirms the auth-service acknowledged the user creation; user remains `pending` until they set their password |

Messages that cannot be processed are nacked with `requeue=false` and routed to the DLQ for manual inspection.

## Running Locally

The included `docker-compose.yml` starts the full stack: PostgreSQL (with both `user_service` and `auth_service` databases), RabbitMQ, the auth-service (built from the sibling `../auth-service` repo), and the user-service.

```bash
docker compose up --build
```

The auth-service repository must be present at the same directory level as this repository.

The user-service will be available at `http://localhost:8080`. The RabbitMQ management UI is available at `http://localhost:15672` (guest / guest).

Database migrations run automatically on startup via Flyway.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/user_service` | JDBC URL for PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `SPRING_RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `SPRING_RABBITMQ_PORT` | `5672` | RabbitMQ port |
| `SPRING_RABBITMQ_USERNAME` | `guest` | RabbitMQ user |
| `SPRING_RABBITMQ_PASSWORD` | `guest` | RabbitMQ password |
| `RABBITMQ_PREFETCH` | `10` | Per-consumer prefetch count |