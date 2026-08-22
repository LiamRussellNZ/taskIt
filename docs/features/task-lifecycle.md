# Feature: Task lifecycle

## Purpose

Enable an ASKER to publish a task and a DOER to carry it through assignment, completion, or withdrawal while keeping ownership and task state explicit.

## Users and permissions

| Role | Can do | Cannot do |
| --- | --- | --- |
| ASKER | Create, edit open tasks, cancel non-final tasks, request a status update on a claimed task, complete a claimed task, and review a drop or completion | Claim their own task or edit a claimed, completed, or cancelled task |
| DOER | Claim an open task, provide a requested status update, complete or drop their own claimed task | Claim their own task, claim a non-open task, or complete/drop a task assigned to another DOER |
| HELPER | View the task and offer assistance when an assistance request is open | Gain Primary Doer task-state controls |

## Workflow

An ASKER creates a task in `OPEN` state. An eligible DOER claims it, making that DOER the Primary Doer and moving it to `CLAIMED`. The ASKER or Primary Doer may complete a claimed task, moving it to `COMPLETED`; the ASKER may cancel any task that is not completed or already cancelled. A Primary Doer may instead drop a claimed task, which records the drop, clears assistance requests, returns the task to `OPEN`, and prevents that DOER from claiming it again.

## Integrations

- **Related features:** Assistance requests, notifications, profiles and reviews.
- **Backend:** `Task`, `TaskService`, `TaskController`, task repositories, status-update and drop entities. `GET /api/tasks/page` provides paged board results without changing the existing list endpoint.
- **Frontend:** Open, My tasks, and My work boards; task form, task-detail controls, and persisted 25/50/75 task-page selector in `frontend/src/App.tsx`.
- **Notifications / profiles / reviews:** Completion by the Primary Doer notifies the ASKER. Drops and completions can each be reviewed by the ASKER and contribute to profile ratings.

## Acceptance criteria

- Given an ASKER, when they create a task, then it appears in the open board with status `OPEN`.
- Given an open task and an eligible DOER who is not its ASKER or a prior dropper, when the DOER claims it, then the task is `CLAIMED` and that DOER is its Primary Doer.
- Given a claimed task, when its Primary Doer drops it, then it returns to `OPEN`, its assistance request is removed, and the same DOER cannot claim it again.
- Given a claimed task, when its ASKER or Primary Doer completes it, then it is `COMPLETED`; a Primary Doer's completion also notifies the ASKER.
- Given an open task, when its ASKER edits it, then its task details update; no other role or task state may be edited.
- Given any task board, when a user selects 25, 50, or 75 tasks per page, then that number is requested from the paged API and the selection is retained after a browser refresh.
- Given a board with more matching tasks than its page size, when a user selects Next or Previous, then the corresponding page is displayed and the controls do not navigate outside the result set.

## Decisions and open questions

- Minimum Doer Rating is canonical product language but is not yet persisted or enforced when claiming a task.
- Decide whether a cancelled task should remain visible in personal task history and public profiles.
