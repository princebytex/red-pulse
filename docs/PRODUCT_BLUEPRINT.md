# Red Pulse Product Blueprint

## Recommended architecture
MVVM + Repository + Firebase service layer.

## Main modules
- Auth
- Dashboard
- Blood Request
- Nearby Donor Search
- Profile
- Notifications
- Leaderboard / Rewards
- Admin Web Panel (recommended)

## Firestore collections
- users
- blood_requests
- donations
- donor_collections
- credit_transactions
- notifications
- rewards
- leaderboards
- admin_notices
- hospitals
- reports
- app_settings

## Credit logic
- donor confirmed donation: +20
- donor collector completed arrangement: +8
- all credit writes should happen via Cloud Functions/admin backend only

## Admin suggestion
Use a separate web admin dashboard with Firebase Auth + Firestore instead of a second Android admin app.
