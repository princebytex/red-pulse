package com.redpulse.app.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.redpulse.app.data.model.AdminNotice;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.CreditTransaction;
import com.redpulse.app.data.model.Donation;
import com.redpulse.app.data.model.User;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void observePendingRequests(MutableLiveData<List<BloodRequest>> liveData) {
        firestore.collection(Constants.BLOOD_REQUESTS)
                .whereEqualTo("status", Constants.STATUS_PENDING_REVIEW)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<BloodRequest> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        BloodRequest request = snapshot.toObject(BloodRequest.class);
                        if (request != null) list.add(request);
                    });
                    liveData.postValue(list);
                });
    }

    public void observeAllRequests(MutableLiveData<List<BloodRequest>> liveData) {
        firestore.collection(Constants.BLOOD_REQUESTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<BloodRequest> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        BloodRequest request = snapshot.toObject(BloodRequest.class);
                        if (request != null) list.add(request);
                    });
                    liveData.postValue(list);
                });
    }

    public void observeUsers(MutableLiveData<List<User>> liveData) {
        firestore.collection(Constants.USERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<User> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        User user = snapshot.toObject(User.class);
                        if (user != null) list.add(user);
                    });
                    liveData.postValue(list);
                });
    }

    public void observeNotices(MutableLiveData<List<AdminNotice>> liveData) {
        firestore.collection(Constants.ADMIN_NOTICES)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<AdminNotice> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        AdminNotice notice = snapshot.toObject(AdminNotice.class);
                        if (notice != null) {
                            if (notice.getId() == null || notice.getId().trim().isEmpty()) {
                                notice.setId(snapshot.getId());
                            }
                            list.add(notice);
                        }
                    });
                    liveData.postValue(list);
                });
    }

    public void approveRequest(BloodRequest request, String adminUid, String note, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId())
                .update(
                        "status", Constants.STATUS_PUBLISHED,
                        "active", true,
                        "adminDecisionReason", note,
                        "reviewedBy", adminUid,
                        "reviewedAt", Timestamp.now(),
                        "publishedAt", Timestamp.now(),
                        "updatedAt", Timestamp.now())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void rejectRequest(BloodRequest request, String adminUid, String reason, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId())
                .update(
                        "status", Constants.STATUS_REJECTED,
                        "active", false,
                        "adminDecisionReason", reason,
                        "reviewedBy", adminUid,
                        "reviewedAt", Timestamp.now(),
                        "updatedAt", Timestamp.now())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void saveNotice(AdminNotice notice, String adminUid, FirestoreCallback<Void> callback) {
        DocumentReference ref = notice.getId() == null || notice.getId().trim().isEmpty()
                ? firestore.collection(Constants.ADMIN_NOTICES).document()
                : firestore.collection(Constants.ADMIN_NOTICES).document(notice.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("id", ref.getId());
        map.put("title", notice.getTitle());
        map.put("message", notice.getMessage());
        map.put("priority", notice.getPriority() == null ? "normal" : notice.getPriority());
        map.put("active", notice.isActive());
        map.put("createdBy", adminUid);
        map.put("createdAt", notice.getCreatedAt() != null ? notice.getCreatedAt() : Timestamp.now());
        map.put("updatedAt", Timestamp.now());
        ref.set(map)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteNotice(String noticeId, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.ADMIN_NOTICES).document(noticeId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void adjustCredits(User user, int delta, String adminUid, String reason, FirestoreCallback<Void> callback) {
        DocumentReference userRef = firestore.collection(Constants.USERS).document(user.getUid());
        firestore.runTransaction(transaction -> {
            User latest = transaction.get(userRef).toObject(User.class);
            if (latest == null) throw new IllegalStateException("User profile not found");
            int updatedBalance = Math.max(0, latest.getTotalCredits() + delta);
            transaction.update(userRef, "totalCredits", updatedBalance, "updatedAt", Timestamp.now());
            DocumentReference txRef = firestore.collection(Constants.CREDIT_TRANSACTIONS).document();
            Map<String, Object> tx = new HashMap<>();
            tx.put("id", txRef.getId());
            tx.put("userId", user.getUid());
            tx.put("type", delta >= 0 ? "admin_credit_added" : "admin_credit_deducted");
            tx.put("points", delta);
            tx.put("balanceAfter", updatedBalance);
            tx.put("monthKey", Constants.currentMonthKey());
            tx.put("description", reason);
            tx.put("createdBy", adminUid);
            tx.put("createdAt", Timestamp.now());
            transaction.set(txRef, tx);
            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void softDeleteUser(User user, String adminUid, String reason, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.USERS).document(user.getUid())
                .update(
                        "blocked", true,
                        "deleted", true,
                        "deletedReason", reason,
                        "available", false,
                        "updatedAt", Timestamp.now(),
                        "deletedBy", adminUid,
                        "deletedAt", Timestamp.now())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadUserHistory(String userId, FirestoreCallback<UserHistory> callback) {
        UserHistory history = new UserHistory();
        history.userId = userId;
        Tasks.whenAllSuccess(
                firestore.collection(Constants.BLOOD_REQUESTS).whereEqualTo("requesterId", userId).get(),
                firestore.collection(Constants.DONATIONS).whereEqualTo("donorId", userId).get(),
                firestore.collection(Constants.DONATIONS).whereEqualTo("receiverId", userId).get(),
                firestore.collection(Constants.CREDIT_TRANSACTIONS).whereEqualTo("userId", userId).get()
        ).addOnSuccessListener(results -> {
            history.requestsCount = ((com.google.firebase.firestore.QuerySnapshot) results.get(0)).size();
            history.donorInterestCount = ((com.google.firebase.firestore.QuerySnapshot) results.get(1)).size();
            history.receiverHistoryCount = ((com.google.firebase.firestore.QuerySnapshot) results.get(2)).size();
            history.creditTransactionsCount = ((com.google.firebase.firestore.QuerySnapshot) results.get(3)).size();

            ((com.google.firebase.firestore.QuerySnapshot) results.get(0)).getDocuments().stream().limit(3).forEach(snapshot -> {
                BloodRequest request = snapshot.toObject(BloodRequest.class);
                if (request != null) history.recentRequests.add(request);
            });
            ((com.google.firebase.firestore.QuerySnapshot) results.get(1)).getDocuments().stream().limit(3).forEach(snapshot -> {
                Donation donation = snapshot.toObject(Donation.class);
                if (donation != null) history.recentDonations.add(donation);
            });
            ((com.google.firebase.firestore.QuerySnapshot) results.get(3)).getDocuments().stream().limit(3).forEach(snapshot -> {
                CreditTransaction transaction = snapshot.toObject(CreditTransaction.class);
                if (transaction != null) history.recentTransactions.add(transaction);
            });
            callback.onSuccess(history);
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static class UserHistory {
        public String userId;
        public int requestsCount;
        public int donorInterestCount;
        public int receiverHistoryCount;
        public int creditTransactionsCount;
        public final List<BloodRequest> recentRequests = new ArrayList<>();
        public final List<Donation> recentDonations = new ArrayList<>();
        public final List<CreditTransaction> recentTransactions = new ArrayList<>();
    }
}
