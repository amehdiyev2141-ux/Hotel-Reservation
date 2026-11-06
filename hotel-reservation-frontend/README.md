# Serein — Hotel Reservation Frontend

A responsive React frontend for the Hotel Reservation API. This project uses Vite, JavaScript, React Router, and Axios. It lives separately from the Spring Boot backend.

## Run locally

Requirements: Node.js 20.19+ or 22.12+ and npm.

From PowerShell:

```powershell
Set-Location hotel-reservation-frontend
Copy-Item .env.example .env.local
npm install
npm run dev -- --host 0.0.0.0 --port 5173 --strictPort
```

Open `http://localhost:5173`. The `.env.example` configures `VITE_API_BASE_URL` to the provided Render API. Vite reads `.env.local` when it starts; restart the dev server after changing it.

Build and preview the production bundle:

```powershell
npm run build
npm run preview
```

## Backend behavior and scope

API calls are kept in `src/services/api.js`. The UI uses only the backend routes that exist: room listing/details/availability, guest creation, reservation creation/details, and reservation cancellation.

The backend stores rooms rather than hotel names, addresses, or destination records. Room cards therefore use API room fields, while the hotel imagery and destination stories are representative editorial content and are labelled as such. Guest count is a planning-only form value because the reservation request DTO does not accept it. The API has no accounts or per-guest reservation listing; this UI stores reservation IDs created in this browser and fetches each through the existing `GET /api/reservations/{id}` endpoint. This is not authentication.

The reservation service first creates a guest record and then submits a reservation with that guest ID. If the second request fails, the guest record may already exist on the backend; retrying unchanged details in the same open booking form reuses that guest ID. No payment functionality is implemented.

If the Render service is sleeping or unavailable, the UI displays its loading and error states and allows retrying.
