# CalorEase (Sprint 1: User Input Handling)

> **Individual Project By:** Oliver Jann Klein Borre  
> **Institution:** Mapua Malayan Digital College  
> **Course:** MO-IT119 Mobile Development Application  
> **Branch:** `sprint-1-user-input`

---

## 📱 App Description

**CalorEase** is a dedicated calorie tracking companion designed to help users establish consistent daily habits and achieve their weight goals. By suggesting personalized daily calorie targets based on user metrics (BMR/TDEE), the app simplifies the journey to a healthier lifestyle. Users can log their daily food intake, track progress through color-coded visual indicators, and monitor weight changes over time.

The application prioritizes a seamless user experience with intuitive navigation, secure data handling, and instant feedback mechanisms.

---

## � Table of Contents

1.  [App Description](#-app-description)
2.  [Features Implemented](#-features-implemented)
    *   [Sprint 1: User Input Handling](#sprint-1-user-input-handling)
    *   [Sprint 2: Navigation & Architecture](#sprint-2-navigation--architecture)
3.  [Tech Stack](#-tech-stack)
4.  [Test User Credentials](#-test-user-credentials)
5.  [Admin Access](#-admin-access)
6.  [License & Copyright](#-license--copyright)

---

## 🚀 Features Implemented

The current version (Milestone 1) includes the complete implementation of Sprints 1 and 2.

### Sprint 1: User Input Handling
*   **Custom Design System:** Generic `CalorEaseTextField` and `CalorEaseButton` components for consistent UI/UX.
*   **Real-time Validation:** Instant reactive feedback for email formats, password strength, and matching fields using `StateFlow`.
*   **Secure Authentication:**
    *   Masked/Unmasked password toggles (`VisualTransformation`).
    *   Input type enforcement (Numeric keyboards for stats, Email layout for logins).
*   **Interactive Feedback:** Material3 ripple effects and touch animations for immediate response.
*   **Modal Input Dialogs:** Custom state-managed dialogs for "Edit Weight" and "Change Goal" actions.
*   **Async Loading States:** Button interaction locking to prevent double-submission during network operations.

### Sprint 2: Navigation & Architecture
*   **Scalable Navigation Graph:** Type-safe `NavHost` architecture protecting routes for Auth, Onboarding, and Dashboard.
*   **Session Management:** DataStore-based persistence to remember login state and handle "Splash" screen routing.
*   **Multi-Step Onboarding:** Linear wizard flow (Name -> Stats -> Goals -> Result) with data retention across configuration changes (rotation).
*   **Bottom Navigation:** Persistent bottom bar synchronized with back-stack state for accurate tab highlighting.
*   **Secure Routing Logic:**
    *   `popUpTo(inclusive=true)` to wipe authentication history from the back stack after login.
    *   **Logout Mechanism:** Securely clears session data and pops the entire back stack to return to the root screen.

---

## 🛠 Tech Stack (Sprint 1)

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Dependency Injection:** Hilt
*   **Local Database:** Room
*   **Persistence:** DataStore (Preferences)
*   **Navigation:** Jetpack Compose Navigation

---

## � Test User Credentials

Use these credentials to test the functionality of the app (Dashboard access, Onboarding bypass).

| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **USER** | `test@calorease.com` | `Test123!` | **Active** (Access to all user features) |

**User Profile Data:**
*   **Nickname:** TestUser
*   **Stats:** Male, 28 years, 175cm, 75kg
*   **Goal:** Lose weight (Daily Target: ~2,078 kcal)

---

## 🛡️ Admin Access

| Role | Email | Password | Status |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@calorease.com` | *(Reserved)* | **Reserved for Sprint 3-4** |

> *Note: Admin features and dashboard implementation are scheduled for the upcoming development phases.*

---

## 📝 License & Copyright

**Copyright © 2026 Oliver Jann Klein Borre.**  
**Mapua Malayan Digital College.**

All rights reserved. This project is submitted for academic purposes.
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

---

## 👨‍💻 Developer

**Oliver Jann Klein Borre**  
Mapua Malayan Digital College  
MO-IT119 Mobile Development Application