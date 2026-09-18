# BT07 - REST API & AJAX CRUD with Spring Boot


## 1. Project Overview

This project is developed for BT07 using Spring Boot.

The main purpose is to implement REST API services and AJAX CRUD operations for Category and Product management.

Main features:

- Build REST API for Category management
- Build REST API for Product management
- Integrate Swagger 3 for API documentation and testing
- Implement AJAX CRUD interface without reloading the page


---

# 2. Technology Stack

- Java 21
- Spring Boot 4.0.0
- Spring Data JPA
- Hibernate ORM
- SQL Server
- Maven
- Springdoc OpenAPI 3
- HTML and JavaScript (served directly by Spring MVC)
- Bootstrap


---

# 3. Repository

GitHub:

https://github.com/pminhquan/REST_API_AJAX


---

# 4. Project Structure

```
src/main/java/com/hcmute/springboot

├── controller
│
├── controller/api
│      ├── CategoryApiController.java
│      └── ProductApiController.java
│
├── entity
│
├── repository
│
├── service
│
└── config
       ├── OpenApiConfig.java
       └── JacksonConfig.java


src/main/resources

├── templates
│      ├── categories-ajax.html
│      └── products-ajax.html
│
└── static
       └── js
              ├── category-ajax.js
              └── product-ajax.js
```


---

# 5. Prerequisites & Database Setup

## Prerequisites

Before running the application, ensure the following prerequisites are installed:

- **Java 21** (JDK 21)
- **Apache Maven 3.9+**
- **Microsoft SQL Server**


## Database Information

Database Management System:

```
SQL Server
```

Database name:

```
jakartaJPA
```


## Create Database and Seed Data

Run the SQL script:

```
database.sql
```

Practical database setup steps:

1. Connect SQL Server Management Studio (SSMS) to your local SQL Server using an account allowed to create databases and tables.
2. Open `database.sql` and execute the entire script including all `GO` batches.
3. Ensure TCP/IP is enabled and configured for the application's `localhost:1433` connection (or adjust documented connection in `application.properties` to your actual instance).
4. Configure a valid SQL login with permissions to `jakartaJPA` before starting the application.

> **Note**: `database.sql` must be run manually via SSMS; it is not executed by us or by the application at startup (`spring.jpa.hibernate.ddl-auto=none`).

The script will:

- Create database `jakartaJPA` if it does not exist
- Create tables: `users`, `categories`, `products`, `otp_tokens`
- Seed default administrator account `test_admin`
- Seed initial categories and sample products


## Configure Database Connection

Edit:

```
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=false;trustServerCertificate=true

spring.datasource.username=sa

spring.datasource.password=123

spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

Note: `spring.jpa.hibernate.ddl-auto=none` is used, so run `database.sql` before starting the application.


---

# 6. Installation and Run Project


## Clone Repository

```bash
git clone https://github.com/pminhquan/REST_API_AJAX.git
```


## Go to Project Folder

```bash
cd REST_API_AJAX
```


## Build Project

```bash
mvn clean install
```


## Run Application

```bash
mvn spring-boot:run
```


Application runs at:

```
http://localhost:8080
```


---

# 7. Authentication & Admin Login

The AJAX management pages (`/categories/ajax` and `/products/ajax`) and admin routes are protected by `AuthenticationInterceptor`. Unauthenticated access redirects to `/login`.

To access administrator features and AJAX CRUD interfaces, log in with the seeded administrator credentials:

- **Login URL**: `http://localhost:8080/login`
- **Username**: `test_admin` (or Email: `admin@test.com`)
- **Password**: `password`
- **Role**: `ADMIN`

The documented default password matches the seeded BCrypt hash, and existing `test_admin` rows are not reset by the script.

---

# 8. Swagger API Documentation


After starting the application:


```
http://localhost:8080/swagger-ui/index.html
```


Swagger provides:

- API documentation
- Request testing
- Response preview


---

# 9. Category REST API


Base URL:

```
/api/categories
```


## Get all categories

```
GET /api/categories
```


## Search categories

Example:

```
GET /api/categories?keyword=phone
```


## Get category by ID

```
GET /api/categories/{id}
```


## Create category

```
POST /api/categories
```


Request body:

```json
{
    "categoryname": "Laptop",
    "images": "laptop.jpg",
    "status": 1
}
```


## Update category

```
PUT /api/categories/{id}
```


Request body:

```json
{
    "categoryname": "Updated Laptop",
    "images": "laptop-new.jpg",
    "status": 1
}
```


## Delete category

```
DELETE /api/categories/{id}
```



---

# 10. Product REST API


Base URL:

```
/api/products
```


## Get all products

```
GET /api/products
```


## Search products

Example:

```
GET /api/products?keyword=macbook
```


## Get product by ID

```
GET /api/products/{id}
```


## Create product

```
POST /api/products
```


Request body:

```json
{
    "productname": "Macbook Pro",
    "description": "Apple Laptop",
    "price": 2500,
    "images": "macbook.jpg",
    "status": 1,
    "category": {
        "categoryid": 1
    }
}
```


## Update product

```
PUT /api/products/{id}
```


## Delete product

```
DELETE /api/products/{id}
```


---

# 11. AJAX CRUD Interface


The project provides AJAX CRUD pages for Category and Product.

CRUD operations are performed without reloading the entire page.

> **Note**: Both AJAX pages require an active `ADMIN` session. Please log in at `/login` with `test_admin` / `password` before navigating to these URLs.


## Category AJAX


URL:

```
http://localhost:8080/categories/ajax
```


Functions:

- Display category list
- Search category
- Create category
- Update category
- Delete category


---

## Product AJAX


URL:

```
http://localhost:8080/products/ajax
```


Functions:

- Display product list
- Search product
- Create product
- Update product
- Delete product


---

# 12. Testing

The automated test suite runs via:

```bash
mvn clean test
```

This runs 26 standalone Mockito unit tests across `CategoryApiControllerTest` and `ProductApiControllerTest`. Note that `mvn clean test` verifies isolated controller logic with mocked service dependencies; it is not proof that live REST endpoints, Swagger UI, browser AJAX interactions, or SQL Server database integration have passed.

---

# 13. Notes


Before running the application:


1. Install Java 21 (JDK 21) and Apache Maven 3.9+
2. Install Microsoft SQL Server
3. Execute `database.sql` to initialize database and seed `test_admin`
4. Configure `application.properties` with your database credentials
5. Run Spring Boot application (`mvn spring-boot:run`)
6. Log in at `http://localhost:8080/login` with `test_admin` / `password` before accessing `/categories/ajax` or `/products/ajax`

