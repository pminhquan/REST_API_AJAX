# Spring Boot JPA Online Store Management System

## Project Overview

This project is a Spring Boot WAR migration of the JPAExercise application.

The system is an online store management application that provides:

- Product storefront for customers
- User registration and authentication
- OTP verification for registration and password recovery
- User profile management
- Administrator management system for:
  - Products
  - Categories
  - Users

---

# Technology Stack

- Java 21
- Spring Boot 4.0.0
- Spring MVC
- Spring Data JPA
- Hibernate ORM 7
- JSP / JSTL
- SiteMesh Decorator 3
- Microsoft SQL Server
- Microsoft JDBC Driver
- BCrypt password hashing
- Maven WAR Packaging

---

# Requirements

Install the following tools before running:

- JDK 21
- Maven 3.x
- Microsoft SQL Server
- SQL Server Management Studio (SSMS)
- IntelliJ IDEA (recommended)

Database information:

```
Database name: jakartaJPA
Database type: Microsoft SQL Server
```

---

# Database Setup

## Step 1: Create Database

Open:

```
database.sql
```

using:

```
SQL Server Management Studio (SSMS)
```

Execute the script.

The script will create:

- Database: jakartaJPA
- Users table
- Categories table
- Products table
- OTP Tokens table
- Required relationships
- Sample data

Sample products:

- iPhone 15 Pro
- Samsung Galaxy S25
- MacBook Air M3
- Dell XPS 13
- AirPods Pro 2
- iPad Air M2
- ASUS ROG Strix
- Apple Watch Series 10

---

# Database Configuration

Open:

```
src/main/resources/application.properties
```

Configure SQL Server connection:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true

spring.datasource.username=<your SQL Server username>

spring.datasource.password=<your SQL Server password>
```

Example:

```
Username: sa
Password: your SQL Server password
```

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=none
```

Database structure is managed through:

```
database.sql
```

---

# Run Application

Open terminal at project root.

## Option 1: Run using Maven

```bash
mvn spring-boot:run
```

## Option 2: Build WAR file

```bash
mvn clean package
```

Run:

```bash
java -jar target/springboot.war
```

Application URL:

```
http://localhost:8080
```

---

# Demo Accounts

## Administrator Account

Use this account to test administrator functions:

```
Username:
test_admin

Password:
password

Role:
ADMIN
```

Administrator features:

- Dashboard
- Category management
- Product management
- User management


## Customer Account

```
Username:
test_customer

Password:
password

Role:
CUSTOMER
```

Customer features:

- View products
- Search products
- View product details
- Update profile information
- Upload avatar


Customer accounts cannot access administrator pages.

---

# Application URLs

| Function | URL |
|---|---|
| Home | `/` |
| Home page | `/home` |
| Login | `/login` |
| Register | `/register` |
| Product list | `/product` |
| Product detail | `/products/detail?id=<product_id>` |
| Profile | `/profile` |
| Admin dashboard | `/admin/dashboard` |
| Category management | `/categories` |
| Product management | `/products` |
| User management | `/admin/users` |

---

# Main Test Functions

## 1. Authentication

URL:

```
/login
```

Test:

- Login with administrator account
- Login with customer account
- Logout


The system supports login using:

- Username
- Email


---

## 2. Registration and OTP Verification

URL:

```
/register
```

Test:

- Create new account
- Verify OTP
- Activate account


---

## 3. Password Recovery

URL:

```
/forgot-password
```

Test:

- Request password reset
- Verify OTP
- Reset password


---

# Customer Functions

Login using customer account.

## Product browsing

URL:

```
/product
```

Functions:

- View product list
- Search products
- Pagination
- Product details


Example:

```
/products/detail?id=1
```

Functions:

- View product information
- View category information


## Profile Management

URL:

```
/profile
```

Functions:

- Update fullname
- Update phone number
- Upload avatar image


---

# Administrator Functions

Login:

```
Username:
test_admin

Password:
password
```


## Dashboard

URL:

```
/admin/dashboard
```

Functions:

- View system statistics
- View recent products


## Category Management

URL:

```
/categories
```

Functions:

- View categories
- Search categories
- Pagination
- Add category
- Edit category
- Delete category


## Product Management

URL:

```
/products
```

Functions:

- View products
- Search products
- Pagination
- Add product
- Edit product
- Delete product
- Upload product image


## User Management

URL:

```
/admin/users
```

Functions:

- View users
- Search users
- Pagination
- Add user
- Edit user
- Delete user


---

# Authorization

The system contains two roles:

## ADMIN

Can access:

- Dashboard
- Product management
- Category management
- User management


## CUSTOMER

Can access:

- Product storefront
- Product details
- Personal profile


Customer accounts cannot access administrator pages.

---

# Upload Features

The system supports multipart upload for:

- User avatar
- Product images


Uploaded images are stored in:

```
src/main/webapp/uploads
```

---

# Project Structure

```
springboot
│
├── src
│   └── main
│       ├── java
│       │   ├── controller
│       │   ├── service
│       │   ├── repository
│       │   └── entity
│       │
│       ├── resources
│       │   └── application.properties
│       │
│       └── webapp
│           ├── JSP views
│           ├── SiteMesh decorators
│           ├── CSS
│           └── JavaScript
│
├── database.sql
├── pom.xml
└── README.md
```

---

# Notes

- Make sure SQL Server is running before starting the application.
- Import database.sql before running the project.
- Configure correct database username and password.
- OTP email functions require SMTP configuration for real email delivery.
- The application uses JSP views with SiteMesh decorators for interface management.

---

# Database

Database name:

```
jakartaJPA
```

Tables:

```
users
products
categories
otp_tokens
```