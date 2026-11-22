# Bug Check Report

## Static checks completed
- XML well-formedness check passed for all app resource XML files and `AndroidManifest.xml`
- Java source syntax parsing passed for all Java files using `javalang`
- Firebase Cloud Functions JavaScript syntax check passed with `node --check`
- Firestore indexes JSON parse check passed
- Resource reference audit passed for:
  - `R.id`
  - `R.layout`
  - `R.string`
  - `R.menu`
  - `R.drawable`
  - `R.color`

## Important functional issues fixed in this revision
1. **Users could potentially create elevated roles during signup**
   - Firestore rules now restrict client-created profile role to `user`.

2. **Requests could bypass the approval workflow**
   - New request creation now persists as `pending_review` with `active = false`.
   - Firestore rules now enforce this creation state.

3. **Emergency notifications were triggered too early**
   - Cloud Function logic was moved to request publication time so alerts fire after admin approval.

4. **Admin manual credit adjustment could fail under security rules**
   - Firestore rules now allow admin-only creation of `credit_transactions`.

5. **Home feed could expose the current user's own request as a donor action target**
   - Home preview now filters out the current user's own requests.

6. **Registration address input was too weak and unstructured**
   - Replaced free-text address flow with Bangladesh hierarchical dropdown selection plus address line.

7. **Admin notice control was too limited**
   - Added edit-in-place workflow for existing notices.

8. **Create request and donor search forms accepted too much free text**
   - Blood group inputs now use dropdown-driven selection.

9. **Blocked / deleted account handling was incomplete at login / main navigation**
   - Login and main entry now guard against blocked or deleted users.

10. **Requester completion flow needed security-compatible closing logic**
   - Rules now support requester-side request completion after donation confirmation.

## Not fully testable inside this environment
- Full Android Gradle build because Android SDK is not available in this container
- Real Firebase Auth / Firestore / FCM runtime execution
- Device permission behavior on physical devices / emulators
- Google Maps API runtime validation

## Required setup on your side
- valid `google-services.json`
- Android Studio + Android SDK
- Firebase project with Auth / Firestore / Functions / Messaging enabled
- deployed Firestore rules, indexes, and Cloud Functions
- at least one admin user document with `role = admin`
