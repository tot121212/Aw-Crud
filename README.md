# Aw Crud

A Spring Boot CRUD application with a "Wheel of Death" game mechanic. Users can register, view user lists, and spin a wheel where the "winner" faces deletion (marked as dead).

## Quick Start

### Prerequisites

- Ubuntu/Debian or similar
- Java 17
- Docker 24.x or newer
- Maven (for development)

### Running Production

```bash
docker compose -f prod.compose.yaml up -d
```

> The application will be available at `http://localhost:9797/`

**Note**: \
This uses `prod.compose.yaml`, which includes both the application and database services. \
The `CRUD/crud/compose.yaml` file is for development only and contains only the database.

### Running Development

1. **Run the application**:

   ```bash
   cd crud
   mvn spring-boot:run
   ```

    ***Note***:\
    No need to worry about starting the database.\
    It will start automatically via `spring-boot-docker-compose`.

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

The production containers uses the following environment variables (defined in `.env`):

```bash
DB_NAME=aw_crud_db
DB_USERNAME=username
DB_PASSWORD=password
SERVER_EXTERNAL_PORT=9797
```
Most of these dont need to be configured except the `SERVER_EXTERNAL_PORT` but only if it's in use.

### Application Properties

Dev configuration is in `src/main/resources/application.properties`:

## Development

### Running Tests

```bash
cd crud
mvn test
```

### Building the Application

```bash
cd crud
mvn clean package
```

### To rebuild the `Dockerfile`

```bash
docker build -t aw-crud-app:latest .
```
Then you can run with `docker compose -f prod.compose.yaml up -d`

### Database Schema

The application uses JPA with automatic schema generation:

- `User`: Main user entity with username, password, etc.

## Security

- Password requirements: 8+ characters with uppercase, lowercase, digit, and special character
- Username requirements: 3-32 alphanumeric characters
- Session-based authentication with `Spring Security`
