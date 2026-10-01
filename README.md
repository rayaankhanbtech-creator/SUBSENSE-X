# ⛏️ SUBSENSE-X
## AI-Powered Mine Subsidence Monitoring & Early Warning System

> **SUBSENSE-X** is an offline-first smart mining safety platform designed to monitor underground mine subsidence, detect abnormal ground behavior, analyze risk, and provide early warnings through a real-time Android command center.

---

## 🚨 Problem

Underground coal mines can experience ground deformation, subsidence, cracks, vibration, and structural instability.

Traditional monitoring methods often depend on periodic inspections and post-event analysis. This can create a delay between the appearance of abnormal behavior and the detection of a potential hazard.

SUBSENSE-X addresses this problem through continuous sensor-based monitoring and automated risk analysis.

---

## 💡 Solution

SUBSENSE-X combines:

- 📡 Distributed sensor monitoring
- 🤖 AI-assisted risk analysis
- 📊 Real-time telemetry visualization
- 🗺️ Spatial sensor mapping
- 🚨 Automated safety alerts
- 📈 Trend and anomaly monitoring
- 📱 Android-based monitoring
- 🔌 Offline-first operation
- 💾 Local data processing and storage

The system is designed so that essential monitoring and safety logic can continue even when there is **no Wi-Fi or Internet connection**.

---

## 🧠 Key Features

### 🔴 Live Monitoring

The application provides a simulated live telemetry stream representing data from mine sensor nodes.

The simulator continuously generates changing values for:

- Ground deformation
- Deformation velocity
- Vibration
- Crack indicators
- Temperature
- Humidity
- Battery level
- Signal strength
- Packet rate

This allows the complete monitoring workflow to be demonstrated without physical hardware.

---

### 🗺️ Sensor Network Visualization

SUBSENSE-X provides a visual sensor grid representing distributed monitoring nodes across a mining area.

Each node can display information such as:

- Node ID
- Current risk state
- Deformation
- Velocity
- Vibration
- Battery
- Signal strength
- Sensor health

---

### ⚠️ Risk Assessment

The application continuously evaluates incoming telemetry and generates a safety risk state.

Possible states include:

- 🟢 STABLE
- 🟡 WATCH
- 🟠 DEVELOPING
- 🔴 HIGH
- 🚨 CRITICAL

The dashboard displays:

- Overall risk score
- Confidence
- Spatial coherence
- Active alerts
- Sensor health
- Maximum deformation
- Maximum velocity
- Maximum vibration

---

## 🚨 Early Warning System

When abnormal conditions are detected, SUBSENSE-X can generate safety warnings.

The system is designed to highlight:

- Rapid deformation
- Increasing vibration
- Localized anomalies
- Progressive subsidence patterns
- Sensor failures
- Multiple-node abnormal behavior

The objective is to provide an early indication of potentially dangerous ground conditions.

---

## 📊 Live Telemetry Dashboard

The monitoring dashboard includes:

| Parameter | Description |
|---|---|
| MAX DEV | Maximum detected deformation |
| VELOCITY | Ground deformation velocity |
| VIB | Maximum vibration |
| TEMP | Simulated temperature |
| HUMID | Simulated humidity |
| PACKETS | Telemetry packet rate |
| RISK | Current overall risk |
| CONFIDENCE | Confidence of the risk assessment |

A live trend visualization allows changes in risk and sensor behavior to be observed over time.

---

## 🧪 Simulation Mode

Because physical sensor hardware may not always be available during development, SUBSENSE-X includes a built-in telemetry simulator.

The simulator produces continuously changing sensor values so that the complete application can be tested and demonstrated.

### Available Demo Scenarios

- **Live Random Feed**
- **Local Deformation**
- **Progressive Subsidence**
- **Sensor Failure**

These scenarios allow different mine safety conditions to be demonstrated without requiring physical sensor nodes.

---

## 📱 Android Application

SUBSENSE-X is built as a native Android application using:

- Kotlin
- Jetpack Compose
- Android SDK
- Material UI components
- Kotlin Coroutines

The application provides a command-center style interface optimized for monitoring and safety visualization.

---

## 🔌 Offline-First Architecture

A major design goal of SUBSENSE-X is operation in environments where Internet connectivity may be unavailable.

The core monitoring workflow is designed around local processing.

```text
Sensor Nodes
     │
     ▼
Local Communication Layer
     │
     ▼
Android Monitoring Application
     │
     ├── Local Telemetry Processing
     ├── Risk Analysis
     ├── Anomaly Detection
     ├── Local Alerts
     └── Local Data

🧮 Risk Intelligence
SUBSENSE-X combines multiple telemetry signals instead of relying on a single sensor value.
The monitoring logic considers factors such as:
Deformation
     +
Velocity
     +
Vibration
     +
Spatial Pattern
     +
Sensor Health
     ↓
Risk Assessment
     ↓
Safety State
This helps distinguish isolated sensor fluctuations from broader abnormal patterns

📈 Analytics
The application provides monitoring indicators including:
Risk score
Risk trend
Spatial coherence
Sensor confidence
Maximum deformation
Maximum velocity
Maximum vibration
Sensor health
Packet activity
Incident history
A transparent decision trace can also be used to understand why a risk state was generated.

🧪 Fault Detection
SUBSENSE-X can identify sensor-related problems such as:
Missing telemetry
Abnormal readings
Low battery
Poor signal
Sensor failure
This is important because a monitoring system should distinguish between a genuine environmental anomaly and a malfunctioning sensor.

🔐 Safety Philosophy
SUBSENSE-X follows a safety-first design approach:
Monitor continuously.
Detect abnormal changes.
Compare multiple signals.
Estimate risk.
Generate warnings.
Preserve local operation when connectivity is unavailable.
The system is intended as a decision-support and early-warning platform, not as a replacement for certified mine safety procedures or professional engineering assessment.

🛠️ Technology Stack
Android
Kotlin
Jetpack Compose
Android SDK
Material Design
Kotlin Coroutines
Data & Intelligence
Real-time telemetry simulation
Local risk analysis
Sensor anomaly detection
Trend analysis
Spatial monitoring
Hardware Integration Roadmap
The software architecture is designed to be extended to physical sensor nodes using technologies such as:
ESP32
LoRa
LoRa mesh
Accelerometers
Tilt sensors
Vibration sensors
Displacement sensors
Crack sensors
Temperature sensors
Humidity sensors


SUBSENSE-X/
│
├── .github/
│   └── workflows/
│       └── subsensex-advanced.yml
│
├── SubsenseXAdvanced/
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── settings.gradle.kts
│   │
│   └── app/
│       ├── build.gradle.kts
│       │
│       └── src/
│           └── main/
│               ├── AndroidManifest.xml
│               │
│               ├── java/
│               │   └── com/
│               │       └── subsensex/
│               │           └── advanced/
│               │               └── MainActivity.kt
│               │
│               └── res/
│                   └── values/
│                       ├── strings.xml
│                       └── themes.xml
│
└── README.md
