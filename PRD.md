# Product Requirements Document: TaskPulse

**Product:** TaskPulse — CLI Task & Time Tracker
**Document status:** Proposal for coursework
**Target platform:** Java 21 command-line application
**Development model:** One shared GitHub repository; at least two separate pull requests per student

> This document defines the intended product and teaching requirements. It does not describe an already implemented application. The starter repository deliberately implements only part of the target behavior.

### 1. Product Overview

TaskPulse is a local command-line application for managing projects, tracking tasks, and recording time spent on work.

Users can create projects, organize tasks with priorities and labels, move tasks through a defined lifecycle, log work, and generate reports. Application data is persisted locally in CSV files.

TaskPulse also serves as a collaborative software-engineering exercise. Students contribute to the same Java codebase through small, independently reviewable pull requests covering bug fixes, refactoring, features, and tests.

### 2. Goals and Success Criteria

#### 2.1 Product goals

- Provide useful task and time tracking without a server or database.
- Separate domain rules, application services, persistence, and CLI interaction.
- Preserve task data across application restarts.
- Make core behavior deterministic and straightforward to test.
- Support incremental development without requiring major architectural changes.

#### 2.2 Educational goals

Students should practice:

- Understanding and extending an unfamiliar Java project.
- Identifying defects and writing regression tests.
- Refactoring without changing observable behavior.
- Implementing features with acceptance criteria.
- Working with Maven, JUnit 5, and automated CI.
- Creating focused pull requests and responding to review feedback.
- Collaborating in one repository without repeatedly modifying the same files.

#### 2.3 Success criteria

| Area | Acceptance measure |
|---|---|
| Contributions | Every student submits at least two separate, substantive PRs. |
| Reproducibility | A fresh checkout builds and runs tests using Maven with Java 21. |
| CI | Every PR and push to `main` triggers automated compilation and tests. |
| Correctness | Completed features satisfy their acceptance criteria and include relevant tests. |
| Persistence | Supported data survives a save/load round trip without information loss. |
| Usability | Invalid commands produce actionable errors rather than uncaught exceptions. |
| Maintainability | CLI, persistence, and business rules remain independently testable. |

### 3. Users and Primary Use Cases

| User | Needs |
|---|---|
| Application user | Organize tasks, record work, inspect deadlines, and export summaries. |
| Student contributor | Find a bounded issue, understand expected behavior, and validate a change locally. |
| Reviewer or instructor | Review focused changes and verify them through tests and CI. |

**Primary workflow:**

1. Create a project.
2. Add tasks with priorities, estimates, and optional deadlines.
3. Start a task.
4. Record work.
5. Block, resume, or complete the task.
6. Inspect project progress and export a report.
7. Close and reopen the application without losing data.

### 4. Scope

#### 4.1 Target coursework release

- Project and task management.
- Task lifecycle validation.
- Labels and priorities.
- Manual work logging.
- Filtering and sorting.
- Local CSV persistence.
- Markdown and JSON report export.
- Command-line interface.
- Automated tests and GitHub Actions CI.

These are target capabilities, not requirements for the initial skeleton.

#### 4.2 Out of scope

- Web or graphical interface.
- Authentication, authorization, or user accounts.
- Concurrent access by multiple running application instances.
- Cloud synchronization or external issue-tracker integration.
- Database server.
- Automatic background time tracking.
- Sprints, dependencies, recurring tasks, and subtasks.
- Production deployment infrastructure.

A worklog author is descriptive text, not an authenticated identity.

### 5. Domain Model

#### 5.1 Project

A project groups related tasks.

| Attribute | Requirement |
|---|---|
| Identifier | Unique, stable UUID. |
| Name | Required; must not be blank. |
| Description | Optional text. |

Project names need not be unique; commands identify projects by UUID.

#### 5.2 Task

| Attribute | Requirement |
|---|---|
| Identifier | Unique, stable UUID. |
| Project | Exactly one existing project. |
| Title | Required; must not be blank. |
| Description | Optional text. |
| Priority | `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL`; default `MEDIUM`. |
| State | `TODO`, `IN_PROGRESS`, `BLOCKED`, or `DONE`; default `TODO`. |
| Estimated minutes | Optional non-negative whole number. |
| Due date | Optional `LocalDate`. |
| Labels | Non-null set; initially empty. |
| Worklogs | Non-null collection; initially empty. |

**Rules:**

- A missing estimate is different from an estimate of zero.
- A task cannot exist without a project.
- Collections must never be exposed in a way that bypasses domain validation.
- Identifiers do not change when tasks are edited or saved.

#### 5.3 Label

For the initial implementation, labels are normalized strings rather than separate entities.

- Leading and trailing whitespace is removed.
- Blank labels are rejected.
- Labels are normalized to lowercase using locale-independent rules.
- Adding an existing normalized label has no effect.
- Removing a label that is not present has no effect.

**Example:** ` Backend ` and `backend` represent the same label.

#### 5.4 Worklog

A worklog is an immutable record of time spent on a task.

| Attribute | Requirement |
|---|---|
| Timestamp | Required `LocalDateTime`. |
| Duration | Positive whole number of minutes. |
| Author | Required non-blank text. |
| Description | Optional text. |

Worklogs are append-only in the target release. Editing and deleting worklogs are outside the initial scope.

Timestamps use local application time; cross-time-zone collaboration is out of scope.

### 6. Functional Requirements

#### FR-01 — Project management

The application shall allow users to:

- Create a project.
- List projects.
- View project details.
- Edit a project’s name and description.
- Delete an empty project.

**Acceptance criteria:**

- Blank project names are rejected.
- Unknown project identifiers produce a clear error.
- Deleting a project containing tasks is rejected.
- A failed operation leaves existing data unchanged.

#### FR-02 — Task management

The application shall allow users to:

- Create a task inside an existing project.
- View and list tasks.
- Edit title, description, priority, estimate, and due date.
- Clear an optional estimate or due date.
- Delete a task after explicit confirmation.
- Add and remove labels.

**Acceptance criteria:**

- New tasks start in `TODO`.
- Blank titles and negative estimates are rejected.
- Invalid dates are rejected.
- Cancelling deletion changes nothing.
- Confirmed task deletion also removes its labels and worklogs.
- Changing state uses the transition rules in FR-03, not a general-purpose edit operation.

#### FR-03 — Task lifecycle

Only the following state transitions are supported:

| Current state | Allowed next state | Additional condition |
|---|---|---|
| `TODO` | `IN_PROGRESS` | None |
| `IN_PROGRESS` | `BLOCKED` | None |
| `IN_PROGRESS` | `DONE` | At least one valid worklog exists |
| `BLOCKED` | `IN_PROGRESS` | None |
| `DONE` | `TODO` | Reopen task; preserve existing worklogs |

**Acceptance criteria:**

- All transitions not listed above are rejected.
- A transition to the current state is rejected with an explanatory message.
- Rejected transitions do not mutate the task.
- Reopening preserves the task’s estimate, labels, due date, and worklogs.
- CLI operations cannot bypass transition validation.

**Examples:**

- `TODO` → `DONE`: rejected.
- `IN_PROGRESS` → `DONE` without worklogs: rejected.
- `IN_PROGRESS` → `DONE` with a positive-duration worklog: accepted.
- `BLOCKED` → `DONE`: rejected.

#### FR-04 — Time tracking

The application shall allow users to:

- Add a worklog to an `IN_PROGRESS` task.
- List a task’s worklogs.
- View total recorded minutes.
- Compare actual effort with the estimate.

**Acceptance criteria:**

- Zero and negative durations are rejected.
- Blank authors are rejected.
- Worklogs cannot be added to `TODO`, `BLOCKED`, or `DONE` tasks.
- Tasks without worklogs report zero recorded minutes.
- Totals use an accumulation type that avoids ordinary integer overflow.
- A missing estimate is displayed as “not estimated,” not zero.

**Calculation definitions:**

$$
\text{actualMinutes} = \sum \text{worklog.durationMinutes}
$$

$$
\text{varianceMinutes} = \text{actualMinutes} - \text{estimatedMinutes}
$$

A positive variance means the task exceeded its estimate. Variance is unavailable when no estimate exists.

#### FR-05 — Search and filtering

Users shall be able to filter tasks by:

- Project.
- State.
- Priority.
- Label.
- Case-insensitive text in title or description.
- Inclusive due-date range.
- Overdue status.

**Rules:**

- Different filter criteria combine using logical AND.
- Text search uses substring matching.
- Tasks without a due date do not match due-date filters.
- An overdue task has a due date before today and is not `DONE`.
- A task due today is not overdue.

**Acceptance criteria:**

- Filtering does not mutate stored tasks.
- No matches produce an empty result, not an error.
- An inverted date range is rejected.
- Date-sensitive tests use a controlled clock rather than depending on the actual day.

#### FR-06 — Sorting

Supported sort orders shall include:

- Title ascending.
- Priority descending.
- Due date ascending.
- Priority descending, then due date ascending.

**Rules:**

- Priority order is `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`.
- Missing due dates sort after dated tasks.
- Title sorting is case-insensitive.
- Equal sort values are resolved by identifier to provide deterministic output.
- Default ordering is title ascending, then identifier.

#### FR-07 — CSV persistence

The application shall persist projects, tasks, labels, and worklogs in a configurable local data directory.

The default location is `.taskpulse` relative to the working directory.

**Requirements:**

- Use UTF-8.
- Use a documented, versioned CSV format.
- Preserve UUIDs, optional values, and relationships.
- Support commas, double quotes, Unicode, and line breaks in text fields.
- Represent dates and timestamps using documented ISO formats.
- Validate loaded records before replacing current in-memory state.
- Reject duplicate identifiers and references to missing projects or tasks.

**Acceptance criteria:**

- Saving and loading reconstructs equivalent application data.
- A missing data directory initializes an empty workspace.
- Malformed or unsupported data produces an actionable error.
- Failed loading does not partially replace the current workspace.
- Corrupt data is not silently discarded or overwritten.
- Temporary storage locations are used in tests.

**Persistence lifecycle:**

- The workspace loads at application startup.
- Successful mutating operations are persisted before success is reported.
- Read-only commands do not modify storage.
- A failed save is reported as a failure, not a successful operation.

Atomic replacement of the complete workspace and crash recovery may be separate extension issues; they must not be claimed as supported until implemented and tested.

#### FR-08 — Reports

The application shall export project summaries as Markdown and JSON.

A report shall contain:

- Project name and identifier.
- Task count by state.
- Overdue task count.
- Total recorded minutes.
- Task details: identifier, title, state, priority, due date, estimate, and recorded minutes.

**Acceptance criteria:**

- JSON output is valid JSON and can be parsed by a standard parser.
- Markdown tables escape content that would break their structure.
- Unestimated tasks remain distinguishable from zero estimates.
- Empty projects produce valid reports with zero totals.
- Export does not mutate application data.
- Existing output files are not overwritten without explicit permission.

#### FR-09 — Command-line interface

The CLI shall provide commands for project management, task management, transitions, labels, worklogs, filtering, and export.

Illustrative command structure:

```text
project add "Programming course"
project list
task add <project-id> "Implement CSV escaping"
task list --state TODO
task transition <task-id> IN_PROGRESS
worklog log <task-id> 45 --author "Student"
task transition <task-id> DONE
report export <project-id> --format markdown
```

Exact command grammar shall be documented before implementation.

**Acceptance criteria:**

- Help describes available commands and required arguments.
- Quoted text arguments are supported.
- Unknown commands, missing arguments, invalid UUIDs, and invalid enum values produce useful messages.
- Expected validation errors do not print stack traces.
- EOF exits an interactive session cleanly.
- CLI parsing can be tested without manually typing into a terminal.

### 7. Architecture and Technical Requirements

#### 7.1 Technology

| Area | Requirement |
|---|---|
| Language | Java 21 |
| Build | Maven |
| Tests | JUnit 5, including parameterized tests where appropriate |
| Coverage | JaCoCo |
| CI | GitHub Actions |
| Storage | Local CSV files |
| Interface | Command-line application |

Dependency and plugin versions shall be pinned in the Maven configuration.

A maintained CSV or JSON library may be introduced through an approved dependency change. Building a general-purpose parser is not a learning requirement.

#### 7.2 Responsibility boundaries

| Layer | Responsibility |
|---|---|
| Domain | Entities, invariants, and value validation |
| Services | Use cases, transitions, queries, and time calculations |
| Storage | Serialization, loading, saving, and storage errors |
| CLI | Parsing commands and presenting results |

**Constraints:**

- Domain classes must not read terminal input or access files.
- CLI code must not duplicate business rules.
- Persistence must not silently correct invalid domain data.
- Time-dependent behavior must support an injectable `Clock`.
- Avoid unnecessary frameworks and abstractions.

### 8. Starter Repository and Intentional Gaps

The instructor-provided skeleton shall be small, buildable, and suitable for incremental contributions.

#### 8.1 Initial working baseline

- Maven configuration for Java 21.
- Domain structure and a minimal executable entry point.
- At least one working end-to-end in-memory example.
- Initial tests that pass.
- GitHub Actions workflow.
- Contribution instructions and PR template.
- A backlog distinguishing defects, refactoring, features, and tests.

#### 8.2 Candidate intentional defects

| Area | Starter defect | Target behavior |
|---|---|---|
| Labels | Collection not initialized | Labels are always usable and non-null |
| Transitions | Invalid state changes accepted | Only FR-03 transitions succeed |
| CSV | Concatenation and naive field splitting | Lossless handling of supported text |
| Search | Duplicate case-sensitive search loops | Shared, consistently defined query behavior |
| Estimates | Missing estimate treated as zero | Missing and zero remain distinct |

These are proposed teaching defects, not claims about existing source code.

#### 8.3 Candidate incomplete features

- Combined filtering.
- Multi-level sorting.
- Estimate-versus-actual reporting.
- Markdown exporter.
- JSON exporter.
- Interactive command processing.
- Robust persistence validation.

**Teaching constraint:** The initial CI must pass. Defects may remain uncovered in the baseline, but a bug-fix PR must introduce a regression test. Tests that intentionally fail must not be merged into `main`.

### 9. Contribution Requirements

Each student shall submit at least **two separate, substantive pull requests**.

Recommended structure:

| PR | Suggested contribution |
|---|---|
| PR 1 | A bounded bug fix or refactoring with regression or characterization tests |
| PR 2 | A feature or meaningful testing improvement with documented acceptance criteria |

Other combinations may be approved by the instructor. The two PRs must not be an artificial split of one trivial change.

#### 9.1 Required PR contents

Every PR shall include:

- A linked issue.
- A short explanation of the problem and solution.
- Relevant automated tests.
- Documentation updates when observable behavior changes.
- Passing CI.
- No unrelated formatting or dependency changes.

For a bug fix, the new regression test should fail against the previous implementation and pass with the fix.

#### 9.2 Shared-repository coordination

- Students claim issues before implementation.
- Issues identify scope, acceptance criteria, and dependencies.
- Contributions should be independently mergeable where practical.
- Shared API changes require coordination.
- Instructor-approved interfaces should be established before parallel storage, CLI, and exporter work.
- Conflicts should be resolved without dropping previously merged behavior.

The backlog must be sized to the actual class. A small set of broad features is insufficient if many students each need two meaningful PRs.

### 10. Testing Requirements

| Area | Minimum scenarios |
|---|---|
| Domain validation | Blank titles, invalid estimates, empty collections |
| Lifecycle | Every source/target state combination; completion with and without worklogs |
| Worklogs | Positive, zero, and negative durations; invalid author; state restrictions |
| Queries | Combined filters, no matches, missing due dates, case handling |
| Sorting | Priority order, missing dates, deterministic ties |
| Persistence | Round trips, quotes, commas, newlines, Unicode, malformed records |
| Reports | Empty data, missing estimates, escaping, valid JSON |
| CLI | Valid commands, missing arguments, unknown identifiers, EOF |

**Additional requirements:**

- Tests must not depend on network access.
- Tests must not write to the real user workspace.
- Tests must not depend on execution order.
- Core business behavior should be tested without launching the CLI.
- Real file I/O tests should use temporary directories rather than mocking all filesystem behavior.

Coverage reports shall be generated from the start. The previously proposed **70% line and branch coverage threshold** is a target for the completed coursework release, not an immediate gate for an intentionally incomplete skeleton.

Coverage does not replace behavioral assertions.

### 11. CI/CD Requirements

GitHub Actions shall run on:

- Every pull request targeting `main`.
- Every push to `main`.

The required validation command is:

```bash
mvn -B clean verify
```

The pipeline shall:

1. Set up Java 21.
2. Compile production and test code.
3. Run automated tests.
4. Generate coverage reports.
5. Package the application.
6. Publish available test and coverage reports as workflow artifacts.

**Repository controls:**

- Require successful CI before merging.
- Require instructor or designated reviewer approval.
- Re-run checks after changes to a PR.

For this local CLI project, automated validation is mandatory. Automated publishing of a runnable JAR on release tags is optional; production deployment is outside scope.

### 12. Non-Functional Requirements

| Area | Requirement |
|---|---|
| Portability | No hardcoded operating-system-specific paths or separators |
| Determinism | Stable ordering and controlled time in tests |
| Reliability | No silent data loss or success messages after failed persistence |
| Maintainability | Focused classes and explicit responsibility boundaries |
| Usability | Actionable messages for expected errors |
| Safety | Explicit confirmation for destructive operations |
| Testability | Business logic independent of terminal and real workspace |
| Privacy | Example data contains no real personal or confidential information |

### 13. Delivery Milestones

| Milestone | Deliverable |
|---|---|
| M1 — Teaching baseline | Buildable skeleton, passing tests, CI, contribution guide, initial issues |
| M2 — Core correctness | Validated domain, transitions, labels, and time tracking |
| M3 — Persistence | Documented CSV format and tested save/load behavior |
| M4 — User workflows | CLI, filtering, sorting, and exporters |
| M5 — Coursework release | Integrated acceptance tests, documentation, coverage gate, packaged application |

Milestones describe dependencies, not fixed dates. Independent issues may proceed in parallel.

### 14. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Too few tasks for the class | Prepare a class-sized backlog with bounded defects, features, and test improvements |
| Frequent merge conflicts | Separate responsibilities and avoid assigning many students to the entry point |
| Ambiguous behavior | Treat the acceptance criteria in this PRD as the target specification |
| Starter bugs block all work | Keep a passing baseline and isolate defects from unrelated use cases |
| CSV implementation dominates the course | Permit a maintained parser library; focus on integration and behavior |
| Coverage encourages superficial tests | Review assertions, edge cases, and regression value |
| Scope expands into a full issue tracker | Keep authentication, web UI, synchronization, and advanced planning out of scope |

### 15. Definition of Done

A contribution is complete when:

- Its issue’s acceptance criteria are satisfied.
- It follows the agreed domain rules and architectural boundaries.
- Relevant automated tests pass locally and in CI.
- Public behavior changes are documented.
- It introduces no unrelated changes.
- Review feedback is resolved.

The coursework release is complete when all in-scope requirements are implemented, the documented workflow succeeds from a fresh checkout, and the repository contains the required student contributions.