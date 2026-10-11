# NEXA-AI: Advanced Full-Stack AI Assistant Platform

NEXA-AI is a unified, production-ready intelligent assistant and research platform built with modern Android architecture, Jetpack Compose, Kotlin, Room (SQLite) persistent database, and Google Gemini AI intelligence.

---

## 🌟 Key Features

### 1. Advanced Multimodal AI Chat Engine
- **Direct REST & SSE Streaming**: Integrates with Google Gemini models (`gemini-3.5-flash`, `gemini-3.1-pro-preview`, `gemini-2.5-flash-image`, `gemini-3.1-flash-lite-preview`).
- **Progressive Token Streaming**: Token-by-token streaming display via Server-Sent Events (SSE).
- **Generation Control**: Interactive "Stop Generating" button, "Regenerate / Retry", and "Edit & Resend" workflow.
- **Message Actions**: Copy markdown response, Read aloud (Text-to-Speech), Like/Dislike ratings, and Native Android Sharing.
- **Syntax-Highlighted Code Blocks**: Language pills (`kotlin`, `python`, `sql`, etc.), copy-code button, and horizontal scrolling for long code blocks.

### 2. Multi-Step Deep Research Lab
- **Deconstructive Hypothesis Engine**: Decomposes complex queries into specific investigative sub-questions.
- **Evidence Verification**: Gathers empirical facts and analyzes technical tradeoffs.
- **Comprehensive Research Synthesis**: Generates full markdown reports with executive summaries, comparisons, and citation indexes.

### 3. AI Studios (Engineering, Writing & STEM)
- **Code Studio**: Architectural review, security auditing, and automated unit test generation.
- **Writing Studio**: Prose elevation, tone transformation, and executive email drafting.
- **Math & Data Studio**: Step-by-step calculus/algebra solver and unstructured text to CSV converter.

### 4. User-Controlled Memory & Context Management
- **Opt-In Memory**: Transparently stores key preferences and instructions across conversations.
- **Inspection & Editing**: View, toggle, edit, or delete persistent memories at any time.
- **Privacy First**: Stored on-device in local SQLite; zero unintended retention.

### 5. Persistent Storage & Workspaces (Room SQLite)
- **Entities**: Conversations, Messages, User Memories, User Accounts, Workspaces.
- **Search & Organization**: Real-time search, pinned conversations, archived threads, and categorization.
- **Confirmation Dialogs**: Protection against accidental deletion.

### 6. Authentication & Administration
- **Secure Accounts**: Salted SHA-256 cryptographic password hashing.
- **Role-Based Access**: Distinguishes administrator privileges (`admin`) from standard users.
- **System Telemetry**: Real-time metrics tracking conversation counts, message activity, token consumption, and service diagnostics.

---

## 🚀 Configuration & Environment Variables

Credentials are securely managed using the AI Studio Secrets panel and `.env.example`:

```bash
# GEMINI_API_KEY: Injected by AI Studio Secrets panel at runtime
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

Users can also specify an override API key in the in-app **Settings & Intelligence** screen without recompiling.

---

## 🛠️ Testing & Verification

Unit tests and Robolectric JVM integration tests verify critical user journeys:

```bash
gradle :app:testDebugUnitTest
```
Passed tests:
- `ExampleUnitTest.testMarkdownCodeBlockParsing`
- `ExampleUnitTest.testSecurityHelperValidation`
- `ExampleUnitTest.testPasswordHashingAndVerification`
- `ExampleRobolectricTest.read string from context`
