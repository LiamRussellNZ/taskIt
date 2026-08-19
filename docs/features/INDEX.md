# Feature index

This directory records product intent and cross-feature decisions for TaskIt. It complements the implementation; source code, migrations, and tests define the behavior currently deployed.

Create one document per feature from `TEMPLATE.md`. Update it whenever the feature's user-visible behavior, authorization, or integration points change.

| Feature | Current scope | Key implementation areas |
| --- | --- | --- |
| Task lifecycle | Askers create, edit, and cancel tasks; eligible doers claim them; the asker or Primary Doer completes claimed tasks. | `backend/src/main/java/nz/taskit/domain/Task.java`, `TaskService.java`, `TaskController.java`, `frontend/src/App.tsx` |
| Assistance requests | A Primary Doer requests help on a claimed task, and another eligible doer can offer assistance. | `TaskAssistanceRequest.java`, `TaskService.java`, `TaskController.java` |
| Task questions | Doers ask questions on open tasks and only the asker answers them. | `TaskQuestion.java`, `TaskService.java`, `TaskController.java` |
| Notifications | Askers are notified about assistance requests, task questions, and task completion. | `UserNotification.java`, `UserNotificationService.java`, `UserController.java` |
| Profiles and reviews | Public profiles expose task history and ratings; askers may submit one completion review for a Primary Doer. | `AppUser.java`, `TaskReview.java`, `UserController.java`, `TaskController.java` |
| Private task chat | The asker and current Primary Doer exchange private messages while a task is claimed. | `private-task-chat.md`, `TaskChatRoom.java`, `TaskChatMessage.java`, `TaskService.java`, `TaskController.java`, `frontend/src/App.tsx` |

Refer to `README.md` and `CONTEXT.md` before expanding a feature. Add detailed documents here when work needs decisions beyond the current implementation.
