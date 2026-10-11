# Library Management System

Full-stack web app: **Spring Boot 4 (Java 21) + JWT + MySQL/H2** backend and a **React (Vite)** frontend.
Same layout as the student registration project: `lms/` is the backend (like `cs/`), `frntd/` is the frontend.

```
LibraryManagement/
├── lms/                                  Spring Boot backend  (port 8080)
│   ├── pom.xml, mvnw, mvnw.cmd, .mvn/
│   └── src/main/java/com/anurag/lms/
│       ├── LmsApplication.java
│       ├── config/       SecurityConfig, DataInitializer
│       ├── controller/   MemberController, BookController, CategoryController, BorrowController
│       ├── dto/          RegisterDTO, LoginDTO, LoginResponseDTO, MemberDTO, BookDTO, IssueDTO, ...
│       ├── entity/       Member, Book, Category, BorrowRecord
│       ├── enums/        Role, BorrowStatus
│       ├── exception/    GlobalExceptionHandler + Bad request / Not found / Conflict exceptions
│       ├── filter/       JwtAuthenticationFilter
│       ├── repository/   MemberRepository, BookRepository, CategoryRepository, BorrowRecordRepository
│       └── service/      JwtService, MemberService, BookService, CategoryService, BorrowService
└── frntd/                                React + Vite frontend (port 5173)
    └── src/              api.js, App.jsx, Login, Register, Dashboard, Books, Borrow, Members, NavBar, StatusStamp
```

## Run it

**Backend** (needs Java 21). Set a private admin password and a randomly generated JWT secret (at least 32 characters) in your shell before starting it.

```bash
export ADMIN_PASSWORD='choose-a-private-password'
export JWT_SECRET="$(openssl rand -base64 48)"
cd lms
./mvnw spring-boot:run
```

On Windows PowerShell, set the variables and start the backend from the project root:

```powershell
$env:ADMIN_PASSWORD = Read-Host "Choose an admin password"
$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
Set-Location lms
.\mvnw.cmd spring-boot:run
```

The app uses a file-based H2 database by default.

To use **MySQL** instead, create nothing by hand (the database `librarydb` is created for you), then:

```bash
export DB_USERNAME=root DB_PASSWORD=your_mysql_password    # Windows: set DB_PASSWORD=...
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

**Frontend** (needs Node 20+):

```bash
cd frntd
npm install
npm run dev
```

Open http://localhost:5173.

## First login

On first start the backend creates an admin account and a few sample books. The admin password is the value you set in `ADMIN_PASSWORD`; no shared default password is configured.

| Email | Password |
|---|---|
| `admin@library.com` | Your `ADMIN_PASSWORD` value |

Set a unique, randomly generated `JWT_SECRET` (at least 32 characters) for every deployment. Never commit passwords or JWT secrets.

## Roles

Anyone can register, and registration always creates a **MEMBER**. An admin promotes people to LIBRARIAN or ADMIN from the Members page.

| | Member | Librarian | Admin |
|---|---|---|---|
| Browse / search books | yes | yes | yes |
| See own loans and fines | yes | yes | yes |
| Add / edit / delete books and categories | | yes | yes |
| Issue and return books, see all loans | | yes | yes |
| List members | | yes | yes |
| Change roles, delete members | | | yes |

## API

| Method | Path | Who |
|---|---|---|
| POST | `/members/register`, `/members/login` | public |
| GET | `/members/me` | signed in |
| GET | `/members` | staff |
| GET / PUT | `/members/{id}` | staff or that member |
| PUT | `/members/{id}/role` | admin |
| DELETE | `/members/{id}` | admin |
| GET | `/books`, `/books?search=`, `/books/{id}`, `/categories` | signed in |
| POST / PUT / DELETE | `/books`, `/books/{id}`, `/categories`, `/categories/{id}` | staff |
| GET | `/borrow/my` | signed in |
| GET | `/borrow`, `/borrow/overdue`, `/borrow/stats` | staff |
| POST | `/borrow/issue` &nbsp;`{ "memberId": 2, "bookId": 1 }` | staff |
| PUT | `/borrow/return/{id}` | staff |

Send the token from login as `Authorization: Bearer <token>`.

## Library rules (in `application.properties`)

- `library.loan-days=14`: a loan is due 14 days after issue
- `library.fine-per-day=2.00`: fine for each day past the due date, charged when the book is returned
- `library.max-active-loans=5`: books one member can have out at once

## Deploying

Backend (`lms`) on **Railway**, frontend (`frntd`) on **Render**. See the chat for the full click-by-click walkthrough. In short:

1. Push this repo to GitHub.
2. On Railway: New Project → Deploy from GitHub → root directory `lms` (it builds the included `Dockerfile`) → add a MySQL plugin → set `SPRING_PROFILES_ACTIVE=mysql`, `JWT_SECRET`, `ADMIN_PASSWORD`, `CORS_ALLOWED_ORIGINS` as variables.
3. On Render: New → Static Site → root directory `frntd` → build command `npm install && npm run build` → publish directory `dist` → set `VITE_API_URL` to the Railway backend's public URL.
4. Update `CORS_ALLOWED_ORIGINS` on Railway to the Render URL, and redeploy.
