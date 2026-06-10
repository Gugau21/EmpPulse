# EmpPulse

Full-stack employee management app: a React + Vite frontend, a Spring Boot (Java 21)
backend, and a PostgreSQL database, all served behind nginx via Docker Compose.

## Prerequisites

| Tool | Version | Used for |
|---|---|---|
| Docker + Compose v2 | any | running the whole stack |
| Node.js | 20+ | frontend lint / standalone dev (optional) |

The backend (Java 21 + Maven) is built **inside** its Docker image, so you do **not**
need a local JDK or Maven to run the project.

### Installing Docker + Compose v2 and Node.js

**Linux (Debian/Ubuntu):**

```bash
# Docker engine
sudo apt update && sudo apt install -y docker.io
sudo usermod -aG docker $USER   # then restart your terminal/session

# Docker Compose v2 (if `docker compose version` fails)
DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
mkdir -p $DOCKER_CONFIG/cli-plugins
curl -SL https://github.com/docker/compose/releases/download/v2.35.1/docker-compose-linux-x86_64 \
  -o $DOCKER_CONFIG/cli-plugins/docker-compose
chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose

# Node.js 20+ (via nvm)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
# restart your terminal, then:
nvm install 20
```

**Windows (PowerShell, via [winget](https://learn.microsoft.com/en-us/windows/package-manager/winget/)):**

```powershell
winget install Docker.DockerDesktop   # Compose v2 is bundled
winget install OpenJS.NodeJS.LTS
```

After installing Docker Desktop, **restart your PC**, launch Docker Desktop, and wait
for "Engine running" before running any `docker` commands.

Verify both are ready:

```bash
docker compose version   # should print v2.x
node --version           # should print v20.x or newer
```

> Note: `cp .env.example .env` only copies a config file — it does **not** install
> Docker or Node. Install those first with the commands above.

---

## 1. Install dependencies

Install frontend tooling (only needed if you want to lint or run the frontend
standalone — Docker installs them itself during the build):

```bash
cd frontend
npm install
cd ..
```

The backend has no local install step; Maven fetches its dependencies during the
Docker build.

---

## 2. Lint

Frontend only:

```bash
cd frontend
npm run lint          # ESLint    (JS/TS, check only)
npm run lint:fix      # ESLint    (JS/TS, auto-fix)
npm run lint:css      # stylelint (CSS,   check only)
npm run lint:css:fix  # stylelint (CSS,   auto-fix)
npm run lint:cpd      # jscpd     (duplicate-code detection; report only, no auto-fix)
npm run format        # Prettier  (all,   check only)
npm run format:fix    # Prettier  (all,   auto-fix)
cd ..
```

---

## 3. Build

Builds all images — the backend jar, the frontend production bundle, and the nginx
layer:

```bash
docker compose build
```

---

## 4. Start

```bash
docker compose up        # add -d to run in the background
```

App → http://localhost (nginx on port 80, proxies `/api` to the backend)

Build and start in one step:

```bash
docker compose up --build
```

Stop everything:

```bash
docker compose down
```

---

## Frontend-only dev mode (optional)

For hot-reload while working on the UI. Requires the backend running (e.g. via
`docker compose up backend db`); Vite proxies `/api` to `http://localhost:8080`.

```bash
cd frontend
npm run dev      # → http://localhost:5173
```
