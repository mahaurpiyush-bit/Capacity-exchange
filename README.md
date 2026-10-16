Surplus Capacity Exchange

A full-stack web application that helps people and businesses discover, list, and book unused operational capacity. The platform connects providers who have available capacity with users who need it, while supporting bookings, status tracking, reviews, and dispute management.

Problem Statement

Available resources and operational capacity often go unused while others struggle to find suitable options. Surplus Capacity Exchange provides a centralized platform where providers can list available resources and users can discover and request bookings.

Technology Stack

- Frontend: React, Vite, Axios
- Backend: Java 17, Spring Boot, Spring Security
- Authentication: JWT-based authentication and role-based authorization
- Data Access: Spring Data JPA
- Database: H2 for local development and testing; MySQL 8.4 for Docker deployment
- Containerization: Docker and Docker Compose

Features

- User registration and login
- Role-based access for different user types
- Create, update, discover, and deactivate resource listings
- Capacity-aware booking validation to prevent overlapping reservations
- Booking approval, rejection, and cancellation workflows
- Simulated payment processing and payment status tracking
- Booking completion and reviews
- Duplicate-review prevention
- Dispute submission and resolution
- Provider and administrator analytics

Project Structure

Components/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml
├── .gitignore
└── README.md

Getting Started

Prerequisites

- Java 17
- Maven, or the project's Maven Wrapper if included
- Node.js and npm
- Docker Desktop, if running the Docker deployment

1. Clone the repository

git clone <YOUR_GITHUB_REPOSITORY_URL>
cd Components

Replace the placeholder URL with your repository URL.

2. Run the backend locally

Open a terminal:

cd backend
mvn spring-boot:run

The API is available at "http://localhost:8080/api".

3. Run the frontend locally

Open a second terminal from the project root:

cd frontend
npm install
npm run dev

Open the local URL printed by Vite in the terminal. The configured development server uses port "4173", if that port is available and configured in Vite.

Docker Deployment

From the project root, configure the required environment variables in a local ".env" file that is not committed to Git.

Set a strong, randomly generated "JWT_SECRET" and any database credentials required by the Compose configuration.

Then run:

docker compose up --build

The configured frontend and API ports are "4173" and "8080", respectively. MySQL is exposed locally on port "3307", according to the current Compose configuration.

Stop the containers with:

docker compose down

Demo Account

In the default local development profile, the application initializes a demo provider and sample resource.

- Email: "provider@test.com"
- Password: "secret123"

Use these credentials only in the local development environment. Do not use demo credentials in production.

Payments

Payment functionality is simulated. The application demonstrates payment initiation and status transitions; it does not process real financial transactions.

Security Notes

- Keep ".env" and other files containing secrets out of version control.
- Configure a strong JWT secret through an environment variable.
- Use appropriate database credentials and production-specific configuration before deployment.
- Never commit real passwords, API keys, or signing secrets.

Future Improvements

- Automated integration and end-to-end testing
- Production deployment and monitoring
- Integration with a real payment provider, with appropriate security controls

License

No license has been specified yet. Add a license if you decide to distribute the project under particular reuse terms.