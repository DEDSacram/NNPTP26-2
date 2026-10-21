# Contributing to TaskPulse

Thank you for contributing to TaskPulse.

## Prerequisites
- Java 21 JDK
- Maven 3.9+ (or use the included `./mvnw` wrapper)

## Building and Testing
Run the full build and test suite:
```bash
./mvnw clean verify
```

## Branch and Commit Guidelines
- Create a dedicated branch for each issue or task: `feature/<name>`, `fix/<name>`, or `refactor/<name>`.
- Keep changes minimal, focused, and well-scoped.
- Do not introduce unrelated modifications or commented-out code.

## Submitting Pull Requests
- Open a pull request against the `main` branch.
- Complete the pull request checklist.
- Ensure all CI workflow checks pass.
- Every bug fix PR must include at least one regression test demonstrating the defect and verifying its resolution.
