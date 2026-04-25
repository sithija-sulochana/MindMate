# MindMate

**MindMate** is an intelligent personal assistant that seamlessly blends sophisticated software logic with real-world user needs. Powered by local large language models and real-time hardware sensor integration, MindMate offers on-device privacy, precise automation, and hands-free technical support.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [System Architecture](#system-architecture)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

MindMate is designed from the ground up to provide context-aware assistance, efficiently coordinating schedules, monitoring environments, and responding to technical queries—all while ensuring your data never leaves your device.

---

## Key Features

- **Conversational AI**: Leverages high-performance local LLMs via [Ollama](https://ollama.com/) for on-device, private smart assistance.
- **Schedule & Task Management**: Automated handling and optimization of events, reminders, and routines.

- **Technical Support**: Instantly assists with technical queries, system diagnostics, and automation tasks.
- **Privacy-First**: MindMate performs all processing locally; no user data is sent to the cloud.

---

## Technology Stack

- **Spring Boot (Java):** Backend API and service orchestration
- **Ollama:** Local LLM server for private, high-speed natural language processing
- **Database:** (e.g., PostgreSQL, MySQL, or embedded; specify as implemented)
- **Authentication & Authorization:** (e.g., JWT, OAuth2, or OS-level, if enabled)
- **API Design:** RESTful services and event-driven integration

---

## Getting Started

### Prerequisites

- **Java 17+**
- **Ollama - phi3 model** (installed and running locally, with preferred LLM model loaded)
- **Database** (if using external; otherwise, embedded DB is supported)
- Compatible hardware sensors & drivers (optional, for sensor features)

### Installation

1. **Clone the repository**
    ```bash
    git clone https://github.com/sithija-sulochana/MindMate.git
    cd MindMate
    ```

2. **Configure Environment**
    - Edit `application.properties` for database, sensor, and optional LLM settings.

3. **Build & Run**
    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```



## Contributing

We welcome contributions—please open an issue to discuss proposals or submit a pull request for review.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Make your changes and commit (`git commit -am 'Describe your change'`)
4. Push and create a pull request

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Ollama](https://ollama.com/)
- Open source sensor and automation communities
