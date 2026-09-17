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
- Thymeleaf
- JavaScript AJAX
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

# 5. Database Setup


## Database Information

Database Management System:

```
SQL Server
```

Database name:

```
jakartaJPA
```


## Create Database

Run the SQL script:

```
database.sql
```


The script will:

- Create database `jakartaJPA`
- Create Category table
- Create Product table
- Insert sample data


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


Run `database.sql` before starting the application.


---

# 6. Installation and Run Project


## Clone Repository

```bash
git clone https://github.com/pminhquan/REST_API_AJAX.git
```


## Go to Project Folder

```bash
cd BT07_REST_API_AJAX
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

# 7. Swagger API Documentation


After starting the application:


```
http://localhost:8080/swagger-ui/index.html
```


Swagger provides:

- API documentation
- Request testing
- Response preview


---

# 8. Category REST API


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

# 9. Product REST API


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

# 10. AJAX CRUD Interface


The project provides AJAX CRUD pages for Category and Product.

CRUD operations are performed without reloading the entire page.


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

# 11. Testing


The project has been tested with:

- Maven build
- REST API endpoints
- Swagger API documentation
- AJAX CRUD functions


Test command:

```bash
mvn clean test
```

---

# 12. Notes


Before running the application:


1. Install Java 21

2. Install SQL Server

3. Execute database.sql

4. Configure application.properties

5. Run Spring Boot application

