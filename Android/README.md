# Android Application Setup Guide

## Requirements
- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 8 or newer
- Android SDK 24 or newer
- Google Play Services
- Physical Android device or emulator with Google Play Services installed

## Project Setup

### 1. Clone the Repository
```bash
git clone https://github.com/bd986650/Multisensory-Data-Integration-for-Behavior-Modeling-Comprehensive-Data.git
cd Android
```

### 2. Configure Google Services
1. Place your `client_secret_[YOUR_CLIENT_ID].apps.googleusercontent.com.json` file in the root of the Android project
2. Make sure you have enabled the following Google APIs in your Google Cloud Console:
   - Google Fit API
   - Google Location Services
   - Google Authentication

### 3. Open Project in Android Studio
1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to the Android folder and select it
4. Wait for the project to sync and index

### 4. Configure Gradle
The project uses Gradle with Kotlin DSL. The main configuration files are:
- `build.gradle.kts` (project level)
- `app/build.gradle.kts` (app level)
- `gradle.properties`

### 5. Build Dependencies
The project uses the following main dependencies:
- AndroidX Core KTX
- Jetpack Compose
- Google Play Services (Fitness, Auth, Location)
- Room Database
- Retrofit for networking
- OSMDroid for maps
- WorkManager for background tasks

### 6. Build and Run
1. Connect your Android device or start an emulator
2. Click "Run" in Android Studio or use the command line:
```bash
./gradlew installDebug
```

## Features
- Google Fit integration
- Location tracking
- Background data collection
- Map visualization
- Data synchronization with server

