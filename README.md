# Aw Crud

A Spring Boot CRUD application with a "Wheel of Death" game mechanic. Users can register, view user lists, and spin a wheel where the "winner" faces deletion (marked as dead).

## Quick Start

### Prerequisites

- Ubuntu/Debian or similar
- Java 17
- Docker 24.x or newer
- Maven (for development)

### Production (Docker)

Run the application with a single command:

```bash
docker compose up -d
```

> The application will be available at `http://localhost:9797`

### Development

1. **Run the application**:

   ```bash
   cd crud
   ./mvnw spring-boot:run
   ```

    ***Note***:\
    No need to worry about starting the database.\
    It will start automatically via the `spring-boot-docker-compose` dependency.

1. **Access the application**:
   Navigate to `http://localhost:9797`

## Architecture

| Component | Technology | Purpose |
|-----------|------------|---------|
| `Application` | Spring Boot 4.0.2 (Java 17) | MVC API and web interface |
| `Database` | PostgreSQL with JPA/Hibernate | User data storage |
| `Frontend` | Thymeleaf templates with CSS/JavaScript | User interface |
| `Security` | Spring Security with login/register | Authentication and authorization |
| `Build` | Maven | Dependency management and building |
| `Game Mechanic` | Wheel of Death | Users can be "deleted" (marked as dead) |

## Game Mechanics

1. **Choosing your risk**:
   - Request a page of users of a varying size.
   - Your username is also be added to the wheel

1. **The Wheel of Death**:
   - Spin the wheel
   - The "winner" is marked as dead (soft delete)

## Configuration

### Environment Variables

The containers uses the following environment variables (defined in `.env`):

```bash
DB_NAME=aw_crud_db
DB_USERNAME=username
DB_PASSWORD=password
SERVER_EXTERNAL_PORT=9797
```

### Application Properties

Key configuration in `src/main/resources/application.properties`:

```properties
server.port=9797
spring.datasource.url=jdbc:postgresql://localhost:9898/aw_crud_db
spring.jpa.hibernate.ddl-auto=update
```

## Development

### Building the Application

```bash
cd crud
./mvnw clean package
```

### Running Tests

```bash
cd crud
./mvnw test
```

### Database Schema

The application uses JPA with automatic schema generation:

- `User`: Main user entity with username, password, and deletion status

## Main API Endpoints

- `GET /` - Home page (login/register)
- `GET /crud` - Main CRUD interface (requires authentication)
- `POST /crud/requestPage` - Request user page
- `POST /crud/spinWheel` - Spin the wheel of death

## Security

- Password requirements: 8+ characters with uppercase, lowercase, digit, and special character
- Username requirements: 3-32 alphanumeric characters
- Session-based authentication with `Spring Security`
