# Feature: Profiles and reviews

## Purpose

Expose public task history and ratings so participants can assess a DOER, and let an ASKER leave one Completion Review after successful work.

## Users and permissions

| Role | Can do | Cannot do |
| --- | --- | --- |
| ASKER | View public profiles and submit one Completion Review for the Primary Doer of their completed task | Review an incomplete task, a task without a Primary Doer, or the same completion twice |
| Primary Doer | View public profiles and receive Completion Reviews and drop reviews | Create their own Completion Review |
| HELPER | View public profiles | Receive a Completion Review for helping under the current scope |

## Workflow

After a claimed task reaches `COMPLETED`, its ASKER may submit one 1–5 Completion Review with optional written text for the Primary Doer. The review is attached to the task and appears on the Primary Doer's public profile. An ASKER may separately give one review to a DOER who dropped a task; received completion and drop ratings contribute to the profile's average rating.

## Integrations

- **Related features:** Task lifecycle and notifications.
- **Backend:** `AppUser`, `TaskReview`, `Task`, `TaskService`, `TaskController`, `UserService`, and `UserController`.
- **Frontend:** Public profile view, profile links, Completion Review form, and drop-review form in `frontend/src/App.tsx`.
- **Notifications / profiles / reviews:** The Primary Doer's completion creates an ASKER notification; reviews and task history are returned by the public profile endpoint.

## Acceptance criteria

- Given a completed task with a Primary Doer, when its ASKER submits a valid 1–5 Completion Review, then it is stored once and appears on the task and Primary Doer's public profile.
- Given a task that is not completed, when its ASKER attempts to submit a Completion Review, then the action is rejected.
- Given a completed task that already has a Completion Review, when its ASKER attempts another, then the action is rejected.
- Given a DOER who has received completion or drop reviews, when a public profile is viewed, then its task history, review details, and average rating are available.

## Decisions and open questions

- Decide whether an ASKER's identity should be shown with public written reviews.
- Decide whether HELPER contributions should be reviewable or reflected in profiles.
