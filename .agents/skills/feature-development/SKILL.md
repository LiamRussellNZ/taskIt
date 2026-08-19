---
name: feature-development
description: Develop TaskIt features with product context, cross-feature impact analysis, and focused clarification questions.
---

# TaskIt feature development

Use this skill when planning, refining, or implementing a TaskIt product feature.

## Read first

1. Read `README.md` for the current application behavior and terminology.
2. Read `CONTEXT.md` for the canonical product language.
3. Read `docs/features/INDEX.md` to identify related features.
4. Read each relevant document under `docs/features/`.
5. Inspect the relevant implementation before treating documentation as complete or current.

Documentation provides product intent; the code, database migrations, and tests are the source of truth for implemented behavior.

## Clarify product fit

Before implementing, identify how the request affects:

- roles and permissions: `ASKER`, `DOER`, and `HELPER`;
- task lifecycle and allowed state transitions;
- API contracts, persistence, and Flyway migrations;
- notifications, public profiles, ratings, and completion reviews;
- frontend boards, task controls, and user-visible terminology;
- existing tests and the feature documentation.

Ask one focused question only when the answer changes user-visible behavior, authorization, data retention, or the implementation approach. State the relevant existing behavior and offer concrete choices where possible. Do not ask questions that code or existing feature documentation can answer.

## Implement

1. Summarize the intended behavior as concise, observable acceptance criteria before changing code.
2. Preserve the terminology in `CONTEXT.md`.
3. Update the backend, frontend, tests, and migrations required by the feature; do not expose controls that the API does not authorize.
4. Update the relevant feature document in the same change. Create one from `docs/features/TEMPLATE.md` if none exists.
5. Update `docs/features/INDEX.md` when a feature is added, renamed, or materially re-scoped.

## Feature-document standards

Keep documents brief and decision-oriented. Record user intent, supported roles, workflow and states, integration points, authorization rules, acceptance criteria, and open decisions. Link to source files rather than copying API schemas or implementation details that will drift.
