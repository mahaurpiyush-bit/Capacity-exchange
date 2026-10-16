# Surplus Capacity Exchange

A full-stack platform for listing, discovering, booking, and managing surplus operational capacity.

## Stack

- Backend: Java 17, Spring Boot, Spring Security, JWT, JPA
- Database: H2 for local development/tests, MySQL 8.4 for Docker deployment
- Frontend: React, Vite, Axios

## Local development

### Backend

```powershell
cd backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080/api`.

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs at `http://localhost:4173`.

On the default local profile, a demo provider and sample resource are created automatically:

- Email: `provider@test.com`
- Password: `secret123`

## Docker deployment

From the project root:

```powershell
docker compose up --build
```

The Docker frontend is available at `http://localhost:4173`, the API at
`http://localhost:8080/api`, and MySQL is exposed locally on port `3307`.

Set a strong `JWT_SECRET` before using the compose file outside development.

## Implemented flows

- JWT registration and login with role-based authorization
- Resource creation, update, discovery, and deactivation
- Capacity-aware booking overlap validation
- Provider approval/rejection and seeker/provider cancellation
- Simulated payment initiation and payment state transitions
- Completion, reviews, duplicate-review prevention, and disputes
- Provider and admin analytics summaries
- Admin dispute listing and resolution
