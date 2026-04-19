# PawMatch Android Application

PawMatch is a native Android application engineered to connect pet owners based on geographical proximity and pet species compatibility. The application facilitates socialization among pets and their owners utilizing a matchmaking interaction model.

## Technology Stack

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Declarative UI)
* **Navigation**: Jetpack Navigation Compose
* **Image Loading**: Coil
* **Backend as a Service**: Firebase
  * **Firebase Authentication**: Secure user session management via Email/Password providers.
  * **Cloud Firestore**: NoSQL real-time document database housing User, Pet, and Match interactions.
  * **Firebase Storage**: For distributed cloud asset management.

## Core Architecture & Features

The application is segregated into distinct phases of feature sets handling independent domains:

### 1. Identity and Profile Management
Users register an account acting as the overarching entity for themselves and their primary pet. 
Profiles strictly leverage predefined Dropdown menus for properties like `City` to prevent database index fragmentation during matchmaking queries.

### 2. Matchmaking Discovery Engine
The discovery mechanism utilizes Jetpack Compose `pointerInput(detectDragGestures)` to render physics-driven draggable cards. The Firestore backend generates candidate batches by performing structured queries matching the user's registered city and their preferred animal parameters, specifically filtering out the authenticated user's own pet entities.

### 3. Interaction and Messaging Integration
Interactions are resolved inside a `MatchesScreen` logic model. Likes are recorded symmetrically across both users. If a mutually verified "Like" signal is intercepted, a formal `Match` object is instantiated globally.
Matched entities appear on a directory where users may inspect detailed specifications of the pet and its owner. Direct communication handles are executed via Explicit Intents triggering the WhatsApp messenger URI format application logic.

## Setup and Installation

To independently deploy and run this project, coordinate the Android environment and Firebase keys:

1. **Clone or Extract Project**: Open the root directory inside Android Studio.
2. **Environment Synchronization**: Ensure you are running Java 17+ and the latest Android Gradle Plugin. Allow Android Studio to perform standard Gradle Synchronization.
3. **Database Connectivity**:
   * Navigate to `Firebase Console` and register the `com.pawmatch.app` package structure to a Firebase Project.
   * Download the generated `google-services.json`.
   * Place the file exactly within the root `app/` directory of the project folder to enable Google Services plugin execution.
4. **Authentication Setup**: Within the Firebase console, under the `Authentication` tab, enable the `Email/Password` provider.
5. **Compilation**: Execute the standard application build from Android Studio or via `./gradlew build`.

## License

This project architecture incorporates templates licensed under the Apache License, Version 2.0.
