# MindMate AI – Contextual Personal Assistant

MindMate is an intelligent personal assistant designed to bridge the gap between software logic and real-world user needs. Built with **Spring Boot** and **React**, it leverages Local LLMs (via **Ollama**) and hardware sensors to provide real-time assistance, schedule management, and technical support.

## 🚀 Key Features
- **Contextual AI Chat:** Integrated with **Llama 3** (via Ollama) for high-reasoning local inference.
- **Google Calendar Sync:** Detects meeting requests and synchronizes them with external APIs.
- **Hardware Integration:** Real-time distance and environment monitoring via **Arduino/ESP32**.
- **High-Performance Architecture:** Optimized for **NVIDIA RTX 4070** to ensure low-latency responses by offloading AI tasks to the GPU, keeping the CPU free for network packet handling (ACK) and API calls.

## 🛠 Tech Stack
- **Backend:** Java 21, Spring Boot 3.x, Spring AI
- **Database:** PostgreSQL
- **AI Engine:** Ollama (Llama 3)
- **Infrastructure:** NVIDIA CUDA 12.x, Windows 11 (preferring IPv4 stack)

## ⚙️ Setup Requirements
1. **Ollama:** Install from [ollama.com](https://ollama.com) and run `ollama run llama3`.
2. **PostgreSQL:** Ensure a database named `mindmate` is created.
3. **Java JDK:** Version 21 or higher.
4. **NVIDIA Drivers:** Latest Game Ready or Studio drivers for RTX acceleration.

## 🏃 Setup Instructions
1. Clone the repository.
2. Update `src/main/resources/application.properties` with your DB credentials.
3. Build the project: `./mvnw clean install`.
4. Run the application: `./mvnw spring-boot:run`.