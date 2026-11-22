package com.redpulse.app.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.Donation;
import com.redpulse.app.data.model.User;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonationRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void expressInterest(BloodRequest request, User donor, FirestoreCallback<Void> callback) {
        String donationId = request.getId() + "_" + donor.getUid();
        DocumentReference donationRef = firestore.collection(Constants.DONATIONS).document(donationId);
        DocumentReference requestRef = firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId());
        firestore.runTransaction(transaction -> {
            Donation existingDonation = null;
            if (transaction.get(donationRef).exists()) {
                existingDonation = transaction.get(donationRef).toObject(Donation.class);
            }
            BloodRequest latestRequest = transaction.get(requestRef).toObject(BloodRequest.class);
            if (latestRequest == null || !latestRequest.isActive() || !Constants.STATUS_PUBLISHED.equals(latestRequest.getStatus())) {
                throw new IllegalStateException("This request is no longer accepting donors");
            }

            boolean shouldIncreaseCount = existingDonation == null || Constants.STATUS_CANCELLED_BY_REQUESTER.equals(existingDonation.getStatus());
            Map<String, Object> donationMap = new HashMap<>();
            donationMap.put("id", donationId);
            donationMap.put("requestId", request.getId());
            donationMap.put("donorId", donor.getUid());
            donationMap.put("donorName", donor.getFullName());
            donationMap.put("donorEmail", donor.getEmail());
            donationMap.put("donorPhone", donor.getMobileNumber());
            donationMap.put("donorBloodGroup", donor.getBloodGroup());
            donationMap.put("receiverId", request.getRequesterId());
            donationMap.put("receiverName", request.getRequesterName());
            donationMap.put("requestBloodGroup", request.getBloodGroup());
            donationMap.put("status", Constants.STATUS_INTEREST_SUBMITTED);
            donationMap.put("creditsGranted", false);
            donationMap.put("note", "Interested to donate");
            donationMap.put("updatedAt", Timestamp.now());
            if (existingDonation == null || existingDonation.getCreatedAt() == null) {
                donationMap.put("createdAt", Timestamp.now());
            }
            if (shouldIncreaseCount) {
                transaction.update(requestRef,
                        "matchedDonorCount", Math.max(0, latestRequest.getMatchedDonorCount()) + 1,
                        "updatedAt", Timestamp.now());
            }
            transaction.set(donationRef, donationMap);
            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadInterestsForRequestOnce(String requestId, FirestoreCallback<List<Donation>> callback) {
        firestore.collection(Constants.DONATIONS)
                .whereEqualTo("requestId", requestId)
                .get()
                .addOnSuccessListener(value -> callback.onSuccess(sortDonations(value.toObjects(Donation.class))))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void observeInterestsForRequest(String requestId, MutableLiveData<List<Donation>> liveData) {
        firestore.collection(Constants.DONATIONS)
                .whereEqualTo("requestId", requestId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    liveData.postValue(sortDonations(value.toObjects(Donation.class)));
                });
    }

    public void confirmDonation(BloodRequest request, Donation donation, String completionNote, FirestoreCallback<Void> callback) {
        DocumentReference donationRef = firestore.collection(Constants.DONATIONS).document(donation.getId());
        DocumentReference requestRef = firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId());
        DocumentReference requesterRef = firestore.collection(Constants.USERS).document(request.getRequesterId());
        firestore.runTransaction(transaction -> {
            BloodRequest latestRequest = transaction.get(requestRef).toObject(BloodRequest.class);
            Donation latestDonation = transaction.get(donationRef).toObject(Donation.class);
            User requester = transaction.get(requesterRef).toObject(User.class);
            if (latestRequest == null) throw new IllegalStateException("Request not found");
            if (latestDonation == null) throw new IllegalStateException("Donor interest not found");
            if (!latestRequest.isActive() || !Constants.STATUS_PUBLISHED.equals(latestRequest.getStatus())) {
                throw new IllegalStateException("This request is no longer live.");
            }
            if (!Constants.STATUS_INTEREST_SUBMITTED.equals(latestDonation.getStatus())) {
                throw new IllegalStateException("This donor response cannot be completed anymore.");
            }
            if (requester == null || requester.getTotalCredits() < Constants.REQUEST_CREDIT_COST) {
                throw new IllegalStateException("You need at least 1 credit in your account before marking blood given.");
            }

            Map<String, Object> donationUpdate = new HashMap<>();
            donationUpdate.put("status", Constants.STATUS_CONFIRMED_BY_RECEIVER);
            donationUpdate.put("confirmedAt", Timestamp.now());
            donationUpdate.put("updatedAt", Timestamp.now());
            donationUpdate.put("note", completionNote);
            transaction.update(donationRef, donationUpdate);

            Map<String, Object> requestUpdate = new HashMap<>();
            requestUpdate.put("status", Constants.STATUS_COMPLETED);
            requestUpdate.put("active", false);
            requestUpdate.put("completionNote", completionNote);
            requestUpdate.put("closedAt", Timestamp.now());
            requestUpdate.put("updatedAt", Timestamp.now());
            requestUpdate.put("collectorUserId", donation.getDonorId());
            transaction.update(requestRef, requestUpdate);
            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void cancelDonationInterest(BloodRequest request, Donation donation, String reason, FirestoreCallback<Void> callback) {
        DocumentReference donationRef = firestore.collection(Constants.DONATIONS).document(donation.getId());
        DocumentReference requestRef = firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId());
        firestore.runTransaction(transaction -> {
            Donation latestDonation = transaction.get(donationRef).toObject(Donation.class);
            BloodRequest latestRequest = transaction.get(requestRef).toObject(BloodRequest.class);
            if (latestDonation == null) throw new IllegalStateException("Donor response not found.");
            if (!Constants.STATUS_INTEREST_SUBMITTED.equals(latestDonation.getStatus())) {
                throw new IllegalStateException("This donor response is already closed.");
            }
            transaction.update(donationRef,
                    "status", Constants.STATUS_CANCELLED_BY_REQUESTER,
                    "note", reason == null || reason.trim().isEmpty() ? "Cancelled by requester" : reason,
                    "updatedAt", Timestamp.now());
            if (latestRequest != null) {
                transaction.update(requestRef,
                        "matchedDonorCount", Math.max(0, latestRequest.getMatchedDonorCount() - 1),
                        "updatedAt", Timestamp.now());
            }
            return null;
        }).addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private List<Donation> sortDonations(List<Donation> items) {
        List<Donation> list = new ArrayList<>();
        if (items != null) list.addAll(items);
        list.sort((left, right) -> {
            if (left.getCreatedAt() == null && right.getCreatedAt() == null) return 0;
            if (left.getCreatedAt() == null) return 1;
            if (right.getCreatedAt() == null) return -1;
            return Comparator.<Long>naturalOrder().reversed().compare(
                    left.getCreatedAt().toDate().getTime(),
                    right.getCreatedAt().toDate().getTime());
        });
        return list;
    }
}
