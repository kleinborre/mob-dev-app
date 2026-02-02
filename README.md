# CalorEase

> **Individual Project By:** Oliver Jann Klein Borre  
> **Institution:** Mapua Malayan Digital College  
> **Course:** MO-IT119 Mobile Development Application  

---

## 📱 App Description

**CalorEase** is a dedicated calorie tracking companion designed to help users establish consistent daily habits and achieve their weight goals. By suggesting personalized daily calorie targets based on user metrics (BMR/TDEE), the app simplifies the journey to a healthier lifestyle. Users can log their daily food intake, track progress through color-coded visual indicators, and monitor weight changes over time.

The application prioritizes a seamless user experience with intuitive navigation, secure data handling, and instant feedback mechanisms.

---

## 📑 Table of Contents

1.  [App Description](#-app-description)
2.  [Features Implemented](#-features-implemented)
    *   [Sprint 1: User Input Handling](#sprint-1-user-input-handling)
    *   [Sprint 2: Navigation & Architecture](#sprint-2-navigation--architecture)
3.  [Future Development](#-future-development-coming-soon)
4.  [Tech Stack](#-tech-stack)
5.  [Test User Credentials](#-test-user-credentials)
6.  [Admin Access](#-admin-access)
7.  [License & Copyright](#-license--copyright)

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

## 🔜 Future Development (Coming Soon)

The following sprints are scheduled for the next Milestone:

*   **Sprint 3 code:** Local Database Management Implementation
*   **Sprint 4 code:** Local and Remote Database Management Implementation

---

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Dependency Injection:** Hilt
*   **Local Database:** Room
*   **Persistence:** DataStore (Preferences)
*   **Navigation:** Jetpack Compose Navigation

---

## 🔑 Test User Credentials

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

## © License & Copyright

**Copyright © 2026 Oliver Jann Klein Borre.**  
**Mapua Malayan Digital College.**

All rights reserved. This project is submitted for academic purposes.
