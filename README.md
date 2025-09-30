# Transactional Outbox Pattern Example

[![Tests](https://github.com/snds-prfct/transactional-outbox-pattern-example/actions/workflows/tests.yaml/badge.svg)](https://github.com/snds-prfct/transactional-outbox-pattern-example/actions/workflows/tests.yaml)

The project that implements [**Transactional Outbox** pattern](https://microservices.io/patterns/data/transactional-outbox.html)

## Components

- [x] **PostgreSQL** database
- [x] **Orders** Microservice
- [ ] **Apache Kafka** Message Broker
- [ ] **Orders Processing** Microservice

## Technologies and Tools

- [x] **Java 21**
- [x] **Spring Boot** 3.5.6
- [x] **PostgreSQL** Database
- [x] **Docker** containerization
- [x] **Flyway** Database Migration Tool
- [x] **MapStruct** for objects mapping
- [x] **GitHub Actions** for CI
- [x] **Swagger** for API documentation
- [x] Unit and Integration tests with
    - **Testcontainers**
    - **SpringBootTest**, **MockMvc**
    - **JUnit**, **Mockito**, **AssertJ**

## API

Swagger API documentation is available under </br>
> `http://{APP_HOST}:{APP_PORT}/api/v1/swagger-ui/index.html`
