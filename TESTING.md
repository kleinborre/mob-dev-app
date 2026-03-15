# CalorEase Sprint 4 Testing Plan

## Overview
This document outlines the structured test cases required for securing the Sprint 4 Deliverables: Firestore Sync, Online Restrictions, and Email API Deliverability. All scenarios must be passed before merging the milestone.

## Test Strategy: Room ↔ Firestore Sync
| Scenario ID | Name | Pre-Condition | Steps to Reproduce | Expected Result | Actual Result |
|-------------|------|---------------|--------------------|-----------------|---------------|
| `SYNC-01` | **Online Entry Pushed** | Device Online, Logged in. | 1. Add Food to Dashboard <br> 2. Open Firebase Console | Firestore populates identical entry within 1-3 seconds. | ⏳ Pending |
| `SYNC-02` | **Offline Queueing** | Airplane Mode On | 1. Add Food to Dashboard <br> 2. Delete a Food | Dashboard UI updates normally. No errors. App remains fast. | ⏳ Pending |
| `SYNC-03` | **Background Reconciliation** | `SYNC-02` completed | 1. Turn off Airplane Mode <br> 2. Wait 15 mins (or force WorkManager sync) | Firestore syncs the queued offline adds and deletes flawlessly. | ⏳ Pending |
| `SYNC-04` | **Conflict Resolution** | External user alters Firestore entry | 1. Change calorie goal manually in Firebase <br> 2. Change calorie goal backwards in local App (Airplane mode) <br> 3. Reconnect Mobile Network. | The mobile entry (having a *newer* `lastUpdated`) overwrites Firebase payload. | ⏳ Pending |

## Test Strategy: Unified Online Constraints
| Scenario ID | Name | Pre-Condition | Steps to Reproduce | Expected Result | Actual Result |
|-------------|------|---------------|--------------------|-----------------|---------------|
| `AUTH-01` | **Offline Login Block** | Airplane Mode On | 1. Enter email/pw <br> 2. Tap Login | `StatusDialog` shows "No network connection". Login stops. | ⏳ Pending |
| `AUTH-02` | **Offline Google OAuth Block** | Airplane Mode On | 1. Tap Google Login | `StatusDialog` shows "No network connection". Login stops. | ⏳ Pending |
| `AUTH-03` | **Offline Onboarding Success** | Airplane Mode On | 1. Navigate Onboarding pages <br> 2. Input Weight/Goal | UI transitions safely. Datastore saves offline state perfectly. | ⏳ Pending |
| `AUTH-04` | **Offline Final Creation Block** | Airplane Mode On + `AUTH-03` done | 1. Submit final Account form. | `StatusDialog` catches network constraint _before_ `createUserWithEmailAndPassword`. | ⏳ Pending |

## Test Strategy: Real-time Email Verification API
| Scenario ID | Name | Pre-Condition | Steps to Reproduce | Expected Result | Actual Result |
|-------------|------|---------------|--------------------|-----------------|---------------|
| `MAIL-01` | **Fake Email Rejection** | Device Online | 1. Type `asjdgags@fake123mx.com` | After 800ms debounce, UI turns Red with "Unreachable Domain" underneath. | ⏳ Pending |
| `MAIL-02` | **Disposable Email Block** | Device Online | 1. Type `tempmail@yopmail.com` | UI turns Red with "Disposable emails not permitted". | ⏳ Pending |
| `MAIL-03` | **API Timeout Fallback** | Airplane mode during type | 1. Type valid email | Local Regex (`Patterns.EMAIL_ADDRESS`) allows typing safely. Firebase handles ultimate failure on submit. | ⏳ Pending |

## ERD Reference Setup
*Once schemas are synced, an ERD link or ASCII block will be appended here tracking `userId` Foreign Keys across the SQL and NoSQL environments.*
