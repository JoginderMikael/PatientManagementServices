# Northstar Health patient management frontend

React and TypeScript client for the patient portal and role-aware staff operations
application. Browser requests use the API gateway; the frontend never connects to
individual service ports or a database.

## Local setup

1. Copy `.env.example` to `.env.local` if the gateway does not run at the default
   `http://localhost:4004` address.
2. Install locked dependencies with `npm ci`.
3. Start the application with `npm start`.

The application does not include development credentials. Use a synthetic account
provided by the local backend seed configuration. Never use production credentials
or real patient information in local development.

## Verification

Run the Phase 1 gates before handing off changes:

```text
npm run typecheck
npm run test:ci
npm run build
```

Tests use Mock Service Worker and synthetic `.example.test` identities. They cover
login, session invalidation, role-filtered routes, cache clearing, gateway response
handling and basic accessibility checks.

## Structure

- `src/app` — provider and route composition
- `src/api` — typed gateway boundary and normalized errors
- `src/auth` — in-memory session lifecycle and route guards
- `src/components` — shared accessible layout, data, feedback, form and safety primitives
- `src/features` — feature-owned production routes
- `src/test` — synthetic factories, request handlers and provider-aware test utilities
- `src/styles` — semantic design tokens and responsive global styles
- `mockups` — interaction reference only; never imported into production code

See `ARCHITECTURE.txt`, `API.txt` and `skills.md` before adding workflows.

## Container

The production image builds the React application and serves it from a small
non-root Node process. Within Compose, same-origin `/auth` and `/api` requests
are proxied to `api-gateway`; browser code never calls a service container directly.

From the repository root:

```text
docker compose build patient-portal-frontend
docker compose up -d patient-portal-frontend
```

Open `http://localhost:8085`. Set `FRONTEND_PORT` in the root `.env` file to use a different host port.
