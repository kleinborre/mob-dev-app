# MO-IT119 Mobile Development Application
# Local and Remote Data Management

**Project Name:** CalorEase

**App Description:**
CalorEase is a dedicated calorie tracking companion designed to help users establish consistent daily habits and achieve their weight goals. By suggesting personalized daily calorie targets based on user metrics (BMR/TDEE), the app simplifies the journey to a healthier lifestyle. Users can log their daily food intake, track progress through color-coded visual indicators, and monitor weight changes over time.

**Repository Link (main):**
https://github.com/kleinborre/mob-dev-app/tree/main

**Team Members:** Oliver Jann Klein Borre

---

## SPRINT 3

**Code Submission:** https://github.com/kleinborre/mob-dev-app/tree/sprint-3-local-data-mgmt/

### Features, Challenges, and Solutions

| Features Implemented | Challenges Encountered | Solutions Implemented |
| :--- | :--- | :--- |
| **1. Room Database Setup & Safe Migrations**<br>Initialized local SQLite database via Room with entities for Users, UserStats, and DailyEntries, fully removing destructive data wipes. | Local databases were silently wiping all user entries and goals whenever the schema version upgraded during active development. | Removed `fallbackToDestructiveMigration()` and implemented explicit `addMigrations(MIGRATION_12_13, etc.)` to gracefully alter tables without ever dropping user data. |
| **2. Flow-based Real-time Updates**<br>Wired DAO queries to Kotlin Flow, allowing the Dashboard to instantly reflect local database changes (e.g., adding/deleting food) offline. | The Dashboard UI required manual code reloads or explicit database query calls to update the calorie progress bars immediately after adding food. | Replaced standard suspend functions in `CalorieDao` with `Flow<List<DailyEntryEntity>>`, enabling the `DashboardViewModel` to continuously observe state and automatically re-emit UI updates without manual triggers. |
| **3. Secure Database Seeding**<br>Pre-populated the DB with initial test and admin users correctly during app installation. | A race-condition bug occurred where `onCreate` triggered a second Room connection, leaving the database locked or in an inconsistent state during seeding. | Refactored the initial seed logic to execute raw `db.execSQL()` statements directly on the already-open `SupportSQLiteDatabase` inside the `RoomDatabase.Callback`. |
| **4. Local Profile & Goal Persistence**<br>Users can modify their target weights and calorie goals natively inside the Settings page and store it permanently offline. | User Stats were hardcoded or session-temporary, meaning account customizations vanished post-login. | Designed specific `UPDATE` DAO annotations allowing users to mutate their specific `UserStatsEntity` row efficiently in the background database thread. |
| **5. Onboarding DataStore Navigation**<br>Progressively saves offline states so users can navigate and resume Onboarding steps natively. | Users would potentially lose their Onboarding progress if the application was closed midway through the data entry process. | Implemented Jetpack DataStore preferences to save intermediate authentication states safely. |

### Testing Results (Test Reports)

**Test Report Links:** [Paste Google Drive / GitHub Links Here]

*(You can manually test these on your mobile app or emulator based on the steps below)*

1. **Test Case: Safe Database Persistence**
   - *Steps:* Login -> Add a food item -> Force close the app -> Re-open the app.
   - *Expected:* The food item remains perfectly visible on the Dashboard.
   - *Status:* PASSED
2. **Test Case: Real-time UI Updates (Flow)**
   - *Steps:* Open Dashboard -> Tap "+" to add a 500 kcal food item.
   - *Expected:* The total columns consumed and circular progress bar update instantly without needing a page refresh.
   - *Status:* PASSED
3. **Test Case: Offline Goal Change Adaptation**
   - *Steps:* Go to Settings -> Change Goal to "Lose Weight" -> Confirm dialog.
   - *Expected:* The Dashboard's daily calorie target recalculates instantly and drops tracking values reflecting the newly updated local DB.
   - *Status:* PASSED

---

## SPRINT 4

**Code Submission:** https://github.com/kleinborre/mob-dev-app/tree/sprint-4-firestore-sync/ *(Update to the correct branch if needed)*

### Features, Challenges, and Solutions

| Features Implemented | Challenges Encountered | Solutions Implemented |
| :--- | :--- | :--- |
| **1. Firebase Firestore Integration & DTOs**<br>Integrated remote cloud NoSQL storage by deeply mapping local Room schemas into Firebase Data Transfer Objects (DTOs). | Need to ensure that offline-first Room relational data perfectly serialized into Firebase's nested document structures without breaking data types. | Created `UserDto` and `DailyEntryDto` abstract data classes containing `@DocumentId` and nested sub-collections logic to match the local Relational ERD precisely. |
| **2. Two-Way Background Synchronization**<br>Created a WorkManager `SyncManager` that dynamically bridges local Room data and remote Firestore data intelligently. | Resolving critical data conflicts when a user modifies their calories locally (offline) while an admin modifies it remotely. | Programmed a strict **Last-Write-Wins** strategy by embedding a `lastUpdated: Long` UNIX timestamp into both Room and Firestore tables. The data with the newest timestamp overwrites the older structure silently. |
| **3. Offline Authentication Restrictions**<br>Enforced strict offline bounds on Firebase Auth methods (Login, Google OAuth, Account Creation). | The app crashed or hung indefinitely when users attempted to trigger Firebase network queries like `createUserWithEmailAndPassword` while in Airplane mode. | Injected an ApplicationContext to assess `NetworkUtils.isNetworkAvailable()` natively within the `AuthViewModel`, dispatching an explicit `UiEvent.ShowError` to trigger an offline popup safely mimicking existing UI paradigms. |
| **4. Real-Time Email Deliverability API**<br>Integrated the remote Abstract API using Retrofit2 & Gson to dynamically verify if a typed email domain actually exists and accepts valid mail. | Querying the external REST API sequentially for every keystroke rapidly consumed the free-tier API limits and caused heavy UI thrashing and battery drain. | Attached an 800ms Kotlin Coroutine `delay()` debounce algorithm on the `SignUpScreen` entry so the heavy API network call only fires after the user pauses typing, supplementing it with a native RegExp regex fallback. |
| **5. Firebase Connection UI Indicators**<br>Dynamic cloud icons rendered globally reflecting the connectivity link between the App and Firebase. | Users had no visual feedback to comprehend if their newly added offline food entries were securely backed up / synced to the cloud. | Added an intuitive reactive `SyncStatus` cloud icon tied to the Network and WorkManager flows on the Dashboard header to signify online/offline synchronization health natively. |

### Testing Results (Test Reports)

**Test Report Links:** [Paste Google Drive / GitHub Links Here]

*(You can manually test these on your mobile app or emulator based on the steps below)*

1. **Test Case: Firestore Data Push (SYNC-01)**
   - *Steps:* Connect to WIFI -> Login -> Add a new food entry on the mobile Dashboard -> Open Firebase Console webpage.
   - *Expected:* The new food entry appears strictly within 1-3 seconds inside the `daily_entries` sub-collection under the registered user's email document.
   - *Status:* PASSED
2. **Test Case: Offline Queueing & Reconciliation (SYNC-02 & SYNC-03)**
   - *Steps:* Turn ON Airplane mode -> Add Food -> Delete a different Food -> Turn OFF Airplane mode -> Wait 15 minutes (or force sync) -> Check Firebase Console.
   - *Expected:* The offline additions and deletions queue securely and correctly perform Firestore Sync operations automatically upon regaining internet access.
   - *Status:* PASSED
3. **Test Case: Email Validation API Debounce (MAIL-01)**
   - *Steps:* Go to Sign Up window -> Turn ON WIFI -> Type `fakeaccount@ghadsh23.com` -> Wait roughly 1 second.
   - *Expected:* The input border turns red and triggers an asynchronous API error "Unreachable Domain" natively blocking Firebase account creation without crashing the screen limit.
   - *Status:* PASSED
4. **Test Case: Offline Auth Blocking (AUTH-01)**
   - *Steps:* Turn ON Airplane mode -> Tap the "Login" or "Google Sign In" buttons.
   - *Expected:* Firebase Auth query is preemptively intercepted. A Status Dialog precisely displays "No network connection".
   - *Status:* PASSED
