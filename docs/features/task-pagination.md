# Feature: Task pagination

## Purpose

Keep every task board responsive by loading a selected number of matching tasks at a time.

## Users and permissions

| Role | Can do | Cannot do |
| --- | --- | --- |
| ASKER | Page through Open tasks and their own requests | View another ASKER's personal board |
| DOER | Page through Open tasks and their own work | View another DOER's personal board |
| HELPER | Page through tasks visible to the active DOER account | Gain task-state permissions through pagination |

## Workflow

Each board loads page one using the saved page size. The user can select 25, 50, or 75 tasks per page, then move between valid pages. Changing the active user, board, category filter, or page size returns to the first page.

## Integrations

- **Related features:** Task lifecycle.
- **Backend:** `TaskRepository`, `TaskService`, and `TaskController`; `GET /api/v2/tasks` returns page content and metadata while the existing unpaged `GET /api/tasks` endpoint remains unchanged.
- **Frontend:** `frontend/src/App.tsx`, `api.ts`, `types.ts`, and `styles.css`.
- **Notifications / profiles / reviews:** None.

## Acceptance criteria

- Given any board, when it loads, then the frontend requests 25, 50, or 75 matching tasks and displays the result count and current page.
- Given a user who selects a page size, when they refresh the browser, then that size remains selected.
- Given a board with more results, when a user selects Next or Previous, then the matching server page is displayed without navigating outside the available pages.
- Given a task mutation reduces the number of available pages, when the selected page no longer exists, then the board reloads the last valid page.
- Given an existing API client that calls `GET /api/tasks`, when pagination is introduced, then the original unpaged array response remains available.
- Given a client that calls `GET /api/v2/tasks`, when it requests a valid page, then it receives page content and pagination metadata.

## Decisions and open questions

- Page size is intentionally limited to 75 to avoid requesting excessively large task payloads.
