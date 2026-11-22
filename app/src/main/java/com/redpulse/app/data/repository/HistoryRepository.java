package com.redpulse.app.data.repository;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.CreditTransaction;
import com.redpulse.app.data.model.Donation;
import com.redpulse.app.data.model.HistoryItem;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void loadUserHistory(String userId, FirestoreCallback<List<HistoryItem>> callback) {
        Tasks.whenAllSuccess(
                firestore.collection(Constants.BLOOD_REQUESTS).whereEqualTo("requesterId", userId).get(),
                firestore.collection(Constants.DONATIONS).whereEqualTo("donorId", userId).get(),
                firestore.collection(Constants.DONATIONS).whereEqualTo("receiverId", userId).get(),
                firestore.collection(Constants.CREDIT_TRANSACTIONS).whereEqualTo("userId", userId).get()
        ).addOnSuccessListener(results -> {
            List<HistoryItem> items = new ArrayList<>();
            QuerySnapshot requests = (QuerySnapshot) results.get(0);
            QuerySnapshot donorActions = (QuerySnapshot) results.get(1);
            QuerySnapshot receiverActions = (QuerySnapshot) results.get(2);
            QuerySnapshot credits = (QuerySnapshot) results.get(3);

            for (BloodRequest request : requests.toObjects(BloodRequest.class)) {
                items.add(new HistoryItem(
                        "Request for " + safe(request.getBloodGroup()) + " created",
                        safe(request.getPatientName()) + " • " + Constants.statusLabel(request.getStatus()),
                        "Request",
                        resolveRequestTime(request)));
            }

            for (Donation donation : donorActions.toObjects(Donation.class)) {
                items.add(new HistoryItem(
                        "You responded to a request",
                        safe(donation.getRequestBloodGroup()) + " • " + Constants.statusLabel(donation.getStatus()),
                        "Donor",
                        resolveDonationTime(donation)));
            }

            for (Donation donation : receiverActions.toObjects(Donation.class)) {
                items.add(new HistoryItem(
                        "Donor activity received",
                        safe(donation.getDonorName()) + " • " + Constants.statusLabel(donation.getStatus()),
                        "Receiver",
                        resolveDonationTime(donation)));
            }

            for (CreditTransaction transaction : credits.toObjects(CreditTransaction.class)) {
                String pointLabel = transaction.getPoints() > 0 ? "+" + transaction.getPoints() : String.valueOf(transaction.getPoints());
                items.add(new HistoryItem(
                        "Credit movement " + pointLabel,
                        safe(transaction.getDescription()) + " • Balance " + transaction.getBalanceAfter(),
                        "Credits",
                        resolveCreditTime(transaction)));
            }

            items.sort((left, right) -> Long.compare(right.getSortTime(), left.getSortTime()));
            callback.onSuccess(items);
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private long resolveRequestTime(BloodRequest request) {
        if (request == null) return 0L;
        if (request.getUpdatedAt() != null) return request.getUpdatedAt().toDate().getTime();
        if (request.getCreatedAt() != null) return request.getCreatedAt().toDate().getTime();
        return 0L;
    }

    private long resolveDonationTime(Donation donation) {
        if (donation == null) return 0L;
        if (donation.getUpdatedAt() != null) return donation.getUpdatedAt().toDate().getTime();
        if (donation.getCreatedAt() != null) return donation.getCreatedAt().toDate().getTime();
        return 0L;
    }

    private long resolveCreditTime(CreditTransaction transaction) {
        if (transaction == null || transaction.getCreatedAt() == null) return 0L;
        return transaction.getCreatedAt().toDate().getTime();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
