# 🏢 EMS Portal - Enterprise Employee Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-16.2-dd0031.svg?logo=angular)](https://angular.io/)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg?logo=mysql)](https://www.mysql.com/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952b3.svg?logo=bootstrap)](https://getbootstrap.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A modern, full-stack **Enterprise Employee Management System (EMS)** designed to streamline HR operations, employee tracking, payroll processing, performance management, document vaults, and recruitment pipelines.

---

## 🌟 Key Features & Modules

### 🔐 1. Authentication & Role-Based Security (RBAC)
- Secure **JWT (JSON Web Token)** stateless authentication.
- Granular role-based permissions:
  - `ROLE_ADMIN`: Complete system administration and configuration.
  - `ROLE_HR`: Employee onboarding, recruitment ATS, documents, and payroll.
  - `ROLE_MANAGER`: Team attendance, leave approvals, and appraisal reviews.
  - `ROLE_EMPLOYEE`: Self-service portal, check-in/out, leave requests, payslips.

### 📊 2. Executive Dashboard
- Real-time KPIs: Active employees, on-leave count, attendance rate, open job positions.
- Quick action shortcuts and dynamic activity logs.
- Departmental distribution and headcount metrics.

### 👥 3. Employee Directory & Profiles
- Comprehensive employee records with personal, professional, and departmental info.
- Search, filter by department/status, and pagination.
- Detailed employee profile view and edit modal.

### 🏢 4. Department Management
- Manage organizational departments (IT, HR, Finance, Marketing, Sales, Operations, Administration).
- Department headcount and hierarchy association.

### ⏱️ 5. Attendance & Time Tracking
- Single-click **Clock In** and **Clock Out**.
- Daily status tracking: Present, Late, Half-day, Absent.
- Monthly attendance overview and history logs.

### 🏖️ 6. Leave Management
- Multi-category leave applications (Casual, Sick, Earned/Paid, Maternity/Paternity).
- Real-time leave balance tracking.
- Manager & HR approval workflows (Pending, Approved, Rejected).

### 💰 7. Payroll & Compensation
- Configurable salary structures (Basic pay, HRA, allowances, provident fund, tax deductions).
- Automated monthly payroll generation.
- Payslip breakdown and downloadable reports.

### 🎯 8. Performance & OKRs
- Quarterly and annual appraisal cycles.
- Employee goal and OKR setting with progress bars.
- 360-degree performance reviews and rating matrix.

### 📁 9. Enterprise Document Vault
- Secure centralized storage for employee contracts, NDAs, and certifications.
- Digital verification status (Verified, Pending Signature, Digitally Signed).
- File upload and instant download management.

### 💼 10. Recruitment & ATS (Applicant Tracking System)
- Create and manage job openings across departments.
- Candidate tracking pipeline: Applied ➔ Screened ➔ Interview Scheduled ➔ Offered ➔ Hired.
- Candidate interview scheduling and status tracking.

### 🤖 11. AI HR Assistant
- Built-in floating AI assistant widget.
- Quick answers to HR policies, leave questions, payroll queries, and system guidance.

---

## 🛠️ Technology Stack

### Backend
| Technology | Description |
| :--- | :--- |
| **Java 17+** | Core programming language |
| **Spring Boot 3.1.5** | Microservice-ready web framework |
| **Spring Security** | Authentication, authorization, and endpoint protection |
| **JJWT (0.11.5)** | JSON Web Token generation & validation |
| **Spring Data JPA / Hibernate** | ORM and persistent repository layer |
| **MySQL 8.0+** | Relational database storage |
| **Lombok** | Boilerplate code reduction |
| **Maven** | Build management and dependency resolution |

### Frontend
| Technology | Description |
| :--- | :--- |
| **Angular 16.2** | Single-page application framework |
| **TypeScript 5.1** | Strongly typed frontend logic |
| **Bootstrap 5.3 & Icons** | Responsive UI styling and design components |
| **ngx-toastr** | Modern notifications and toast messages |
| **RxJS 7.8** | Reactive streams and state handling |

---

## 📂 Project Structure

```text
EMS-Portal/
├── backend/                             # Spring Boot Backend
│   ├── pom.xml                          # Maven dependencies and configuration
│   └── src/
│       └── main/
│           ├── java/com/ems/
│           │   ├── config/              # Security, CORS, JWT, and DataSeeder
│           │   ├── controller/          # REST API endpoints (Auth, Employee, Leaves...)
│           │   ├── dto/                 # Request and response data transfer objects
│           │   ├── entity/              # JPA database entities (User, Employee, Role...)
│           │   ├── exception/           # Global exception handler & custom exceptions
│           │   ├── repository/          # Spring Data JPA repositories
│           │   ├── security/            # JWT filter, UserDetailsService, Token provider
│           │   └── service/             # Business logic & AI service implementations
│           └── resources/
│               └── application.properties # Server port, MySQL DB credentials, JWT secret
│
├── frontend/                            # Angular 16 Frontend
│   ├── package.json                     # Node.js dependencies and scripts
│   ├── angular.json                     # Angular CLI build & serve configurations
│   ├── tsconfig.json                    # TypeScript compiler configuration
│   └── src/
│       ├── app/
│       │   ├── core/                    # Auth guards, JWT interceptors, services, models
│       │   ├── features/                # Domain feature modules:
│       │   │   ├── auth/                # Login page
│       │   │   ├── dashboard/           # Analytics and statistics
│       │   │   ├── employees/           # Employee management & forms
│       │   │   ├── departments/         # Department listing
│       │   │   ├── attendance/          # Attendance tracking
│       │   │   ├── leaves/              # Leave request & approval
│       │   │   ├── payroll/             # Salary and payslips
│       │   │   ├── performance/         # OKRs and performance reviews
│       │   │   ├── documents/           # Secure document vault
│       │   │   └── recruitment/         # ATS candidate pipeline
│       │   └── shared/                  # Header, sidebar, layouts, AI assistant widget
│       ├── assets/                      # Static images and icons
│       └── environments/                # Environment configurations (API URLs)
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
Make sure you have the following installed on your machine:
- **Java JDK 17** or higher
- **Node.js** (v18 or v20 LTS recommended) & **npm**
- **MySQL Server** (running on port 3306)
- **Apache Maven** (or bundled Maven in your IDE)

---

### 1. Database Configuration
By default, the backend connects to MySQL on port `3306`.
Check or update `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ems_portal?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```
*(The `ems_portal` database will be created automatically if it does not already exist).*

---

### 2. Run the Backend (Spring Boot)

Open a terminal in the `backend` folder:

```bash
# Using Maven wrapper or local Maven
mvn clean compile
mvn spring-boot:run
```

- **Backend Port**: `http://localhost:8080`
- On first startup, `DataSeeder` automatically populates default roles, departments, employee records, payroll, and the default admin user.

---

### 3. Run the Frontend (Angular)

Open a terminal in the `frontend` folder:

```bash
# Install dependencies (if not already installed)
npm install

# Start development server
npm start
# or: ng serve
```

- **Frontend Application URL**: `http://localhost:4200`

---

## 🔑 Default Login Credentials

| Role | Email | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin@ems.com` | `Admin@123` | Full administrative privileges across all modules |

---

## 🌐 API Endpoints Overview

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/auth/login` | `POST` | Authenticate user and receive JWT token |
| `/api/auth/change-password` | `POST` | Change authenticated user password |
| `/api/employees/**` | `GET / POST / PUT / DELETE` | Employee CRUD operations & profiles |
| `/api/departments/**` | `GET / POST / PUT / DELETE` | Department management |
| `/api/attendance/**` | `GET / POST` | Clock in, clock out, and attendance logs |
| `/api/leaves/**` | `GET / POST / PUT` | Submit leaves, get balance, approve/reject |
| `/api/payroll/**` | `GET / POST` | Salary structures and monthly payroll |
| `/api/performance/**` | `GET / POST` | Performance reviews and OKR management |
| `/api/documents/**` | `GET / POST` | Document vault uploads and digital sign |
| `/api/recruitment/**` | `GET / POST / PUT` | ATS job postings and candidate tracking |
| `/api/ai/**` | `POST` | AI assistant queries and recommendations |

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).