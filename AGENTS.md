# AGENTS — Repo Guide for AI Coding Agents

Purpose: concise, actionable guidance to quickly make safe, small changes in this repo.

1) Big picture
- Backend: Spring Boot Java app in `src/main/java/com/resume` (controllers, service, dto, repository, mapper). Entry: `ResumeApplication.java`.
- Frontend: Vue 3 app in `frontend/src` (pages, components, `frontend/package.json`, `vite.config.js`). UI calls backend via `frontend/src/api/*.js` (e.g. `ai.js`, `resume.js`).
- DB: Flyway-style migrations in `src/main/resources/db/migration/V*.sql` (V1..V3 present). Runtime config in `src/main/resources/application.yml`.
- Artifacts: build outputs under `target/` (don't edit compiled classes).

Data flow (concrete): frontend -> `frontend/src/api/ai.js` -> backend `AiController` -> services -> repositories -> DB (migrations in `db/migration`). Resume templates live in `frontend/src/pages/Template` and persist to template-related tables updated by migrations (see `V2__update_template_table.sql`).

2) Critical developer workflows (Windows)
- Install/build backend: use provided scripts or Maven:
  - Setup maven/JDK: `setup-maven.bat` and `setup-java17.bat` / `fix-path.bat` exist for Windows convenience.
  - Compile: run `compile.bat` or run `mvn -DskipTests package` from repo root.
  - Run backend: `start-backend.bat` or `start-backend-with-jdk17.bat` (use the JDK17 script if you need Java 17).
  - Run DB scripts manually: `run-sql.bat` (uses scripts in project root).
- Frontend: `cd frontend`; `npm install`; `npm run dev` (Vite dev server defined in `frontend/package.json`).
- Common: `create-table.bat`, `run-sql.bat` and other .bat files exist for DB and environment setup—prefer using them on Windows to mirror maintainer workflows.

3) Project-specific conventions & patterns
- Controllers/services/DTOs: backend uses layered organization: `controller` -> `service` -> `repository/mapper`. DTOs live in `dto/` (e.g. ApiResponse, Page classes in `src/main/java/com/resume/dto`).
- SQL versioning: schema migrations use sequential `V{n}__desc.sql` files under `src/main/resources/db/migration`. Always add a new `V{next}__*.sql` rather than editing old migrations for history safety.
- Frontend modules: pages grouped under `frontend/src/pages/*` (e.g. `Resume`, `Template`, `AI`); small components under `frontend/src/components` and API wrappers under `frontend/src/api`.
- Build artifacts: do not commit changes inside `target/` — edit source and rebuild.

4) Integration points & external dependencies
- AI integration: frontend wrapper `frontend/src/api/ai.js` and backend `AiController` (class in compiled `target/classes` and source under `src/main/java/.../controller`) — check `application.yml` for keys. Do NOT commit API keys; use env or application override.
- DB: Data access via mappers and repositories; migrations are Flyway-style SQL files in `src/main/resources/db/migration`.
- Packaging: `pom.xml` contains project dependencies and build lifecycle; frontend has its own `frontend/package.json`.

5) Where to change behavior (examples)
- API surface: edit or add controllers in `src/main/java/com/resume/controller` and corresponding services in `.../service`.
- Business logic: `.../service` and `.../mapper` (MyBatis or repository layer) — modify tests and add new DTOs in `.../dto` as needed.
- UI: edit `frontend/src/pages/*` and `frontend/src/components/*`; adjust API calls in `frontend/src/api/*.js`.
- DB schema: add `src/main/resources/db/migration/V4__your_change.sql` (increment the version number).

6) Safe change & PR guidance for AI agents
- Make small, focused PRs (single concern). Include:
  - Source change + tests (if applicable) + DB migration file when schema changes.
  - Update `README.md` or add a short note if behaviour/commands change.
  - Run `compile.bat` / `mvn package` and `cd frontend && npm run build` to validate.
- Never commit secrets (check `application.yml`); prefer environment variables.
- Preserve existing .bat scripts and Windows-friendly paths when adding automation.

7) Agent-convention files discovery
- Searched for common agent files (.github/copilot-instructions.md, AGENT.md, AGENTS.md, CLAUDE.md, .cursorrules, .windsurfrules, .clinerules and rule folders). None were found in repository root or subfolders; there are no repository-level AI instruction files to inherit.

Quick checklist for an agent before opening a PR:
- [ ] Run `compile.bat` (or `mvn -DskipTests package`) and `start-backend(-with-jdk17).bat` locally.
- [ ] Run frontend dev server: `cd frontend; npm install; npm run dev` and exercise changed UI.
- [ ] Add a new `V{n}__*.sql` migration for schema changes.
- [ ] Remove or avoid committing secrets in `application.yml`.
- [ ] Keep PRs small and document the change in `README.md` or migration header.

EOF

