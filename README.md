# Webhook Solver Application

A Spring Boot application that automatically interacts with a remote API at application startup, without any manual HTTP trigger.

## Features

- Automatically calls the `/generateWebhook` endpoint on startup
- Solves assigned problems based on registration number
- Implements two types of problems:
  1. Mutual Followers: Identifies pairs of users who follow each other
  2. Nth-Level Followers: Finds users at a specific level in the follows graph
- Sends results to provided webhook with JWT authentication
- Implements retry policy (up to 4 attempts) for failed webhook calls

## Technical Stack

- Java 11
- Spring Boot 2.7.0
- Maven
- RestTemplate for HTTP calls
- Lombok for reducing boilerplate code

## Building the Project

```bash
mvn clean install
```

## Running the Application

```bash
mvn spring-boot:run
```

## Project Structure

```
src/main/java/com/example/webhooksolver/
├── WebhookSolverApplication.java
├── model/
│   ├── User.java
│   ├── WebhookRequest.java
│   └── WebhookResponse.java
└── service/
    └── WebhookService.java
```

## Download

You can download the latest JAR file from the [Releases](https://github.com/yourusername/webhook-solver/releases) page.

Direct download link: [webhook-solver-0.0.1-SNAPSHOT.jar](https://github.com/yourusername/webhook-solver/releases/download/v1.0.0/webhook-solver-0.0.1-SNAPSHOT.jar)

## License

MIT License 