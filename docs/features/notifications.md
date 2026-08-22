# Feature: Notifications

## Purpose

Inform an ASKER about task events that require attention without exposing one user's notifications to another.

## Users and permissions

| Role | Can do | Cannot do |
| --- | --- | --- |
| ASKER | View their own notifications and navigate to the related task | View another user's notifications |
| DOER | Trigger notifications by requesting assistance, asking a question, or completing an assigned task | View an ASKER's notifications |
| HELPER | Trigger no dedicated notification events in the current scope | View another user's notifications |

## Workflow

The system creates a notification for the task's ASKER when the Primary Doer requests assistance, a DOER asks a question, or the Primary Doer completes the task. Notifications are returned newest first only to the recipient and link to the related task in the frontend.

## Integrations

- **Related features:** Task lifecycle, assistance requests, and task questions.
- **Backend:** `UserNotification`, `UserNotificationService`, `UserController`, `TaskService`, and `UserNotificationRepository`.
- **Frontend:** Notifications menu and notification panel in `frontend/src/App.tsx`.
- **Notifications / profiles / reviews:** This feature is the notification surface; completion notifications direct the ASKER to submit a Completion Review.

## Acceptance criteria

- Given a Primary Doer who requests assistance, when the request is created, then the task's ASKER has an assistance-request notification.
- Given a DOER who asks a question, when the question is created, then the task's ASKER has a question notification.
- Given a Primary Doer who completes a task, when completion succeeds, then the task's ASKER has a completion notification.
- Given a user, when they request another user's notifications, then the request is rejected.

## Decisions and open questions

- Notifications are currently retained and have no read state; decide whether to add read, dismissal, and retention behavior.
- Decide which future events, such as a HELPER offering assistance or a status-update response, should notify participants.
