# ⛏️ SUBSENSE-X
## AI-Powered Mine Subsidence Monitoring & Early Warning System

> Offline-first Android command center for simulated mine telemetry, spatial anomaly detection, risk analysis, and local safety alerts.

## Overview

SUBSENSE-X demonstrates a mine-subsidence monitoring workflow in which distributed sensor telemetry is processed locally on an Android device. The current prototype uses a built-in simulator so the complete workflow can be demonstrated without physical sensor hardware.

**Important:** This is a prototype/decision-support system. It is not a replacement for certified mine-safety procedures, professional engineering assessment, or field-calibrated monitoring equipment.

## Key Features

- 📡 25-node live telemetry simulation
- 🗺️ 5×5 spatial sensor field
- 🤖 Local multi-signal risk analysis
- ⚠️ Risk states: STABLE, WATCH, DEVELOPING, HIGH, CRITICAL
- 📈 Live risk trend and analytics
- 🔎 Spatial coherence and decision evidence
- 🚨 Local safety notifications
- 🧪 Demo scenarios: random feed, local deformation, progressive subsidence, sensor failure
- 🔌 Offline-first processing
- 🧾 Local incident log and sensor-health monitoring
- 📱 Native Android UI built with Jetpack Compose

## Demo Scenarios

| Scenario | Purpose |
|---|---|
| Live random feed | Normal simulated telemetry |
| Local deformation | Demonstrates a concentrated deformation pattern |
| Progressive subsidence | Demonstrates increasing deformation over time |
| Sensor failure | Demonstrates sensor-integrity detection and exclusion from ground-risk scoring |

## Risk Analysis

The prototype combines multiple telemetry signals instead of relying on a single reading:

```text
Deformation + Velocity + Vibration + Crack Indicator
                    +
              Spatial Pattern
                    +
               Sensor Health
                    ↓
             Risk Assessment
                    ↓
              Safety State
                    ↓
             Local Warning
```

The dashboard exposes the risk score, confidence indicator, spatial coherence, maximum deformation, velocity, vibration, node health, and incident history.

## Offline-First Architecture

```text
Sensor / Simulator
       ↓
Local Telemetry Processing
       ↓
Spatial + Anomaly Analysis
       ↓
Risk Engine
       ↓
Android Dashboard
       ├── Live Map
       ├── Alerts
       ├── Analytics
       └── System Status
```

The current prototype performs its monitoring logic locally. Internet connectivity is not required for the core simulation and risk workflow.

## Android Stack

- Kotlin
- Android SDK 35
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- Gradle Kotlin DSL
- Java 17

Application ID: `com.subsensex.advanced`

Minimum Android version: API 26 (Android 8.0)

## Project Structure

```text
SUBSENSE-X/
├── .github/
│   └── workflows/
│       └── subsensex-advanced.yml
├── README.md
└── SUBSENSE-X/
    └── Subsensexadvanced/
        ├── build.gradle.kts
        ├── gradle.properties
        ├── settings.gradle.kts
        └── app/
            ├── build.gradle.kts
            └── src/main/
                ├── AndroidManifest.xml
                ├── java/com/subsensex/advanced/
                │   └── MainActivity.kt
                └── res/values/
                    ├── strings.xml
                    └── themes.xml
```

## Build

The repository includes a GitHub Actions workflow that builds a debug APK from the nested Android project and uploads the generated APK as the `SUBSENSE-X-installable` artifact.

The latest verified workflow run completed successfully.

### Android Studio

Open the Android project directory:

```text
SUBSENSE-X/Subsensexadvanced/
```

Then let Android Studio sync Gradle and run the `app` configuration on an emulator or Android device.

### GitHub Actions

Workflow:

```text
.github/workflows/subsensex-advanced.yml
```

The workflow uses the correct nested project path and verifies the generated APK.

## Installation

Download the APK from the successful GitHub Actions run's **Artifacts** section, install it on an Android device, and allow notification permission when prompted.

## Hardware Integration Roadmap

The software can later be connected to physical monitoring hardware such as:

- ESP32 sensor nodes
- LoRa / LoRa mesh gateways
- Accelerometers and tilt sensors
- Vibration sensors
- Displacement sensors
- Crack sensors
- Temperature and humidity sensors

The current repository intentionally uses simulation so the monitoring and decision-support workflow can be demonstrated independently of hardware availability.

## Safety Note

SUBSENSE-X is an academic/prototype project. Thresholds and simulated values are not field-calibrated safety limits. Any real deployment would require validated sensors, engineering review, domain-specific thresholds, communications testing, cybersecurity controls, and compliance with applicable mine-safety requirements.

## Project Status

- ✅ Android project builds successfully in GitHub Actions
- ✅ Debug APK generated and verified by CI
- ✅ Offline simulation and risk workflow implemented
- ✅ Live map, alerts, analytics, and system views implemented
- 🔄 Physical sensor/gateway integration remains a future extension
- 🔄 Field validation and production safety certification are outside the current prototype scope

## License

No license has been declared yet. Add an appropriate license before external redistribution if required.
