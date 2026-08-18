# TaskIt

TaskIt is an API-first task board for people requesting and doing local or remote tasks. It has a Spring Boot API and a
React/Vite client.

## Prerequisites

- Java 21 and Maven 3.9+
- Node.js 20+ and npm

## Run on Windows

Start PostgreSQL in Docker from the repository root:

```powershell
docker compose up -d postgres
```

Open two PowerShell windows from the repository root:

```powershell
cd backend
mvn spring-boot:run
```

```powershell
cd frontend
npm install
npm run dev
```

Open the Vite URL printed by the second command (normally `http://localhost:5173`). The frontend proxies `/api` to the
backend at `http://localhost:8080`.

PostgreSQL data is retained in the `taskit-postgres-data` Docker volume. To remove the local database entirely, run
`docker compose down -v`. Copy `.env.example` to `.env` to change the local PostgreSQL database, user, password, or host
port; `.env` is intentionally not committed.

## Run containerized

The PostgreSQL and application containers have independent lifecycles. Start PostgreSQL first:

```powershell
docker compose up -d postgres
```

Then build and run the application container:

```powershell
docker compose up --build app
```

Open `http://localhost:8080` to use the frontend and API. The application container serves the Vite production build and
connects to PostgreSQL through the internal Compose network. Stop the application with `Ctrl+C`; PostgreSQL continues
running. Use `docker compose up -d --build app` to run the application in the background, then view its logs with
`docker compose logs -f app`.

## API and development identity

The interactive API documentation is available at `http://localhost:8080/swagger-ui/index.html`.

Create a user with `POST /api/users`, then use its numeric ID for the mandatory `X-User-Id` header on task-changing
endpoints:

```powershell
curl.exe -X POST http://localhost:8080/api/tasks `
  -H "Content-Type: application/json" `
  -H "X-User-Id: 1" `
  -d "{\"title\":\"Move a sofa\",\"description\":\"Need help Saturday morning\",\"category\":\"Moving\",\"location\":\"Wellington\",\"remote\":false}"
```

An `ASKER` may create, edit, and cancel their own tasks. A `DOER` may claim open tasks. Either the task asker or its
assigned doer may complete a claimed task.

Claimed doers can request assistance through `POST /api/tasks/{id}/assistance-requests`. The task then appears on the
Open tasks board as claimed and needing assistance; another eligible doer can use
`POST /api/tasks/{id}/assistance-requests/offer`. Helpers do not gain task-state controls.

Doers can post questions on open tasks with `POST /api/tasks/{id}/questions`; only the asker can answer with
`POST /api/tasks/{id}/questions/{questionId}/answer`. Assistance requests and questions notify the asker. Fetch a user's
unread notifications only as that user with `GET /api/users/{id}/notifications` and `X-User-Id: {id}`. Public profile
data, including ratings, reviews, and task history, is available from `GET /api/users/{id}/profile`; the frontend links
user names and the active-user menu to it.

When an assigned doer completes a task, the asker receives a notification and may submit one 1–5 review through
`POST /api/tasks/{id}/completion-review`. Completed-task reviews determine a doer's average received rating.

## Verify

Docker must be running because backend integration tests use a disposable PostgreSQL Testcontainers instance.

```powershell
cd backend
mvn test

cd ..\frontend
npm install
npm run typecheck
npm run build
```

## Future development

Ideas for future development. Think how to implement a feature funnel.
How to know a feature can be picked up for Copilot or another agent or even a human developer to implement.

| Idea                                                                                                                      | Status |
|---------------------------------------------------------------------------------------------------------------------------|--------|
| Containerize the Spring API and frontend for consistent local and cloud deployments.                                      | Done   |
| If user is DOER then post a task button should not display on interface                                                   | To do  |
| Feature development/ fleshing out a feature skill. Think about current features and how it should affect them. Think BDD. | To do  |
| Implement a chat feature. So I can chat with everyone involved with a task. this should be a seperate chat room           | To do  |
| Introduce API versioning when a breaking API change is needed.                                                            | To do  |
| Deploy separate test and production environments on cost-conscious AWS or Azure infrastructure.                           | To do  |
| Replace the development `X-User-Id` header with production authentication and authorization.                              | To do  |
| Add CI/CD, managed secrets, monitoring, backups, and tested database restores for cloud deployments.                      | To do  |

### Flesh out features more.

#### Feature: Assistance requests

- DOER: I want a way to cancel assistance requests.
- DOER: Respond to accept assistance requests.
- DOER: Respond to accept reject requests.
- HELPER: On my work tab I want a section for tasks I am helping with. And all tasks that I have offered to help with.
- HELPER: I want to be able to cancel my offer to help.



