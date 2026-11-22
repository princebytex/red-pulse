# Red Pulse Update Notes

This revision upgrades the starter project into a more production-ready donor / requester platform.

## Major functional upgrades
- Added **Bangladesh structured address registration** with cascading dropdowns:
  - Division
  - District
  - Zila
  - City Corporation
  - Upazila
  - Union
  - remaining address line
- Added **admin-reviewed request workflow**:
  - every new request starts as `pending_review`
  - admin can approve or reject
  - rejection supports reason capture
- Added **live donor feed** for only admin-approved requests
- Added **donor interest workflow**:
  - donor taps interest
  - requester can see donor contact details
  - requester confirms donation completion
  - request is closed
  - donor credits are rewarded automatically
- Added **requester delete / cancel flow** with reason submission
- Added **admin CMS screens**:
  - dashboard
  - request review
  - user management
  - notice control
- Added **notice editing support** from the admin notice list
- Added **user history view** in admin panel
- Added **admin credit adjustment** with `credit_transactions` entries
- Added **soft delete / block** support for users

## UI / UX upgrades
- Reworked login and register screens with more professional card-based layout
- Registration now includes official-style location hierarchy selection
- Added clearer home dashboard quick actions
- Added cleaner request submission messaging about admin approval
- Improved donor search input with blood-group dropdown
- Improved drawer header with role-aware badge
- Added role-aware drawer navigation for user vs admin

## Security / backend upgrades
- Updated Firestore rules to:
  - prevent privilege escalation during user creation
  - require request creation as `pending_review`
  - allow admin credit transaction writes
  - support requester-side close / completion flow
- Updated Firestore indexes for the new queries
- Updated Cloud Functions to:
  - notify requester when a donor expresses interest
  - notify requester when admin approves / rejects a request
  - send emergency donor notifications **after admin approval**, not at raw creation time
  - reward donor credits after confirmed donation

## Developer notes
- Admin access is role-based through the same login screen.
- The Firestore `users/{uid}` document must have `role = admin` for admin dashboard access.
- The project now includes `app/src/main/assets/bd_address_hierarchy.json` for local cascading address lookup.
