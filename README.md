# 🥗 CalorEase - your smart calorie companion

> **Individual Project By:** Oliver Jann Klein Borre  
> **Institution:** Mapua Malayan Digital College  
> **Course:** MO-IT119 Mobile Development Application  

---

![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/compose-Material_3-blue.svg)
![Firebase](https://img.shields.io/badge/firebase-Firestore_%7C_Auth-orange.svg)
![Room](https://img.shields.io/badge/room-Database_v16-green.svg)
![Retrofit2](https://img.shields.io/badge/retrofit-Abstract_API-red.svg)
![WorkManager](https://img.shields.io/badge/background-WorkManager-blueviolet.svg)

## 📱 App Description

**CalorEase** is a comprehensive offline-first calorie tracking companion designed to help users establish consistent daily habits and achieve their weight goals. The app calculates personalized daily calorie targets based on user metrics (BMR/TDEE) and provides an intuitive, real-time interface for logging food intake, tracking progress through visual indicators, and monitoring weight changes over time.

The application features dual-mode functionality with separate dashboards for regular users and administrators, complete offline support utilizing a Last-Write-Wins Room database, and seamless secure NoSQL data synchronization with Firebase Firestore via background workers.

---

## 🔑 Test User Credentials

### Regular User Account
| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **USER** | `lirioroineil@gmail.com` | `CaloreaseA3105!` | **Active** |

### Administrator Account
| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `christinegaemaruquin@gmail.com` | `CaloreaseA3105!` | **Active** |

**Admin Features:**
- Full access to user dashboard (personal tracking)
- Admin dashboard with statistics (total users, active/deactivated accounts)
- User management (view all users, deactivate/reactivate accounts)
- Dashboard mode toggle (switch between admin and user views)

---

## 🚀 Features Implemented

### Sprint 1: User Input Handling & Visual Architecture
1. **Email Format & Password Strength Validation** - Real-time email and password indicators assessing security strengths via regex logic.
2. **Numeric Input Bounds** - Range protections mapping physical metrics (Age, Height, Weight) against impossible parameters.
3. **Calorie Matrix Protections** - Protected float parameters scaling up to 10,000kcal single-entry ceilings.
4. **Interactive Security Dialogs** - Dual-stage confirmations protecting account deletions, logouts, and severe calorie target updates dynamically.
5. **Form Submission Mutexes** - Disabled UI mechanisms ensuring overlapping network or database submissions cannot collide natively.

### Sprint 2: Navigation Matrices
1. **Type-Safe Navigation Graphs** - Fully structured compile-time safe routing matrices mapping 17 unique Composable destinations.
2. **Offline Splash / Session States** - Memory injection checking local DataStore values to bypass Login screens if persistent sessions are active.
3. **Admin Dashboard Topologies** - Independent UI trees built natively for elevated Admin roles scaling user matrices.
4. **Transient Stack Cleanses** - `popUpTo(inclusive)` parameters systematically purging Auth stacks so users cannot back-swipe into logged-out topologies.

### Sprint 3: Offline Data Management (Room SQLite)
1. **Safe Database Migrations (`MIGRATION`)** - Destructive migrations disabled. Implemented native non-destructive schema altercations allowing seamless offline software upgrades without data wipes.
2. **Flow-Based Live Recomposition** - Room DAO mappings wrapped purely in Kotlin `Flow` parameters causing the UI (circular trackers, historical columns) to repaint automatically upon any background local DB modifications.
3. **Database Race-Condition Eliminations** - Protected internal seeding protocols natively inside the SQLite onCreate callbacks, mapping the Test and Admin accounts safely.
4. **Offline Auth Resilience** - Local validation layers saving Onboarding checkpoints (Nickname -> Stats -> Goal) across sessions ensuring App closes mid-tutorial do not dump data.
5. **Soft Account Deactivations** - Admin panels mapped to boolean database toggles (`isActive`) preventing users from logging in, without destroying their historical food data for Admin analytics.

### Sprint 4: Cloud Synchronizations & API Validations
1. **Firestore NoSQL Integration & DTO Maps** - Integrated comprehensive Firebase sub-collection architecture accurately mimicking the complex SQLite ERD mappings in scalable Cloud topologies.
2. **Two-Way Background Reconciliation** - Scheduled `WorkManager` protocols analyzing `lastUpdated` timestamp vectors across local and remote ecosystems executing a sophisticated "Last-Write-Wins" queueing network.
3. **Offline Network Restraints** - Injected rigorous native `NetworkUtils` checks seamlessly into Auth topologies, preemptively deploying standard `StatusDialog` blockades preventing App crashes against offline Firebase calls.
4. **Abstract Email Deliverability Verification** - Integrated `Retrofit2` mapping the remote Abstract API with automated 800ms Kotlin Coroutine `delay()` debounce logic. Verifies if email domains actually exist and rejects disposable accounts natively before firing Firebase registers.
5. **One-Tap Google OAuth** - Implemented the modern Android `CredentialManager` API bypassing redundant legacy Google Clients mapping seamless Single Sign-On (SSO) routines natively into Auth flows.
6. **Reactive Sync Visuals** - Deployed dynamic UI indicator clouds to the Core Dashboards analyzing local Network boundaries scaling native status warnings to users implicitly.
7. **Resilient Reinstall Sync Deployments** - `SyncManager` natively identifies missing offline `UserStats` during fresh application reinstalls, instantly triggering a blocking download override ignoring UNIX timestamps to safely rescue cloud data from zero-state local deletions.

---

## 🛠 Tech Stack

- **Language:** Kotlin 2.1.0+
- **UI Framework:** Jetpack Compose (Material3)
- **Architecture:** MVVM Design Patterns
- **Dependency Injection:** Dagger Hilt
- **Local Database:** Room Database (Flow architectures)
- **Remote Database:** Firebase Firestore (NoSQL Document stores)
- **Authentication:** Firebase Auth & Android CredentialManager (Google OAuth)
- **Networking APIs:** Retrofit2 & Gson (Abstract API Deliverability)
- **Background Processes:** Android WorkManager
- **Persistence:** Jetpack DataStore (Preferences)

---

## 💻 Development Environment

### Android Studio
- **Build:** #AI-252.28238.7.2523.14688667
- **Runtime Version:** 21.0.8+-14196175-b1038.72 amd64
- **Gradle Version:** 9.1.0
- **Kotlin Version:** 2.1.0
- **Compose Compiler:** 2.1.0

### Tested Devices
- **POCO F3:** 6.67" AMOLED, 1080x2400 (Android 11)
- **Huawei Nova 400:** 6.5" OLED, 1080x2340 (Android 12+)

---

## 🏗️ Project Structure

```text
app/src/main/java/com/sample/calorease/
├── data/
│   ├── local/              
│   │   ├── dao/                 # Room DAOs (Query definitions)
│   │   ├── entity/              # SQL Tables (User, Stats, Entries)
│   │   └── AppDatabase.kt       # Room configurations & Safe Migrations
│   ├── remote/             
│   │   ├── api/                 # Retrofit2 HTTP configurations & JSON Models
│   │   └── FirestoreService.kt  # NoSQL Document & Subcollection topologies
│   ├── repository/              # Centralized repository implementations
│   └── session/                 # DataStore mapping engines
├── domain/             
│   ├── model/                   # Pure native application paradigms
│   ├── repository/              # Repository interfaces isolating Logic
│   ├── sync/                    # WorkManager & Last-Write-Wins Schedulers
│   └── usecase/                 # Decoupled mathematical BMR/TDEE calculations
└── presentation/
    ├── components/              # Reusable responsive components & dialogs
    ├── navigation/              # Compose Navigation architectures
    ├── screens/                 # Core modular User Interfaces
    ├── theme/                   # Aesthetic mappings (Color, Type, Theme)
    └── viewmodel/               # ViewModels mapping Logic streams
```

---

## 📊 Key Functionalities

### User Features
- **Personalized Calorie Tracking** - BMR/TDEE-based daily targets
- **Food Intake Logging** - Add, edit, delete daily calorie entries with real-time UI recompilations.
- **Progress Visualization** - Flow-state color-coded dashboards actively reading SQL alterations.
- **Physical Stats Tracking** - Seamless Profile updates syncing across DataStore, Mobile, and Web simultaneously.
- **External Email Validations** - Account creation actively blocks unreachable or synthetic temporary email accounts dynamically.
- **Offline Tracking** - Enter and customize foods dynamically during airplane mode with passive WorkManager cloud syncs triggering automatically upon network recovery.

### Admin Features
- **User Statistics Dashboard** - Total users, active/deactivated counts
- **Soft Delete Management** - Non-destructive account blocks preserving legacy metric tracking securely.
- **Dual Dashboard Access** - Toggle between elevated management structures and personal health dashboards.

---

## 🔐 Security & Data Operations

- **Cloud/Local Mirroring** - All primary read operations load flawlessly out of Room SQLite offline, shielding application limits.
- **Asynchronized Networking** - Cloud modifications are queued invisibly utilizing intelligent timestamp vectors to prevent conflict merges gracefully.
- **Preemptive Error Restraints** - Application checks API endpoints and network states intrinsically before emitting network bounds avoiding crash loops.
- **Password Policies** - Deep character boundaries requiring digits and alphanumerics parsed natively across Regex expressions.

---

## 📈 Database Schema (Local ↔ Remote)

### Entity/Document Paradigm

**1. User Profile Architecture** 
* **Room SQLite (Offline Source of Truth)**
  - `UserEntity`: (Fields: `email` [PrimaryKey], `accountStatus`, `accountCreated`, `lastUpdated`)
  - `UserStatsEntity`: (Fields: `userId` [ForeignKey], `nickname`, `age`, `gender`, `heightCm`, `weightTargetKg`)
* **Firebase Firestore (Cloud Sync)**
  - Collection: `users`
  - Document ID: `email` string
  - Merged Fields: Natively compiles `UserEntity` + `UserStatsEntity` properties relying upon a strict UNIX `lastUpdated` timestamp logic for resolution conflicts.

**2. Tracking Log Architecture**
* **Room SQLite (Offline Source of Truth)**
  - `DailyEntryEntity`: (Fields: `entryId` [PrimaryKey AutoGen], `userId` [ForeignKey], `date` [String YYYY-MM-DD], `calories` [Float], `lastUpdated`)
* **Firebase Firestore (Cloud Sync)**
  - Sub-collection: `daily_entries` (Nested inherently under the specific `users` specific document).
  - Document ID: `date` string (Ensuring one uniform payload entry maximum per day matching Mobile logic scaling naturally without overlap).

---

## 🚀 Getting Started

### Prerequisites
- Android Studio AI-252 or later
- Android SDK 34 (API level 34)
- JDK 25 or later

### Setup Instructions
1. Clone the repository
2. Open project in Android Studio
3. **Firebase Injection:**
   - Create a project on [Firebase](https://console.firebase.google.com/).
   - Enable **Firestore** and **Authentication** (Email/Password & Google OAuth).
   - Inject your `google-services.json` securely into the `app/` directory root.
4. **Abstract API Injection:**
   - Register securely upon [Abstract API](https://www.abstractapi.com/api/email-verification-validation-api) for a free Deliverability API key.
   - Insert the key securely into `app/src/main/java/com/sample/calorease/data/remote/api/AbstractEmailApi.kt`.
5. Sync Gradle configurations.
6. Target an emulator or specific hardware deploying Android 8.0+.

---

## 📝 License & Copyright

All rights reserved. This project is submitted for academic purposes.