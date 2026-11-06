# Hotel Reservation

A full-stack hotel room reservation project with a Spring Boot REST API, PostgreSQL persistence, and a responsive React booking website. The backend API and frontend are deployed on Render.

| Service | URL |
| --- | --- |
| Live website | <https://hotel-reservation-frontend-f402.onrender.com> |
| Backend API | <https://hotel-reservation-84x6.onrender.com> |
| GitHub repository | <https://github.com/amehdiyev2141-ux/Hotel-Reservation> |

## Features

- Browse rooms returned by the live API, inspect room details, and search room availability by dates.
- Filter the listing by `SINGLE`, `DOUBLE`, and `SUITE` room types and sort by nightly price.
- Create guest records and reservations using the existing REST endpoints.
- Calculate reservation totals as nightly price multiplied by the number of nights.
- Prevent overlapping active reservations for the same room; cancelled reservations do not block availability.
- Cancel active reservations without physically deleting them.
- Validate guest fields, room fields, reservation dates, and request values.
- Show loading, error, and empty states in the frontend.
- Keep reservation IDs created in this browser in local storage. There are no accounts or authentication features.

The backend models rooms, guests, and reservations; it does not model hotel names, addresses, amenities, or destination inventory. Frontend hotel imagery and destination stories are editorial examples, not database data. Guest count is not part of the reservation request DTO, and payment is not implemented.

## Technologies

### Backend

- Java 21
- Spring Boot 4.1.2
- Spring Web MVC, Spring Data JPA, Hibernate, Bean Validation
- PostgreSQL
- Gradle Wrapper
- Lombok
- JUnit 5, Mockito, Spring Boot Test, MockMvc
- Docker

### Frontend

- JavaScript, React, Vite
- React Router
- Axios
- Responsive CSS

## Architecture and project structure

The repository root contains the Spring Boot API. The separate `hotel-reservation-frontend/` directory contains the React/Vite application. The frontend calls only endpoints implemented by the backend.

```text
.
├── Dockerfile
├── build.gradle
├── gradlew / gradlew.bat
├── src/
│   ├── main/java/com/example/hotelreservation/
│   │   ├── config/       # CORS configuration
│   │   ├── controller/   # Room, guest, reservation REST controllers
│   │   ├── dto/          # Reservation request and response records
│   │   ├── entity/       # RoomEntity, Guest, ReservationEntity and enums
│   │   ├── exception/    # Global REST exception handler
│   │   ├── repository/   # Spring Data JPA repositories
│   │   └── service/      # Reservation and room business rules
│   ├── main/resources/
│   │   ├── application.properties
│   │   ├── schema.sql
│   │   └── seed-sample-rooms.sql
│   └── test/java/com/example/hotelreservation/
└── hotel-reservation-frontend/
    ├── src/components/
    ├── src/pages/
    ├── src/services/    # Axios API calls and browser reservation ID storage
    ├── package.json
    └── vite.config.js
```

## Local setup

### Requirements

- Java 21
- Node.js supported by the installed Vite version and npm
- PostgreSQL 16 or compatible PostgreSQL
- Docker, if you want to run PostgreSQL in a container
- Git

### 1. Clone the repository

```powershell
git clone https://github.com/amehdiyev2141-ux/Hotel-Reservation.git
Set-Location Hotel-Reservation
```

### 2. Start a local PostgreSQL database (optional Docker method)

Use a local development password and keep it out of source control. This creates a new local container; it does not connect to or alter the production database.

```powershell
docker run --name hotel-postgres `
  -e POSTGRES_DB=hotel_db `
  -e POSTGRES_USER=hotel_user `
  -e POSTGRES_PASSWORD=replace-with-a-local-password `
  -p 5432:5432 `
  -d postgres:16
```

For a container that already exists, use `docker start hotel-postgres`. The project has no Docker Compose file.

### 3. Configure and run the backend

Set these variables in the PowerShell session that will run Gradle. Use credentials for your local database; never put production secrets in README or source files.

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/hotel_db"
$env:DB_USERNAME = "hotel_user"
$env:DB_PASSWORD = "replace-with-a-local-password"

.\gradlew.bat bootRun
```

The API listens on `http://localhost:8080` by default. JPA uses `spring.jpa.hibernate.ddl-auto=update`; it updates the schema without dropping the database. The password is supplied through `DB_PASSWORD`.

### 4. Run the frontend

Open another PowerShell window:

```powershell
Set-Location Hotel-Reservation\hotel-reservation-frontend
Copy-Item .env.example .env.local
npm install
npm run dev -- --host 0.0.0.0 --port 5173 --strictPort
```

Open <http://localhost:5173>. The `.env.example` sets `VITE_API_BASE_URL` to the live API. To use a local backend instead, set it in `.env.local`:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
```

Vite reads environment variables when it starts, so restart the dev server after changing `.env.local`.

## Environment variables

| Variable | Used by | Description |
| --- | --- | --- |
| `DB_URL` | Backend | JDBC URL; local default is `jdbc:postgresql://localhost:5432/hotel_db` |
| `DB_USERNAME` | Backend | PostgreSQL username; local default is `hotel_user` |
| `DB_PASSWORD` | Backend | PostgreSQL password; configure outside source control |
| `PORT` | Backend | HTTP port; defaults to `8080` |
| `VITE_API_BASE_URL` | Frontend build | Backend base URL; set to `https://hotel-reservation-84x6.onrender.com` in the Render Static Site environment |

## API

All endpoints are under `/api`. JSON dates use the `YYYY-MM-DD` format. The frontend does not use the global reservation listing endpoint as a user's private list: it stores its own created reservation IDs in this browser and loads them individually.

### Rooms

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/rooms` | List rooms |
| `POST` | `/api/rooms` | Create a room |
| `GET` | `/api/rooms/{id}` | Get a room |
| `DELETE` | `/api/rooms/{id}` | Delete a room |
| `GET` | `/api/rooms/available?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD` | Find available rooms |

### Guests

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/guests` | List guests |
| `POST` | `/api/guests` | Create a guest |
| `GET` | `/api/guests/{id}` | Get a guest |
| `DELETE` | `/api/guests/{id}` | Delete a guest |

### Reservations

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/reservations` | List reservation responses |
| `POST` | `/api/reservations` | Create a reservation |
| `GET` | `/api/reservations/{id}` | Get a reservation response |
| `PATCH` | `/api/reservations/{id}/cancel` | Cancel an active reservation |
| `DELETE` | `/api/reservations/{id}` | Physically delete a reservation |

### Example requests

Create a room (enum values are `SINGLE`, `DOUBLE`, and `SUITE`):

```json
{
  "number": 102,
  "type": "SINGLE",
  "pricePerNight": 95.00
}
```

Create a guest:

```json
{
  "fullName": "Aysel Mammadova",
  "email": "aysel@example.com",
  "phoneNumber": "+994501112233"
}
```

Create a reservation (`ReservationRequest`):

```json
{
  "roomId": 1,
  "guestId": 2,
  "checkIn": "2026-12-10",
  "checkOut": "2026-12-13"
}
```

The server calculates the price and status. A `ReservationResponse` is shaped like:

```json
{
  "id": 5,
  "roomId": 1,
  "roomNumber": 101,
  "guestId": 2,
  "guestName": "Aysel Mammadova",
  "checkIn": "2026-12-10",
  "checkOut": "2026-12-13",
  "totalPrice": 360.00,
  "status": "ACTIVE"
}
```

## Reservation rules, validation, and errors

- `checkIn` cannot be in the past, both for availability searches and reservation creation.
- `checkOut` must be later than `checkIn`.
- A reservation needs an existing room and guest.
- Guest name, email, and phone are required; email must be valid.
- Room number and type are required, and nightly price must be positive.
- Room types are persisted by enum name: `SINGLE`, `DOUBLE`, `SUITE`.
- Only overlapping `ACTIVE` reservations block a room. `CANCELLED` reservations do not.
- Total price is `nights × pricePerNight`; newly created reservations start as `ACTIVE`.
- Cancellation changes status to `CANCELLED`; the separate `DELETE` endpoint physically deletes a reservation.

Handled errors return a JSON object with `timestamp`, `status`, `error`, `message`, and `path`.

| Status | Typical case |
| --- | --- |
| `400 Bad Request` | Invalid fields, malformed dates, or invalid date range |
| `404 Not Found` | Room, guest, or reservation ID does not exist |
| `409 Conflict` | An active reservation overlaps the requested dates, or a cancelled reservation is cancelled again |

Example:

```json
{
  "timestamp": "2026-11-06T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Room is already booked for these dates",
  "path": "/api/reservations"
}
```

## Sample room data

`src/main/resources/seed-sample-rooms.sql` is a manual PostgreSQL script for rooms 102–105. It inserts only when that room number is absent, locks the room table for the transaction, and does not update or delete existing rooms or reservations. It is deliberately not named `data.sql` and is not automatically executed when the application starts. Start the backend once first so Hibernate has created `room_entity`, then run the script against the intended database.

For Render PostgreSQL, use the database's external connection details and a PostgreSQL client. Do not paste credentials into source control or commit a connection URL. For example, provide the host, port, database, and user interactively and let `psql` prompt for the password:

```powershell
psql -h "your-render-external-host" -p 5432 -U "your-database-user" -d "your-database-name" -W -v ON_ERROR_STOP=1 -f src/main/resources/seed-sample-rooms.sql
```

The script is safe to run again: numbers already present are skipped, so existing room values are left untouched. Verify the results with:

```sql
SELECT id, number, type, price_per_night
FROM room_entity
WHERE number IN (102, 103, 104, 105)
ORDER BY number;
```

## Tests and builds

Run the backend suite and clean build from the repository root:

```powershell
.\gradlew.bat test
.\gradlew.bat clean build
```

Mockito service tests and MockMvc controller tests do not require a real database. `AvailableRoomsTests` and `HotelReservationApplicationTests` load the Spring application and need the configured PostgreSQL datasource. Point `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` at a local disposable test database before running the full suite; do not use production data for tests.

Build the frontend from its directory:

```powershell
Set-Location hotel-reservation-frontend
npm install
npm run build
```

## Render deployment

The backend and frontend are already deployed on Render. The current deployment layout is:

### Backend — Render Web Service

- Root directory: repository root
- Runtime: Docker, using the root `Dockerfile`
- Java runtime: Java 21
- Configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the Render service environment. The container uses Render's `PORT` value and defaults to port `8080`.
- Do not commit or publish production credentials.

### Frontend — Render Static Site

- Root directory: `hotel-reservation-frontend`
- Build command: `npm install && npm run build`
- Publish directory: `dist`
- Environment variable: `VITE_API_BASE_URL=https://hotel-reservation-84x6.onrender.com`

Backend CORS allows the deployed website origin and local Vite origins `http://localhost:3000` and `http://localhost:5173`.

## License

No license file is currently included in the repository.
