# Feature: Assistance requests

## Purpose

Let a Primary Doer ask another eligible DOER to help with a claimed task without transferring responsibility for the task.

## Users and permissions

| Role | Can do | Cannot do |
| --- | --- | --- |
| ASKER | View the assistance request and receive its notification | Request or offer assistance for their own task |
| Primary Doer | Request one assistance request for their claimed task | Request assistance on an open, completed, or cancelled task |
| HELPER | Offer assistance for a claimed task with an unfilled request | Offer on their own task, on the Primary Doer's task, or after another HELPER has offered |

## Workflow

The Primary Doer requests assistance on a `CLAIMED` task, creating one request and notifying the ASKER. Another eligible DOER may offer assistance, becoming the HELPER. The task remains claimed by its Primary Doer, who retains all task-state controls. If the Primary Doer drops the task, its assistance request is removed.

## Integrations

- **Related features:** Task lifecycle, notifications, profiles and reviews.
- **Backend:** `TaskAssistanceRequest`, `Task`, `TaskService`, `TaskController`, and `TaskAssistanceRequestRepository`.
- **Frontend:** Assistance indicator on the open board and request/offer controls in task details.
- **Notifications / profiles / reviews:** Requesting assistance notifies the ASKER. A HELPER is displayed on the task but does not receive completion-review or task-state privileges.

## Acceptance criteria

- Given a claimed task, when its Primary Doer requests assistance, then one unfilled assistance request is created and the ASKER is notified.
- Given an unfilled assistance request, when an eligible DOER offers assistance, then that DOER is recorded as the HELPER and the Primary Doer remains in control.
- Given a task with an assistance request, when a second DOER attempts to offer assistance, then the offer is rejected.
- Given a task with an assistance request, when the Primary Doer drops it, then the task returns to open and the assistance request is removed.

## Decisions and open questions

- Define whether a Primary Doer can cancel an unfilled assistance request.
- Define whether a Primary Doer must accept or reject a HELPER's offer before it is active.
- Define whether a HELPER can withdraw an offer and whether My work should separately show offered and active helping work.
