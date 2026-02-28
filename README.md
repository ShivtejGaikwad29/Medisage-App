# 🩺 Medisage – AI Powered Medical Assistant (Android App)

Medisage is an **AI-powered Android healthcare application** that helps users manage medical reports, receive AI-based assistance, and set medicine reminders — all in one place.

The app integrates **Firebase**, **Cloudinary**, and **HuggingFace AI** to provide a modern smart-health experience.

---

## 🚀 Features

✅ Secure User Authentication (Firebase Auth)
✅ Upload & Manage Medical Reports (Cloudinary Storage)
✅ AI Chat Assistant using HuggingFace API
✅ Medicine Reminder & Notification System
✅ Medical Report Storage using Firebase Realtime Database
✅ Modern Android UI with RecyclerView & Material Design

---

## 🏗️ Tech Stack

### 📱 Android

* Java
* Android SDK
* RecyclerView
* Retrofit (API Calls)
* Notifications & AlarmManager

### ☁️ Backend Services

* **Firebase Authentication**
* **Firebase Realtime Database**
* **Cloudinary** (Media Storage)
* **HuggingFace API** (AI Integration)

### 🛠 Tools

* Android Studio
* Gradle (KTS)
* Git & GitHub

---

## 📂 Project Structure

```
Medisage-App/
│
├── app/
│   ├── src/main/java/com/example/medisageapp/
│   │   ├── Activities
│   │   ├── Adapters
│   │   ├── Models
│   │   └── API Services
│   │
│   ├── res/              # UI layouts & resources
│   └── google-services.json
│
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## ⚙️ Setup Instructions (Run Locally)

### 1️⃣ Clone Repository

```bash
git clone https://github.com/ShivtejGaikwad29/Medisage-App.git
cd Medisage-App
```

---

### 2️⃣ Open in Android Studio

* Open Android Studio
* Select **Open Project**
* Choose the cloned folder

Wait for Gradle Sync to finish.

---

### 3️⃣ Add API Keys (IMPORTANT)

Create or edit:

```
local.properties
```
## Firebase Configuration

This project includes a Firebase configuration for demonstration purposes.

You can optionally use your own Firebase project:

1. Create a Firebase project
2. Add Android app with package:
   com.example.medisageapp
3. Download your own `google-services.json`
4. Replace:
   app/google-services.json

Add your own HuggingFace token:

```
HUGGINGFACE_API_KEY=your_token_here
```

⚠️ This file is ignored by Git for security reasons.

---

### 4️⃣ Firebase Setup

1. Go to Firebase Console
2. Create a new project
3. Add Android App with package:

```
com.example.medisageapp
```

4. Download `google-services.json`
5. Replace file inside:

```
app/google-services.json
```

---

### 5️⃣ Cloudinary Setup

1. Create Cloudinary account
2. Create an **Unsigned Upload Preset**
3. Update preset name inside project if needed:

```java
.unsigned("medisage_preset")
```

---

### 6️⃣ Run the App

Connect emulator or device:

```
Run ▶ app
```

---

## 🔐 Security Practices

* API keys are NOT stored in source code
* Sensitive tokens stored in `local.properties`
* Firebase client configuration is public-safe
* No secrets committed to Git history

---


## 🎯 Future Improvements

* Doctor Appointment Integration
* OCR Prescription Scanner
* Health Analytics Dashboard
* Backend API (Spring Boot)
* Offline Report Access

---

## 👨‍💻 Author

**Shivtej Gaikwad**

* GitHub: https://github.com/ShivtejGaikwad29
* Final Year Engineering Project – Medisage App

---

## ⭐ Support

If you like this project, consider giving it a ⭐ on GitHub!

---
