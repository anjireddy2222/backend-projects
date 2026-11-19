# Backend projects for students

Two complete teaching projects using Java 17, Spring Boot 3.5, Spring Data JPA and MySQL. Each application uses a small controller → service → repository structure. Authentication uses Spring Security, BCrypt password hashing and signed JWTs.

| Project | Applications | Ports | Guide |
| --- | --- | --- | --- |
| Amazon-inspired backend | Product catalog + customer service | 8081, 8082 | [Amazon guide](amazon-clone/README.md) |
| Help desk backend | Tickets, users and support workflows | 8083 | [Help desk guide](help-desk/README.md) |

## Option 1: Run everything with Docker

Install Docker with Docker Compose, then run from this repository:

```powershell
Copy-Item .env.example .env
docker compose --profile apps up --build -d
docker compose logs -f catalog customer helpdesk
```

On macOS/Linux use `cp .env.example .env`. The first build downloads dependencies. Wait for each application to log `Started Application` before sending requests. Use `--profile amazon` or `--profile helpdesk` instead of `--profile apps` to run just one project.

MySQL runs on `localhost:3306`. The local test mailbox is at <http://localhost:8025>. It captures password-reset emails without sending real emails. Application URLs are listed above; these are API backends, so there is no home-page UI.

```powershell
docker compose --profile apps down
```

Stopping containers preserves the MySQL volume. Initialization SQL runs only when the volume is first created. Changing `.env` passwords does not change accounts already stored in MySQL.

## Option 2: Run Java locally

Install JDK 17 and set `JAVA_HOME`. The included Maven Wrapper downloads Maven; a separate Maven installation is unnecessary. Import each service's `pom.xml` in your IDE, or use the root `pom.xml` to open all three.

Start only MySQL and the mailbox:

```powershell
Copy-Item .env.example .env
docker compose up -d mysql mailpit
```

Alternatively, run [database/local-setup.sql](database/local-setup.sql) in MySQL Workbench. The applications create their tables at startup using JPA. For password resets, run Mailpit or configure your own SMTP server.

Build and run all tests from the repository root:

```powershell
.\mvnw.cmd clean verify
```

Open three terminals at the repository root. Run the following in separate terminals:

```powershell
# Terminal 1: catalog, including three sample products
.\mvnw.cmd -f amazon-clone/catalog-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=demo"
```

```powershell
# Terminal 2: customer accounts, carts and addresses
$env:JWT_SECRET = "classroom-only-change-this-secret-1234567890"
.\mvnw.cmd -f amazon-clone/customer-service/pom.xml spring-boot:run
```

```powershell
# Terminal 3: help desk, including an admin and an agent
$env:JWT_SECRET = "classroom-only-change-this-secret-1234567890"
$env:DEMO_ADMIN_PASSWORD = "AdminDemo123!"
$env:DEMO_AGENT_PASSWORD = "AgentDemo123!"
.\mvnw.cmd -f help-desk/pom.xml spring-boot:run "-Dspring-boot.run.profiles=demo"
```

On macOS/Linux replace `.\mvnw.cmd` with `bash mvnw` and set environment variables with `export NAME='value'`. Environment variables must be set in the terminal that starts the application. `.env` is read by Docker Compose; Maven does not read it automatically.

The demo accounts are `admin@example.test` and `agent@example.test`. Their passwords are the environment values used on the first demo startup. Customers register through the API. Demo data is inserted only if absent. Do not enable the demo profile for a public deployment.

## Try the APIs

Open [Amazon requests](amazon-clone/requests.http) or [help desk requests](help-desk/requests.http) in IntelliJ HTTP Client or VS Code with REST Client. Run registration and login first, then copy `accessToken` into the token variable at the top. These files also provide bodies you can copy into Postman.

Protected endpoints require `Authorization: Bearer <accessToken>`. Tokens last one hour. Password-reset tokens last 15 minutes and can be used once. Reset tokens are emailed and only their hashes are stored in the database. Existing JWTs remain valid until their one-hour expiry after a password change; this simple project has no token revocation store.

## Learn the structure

```text
amazon-clone/
  catalog-service/src/main/java/com/example/amazon/catalog/
    product/            Product entity, repository, service and controller
    common/             API errors
  customer-service/src/main/java/com/example/amazon/customer/
    auth/               Registration, login, JWT and password reset
    cart/               Cart operations and catalog HTTP client
    address/            Saved delivery addresses
    common/             API errors
help-desk/src/main/java/com/example/helpdesk/
  auth/                 Accounts, roles, JWT and demo accounts
  ticket/               Tickets, comments, history and workflow rules
  common/               API errors
```

Start reading a controller, follow its call into the service, then inspect the repository and entity. Request DTOs are Java records. Entities use public fields to avoid repetitive getters/setters and Lombok. Business logic stays in services. `@Transactional` keeps related database writes together.

## Configuration and teaching scope

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Database connection for each service |
| `JWT_SECRET` | Required secret of at least 32 bytes for customer/help desk services |
| `PORT` | Override the application's default port |
| `CATALOG_URL` | Catalog address used by the customer service |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP connection |
| `MAIL_AUTH`, `MAIL_TLS`, `MAIL_FROM` | SMTP authentication, STARTTLS and sender |
| `DEMO_ADMIN_PASSWORD`, `DEMO_AGENT_PASSWORD` | Initial help desk demo passwords |

The Amazon project demonstrates two independently runnable services communicating over HTTP, each owning its own database. There is no gateway, service registry, Kafka, payment processing or checkout. Cart stock checks do not reserve stock. The help desk uses fixed category and priority enums so students can follow the workflow without extra configuration screens.

Tests use H2 and a mocked email sender; customer API tests stub the catalog. They exercise authentication, reset expiry/reuse, ownership, quantities, workflow transitions, role checks and pagination. The catalog HTTP client also has a separate HTTP integration test. MySQL, Docker and AWS should be tested in your deployment environment.

For a production extension, add database migrations, rate limiting, token revocation and a real mail provider. The classroom setup uses `ddl-auto=update` and local example credentials.

## Git and GitHub

The repository includes `.gitignore` rules for secrets and build output, plus a [GitHub Actions workflow](.github/workflows/build.yml) that runs all tests on pushes and pull requests. When ready, review `git diff`, commit the project files, and push to your own GitHub remote. No remote repository is created by these projects.

See the [database guide](database/README.md) for table relationships and [AWS deployment exercise](docs/aws-deployment.md) for EC2/RDS configuration. Java requirements follow the [Spring Boot documentation](https://docs.spring.io/spring-boot/3.5/system-requirements.html); JWT support uses [Spring Security's resource server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
