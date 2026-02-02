# CalorEase (Sprint 1: User Input Handling)

> **Individual Project By:** Oliver Jann Klein Borre  
> **Institution:** Mapua Malayan Digital College  
> **Course:** MO-IT119 Mobile Development Application  
> **Branch:** `sprint-1-user-input`

---

## 📱 Sprint 1 Overview

This branch focuses on the **User Input Handling** milestone. It establishes the application's interactive foundation, focusing on creating a robust, secure, and responsive data entry experience. The primary goal was to ensure all user inputs—from text fields to complex dialogs—are validated in real-time and provide immediate, meaningful feedback.

---

## 🚀 Features Implemented (Sprint 1)

### 1. Custom Design System & Input Components
*   **`CalorEaseTextField`:** A standardized, reusable text input component built on Material3.
    *   Supports custom leading/trailing icons.
    *   Integrated error state styling (red outlines, error text).
    *   Configurable keyboard actions (`ImeAction.Next`, `ImeAction.Done`).
*   **`CalorEaseButton`:** A unified button component with built-in state management.
    *   **Async Loading Support:** Replaces text with a circular progress indicator during operations.
    *   **Interaction Locking:** Automatically disables touch events while loading to prevent double-submissions.

### 2. Real-Time Form Validation
*   **Reactive Error Handling:** Utilizes Kotlin `StateFlow` to validate inputs as the user types.
*   **Validation Logic:**
    *   **Email:** Checks against standard email regex patterns.
    *   **Password:** Enforces minimum length requirements.
    *   **Matching:** Verifies "Confirm Password" matches "Password" instantly.
*   **UI Feedback:** Submit buttons remain disabled until all form criteria are met.

### 3. Secure Authentication UI
*   **Password Visibility Toggle:** Implemented `VisualTransformation` logic to allow users to securely toggle between masked (dots) and unmasked password text.
*   **Input Constraints:**
    *   **Numeric Fields:** Strict `KeyboardType.Number` enforcement for Weight/Height inputs to prevent invalid character entry.
    *   **Email Fields:** Optimized keyboard layout (`@` symbol accessible) for email entry.

### 4. Interactive Dialogs
*   **Edit Weight & Change Goal:** Custom modal dialogs that maintain focus and handle state independently of the parent screen.
*   **Local State Management:** Validates dialog inputs before passing data back to the main ViewModel.

### 5. Touch Feedback
*   **Ripple Effects:** All interactive elements (`clickable`, Buttons) feature instant Material3 ripple animations to confirm user intent before actions complete.

---

## 🛠 Tech Stack (Sprint 1)

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material3)
*   **State Management:** ViewModel & StateFlow
*   **Input Handling:** KeyboardOptions, KeyboardActions, VisualTransformation

---

## 🧪 What To Test (Sprint 1 Scope)

1.  **Validation:** Type "invalid-email" in the Login field and observe the immediate error state.
2.  **Security:** Type a password and click the "Eye" icon to toggle visibility.
3.  **Interaction:** Click "Sign Up" with empty fields and observe that the action is blocked (button disabled).
4.  **Constraints:** Try typing letters into the "Weight" field in the Settings dialog (should be blocked).

---

## © License & Copyright

**Copyright © 2026 Oliver Jann Klein Borre.**  
**Mapua Malayan Digital College.**

All rights reserved.
