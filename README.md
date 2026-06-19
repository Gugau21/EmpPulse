# EmpPulse

Full-stack employee management app — a React + Vite frontend, a Spring Boot (Java 21)
backend, and a PostgreSQL database, served behind nginx via Docker Compose.

## Prerequisites

Only **Docker Engine with Compose v2** is required:

```bash
docker compose version   # should print v2.x
```

Install instructions: <https://docs.docker.com/engine/install/>

## Build & run

```bash
# 1. Get the code (clone, or unzip the archive and cd into the folder)
git clone <repo-url> EmpPulse
cd EmpPulse

# 2. Create the environment file (the committed defaults work out of the box)
cp .env.example .env

# 3. Build all images and start the full stack
docker compose --profile prod up --build      # add -d to run in the background
```

Then open **<http://localhost>**.

Sign in with the bootstrap Owner account from `.env` (`OWNER_EMAIL` / `OWNER_PASSWORD`,
default `owner@mail.com` / `pass`).

Stop everything:

```bash
docker compose --profile prod down            # add -v to also delete the database volume
```

> Outbound email is disabled by default (`MAIL_ENABLED=false`); the app runs fully
> without configuring SMTP.

## Development

Frontend linting/formatting, backend code formatting, and frontend hot-reload mode are
documented in **[`src/README.md`](src/README.md)**.
