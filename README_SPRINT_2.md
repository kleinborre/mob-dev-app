# CalorEase (Sprint 2: Navigation Implementation)

> **Individual Project By:** Oliver Jann Klein Borre  
> **Institution:** Mapua Malayan Digital College  
> **Course:** MO-IT119 Mobile Development Application  
> **Branch:** `sprint-2-navigation`

---

## 🧭 Sprint 2 Overview

This branch focuses on the **Navigation Implementation** milestone. It establishes the application's architectural backbone, handling screen routing, user session persistence, and complex multi-step flows. The goal was to create a secure, state-aware navigation system that manages the user's journey from launch to dashboard seamlessly.

---

## 🚀 Features Implemented (Sprint 2)

### 1. Advanced Navigation Architecture
*   **Hash-Map Based NavHost:** Utilizes a scalable `NavHost` setup with type-safe route definitions (`Screen` sealed classes).
*   **Separation of Concerns:** Distinct navigation graphs for:
    *   **Auth Flow:** Login, Sign Up.
    *   **Onboarding Flow:** 4-step wizard.
    *   **Main Flow:** Dashboard, Settings (with Bottom Bar).

### 2. Session Management & Persistence
*   **DataStore Integration:** Implemented `SessionManager` to persist user login state (`isLoggedIn`) and tokens asynchronously.
*   **Intelligent Routing:**
    *   **Splash Logic:** A "Loading" state determines startup destination (Login vs Dashboard) based on saved session data.
    *   **Race Condition Handling:** Prevents login screen "flash" for authenticated users.

### 3. Secure Back-Stack Management
*   **Auth History Clearing:** Implemented `popUpTo(inclusive=true)` logic to completely wipe the Login/SignUp history from the back stack upon successful authentication.
*   **Secure Logout:** Critical logic that clears the session and pops the entire back stack to root, preventing users from navigating "Back" into a private session after logging out.

### 4. Multi-Step Onboarding Flow
*   **Linear Wizard:** Connected 4 sequential screens (Name -> Stats -> Goals -> Result).
*   **Data Retention:** Scoped ViewModels to the Navigation Graph to ensure user inputs (Name, Weight, Height) are preserved across steps and configuration changes (Screen Rotation) without data loss.

### 5. Bottom Navigation Bar
*   **Route Synchronization:** Persistent bottom bar that automatically updates its highlighted state based on the current `NavDestination`.
*   **Deep Hierarchy Support:** Maintains correct tab highlighting even when navigating to sub-screens (e.g., Settings -> Edit Profile -> Back).

---

## 🛠 Tech Stack (Sprint 2)

*   **Language:** Kotlin
*   **Navigation:** Jetpack Compose Navigation
*   **Persistence:** DataStore Preferences
*   **Dependency Injection:** Hilt (ViewModel integration)
*   **Architecture:** Single Activity, Multi-Screen

---

## 🧪 What To Test (Sprint 2 Scope)

1.  **Session Persistence:** proper Login, force close app, reopen. *Result:* App should skip Login and go straight to Dashboard.
2.  **Back-Stack Security:** From Dashboard, press "Back". *Result:* App should exit/minimize, NOT return to Login screen.
3.  **Rotation Logic:** In Onboarding Step 2, enter data and rotate phone. *Result:* Data and screen position should remain intact.
4.  **Logout Flow:** Click "Sign Out". *Result:* User is returned to "Getting Started", and pressing "Back" does not return to Dashboard.

---

## © License & Copyright

**Copyright © 2026 Oliver Jann Klein Borre.**  
**Mapua Malayan Digital College.**

All rights reserved.
