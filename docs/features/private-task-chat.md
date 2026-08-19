# Feature: Private task chat

## Purpose

Enable an asker and the current Primary Doer to coordinate on a claimed task through private, persisted plain-text messages.

## Users and permissions

| Role | Can do | Cannot do |
| --- | --- | --- |
| Asker | Read and send messages while their task is claimed. | Access the room after completion, cancellation, or a doer drop. |
| Primary Doer | Read and send messages while assigned to the claimed task. | Access a former room after dropping the task or another doer claims it. |
| Helper or other user | Nothing. | Read or send chat messages. |

## Workflow

A room is created when a doer claims a task. The asker and that Primary Doer may exchange messages while the task remains claimed. Each message creates an in-app notification for the other participant. The room closes immediately when the task is completed, cancelled, or dropped. A re-claim creates a new room, so a replacement Primary Doer cannot access the previous room.

The frontend refreshes an open chat every 15 seconds through the REST API. Real-time sockets, attachments, read status, editing, and deletion are out of scope.

## Integrations

- **Related features:** Task lifecycle and notifications.
- **Backend:** `TaskService`, `TaskController`, chat room/message domain entities, repositories, and Flyway migration `V3__add_task_chat.sql`.
- **Frontend:** Task detail panel, `api.ts`, `types.ts`, and `styles.css`.
- **Notifications / profiles / reviews:** Each message creates a `CHAT_MESSAGE` notification for its recipient. Profiles and reviews are unaffected.

## Acceptance criteria

- Given a claimed task, when its asker or Primary Doer sends a nonblank message, then the other participant receives a chat notification.
- Given any other user, when they request or post a task chat message, then the API rejects the request.
- Given a completed, cancelled, or dropped task, when either former participant requests its chat, then the API rejects the request.
- Given a dropped task is claimed by a replacement Primary Doer, when the replacement opens chat, then they cannot access the former Primary Doer's messages.

## Decisions and open questions

- Message history is retained for application records but is inaccessible after its room closes.
