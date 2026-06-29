# FieldOps — Mobile Field Service Management System

**FieldOps** is a professional, high-performance, offline-first mobile application tailored for field operations technicians. Built natively in Kotlin and Jetpack Compose utilizing modern Material Design 3 principles, the platform streamlines work order lifecycle control, GPS routing, on-site arrival verification, dynamic progress reporting, and database synchronization.

---

## 🎨 Visual Identity & Aesthetic Principles

FieldOps adheres strictly to **Material Design 3 (M3)** with a focus on high information-density, clarity, and ease-of-use under complex field conditions:
*   **Contrast & Adaptability**: Features optimized typography pairing (using sharp headers and readable mono labels for status markers) to ensure visibility in varied lighting conditions.
*   **Visual Status Communication**: Unified status badges dynamically guide the technician's eyes:
    *   🔵 `Assigned` (Blue) — Direct dispatch queues.
    *   🟣 `Accepted` (Purple) — Technician confirmed.
    *   🟡 `In Progress` (Amber) — Active service loops.
    *   🔴 `On Hold` (Red) — Awaiting parts or client verification.
    *   🟢 `Completed` (Emerald) — Successful service closure.
*   **Touch Targets**: Generous padding and minimum 48dp interactive controls are enforced throughout to guarantee robust control, even when operating with protective gloves.

---

## 🚀 Core Functional Modules

### 1. Operations Dashboard
*   **KPI Indicator Ribbon**: High-level operational summaries displaying outstanding dispatches, active work streams, pending offline edits, and notifications.
*   **Today's Schedule**: Quick access list detailing customer names, locations, service categories, and immediate progress triggers.
*   **Hero Banners**: Engaging visual cues reminding technicians of their shift metrics and safety directives.

### 2. Live Job List & Filter Center
*   **Unified Search Engine**: Search through assigned requests instantly by ID, customer name, location address, or equipment/service types.
*   **Filter Pill Tabs**: Seamless horizontal scroll tabs to segment orders between *All*, *Assigned*, *Active*, and *Completed* service logs.

### 3. Integrated Job Details & Control Centre
*   **Work Order Life Cycle Controller**: Smooth transitions with context-aware buttons allowing technicians to accept/reject, start, hold, or complete service requests.
*   **GPS Tracking & Map Navigation**: Integration with system dialers and automated geo-routing to open Google Maps for immediate site navigation.
*   **Secure QR Site Verification**: arrival-check mechanisms demanding localized QR/code verification to guarantee the technician is physically on-site before executing repairs.
*   **Media Attachments**: High-fidelity photographic upload and delete triggers to document physical diagnostic conditions and completed repairs.
*   **Technical Progress Notes**: Persistent text notes allowing technicians to update notes about active work-in-progress.

### 4. Advanced Service Reports & PDF Exporter
*   **Structured Technical Forms**: Input fields for technical findings, diagnostics, actions taken, and supervisor remarks.
*   **PDF Live Preview & Generation**: Generates clean, formatted diagnostic service sheets with timestamp records, customer data, and diagnostic summaries to simulate print/export storage.

### 5. Offline-First Synchronization & Settings Engine
*   **Failsafe Local Persistence**: Powered by **Room SQLite Database** storing user configurations, customer directory pools, maintenance logs, and active schedules.
*   **Network Operations Sandbox**: Allows toggling between *Online* and *Offline* network states instantly to simulate degraded field environments.
*   **Queued Cache Dispatcher**: Caches any state updates locally while offline, prompting the technician with sync alerts when online status is restored to safely dispatch queued updates.
*   **FCM Alerts Sandbox**: FCM Simulator allowing dispatcher simulation for testing emergency leaks, scheduling reminders, or service updates directly within the UI.
*   **KPI Metrics & Efficiency KPI**: Interactive progression bars highlighting personal shift performance, completed metrics, and target margin qualifiers.

---

## 🛠️ Architecture & Tech Stack

FieldOps is designed with standard enterprise Clean Architecture (MVVM) patterns:
```
com.example
│
├── data
│   ├── local        # Room Database, DAOs, Conversions
│   ├── model        # Immutable Kotlin Entities (User, Job, Customer)
│   └── repository   # Unified Repository managing Offline Sync logic
│
├── ui
│   ├── screens      # Compose UI Screens (Dashboard, List, Details, Settings, Alerts)
│   ├── theme        # Centralized M3 Color Schemes, Typography, Shapes
│   └── viewmodel    # Flow State holder managing UI logic and DB operations
│
└── MainActivity.kt  # Root Entry Point & Navigation Graph definition
```

### Key Libraries & Jetpack Tooling
*   **Jetpack Compose**: Declarative layouts, Scaffold, dynamic Material 3 components.
*   **Room Database**: Local SQLite persistence with custom Entity-relationship schema.
*   **Coroutines & StateFlow**: Lightweight reactive state observation (`collectAsState()`).
*   **Navigation Compose**: Type-safe declarative app routing.
*   **Coil / Painter**: Dynamic resource vector bindings and image loading overlays.

---

## 📦 Build & Development Instructions

### Gradle Build Targets
The build configuration uses Kotlin DSL (`.gradle.kts`) and centralizes dependency management through TOML catalogs.

*   **Assemble Debug APK**:
    ```bash
    gradle assembleDebug
    ```
*   **Execute Local JVM Tests**:
    ```bash
    gradle :app:testDebugUnitTest
    ```

---
*Created and maintained as a production-grade Field Operations assistant on Google AI Studio.*
