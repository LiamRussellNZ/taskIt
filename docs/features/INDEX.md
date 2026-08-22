# Feature index

This directory records product intent and cross-feature decisions for TaskIt. It complements the implementation; source code, migrations, and tests define the behavior currently deployed.

Create one document per feature from `TEMPLATE.md`. Update it whenever the feature's user-visible behavior, authorization, or integration points change.

| Feature | Current scope | Key implementation areas |
| --- | --- | --- |
| Task lifecycle | Askers create, edit, and cancel tasks; eligible doers claim them; the asker or Primary Doer completes claimed tasks. | `backend/src/main/java/nz/taskit/domain/Task.java`, `backend/src/main/java/nz/taskit/service/TaskService.java`, `backend/src/main/java/nz/taskit/web/TaskController.java`, `frontend/src/App.tsx` |
| Assistance requests | A Primary Doer requests help on a claimed task, and another eligible doer can offer assistance. | `backend/src/main/java/nz/taskit/domain/TaskAssistanceRequest.java`, `backend/src/main/java/nz/taskit/service/TaskService.java`, `backend/src/main/java/nz/taskit/web/TaskController.java` |
| Task questions | Doers ask questions on open tasks and only the asker answers them. | `backend/src/main/java/nz/taskit/domain/TaskQuestion.java`, `backend/src/main/java/nz/taskit/service/TaskService.java`, `backend/src/main/java/nz/taskit/web/TaskController.java` |
| Notifications | Askers are notified about assistance requests, task questions, and task completion. | `backend/src/main/java/nz/taskit/domain/UserNotification.java`, `backend/src/main/java/nz/taskit/service/UserNotificationService.java`, `backend/src/main/java/nz/taskit/web/UserController.java` |
| Profiles and reviews | Public profiles expose task history and ratings; askers may submit one completion review for a Primary Doer. | `backend/src/main/java/nz/taskit/domain/AppUser.java`, `backend/src/main/java/nz/taskit/domain/TaskReview.java`, `backend/src/main/java/nz/taskit/web/UserController.java`, `backend/src/main/java/nz/taskit/web/TaskController.java` |

Refer to `README.md` and `CONTEXT.md` before expanding a feature. Add detailed documents here when work needs decisions beyond the current implementation.
