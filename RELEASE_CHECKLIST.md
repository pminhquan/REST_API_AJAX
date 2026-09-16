# Final release checklist

Release only after every required gate has direct evidence. A green unit-test run alone is not a release decision.

## 1. Scope and source freeze

- [ ] Confirm the release candidate is the intended source revision.
- [ ] Confirm release preparation changed documentation only: `README.md`, `MANUAL_QA_CHECKLIST.md`, and `RELEASE_CHECKLIST.md`.
- [ ] Confirm no Java, JSP, CSS, database, or configuration files changed unexpectedly.
- [ ] Review the complete diff; do not commit or publish generated files, credentials, logs, screenshots, or local bridge artifacts.

## 2. Requirements and configuration

- [ ] JDK 21 is installed on the deployment host.
- [ ] Maven is available for the build host, or the exact WAR artifact is available for deployment.
- [ ] SQL Server is reachable from the application host.
- [ ] `DB_URL`, `DB_USER`, and `DB_PASSWORD` are explicitly configured for the target environment.
- [ ] The development database-password fallback is not used in a shared or production environment.
- [ ] SMTP host, port, authentication, username, password, and STARTTLS settings are configured through environment/system properties.
- [ ] `APP_UPLOAD_DIR` points to a writable, backed-up, persistent directory outside the exploded WAR.
- [ ] Secrets are supplied by the deployment secret store and are absent from the repository, WAR, logs, and screenshots.

## 3. Database gate

- [ ] A current database backup exists and the rollback owner is identified.
- [ ] `database.sql` was reviewed and run with a SQL Server batch-aware tool where schema setup is required.
- [ ] Existing `users.Role` values are valid (`ADMIN` or `CUSTOMER`) before migration.
- [ ] Required tables, constraints, and seed category/products are present.
- [ ] `spring.jpa.hibernate.ddl-auto=none` is understood; the application will not repair a missing schema at startup.
- [ ] `database-test-cleanup.sql` is not scheduled against production data.

## 4. Build and artifact gate

- [ ] Run `mvn clean package` from `D:\PROJECT\lt-web-project\springboot`.
- [ ] Record the test count, failures, errors, skips, and build exit code.
- [ ] Confirm `target\springboot.war` exists and is the artifact being deployed.
- [ ] Record the WAR byte length and SHA-256 checksum.
- [ ] Confirm the checksum is the same for the copied/deployed artifact.
- [ ] Retain the previous known-good WAR for rollback.

## 5. Deployment gate

- [ ] Deploy the exact WAR to the approved environment.
- [ ] Confirm the effective HTTP port and external Tomcat context path, if applicable.
- [ ] Confirm the application starts without schema, upload-root, SMTP, or JSP compilation errors.
- [ ] Confirm the upload directory is writable and existing uploads are readable.
- [ ] Confirm packaged seed images are available where expected.
- [ ] Confirm logs do not expose passwords, OTP values, or connection secrets.

## 6. Runtime and authorization gate

- [ ] Smoke the public home/catalog/detail routes.
- [ ] Smoke login, logout, registration, OTP verification, forgot-password, and password reset.
- [ ] Smoke authenticated profile access and profile update.
- [ ] Confirm unauthenticated access is rejected for protected routes.
- [ ] Confirm customer access is rejected for `/admin`, `/categories`, `/admin/users`, and `/products`.
- [ ] Confirm admin access to dashboard, category, user, and product consoles.
- [ ] Confirm `admin.jsp` decoration and navigation are present on admin routes.
- [ ] Confirm CSRF rejection for missing/invalid mutation tokens.

## 7. Functional QA gate

- [ ] Complete [MANUAL_QA_CHECKLIST.md](MANUAL_QA_CHECKLIST.md) with a named tester and environment.
- [ ] Cover category, user, and product add/edit/delete flows.
- [ ] Cover search and pagination on public and management views.
- [ ] Cover valid, invalid, oversized, and persistence-tested uploads.
- [ ] Confirm no destructive test records or uploaded files remain outside the approved test scope.

## 8. Rollback and handoff

- [ ] Document the deployed WAR checksum, deployment time, host/context, and operator.
- [ ] Document the database backup location and upload backup location.
- [ ] Confirm the rollback procedure restores the previous WAR without overwriting database/upload data.
- [ ] Record open defects, waivers, and owners; unresolved release-blocking items prevent release.
- [ ] Obtain the release owner’s approval before publish/deploy.

## Release decision

- [ ] READY TO RELEASE — every required gate has direct evidence.
- [ ] BLOCKED — list the missing gate and owner below.

Decision:

Evidence links/locations:

Blockers or waivers:

Release owner:

Approval date:
