# Serein — Hotel Reservation Frontend

Serein is the responsive React/Vite website for the Hotel Reservation API. The live site is deployed as a Render Static Site and uses the deployed Spring Boot backend.

- Website: <https://hotel-reservation-frontend-f402.onrender.com>
- Backend API: <https://hotel-reservation-84x6.onrender.com>
- Repository: <https://github.com/amehdiyev2141-ux/Hotel-Reservation>

## Features

- Browse live room inventory, room details, and date-based availability.
- Filter rooms by the backend enum values `SINGLE`, `DOUBLE`, and `SUITE`.
- Create guest and reservation records through the existing API.
- View reservation IDs created in this browser and cancel active reservations.
- Loading, retryable error, and empty states.

The API models rooms, not hotels or destinations. Room and destination images are representative editorial photography. There are no user accounts, authentication, payment processing, or guest-count field in the reservation API. Reservation IDs are stored in this browser only.

## Local development

Requirements: Node.js and npm.

From PowerShell:

```powershell
Copy-Item .env.example .env.local
npm install
npm run dev -- --host 0.0.0.0 --port 5173 --strictPort
```

Open <http://localhost:5173>. Vite loads `VITE_API_BASE_URL` at startup. The example file targets the deployed API; to use a local backend, set this in `.env.local` and restart Vite:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
```

Build and preview:

```powershell
npm run build
npm run preview
```

## API integration

Axios requests are maintained in `src/services/api.js`. The frontend uses existing backend endpoints for room listing, room details, availability search, guest creation/details, reservation creation/details, and cancellation. The “My Reservations” screen reads IDs from local storage and calls the individual reservation endpoint; the backend does not provide a per-user history endpoint or authentication.

The reservation API creates a guest and then a reservation. If reservation creation fails after the guest was stored, the guest record remains. Retrying in the same open form with unchanged guest details reuses that created guest ID.

## Render Static Site configuration

- Root directory: `hotel-reservation-frontend`
- Build command: `npm install && npm run build`
- Publish directory: `dist`
- Environment variable: `VITE_API_BASE_URL=https://hotel-reservation-84x6.onrender.com`

The backend CORS allowlist includes this deployed website origin and the local origins `http://localhost:3000` and `http://localhost:5173`.
