# Hotel Reservation API

A REST API for managing hotel rooms, guests, and reservations. The application is built with Spring Boot and persists data in PostgreSQL through Spring Data JPA.

## Project Overview

The API provides CRUD operations for rooms, guests, and reservations, plus room availability search and reservation cancellation. Reservation creation accepts room and guest IDs, checks availability, calculates the total price, and returns a response DTO.

This repository contains a backend API only. It does not include a frontend, authentication, login/register flows, or JWT security.

## Features

- **Room management:** Create, list, find, and delete rooms.
- **Guest management:** Create, list, find, and delete guests.
- **Reservation management:** Create, list, find, and delete reservations.
- **Available room search:** Find rooms without an overlapping active reservation for a requested date range.
- **Reservation cancellation:** Change an active reservation's status to `CANCELLED`; cancellation does not physically delete it.
- **Automatic total price calculation:** Multiply the nightly room price by the number of nights.
- **Date validation:** Check-out must be after check-in. The availability search also rejects check-in dates in the past.
- **Reservation overlap prevention:** An active reservation cannot overlap another active reservation for the same room.
- **Request validation:** Validate required room fields, guest fields and email format, and reservation request fields.
- **Global exception handling:** Return a consistent JSON error body for handled exceptions.
- **Local development CORS:** Allow the configured origins `http://localhost:3000` and `http://localhost:5173` on `/api/**`.

## Technologies

| Technology | Use in this project |
| --- | --- |
| Java 21 | Java toolchain configured in Gradle |
| Spring Boot 4.1.2 | Application framework |
| Spring Data JPA | Repository and persistence abstraction |
| Hibernate | JPA provider managed by Spring Boot |
| PostgreSQL | Application database and runtime JDBC driver |
| Gradle Wrapper | Build and run tasks |
| Lombok | Entity accessors and constructor generation |
| JUnit 5 | Automated tests |
| Mockito | Repository and service mocks in unit and MVC-slice tests |
| Spring Boot Test / MockMvc | Application context and HTTP controller tests |
| Docker | Multi-stage Java 21 container image for Render or local use |

## Project Structure

```text
.
|-- build.gradle
|-- Dockerfile
|-- .dockerignore
|-- gradlew
|-- gradlew.bat
|-- settings.gradle
|-- gradle/wrapper/
|   |-- gradle-wrapper.jar
|   `-- gradle-wrapper.properties
|-- README.md
`-- src/
    |-- main/
    |   |-- java/com/example/hotelreservation/
    |   |   |-- HotelReservationApplication.java
    |   |   |-- config/
    |   |   |   `-- CorsConfig.java
    |   |   |-- controller/
    |   |   |   |-- GuestController.java
    |   |   |   |-- ReservationController.java
    |   |   |   `-- RoomController.java
    |   |   |-- dto/
    |   |   |   |-- ReservationRequest.java
    |   |   |   `-- ReservationResponse.java
    |   |   |-- entity/
    |   |   |   |-- Guest.java
    |   |   |   |-- ReservationEntity.java
    |   |   |   |-- ReservationStatus.java
    |   |   |   |-- RoomEntity.java
    |   |   |   `-- RoomType.java
    |   |   |-- exception/
    |   |   |   `-- GlobalExceptionHandler.java
    |   |   |-- repository/
    |   |   |   |-- GuestRepository.java
    |   |   |   |-- ReservationRepository.java
    |   |   |   `-- RoomRepository.java
    |   |   `-- service/
    |   |       |-- GuestService.java
    |   |       |-- ReservationService.java
    |   |       `-- RoomService.java
    |   `-- resources/
    |       |-- application.properties
    |       `-- schema.sql
    `-- test/java/com/example/hotelreservation/
        |-- AvailableRoomsTests.java
        |-- ControllerIntegrationTests.java
        |-- GuestServiceTests.java
        |-- HotelReservationApplicationTests.java
        |-- ReservationCancellationTests.java
        |-- ReservationServiceTests.java
        `-- RoomServiceTests.java
```

## Database Configuration

The application uses PostgreSQL. The current local datasource settings are in `src/main/resources/application.properties`; Spring environment variables can override them. For production, configure connection details and credentials through the deployment environment, and do not commit real passwords or secrets.

The application reads database settings from these environment variables:

| Variable | Example |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/hotel_db` |
| `DB_USERNAME` | `hotel_user` |
| `DB_PASSWORD` | `replace-with-a-local-password` |

The URL and username have local-development defaults in `application.properties`; set all three variables explicitly in deployment. The password has no default. The examples above are placeholders for local development. Use the same database name, user, and password when starting PostgreSQL and the application.

### Start PostgreSQL with Docker (optional)

There is no Docker Compose configuration in the repository. To run a local PostgreSQL container manually, use a development-only password and replace it with your own value:

```powershell
docker run --name hotel-reservation-postgres `
  -e POSTGRES_DB=hotel_db `
  -e POSTGRES_USER=hotel_user `
  -e POSTGRES_PASSWORD=replace-with-a-local-password `
  -p 5432:5432 `
  -d postgres:16
```

To stop the container:

```powershell
docker stop hotel-reservation-postgres
```

To start the existing container again:

```powershell
docker start hotel-reservation-postgres
```

Hibernate is configured with `ddl-auto=update`. SQL initialization is deferred until after Hibernate initializes the schema. `schema.sql` updates the reservation status constraint and converts legacy `CANCELED` values to `CANCELLED` when the application starts.

## Installation and Setup

### Prerequisites

- Java 21
- Git
- PostgreSQL 16 or another compatible PostgreSQL version
- Docker only if you plan to use the optional container command above

### 1. Clone the repository

Replace the example repository URL with the actual URL for your Git remote:

```powershell
git clone https://github.com/OWNER/REPOSITORY.git
Set-Location REPOSITORY
```

### 2. Start PostgreSQL

Start a local PostgreSQL server or use the optional Docker command above. Ensure the database, username, and password match the application settings you will provide.

### 3. Configure the datasource

In the same PowerShell session used to run Gradle, set the datasource variables. Update the URL, username, and password for your local database:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/hotel_db"
$env:DB_USERNAME = "hotel_user"
$env:DB_PASSWORD = "replace-with-a-local-password"
```

### 4. Build the application

```powershell
.\gradlew.bat clean build
```

### 5. Start Spring Boot

```powershell
.\gradlew.bat bootRun
```

The API is available at `http://localhost:8080` by default.

## API Documentation

All endpoints are rooted at `/api`. Reservation and guest endpoints use the DTO/entity fields described below. No `PUT` endpoint is currently implemented.

### Rooms

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/rooms` | List all rooms |
| `POST` | `/api/rooms` | Create a room |
| `GET` | `/api/rooms/{id}` | Get a room by ID |
| `DELETE` | `/api/rooms/{id}` | Delete a room |
| `GET` | `/api/rooms/available?checkIn={date}&checkOut={date}` | List rooms available for the date range |

Availability dates use ISO format (`YYYY-MM-DD`).

### Guests

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/guests` | List all guests |
| `POST` | `/api/guests` | Create a guest |
| `GET` | `/api/guests/{id}` | Get a guest by ID |
| `DELETE` | `/api/guests/{id}` | Delete a guest |

### Reservations

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/reservations` | List reservations as `ReservationResponse` objects |
| `POST` | `/api/reservations` | Create a reservation from a `ReservationRequest` |
| `GET` | `/api/reservations/{id}` | Get one reservation as a `ReservationResponse` |
| `DELETE` | `/api/reservations/{id}` | Physically delete a reservation |
| `PATCH` | `/api/reservations/{id}/cancel` | Change an active reservation's status to `CANCELLED` |

## Request Examples

### Create a room

`RoomType` accepts `SINGLE`, `DOUBLE`, or `SUITE`.

```json
{
  "number": 101,
  "type": "DOUBLE",
  "pricePerNight": 125.00
}
```

### Create a guest

```json
{
  "fullName": "Aysel Mammadova",
  "email": "aysel@example.com",
  "phoneNumber": "+994501112233"
}
```

### Create a reservation

The request contains IDs and dates; the server loads the room and guest and calculates `totalPrice` and `status`.

```json
{
  "roomId": 1,
  "guestId": 2,
  "checkIn": "2026-12-10",
  "checkOut": "2026-12-13"
}
```

A reservation response uses this shape:

```json
{
  "id": 5,
  "roomId": 1,
  "roomNumber": 101,
  "guestId": 2,
  "guestName": "Aysel Mammadova",
  "checkIn": "2026-12-10",
  "checkOut": "2026-12-13",
  "totalPrice": 375.00,
  "status": "ACTIVE"
}
```

## Business Rules and Validation

- Check-out must be later than check-in.
- The available-room search rejects a check-in date earlier than today. Reservation creation itself currently validates the date order but does not reject a past check-in date.
- A room cannot have overlapping reservations with `ACTIVE` status. The overlap check uses `existing.checkIn < requested.checkOut` and `existing.checkOut > requested.checkIn`, so a reservation ending on the requested check-in date does not overlap.
- `CANCELLED` reservations are excluded from the availability query and do not block a new reservation.
- Total price is the number of nights multiplied by the room's `pricePerNight`.
- New reservations receive `ACTIVE` status.
- Cancellation changes status to `CANCELLED`; it does not physically delete the reservation. The separate `DELETE` endpoint physically deletes it.
- Room validation requires `number`, `type`, and a positive `pricePerNight`.
- Guest validation requires `fullName`, `email`, and `phoneNumber`; email must have a valid format.
- Reservation requests require `roomId`, `guestId`, `checkIn`, and `checkOut`.

## Error Handling

Handled errors use the following status codes:

| Status | Meaning |
| --- | --- |
| `400 Bad Request` | Invalid room/guest/reservation request fields or invalid reservation dates |
| `404 Not Found` | Room, guest, or reservation was not found |
| `409 Conflict` | The room is already booked for overlapping dates, or a cancelled reservation is cancelled again |

Error responses contain `timestamp`, `status`, `error`, `message`, and `path`:

```json
{
  "timestamp": "2026-11-06T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Room is already booked for these dates",
  "path": "/api/reservations"
}
```

## Testing

The current test report contains **63 passing tests**. This is a snapshot of the suite at the time this README was prepared; run the command below for the current result after tests change.

- **Service unit tests** (`RoomServiceTests`, `GuestServiceTests`, `ReservationServiceTests`, and `ReservationCancellationTests`) use Mockito repository mocks and do not use a database.
- **Controller HTTP tests** (`ControllerIntegrationTests`) use Spring Boot's `@WebMvcTest` and MockMvc, with service beans mocked. They verify routing, JSON serialization, validation, and exception responses without connecting to a database.
- **Application/availability tests** (`HotelReservationApplicationTests` and `AvailableRoomsTests`) load the Spring application and JPA repositories. They use the datasource configured for the test run; the project currently has no H2 dependency, so run them with a reachable PostgreSQL database. The optional Docker PostgreSQL instructions above can provide one locally.

Run all tests and the build from Windows PowerShell:

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to a reachable PostgreSQL test database first. The application-context and availability tests use the configured datasource.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

## Deployment (Render)

The repository now includes a multi-stage `Dockerfile` for a Java 21 image. Deployment is **not completed**. Connect the Git repository to a Render Web Service using the Docker runtime, then provide `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the service's environment settings. The container reads Render's `PORT` variable and defaults to port 8080 when it is absent. Do not commit production secrets. Render's dashboard applies environment variable changes to the selected service and deploys them when requested ([Render environment variables](https://render.com/docs/configure-environment-variables)); see also [Render Docker deployments](https://render.com/docs/docker).

No Render service URL is listed because deployment has not been completed.

## License

No license file is present in this repository. No license is declared here; add a license file if the project owner chooses to publish one.
