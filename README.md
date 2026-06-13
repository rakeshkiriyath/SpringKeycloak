# SpringKeycloak — Multi-App SSO with Keycloak

A demo of single sign-on (SSO) across two independent Spring Boot applications using
[Keycloak](https://www.keycloak.org/) as the OIDC identity provider.

- **student-app** (port `8080`) — student-facing app
- **professor-app** (port `8082`) — professor-facing app with full student CRUD
- **Keycloak** (port `8081`) — realm `university-realm`, shared by both apps

Both apps authenticate against the same Keycloak realm via OAuth2/OIDC
(`spring-boot-starter-oauth2-client`). Logging into either app establishes a
Keycloak SSO session, so visiting the other app does not prompt for credentials
again. Logging out triggers RP-initiated logout, which ends the Keycloak SSO
session for both apps.

## Architecture

```
                ┌──────────────────────┐
                │   Keycloak (8081)     │
                │  realm: university-   │
                │        realm          │
                └─────────┬─────────────┘
                           │ OIDC (authorization code flow)
            ┌──────────────┴──────────────┐
            │                              │
  ┌─────────▼─────────┐         ┌──────────▼─────────┐
  │  student-app       │         │  professor-app      │
  │  (port 8080)        │         │  (port 8082)        │
  │  client:            │         │  client:            │
  │  student-app-client │         │  professor-app-client│
  └─────────┬───────────┘         └──────────┬──────────┘
            │                                  │
            └───────────────┬──────────────────┘
                             │
                  ┌──────────▼──────────┐
                  │  PostgreSQL          │
                  │  "university" DB     │
                  │  "user" / roles      │
                  └──────────────────────┘
```

## Features

- Keycloak-backed login (OIDC authorization code flow) for both apps
- Realm roles (`STUDENT`, `PROFESSOR`) mapped from the `realm_access.roles` ID
  token claim to Spring Security authorities (`ROLE_STUDENT`, `ROLE_PROFESSOR`)
- Shared SSO session — log in once on either app, the other app does not
  re-prompt for credentials
- RP-initiated logout — logging out of either app ends the shared Keycloak
  session (`OidcClientInitiatedLogoutSuccessHandler`)
- `/home` — profile page populated directly from ID token claims (name, email,
  username)
- `/manageStudents` — restricted to `ROLE_PROFESSOR`
  - student-app: read-only list of students
  - professor-app: full CRUD (update/delete students)

## Prerequisites

- Java 17
- PostgreSQL (database named `university`)
- Keycloak 26.x

## Setup

### 1. Database

Create a `university` database with `"user"` and `roles` tables matching the
JPA entities:

```sql
CREATE TABLE "user" (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255),
    email       VARCHAR(255) NOT NULL,
    username    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL
);

CREATE TABLE roles (
    id      BIGSERIAL PRIMARY KEY,
    role    VARCHAR(255) NOT NULL,
    user_id BIGINT REFERENCES "user"(id)
);
```

> The `password` column is no longer used for authentication (Keycloak handles
> login) but must be non-null — any placeholder value works.

Seed users matching the Keycloak realm users below (`john`, `sachin`,
`deepak`, `naresh`) with roles `STUDENT` / `PROFESSOR` so `/manageStudents`
returns data.

### 2. Keycloak realm

Import [`keycloak/university-realm-realm.json`](keycloak/university-realm-realm.json)
into Keycloak, e.g. by placing it in Keycloak's `data/import/` directory and
starting with `--import-realm`:

```
<KEYCLOAK_HOME>/bin/kc.bat start-dev --import-realm
```

This provisions:

- Realm `university-realm`
- Realm roles `STUDENT`, `PROFESSOR`
- Clients `student-app-client` / `professor-app-client` (confidential, with
  `post.logout.redirect.uris` configured for RP-initiated logout)
- Test users `john`, `sachin`, `deepak`, `naresh` (all password `password`)

| Username | Role      |
|----------|-----------|
| john     | STUDENT   |
| sachin   | PROFESSOR |
| deepak   | STUDENT   |
| naresh   | STUDENT   |

### 3. Run the apps

Each module reads the Postgres password from a JVM argument:

```bash
./mvnw -pl student-app spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.password=<your-db-password>"
./mvnw -pl professor-app spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.datasource.password=<your-db-password>"
```

### 4. Try it out

1. Visit `http://localhost:8080/` → redirected to Keycloak login.
2. Log in as `sachin` / `password` → lands on `/home` with a "Manage Students"
   link (PROFESSOR role).
3. In the same browser, visit `http://localhost:8082/manageStudents` →
   loads directly, no second login (SSO).
4. Click **Logout** → ends the Keycloak session; visiting `/home` or
   `/manageStudents` on either app now requires a fresh login.

## Security note

This project is configured for **local development only**. Client secrets,
the Keycloak admin account, and the seeded test user passwords (`password`)
are placeholders for demo purposes and must not be reused in any
production-like environment.

## Project layout

```
student-app/    Spring Boot app, port 8080, client "student-app-client"
professor-app/  Spring Boot app, port 8082, client "professor-app-client"
keycloak/       Realm export for importing university-realm into Keycloak
```
