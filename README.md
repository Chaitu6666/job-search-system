# 🚀 Job Search System

A scalable and distributed **Job Search Platform** built using **Spring Boot Microservices Architecture**. This application enables job seekers to search and apply for jobs while providing a robust backend ecosystem powered by Spring Cloud components.

## 📌 Features

### 👤 User Management
- User Registration
- User Authentication
- User Profile Management

### 💼 Job Management
- Create Job Listings
- Update Job Details
- Delete Jobs
- Search Available Jobs
- View Job Information

### 📋 Job Basket
- Save Jobs for Later
- Manage Saved Jobs
- Easy Job Tracking

### 📝 Application Management
- Apply for Jobs
- Track Application Status
- Manage Candidate Applications

### 📨 Notification Service
- Messaging Support
- Event-Based Communication
- Notification Processing

### 🌐 API Gateway
- Centralized Routing
- Load Balancing
- Request Forwarding

### 🔍 Service Discovery
- Eureka Server Integration
- Dynamic Service Registration
- Service Lookup

---

## 🏗️ Microservices Architecture

```text
                +----------------+
                |   API Gateway  |
                +-------+--------+
                        |
     -----------------------------------------
     |         |          |        |         |
     ▼         ▼          ▼        ▼         ▼

+---------+ +---------+ +---------+ +---------+ +-------------+
| User    | | Job     | | Basket  | | Apply   | | Message     |
| Service | | Service | | Service | | Service | | Service     |
+---------+ +---------+ +---------+ +---------+ +-------------+

                        |
                        ▼

               +----------------+
               | Eureka Server  |
               +----------------+
```

---

## 🛠️ Tech Stack

### Backend
- Java 17+
- Spring Boot 3
- Spring Cloud
- Spring Security
- Spring Data JPA
- Hibernate

### Microservices
- Eureka Server
- Spring Cloud Gateway
- REST APIs

### Database
- MySQL

### Build Tool
- Maven

### Tools & IDE
- IntelliJ IDEA
- Postman
- Git
- GitHub

---

## 📂 Project Structure

```text
Job Search System
│
├── api-gateway
├── service-registry
├── user-service
├── job-service
├── jobbasket-service
├── application-service
└── message-service
```

---

## ⚙️ Service Responsibilities

### API Gateway
- Single entry point for clients
- Request routing
- Load balancing

### Service Registry
- Eureka Discovery Server
- Service registration and discovery

### User Service
- User management
- Authentication
- Profile operations

### Job Service
- Job CRUD operations
- Job search functionality

### Job Basket Service
- Save and manage favorite jobs

### Application Service
- Handle job applications
- Track application status

### Message Service
- Notification and messaging operations

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.8+
- MySQL 8+
- Git

### Clone Repository

```bash
git clone https://github.com/Chaitu6666/job-search-system.git
```

```bash
cd job-search-system
```

---

## ▶️ Start Services

### Start Eureka Server

```bash
cd service-registry
mvn spring-boot:run
```

### Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

### Start Remaining Services

```bash
cd user-service
mvn spring-boot:run
```

```bash
cd job-service
mvn spring-boot:run
```

```bash
cd jobbasket-service
mvn spring-boot:run
```

```bash
cd application-service
mvn spring-boot:run
```

```bash
cd message-service
mvn spring-boot:run
```

---

## 🔧 Configuration

Update the following files as needed:

```properties
application.properties
application.yml
```

Configure:

- Database URL
- Username
- Password
- Eureka Server URL
- Gateway Routes

---

## 📡 Service Registration

Verify services at:

```text
http://localhost:8761
```

---

## 🧪 API Testing

Use:

- Postman
- Swagger (if configured)

Example Endpoint:

```http
GET /jobs
```

```http
POST /applications
```

```http
POST /users/register
```

---

## 🔐 Security

- Spring Security Integration
- Role-Based Access Control
- Secure API Communication

---

## 📈 Future Enhancements

- JWT Authentication
- Kafka Integration
- Docker Containerization
- Kubernetes Deployment
- CI/CD Pipeline
- Resume Upload Feature
- Email Notifications
- AI-Based Job Recommendations

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a Pull Request

---

## 👨‍💻 Author

**Ponnapati Chaithanya Kumar**

Software Engineer

GitHub: https://github.com/Chaitu6666

---

## ⭐ Support

If you found this project useful, please give it a ⭐ on GitHub.
