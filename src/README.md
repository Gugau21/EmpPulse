# EmpPulse — Development

Tooling for working on the code. To just build and run the app, see the
[root README](../README.md).

Layout: [`frontend/`](frontend) (React + Vite), [`backend/`](backend) (Spring Boot,
Java 21 + Maven), [`nginx/`](nginx) (reverse-proxy config).

## Frontend (`src/frontend`)

Requires **Node.js 20+**. Install dependencies once:

```bash
cd src/frontend
npm install
```

Lint & format:

```bash
npm run lint           # ESLint            (check)
npm run lint:fix       # ESLint            (auto-fix)
npm run lint:css       # stylelint (CSS)   (check)
npm run lint:css:fix   # stylelint (CSS)   (auto-fix)
npm run lint:cpd       # jscpd duplicate-code report
npm run format         # Prettier          (check)
npm run format:fix     # Prettier          (auto-fix)
```

### Hot-reload dev server

Vite proxies `/api` to the backend on `localhost:8080`, so run the backend (via the
`dev` profile, which publishes port 8080) and the database first:

```bash
# from the repo root:
docker compose --profile dev up backend-dev db

# then, in src/frontend:
npm run dev            # → http://localhost:5173
```

## Backend (`src/backend`)

Java 21 + Maven. It is built inside Docker, so a local JDK/Maven is **not** required to
run the app — only to work on the backend directly.

Format Java sources with google-java-format (run from `src/`):

```bash
cd src
bash backend/format.sh
```

Run the test suite (requires a local JDK 21 + Maven):

```bash
cd src/backend
mvn test
```
