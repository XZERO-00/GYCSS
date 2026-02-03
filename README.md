# 🛡️ GYCSS — Guardian for Your Care & Senior Safety  
> *A modern, AI-powered senior safety and care management mobile application.*

<p align="center">
  <img src="assets/logo.png" width="120" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Private%20Project-red?style=for-the-badge">
  <img src="https://img.shields.io/badge/Kotlin-2.1.0-blueviolet?style=for-the-badge&logo=kotlin">
  <img src="https://img.shields.io/badge/Android-API%2035-green?style=for-the-badge&logo=android">
  <img src="https://img.shields.io/badge/Firebase-Backend-orange?style=for-the-badge&logo=firebase">
</p>

---

## 🚀 Overview

**GYCSS** is a next-generation **Senior Safety & Care Companion App** designed to ensure the **physical security, health awareness, and emergency readiness** of senior citizens through intelligent monitoring, real-time communication, and cloud-powered services.

This repository is currently **private** and under active development.

---

## 📲 Access & Distribution

<p align="center">
  <img src="https://img.shields.io/badge/APK-Internal%20Testing%20Only-blue?style=for-the-badge&logo=android">
  <img src="https://img.shields.io/badge/Play%20Store-Private%20Release-lightgrey?style=for-the-badge&logo=google-play">
</p>

> 🔒 APK distribution and Play Store access are limited to internal testers only.

---

## ✨ Key Features

- 🔐 **Secure Multi-Role Authentication** — Seniors, Volunteers & Caregivers
- 📍 **Live GPS Tracking** — Real-time location sharing
- 🚨 **Emergency SOS System** — One-tap alerts with location
- ❤️ **Health Monitoring Dashboard** — Daily vitals & history
- 🔔 **Smart Notifications** — Medication, emergency, & reminder alerts
- 🤝 **Volunteer Assistance Network** — Nearby support on demand
- ☁️ **Cloud Sync** — Secure Firebase-backed data storage
- 🎨 **Modern UI/UX** — Material 3, responsive layouts, smooth animations

---

## 🛠 Core Tech Stack

### 🔹 Platform & Build
- **Language:** Kotlin 2.1.0 (K2 Compiler)
- **Min SDK:** API 24 (Android 7.0)
- **Target/Compile SDK:** API 35 (Android 15)
- **Build System:** Gradle 8.13+ with Version Catalogs (`libs.versions.toml`)

### 🔹 Architecture & Logic
- **Pattern:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt (Dagger)
- **Async:** Kotlin Coroutines & Flow
- **Local DB:** Room Persistence Library (KSP)
- **Key-Value Storage:** Jetpack DataStore (Preferences)

### 🔹 Backend & Services (Firebase)
- **Auth:** Firebase Authentication (Email & Google Sign-In)
- **Database:** Cloud Firestore
- **Storage:** Firebase Storage (user profiles, documents)
- **Push Notifications:** Firebase Cloud Messaging (FCM)
- **Analytics & Monitoring:** Firebase Analytics & Crashlytics

### 🔹 Maps & Location
- **Maps:** Google Maps SDK for Android
- **Location:** FusedLocationProviderClient (Play Services)

### 🔹 UI & UX
- **Design System:** Material Design 3 (M3)
- **Layouts:** ConstraintLayout (fully responsive)
- **Navigation:** Jetpack Navigation Component
- **Binding:** ViewBinding & DataBinding
- **Animations:** AnimatorSet, ObjectAnimator, ValueAnimator
- **Image Loading:** Glide / Coil
- **Edge-to-Edge UI:** Activity 1.8.0+ `enableEdgeToEdge`

### 🔹 Tools & Security
- **IDE:** Android Studio Ladybug (2024.2.1+)
- **Annotation Processing:** KSP (Kotlin Symbol Processing)
- **Secrets Management:** Secrets Gradle Plugin
- **Credential Manager:** Android Credentials API (One-Tap Login)
- **Logging:** Timber
- **Serialization:** Kotlinx Serialization (JSON)

---

## 🏗 Project Architecture

GYCSS/
│── app/
│ ├── core/
│ ├── data/
│ ├── domain/
│ ├── ui/
│ ├── di/
│ ├── model/
│ └── utils/
│── res/
│── build.gradle.kts
│── settings.gradle.kts
│── AndroidManifest.xml
│── libs.versions.toml

yaml
Copy code

---

## ⚙️ Setup & Installation (Internal Use)

1. Clone the private repository.
2. Open in Android Studio.
3. Add your `google-services.json` file to `app/`.
4. Ensure Firebase services are enabled.
5. Run the app on a device or emulator.

---

## 🔐 Security & Privacy

- 🔒 Firebase Auth with role-based access
- 🔑 API keys protected via Secrets Gradle Plugin
- 🧾 Crash monitoring via Crashlytics
- 🛡 Secure Firestore rules

See [`SECURITY.md`](SECURITY.md) for responsible disclosure guidelines.

---

## 🤝 Contribution Policy

This is currently a **private project**. Contributions are limited to approved collaborators.

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for internal guidelines.

---

## 📜 Code of Conduct

All contributors must follow our community standards.

See [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

---

## 👨‍💻 Author

**Atharva Vishal Pawar**  
🎓 Diploma in Computer Engineering  
📧 Email: atharvavishalpawar@gmail.com  
🐙 GitHub: https://github.com/XZERO-00  
🔗 LinkedIn: https://www.linkedin.com/in/atharva-pawar02  

---

## 📜 License

This project is licensed under the **MIT License**.

---

## ⭐ Internal Use Notice

This repository and its contents are confidential and intended for internal development only.

---
