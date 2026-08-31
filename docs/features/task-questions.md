# Feature: Task questions

## Purpose

Allow DOERs to clarify an open task before claiming it, with answers provided by the task's ASKER.

## Users and permissions

| Role | Can do | Cannot do |
| --- | --- | --- |
| ASKER | View and answer each question on their task once | Answer a question on another ASKER's task or change an existing answer |
| DOER | Ask a question on an open task and view questions and answers | Ask on a non-open task |
| HELPER | View the task's questions and answers | Gain separate question-authoring or answer permissions |

## Workflow

An eligible DOER asks a question while the task is `OPEN`. The question is retained with its author and timestamp, and the ASKER receives a notification. The ASKER answers the question once; the answer and timestamp then appear with the original question. Questions cannot be added once a task has been claimed.

## Integrations

- **Related features:** Task lifecycle and notifications.
- **Backend:** `TaskQuestion`, `Task`, `TaskService`, `TaskController`, and `TaskQuestionRepository`.
- **Frontend:** Question indicators on open-task cards and question/answer controls in task details.
- **Notifications / profiles / reviews:** Creating a question notifies the ASKER. Questions do not affect profiles or reviews.

## Acceptance criteria

- Given an open task, when a DOER asks a question, then the question identifies the DOER, is shown on the task, and notifies the ASKER.
- Given an unanswered question, when the task's ASKER answers it, then the answer is retained and visible with the question.
- Given a question, when a user other than the task's ASKER attempts to answer it, then the action is rejected.
- Given a claimed, completed, or cancelled task, when a DOER attempts to ask a question, then the action is rejected.

## Decisions and open questions

- Decide whether an ASKER can edit an answer or a DOER can withdraw a question before it is answered.
