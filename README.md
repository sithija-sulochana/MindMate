# 🧠 MindMate: The Cognitive AI Assistant

MindMate is an intelligent personal assistant designed to bridge the gap between robust software logic and human-centric needs. Unlike traditional assistants, MindMate functions with a **psychologically-aware (Mentalist)** approach, utilizing local Large Language Models (LLMs) and seamless API automation to manage your digital life while maintaining absolute on-device privacy.

---

## 📌 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Getting Started](#getting-started)
- [Network Optimization](#network-optimization)
- [License](#license)

---

## 🌟 Overview

MindMate is built for the high-performance user who demands efficiency and privacy. By integrating **Spring Boot** with **Ollama**, it acts as a digital mentalist—analyzing your prompts with high reasoning capabilities to predict needs, automate scheduling, and provide technical support—all powered by your local GPU.

---

## ✨ Key Features

- **Psychologically-Aware Conversational AI**: Powered by **Llama 3 / Phi-3** via [Ollama](https://ollama.com/), providing empathetic and logically deductive interactions.
- **Automated Calendar Orchestration**: Integrated with **Google/Microsoft Calendar APIs** to detect, schedule, and manage meetings directly from natural language conversations.
- **Privacy-by-Design**: All LLM inference is performed locally. No sensitive chat data or meeting details are transmitted to external AI clouds.
- **Real-time Streaming**: Utilizes Server-Sent Events (SSE) to deliver AI responses word-by-word for a seamless, "human-like" typing experience.
- **Hardware-Accelerated Performance**: Optimized specifically for **NVIDIA RTX GPUs** to ensure near-instantaneous reasoning.

---

## 🛠 Technology Stack

- **Backend:** Java 21, Spring Boot 3.x, Spring AI
- **LLM Engine:** Ollama (Llama 3 / Phi-3)
- **APIs:** Google Calendar API (OAuth2 integration)
- **Database:** PostgreSQL (Persistence), H2 (Local Dev)
- **Reactive Framework:** Project Reactor (Flux/Mono) for non-blocking I/O
- **Client Communication:** RESTful APIs & SSE

---

## 🏗 System Architecture

MindMate leverages a high-performance data flow to ensure the CPU remains responsive even during heavy AI inference:

1. **User Prompt**: Received via React Frontend (Port 3000).
2. **Spring Boot (Port 8080)**: Processes the intent and determines if a Calendar action or AI response is needed.
3. **API Integration**: Backend handles OAuth2 handshakes and ACK packets for external calendar sync.

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK) 21+**
- **Ollama Desktop** (Installed and running)
- **PostgreSQL** (Active instance)

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone [https://github.com/sithija-sulochana/MindMate.git](https://github.com/sithija-sulochana/MindMate.git)
   cd MindMate/backend