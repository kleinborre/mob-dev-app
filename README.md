# CalorEase

> **Individual Project By:** Oliver Jann Klein Borre
> **Institution:** Mapua Malayan Digital College
> **Course:** MO-IT119 Mobile Development Application

---

## 📱 App Description

**CalorEase** is a comprehensive calorie tracking companion designed to help users establish consistent daily habits and achieve their weight goals. The app calculates personalized daily calorie targets based on user metrics (BMR/TDEE) and provides an intuitive interface for logging food intake, tracking progress through visual indicators, and monitoring weight changes over time.

The application features dual-mode functionality with separate dashboards for regular users and administrators, complete offline support with Room database, and real-time data synchronization across all screens.

---

## 🔑 Test User Credentials

### Regular User Account
| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **USER** | `test@calorease.com` | `Test123!` | **Active** |

**Profile Data:**
- **Nickname:** TestUser
- **Stats:** Male, 28 years, 175cm, 75kg
- **Goal:** Lose weight (Daily Target: ~2,078 kcal)

### Administrator Account
| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@calorease.com` | `Admin123!` | **Active** |

**Admin Features:**
- Full access to user dashboard (personal tracking)
- Admin dashboard with statistics (total users, active/deactivated accounts)
- User management (view all users, deactivate/reactivate accounts)
- Dashboard mode toggle (switch between admin and user views)

---

## 🚀 Features Implemented

### Sprint 1: User Input Handling & Validation

1. **Email Format Validation** — Real-time email validation with regex-based format checking
2. **Password Strength Validation** — Real-time strength indicator with color-coded feedback and requirement checklist
3. **Required Field Detection** — Comprehensive empty field detection; submit buttons disabled when fields are empty
4. **Password Confirmation Matching** — Real-time validation ensuring password and confirm password match exactly
5. **Numeric Input Validation (Physical Stats)** — Validated ranges: age (13–120), height (100–250 cm), weight (30–300 kg)
6. **Calorie Entry Input Validation** — Decimal support with range validation (1–10,000 kcal)
7. **Visual Password Toggle** — Show/hide password with eye icon across all password fields
8. **Keyboard Type Enforcement** — Context-appropriate keyboards (Email, Numeric, Decimal, Text)
9. **Logout Confirmation Dialog** — Two-step confirmation preventing accidental logouts
10. **Delete Account Double Confirmation** — Two-stage flow with "cannot be undone" warning
11. **Goal Change Confirmation Dialog** — Shows calorie impact before applying changes
12. **Weight Edit Confirmation** — Displays old vs. new weight with recalculation preview
13. **Invalid Input Error Messages** — Specific, actionable error messages for each validation type
14. **Real-time Input Feedback** — Instant visual feedback with colored borders and dynamic error messages
15. **Form Submission State Management** — Loading states preventing double-submission with visual indicators
16. **Mode Switch Confirmation Dialogs** — Confirmation dialogs before switching between User and Admin modes
17. **"0" Placeholder Auto-Clear (Onboarding)** — Height/weight fields show 0 as guide, auto-clear on first key input

#### Sprint 1 — Bug Fixes (Post-Implementation Patches)

| # | Bug | Root Cause | Fix Applied |
|---|-----|-----------|-------------|
| 1 | Onboarding Next buttons required double-tap | Direct validation call inside `onClick` caused recomposition delay before state settled | Stored result in local `val` before the `if` check in all 3 onboarding screens |
| 2 | Login button required double-tap to pass email | `onValueChange = viewModel::updateEmail` (direct method reference) yielded a stale function capture → state not updated at click time | Replaced with lambda `{ email -> viewModel.updateEmail(email) }` in both Email and Password fields |
| 3 | Login email StateFlow timing bug | `login()` read `_authState.value.email` from ViewModel scope, which lagged behind the composable's `authState` snapshot | Added `email`/`password` parameters to `login()`; LoginScreen now captures `authState.email` and passes it directly, bypassing cross-scope StateFlow lag |
| 4 | Email field `trimEnd()` leaving leading whitespace | `updateEmail` only trimmed trailing spaces; login used `trim()` — mismatch caused format validation failure on first attempt | Changed `updateEmail` to call `email.trim()` (both ends) |

---

### Sprint 2: Navigation & Architecture

1. **Type-Safe Navigation Graph** — Sealed class `Screen` routes with compile-time safety
2. **Authentication Flow Navigation** — Linear flow: Getting Started → Login/Sign Up → Dashboard
3. **Back Stack Management** — Strategic `popUpTo(inclusive = true)` preventing return to auth screens
4. **Multi-Step Onboarding Flow** — 4-screen wizard (Name → Stats → Goal → Results) with progressive DataStore saves
5. **Bottom Navigation Bar** — Persistent 3-tab navigation (Dashboard, Food History, Settings)
6. **Admin Dashboard Navigation Toggle** — Switch between User and Admin dashboards with session-persisted mode
7. **Conditional Back Button Visibility** — Back arrow on Login only shown to new/fresh-install users; hidden after any prior login (`hasEverLoggedIn` flag in DataStore)
8. **Session-Based Start Destination** — Dynamic routing to Getting Started / Login / Dashboard based on session state
9. **Deep Navigation to Specific Tabs** — Direct tab navigation with proper back stack behavior
10. **Logout Navigation Flow** — Full session clear and navigation reset on logout
11. **Post-Authentication Navigation** — Automated routing to Dashboard with back stack clearing
12. **Onboarding Completion Navigation** — Single-direction flow preventing return to onboarding screens
13. **Role-Based Dashboard Routing** — Automatic routing based on user role (admin vs. regular user)
14. **Fragment Transition State Management** — State persistence across screen transitions and configuration changes
15. **Navigation Icon Synchronization** — Real-time bottom nav icon highlighting synced to current screen

#### Sprint 2 — Bug Fixes (Post-Implementation Patches)

| # | Bug | Root Cause | Fix Applied |
|---|-----|-----------|-------------|
| 1 | Swipe-back from Login went to Onboarding Step 1 | Back button visible to all users including returning ones | `canGoBack` flag now checks `previousRoute == GettingStarted && !hasEverLoggedIn` |
| 2 | Admin mode reset to User after logout/re-login | `clearSession()` wiped `LAST_DASHBOARD_MODE` alongside credentials | `clearSession()` now preserves only `HAS_EVER_LOGGED_IN`; dashboard mode fully clears on logout |
| 3 | Admin mode switch required double confirmation | Confirmation dialog stacked on top of direct switch trigger | Removed redundant dialog trigger; switch now executes directly via single confirm step |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Dependency Injection** | Hilt |
| **Local Database** | Room (Flow-based real-time updates) |
| **Session Persistence** | DataStore (Preferences) |
| **Navigation** | Jetpack Compose Navigation |
| **Async** | Kotlin Coroutines & Flow |

---

## 💻 Development Environment

### Android Studio
- **Build:** `#AI-252.28238.7.2523.14688667`
- **Built on:** January 9, 2026
- **Runtime:** OpenJDK 21.0.8+ (JetBrains)

### JDK Version
```
java 25 2025-09-16 LTS
Java(TM) SE Runtime Environment (build 25+37-LTS-3491)
```

### Gradle & Kotlin
- **Gradle:** 9.1.0
- **Kotlin:** 2.1.0
- **Compose Compiler:** 2.1.0
- **Room DB Version:** v12

---

## 📱 Tested Devices

| Device | Display | Resolution | OS |
|--------|---------|-----------|-----|
| POCO F3 | 6.67" AMOLED 120Hz | 1080 × 2400 (~395 ppi) | Android 11 (MIUI 12.5+) |
| Huawei Nova 400 | 6.5" OLED | 1080 × 2340 (~396 ppi) | Android 12+ |

---

## 🏗️ Project Structure

```
app/src/main/java/com/sample/calorease/
├── data/
│   ├── local/
│   │   ├── dao/              # Room DAOs (CalorieDao)
│   │   ├── entity/           # DB entities (User, UserStats, DailyEntry)
│   │   ├── AppDatabase.kt    # Room DB config (v12)
│   │   └── Converters.kt     # Type converters
│   ├── model/                # Data models (DailyEntry, UserStats)
│   ├── repository/           # Repository implementations (User, CalorieRepo)
│   └── session/              # SessionManager (DataStore)
├── di/                       # Hilt modules (AppModule)
├── domain/
│   ├── model/                # Domain models (Gender, WeightGoal, ActivityLevel)
│   ├── repository/           # Repository interfaces
│   └── usecase/              # Business logic (BMR/TDEE CalculatorUseCase)
├── presentation/
│   ├── components/           # Reusable composables (8 components)
│   │   ├── CalorEaseButton.kt
│   │   ├── CalorEaseTextField.kt
│   │   ├── CalorEaseCard.kt
│   │   ├── CalorEaseProgressBar.kt
│   │   ├── CalorEaseOutlinedButton.kt
│   │   ├── AuthScaffold.kt
│   │   ├── BottomNavigationBar.kt
│   │   └── AdminBottomNavigationBar.kt
│   ├── navigation/           # Screen sealed class (15 routes)
│   ├── screens/              # 17 screen composables
│   ├── theme/                # Colors, Typography, Theme
│   └── viewmodel/            # 8 ViewModels
├── util/
│   ├── ValidationUtils.kt    # Centralized validation logic
│   ├── DateUtils.kt          # Date formatting helpers
│   ├── Constants.kt
│   └── HelperFunctions.kt
├── CalorEaseApp.kt           # Hilt Application entry
└── MainActivity.kt           # Single-activity host
```

---

## 📊 Screen Inventory (17 Screens)

| Screen | Type | Purpose |
|--------|------|---------|
| `GettingStartedScreen` | Auth | App intro (first-launch only) |
| `LoginScreen` | Auth | Email/password login |
| `SignUpScreen` | Auth | Account registration |
| `ForgotPasswordScreen` | Auth | Password recovery placeholder |
| `OnboardingNameScreen` | Onboarding | Step 1 — Nickname input |
| `OnboardingStatsScreen` | Onboarding | Step 2 — Age, gender, height, weight |
| `OnboardingGoalsScreen` | Onboarding | Step 3 — Weight goal selection |
| `OnboardingResultsScreen` | Onboarding | Step 4 — Calculated BMR/TDEE preview |
| `DashboardScreen` | User | Main calorie tracker |
| `FoodLogsScreen` | User | Full food entry history |
| `SettingsScreen` | User | Profile, weight, goal, account |
| `StatisticsScreen` | User | Statistics placeholder |
| `StatsScreen` | User | Detailed physical stats |
| `AdminStatsScreen` | Admin | User registration stats & chart |
| `AdminUsersScreen` | Admin | User management & search |
| `AdminSettingsScreen` | Admin | Admin account actions |
| `AddCalorieSheet` | Shared | Bottom sheet for food entry |

---

## 📈 Database Schema

### Entities
| Entity | Key Fields |
|--------|-----------|
| `UserEntity` | `userId`, `email`, `password`, `role`, `accountStatus` |
| `UserStatsEntity` | `nickname`, `age`, `gender`, `height`, `weight`, `onboardingCompleted` |
| `DailyEntryEntity` | `entryId`, `userId`, `date`, `foodName`, `calories` |

### Features
- **Flow-based updates** — Real-time UI sync without polling
- **Offline-first** — Full functionality without network
- **Soft delete** — Account deactivation preserves data integrity
- **DB version:** 12 (with managed migrations)

---

## 🎨 Design System

| Token | Value |
|-------|-------|
| **Primary Color** | Dark Turquoise `#00CED1` |
| **Typography** | Poppins (Google Fonts) |
| **Component Style** | Material 3 with custom rounded corners |
| **Button Scheme** | Turquoise (primary), Black (destructive/switch), Dark Red (female) |

---

## 🔐 Security & Data

- **Local-First Architecture** — All data stored on-device (Room + DataStore)
- **Session Management** — Persistent login with selective session clearing on logout
- **Account Deactivation** — Soft delete preserving historical data
- **Input Validation** — Comprehensive client-side validation at ViewModel layer
- **Password Policy** — Min 6 chars login / 8 chars signup with mixed-case + digit + special char

---

## 🚀 Getting Started

### Prerequisites
- Android Studio AI-252 or later
- JDK 25 or later
- Android SDK 34 (API 34)
- Kotlin 2.1.0+

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on emulator or physical device (Android 8.0+)

### First Launch
1. App opens on **Getting Started** screen (first-time only)
2. Tap "Get Started" → **Sign Up** or **Login**
3. Use test credentials or create a new account
4. Complete onboarding: Name → Stats → Goal → Results
5. Start tracking on the Dashboard

---

## 📝 License & Copyright

**Copyright © 2026 Oliver Jann Klein Borre.**
**Mapua Malayan Digital College.**

All rights reserved. This project is submitted for academic purposes.

---

## 👨‍💻 Developer

**Oliver Jann Klein Borre**
Mapua Malayan Digital College
MO-IT119 Mobile Development Application


---

## 📱 App Description

**CalorEase** is a comprehensive calorie tracking companion designed to help users establish consistent daily habits and achieve their weight goals. The app calculates personalized daily calorie targets based on user metrics (BMR/TDEE) and provides an intuitive interface for logging food intake, tracking progress through visual indicators, and monitoring weight changes over time.

The application features dual-mode functionality with separate dashboards for regular users and administrators, complete offline support with Room database, and real-time data synchronization across all screens.

---

## 🔑 Test User Credentials

### Regular User Account
| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **USER** | `test@calorease.com` | `Test123!` | **Active** |

**Profile Data:**
- **Nickname:** TestUser
- **Stats:** Male, 28 years, 175cm, 75kg
- **Goal:** Lose weight (Daily Target: ~2,078 kcal)

### Administrator Account
| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@calorease.com` | `Admin123!` | **Active** |

**Admin Features:**
- Full access to user dashboard (personal tracking)
- Admin dashboard with statistics (total users, active/deactivated accounts)
- User management (view all users, deactivate/reactivate accounts)
- Dashboard mode toggle (switch between admin and user views)

---

## 🚀 Features Implemented

### Sprint 1: User Input Handling

1. **Email Format Validation** - Real-time email validation with instant visual feedback for correct/incorrect email formats
2. **Password Strength Validation** - Real-time password strength indicator with color-coded feedback and requirement checklist
3. **Required Field Detection** - Comprehensive empty field detection with disabled submit buttons when fields are empty
4. **Password Confirmation Matching** - Real-time validation ensuring password and confirm password fields match exactly
5. **Numeric Input Validation (Physical Stats)** - Numeric-only validation for age (13-120), height (100-250cm), weight (30-300kg)
6. **Calorie Entry Input Validation** - Decimal support with range validation (0-10,000 kcal)
7. **Visual Password Toggle** - Show/hide password functionality with eye icon in all password fields
8. **Keyboard Type Enforcement** - Context-appropriate keyboards (Email, Numeric, Decimal, Text)
9. **Logout Confirmation Dialog** - Two-step confirmation preventing accidental logouts
10. **Delete Account Double Confirmation** - Two-stage confirmation flow with "cannot be undone" warning
11. **Goal Change Confirmation Dialog** - Shows impact on daily calorie target before applying changes
12. **Weight Edit Confirmation** - Displays old vs new weight with recalculation preview
13. **Invalid Input Error Messages** - Specific, actionable error messages for each validation type
14. **Real-time Input Feedback** - Instant visual feedback with colored borders and dynamic error messages
15. **Form Submission State Management** - Loading states preventing double-submission with visual indicators

### Sprint 2: Navigation & Architecture

1. **Type-Safe Navigation Graph** - Sealed class routes ensuring compile-time route safety
2. **Authentication Flow Navigation** - Linear flow from Getting Started through Login/Sign Up to Dashboard
3. **Back Stack Management** - Strategic `popUpTo(inclusive=true)` preventing return to auth screens
4. **Multi-Step Onboarding Flow** - 4-screen wizard (Name → Stats → Goal → Results) with data retention
5. **Bottom Navigation Bar** - Persistent 3-tab navigation (Dashboard, Food History, Settings)
6. **Admin Dashboard Navigation Toggle** - Switch between User and Admin dashboards with mode persistence
7. **Conditional Back Button Visibility** - Context-aware back button based on navigation history
8. **Session-Based Start Destination** - Dynamic routing to Getting Started/Login/Dashboard based on session state
9. **Deep Navigation to Specific Tabs** - Direct tab navigation with proper back stack behavior
10. **Logout Navigation Flow** - Complete session clearing and navigation reset on logout
11. **Post-Authentication Navigation** - Automated routing to Dashboard with back stack clearing
12. **Onboarding Completion Navigation** - Single-direction flow preventing return to onboarding
13. **Role-Based Dashboard Routing** - Automatic routing based on user role (admin vs regular user)
14. **Fragment Transition State Management** - State persistence across screen transitions and configuration changes
15. **Navigation Icon Synchronization** - Real-time bottom nav icon highlighting with current screen

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Hilt
- **Local Database:** Room (with Flow-based real-time updates)
- **Persistence:** DataStore (Preferences)
- **Navigation:** Jetpack Compose Navigation
- **Asynchronous:** Kotlin Coroutines & Flow

---

## 💻 Development Environment

### Android Studio
- **Build:** #AI-252.28238.7.2523.14688667
- **Built on:** January 9, 2026
- **Runtime Version:** 21.0.8+-14196175-b1038.72 amd64
- **VM:** OpenJDK 64-Bit Server VM by JetBrains s.r.o.

### JDK Version
```
java 25 2025-09-16 LTS
Java(TM) SE Runtime Environment (build 25+37-LTS-3491)
Java HotSpot(TM) 64-Bit Server VM (build 25+37-LTS-3491, mixed mode, sharing)
```

### Gradle Configuration
- **Gradle Version:** 9.1.0
- **Kotlin Version:** 2.1.0
- **Compose Compiler:** 2.1.0

---

## 📱 Tested Devices

### POCO F3
- **Display:** 6.67" AMOLED, 120Hz
- **Resolution:** 1080 x 2400 pixels
- **Density:** ~395 ppi (xxhdpi)
- **OS:** Android 11 (MIUI 12.5+)

### Huawei Nova 400
- **Display:** 6.5" OLED
- **Resolution:** 1080 x 2340 pixels  
- **Density:** ~396 ppi (xxhdpi)
- **OS:** Android 12+

---

## 🏗️ Project Structure

```
app/src/main/java/com/sample/calorease/
├── data/
│   ├── local/
│   │   ├── dao/           # Room DAOs (CRUD operations)
│   │   ├── entity/        # Database entities
│   │   └── AppDatabase.kt # Room database configuration
│   ├── repository/        # Repository implementations
│   └── session/           # Session management (DataStore)
├── domain/
│   ├── model/             # Domain models (WeightGoal, etc.)
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Business logic (Calculator, etc.)
└── presentation/
    ├── components/        # Reusable UI components
    ├── navigation/        # Navigation routes
    ├── screens/           # Screen composables
    ├── theme/             # App theme (colors, typography)
    └── viewmodel/         # ViewModels (state management)
```

---

## 🎨 Design Features

- **Modern Material3 Design** - Following Material Design 3 guidelines
- **Custom Color Palette** - Dark Turquoise primary color (#00CED1)
- **Poppins Typography** - Clean, modern font family throughout
- **Responsive Layouts** - Support for portrait and landscape orientations
- **Dark Mode Ready** - Theme system prepared for dark mode implementation

---

## 📊 Key Functionalities

### User Features
- **Personalized Calorie Tracking** - BMR/TDEE-based daily targets
- **Food Intake Logging** - Add, edit, delete daily calorie entries
- **Progress Visualization** - Color-coded progress bars (red/yellow/green)
- **Weight Goal Management** - 7 goal options (lose/maintain/gain weight)
- **Physical Stats Tracking** - Age, gender, height, weight with auto-recalculation
- **Food History** - View all past entries with edit/delete capabilities
- **Profile Management** - Update weight, change goals, manage account

### Admin Features
- **User Statistics Dashboard** - Total users, active/deactivated counts
- **User Registration Trends** - 7-day registration chart
- **User Management** - View all users, search functionality
- **Account Status Control** - Activate/deactivate user accounts
- **Dual Dashboard Access** - Toggle between admin and personal tracking views

---

## 🔐 Security & Data

- **Local-First Architecture** - All data stored securely on device with Room
- **Session Management** - Persistent login with DataStore
- **Account Deactivation** - Soft delete preserving data integrity
- **Input Validation** - Comprehensive client-side validation preventing invalid data
- **Password Requirements** - Strong password policy (6+ chars, mixed case, digit, special char)

---

## 📈 Database Schema

### Entities
- **UserEntity** - User accounts (email, password, role, stats, accountStatus)
- **UserStatsEntity** - Physical metrics (nickname, age, gender, height, weight)
- **DailyEntryEntity** - Calorie entries (date, amount, userId)

### Features
- **Flow-based Updates** - Real-time UI synchronization
- **Database Migrations** - Version management (current: v12)
- **Offline Support** - Full functionality without network

---

## 🚀 Getting Started

### Prerequisites
- Android Studio AI-252 or later
- JDK 25 or later
- Android SDK 34 (API level 34)
- Kotlin 2.1.0+

### Installation
1. Clone the repository
2. Open project in Android Studio
3. Sync Gradle dependencies
4. Run on emulator or physical device (Android 8.0+)

### First Launch
1. App opens on **Getting Started** screen (first time only)
2. Tap "Get Started" → Choose **Sign Up** or **Login**
3. Use test credentials or create new account
4. Complete onboarding (Name → Stats → Goal → Results)
5. Start tracking calories on Dashboard!

---

## 📝 License & Copyright

**Copyright © 2026 Oliver Jann Klein Borre.**  
**Mapua Malayan Digital College.**

All rights reserved. This project is submitted for academic purposes.