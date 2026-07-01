# TradeMatch Exchange Platform

## Overview

TradeMatch Exchange Platform is a modular electronic trading platform built using Java and Spring Boot.

The objective is to simulate how a modern electronic exchange processes buy and sell orders using a Price-Time Priority matching algorithm while exposing a production-quality REST API.

This project is inspired by the architecture used in modern financial exchanges and payment systems.

---

# High Level Architecture

Client

↓

REST API (Spring Boot)

↓

Controller Layer

↓

Service Layer

↓

Exchange Engine

↓

Matching Engine

↓

Order Book

↓

Trade Repository

---

# Core Components

## Controller Layer

Responsible for exposing REST endpoints.

Examples

- OrderController
- OrderBookController
- TradeController

Responsibilities

- Receive HTTP requests
- Validate request format
- Return JSON responses
- Never contain business logic

---

## Service Layer

Acts as the application's business layer.

Responsibilities

- Process requests
- Call Exchange
- Convert DTOs
- Handle business workflows

---

## Exchange

Acts as the orchestrator of the trading platform.

Responsibilities

- Manage Order Books
- Validate stocks
- Coordinate Matching Engine
- Coordinate Risk Manager

---

## Matching Engine

Responsible for trade execution.

Implements

- Price-Time Priority
- Market Orders
- Limit Orders
- Partial Fills

---

## Order Book

Maintains

Buy Orders

Sell Orders

using Priority Queues.

Responsible for

- Add Order
- Cancel Order
- Modify Order
- Best Bid
- Best Ask
- Market Depth

---

## Risk Manager

Validates incoming orders before they enter the exchange.

Examples

- Quantity Validation
- Price Validation
- Stock Validation

---

## Repository Layer

Responsible for data persistence.

Current

In-memory

Future

Spring Data JPA + PostgreSQL

---

# REST APIs

Current

POST /orders

GET /orderbook/{symbol}

Future

GET /orders

GET /orders/{id}

PUT /orders/{id}

DELETE /orders/{id}

GET /trades

GET /marketdepth/{symbol}

GET /statistics

---

# Technology Stack

Java 25

Spring Boot

Maven

JUnit 5

Postman

Git

GitHub

Future

PostgreSQL

Docker

Redis

Kafka

WebSockets

Swagger

GitHub Actions

AWS

---

# Engineering Goals

Clean Architecture

SOLID Principles

RESTful APIs

Thread-safe Order Processing

Production-quality Logging

Automated Testing

CI/CD

Cloud Deployment

Observability

Scalability

---

# Long-Term Vision

TradeMatch Exchange Platform should demonstrate the design and implementation of a production-style electronic trading backend suitable for software engineering interviews in fintech and backend engineering roles.