package com.redpulse.app.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.model.User;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void createRequest(BloodRequest request, FirestoreCallback<String> callback) {
        if (request.getRequesterId() == null || request.getRequesterId().trim().isEmpty()) {
            callback.onError("Please sign in again before posting a request.");
            return;
        }

        DocumentReference doc = firestore.collection(Constants.BLOOD_REQUESTS).document();
        DocumentReference userRef = firestore.collection(Constants.USERS).document(request.getRequesterId());

        firestore.runTransaction(transaction -> {
            User latestUser = transaction.get(userRef).toObject(User.class);
            if (latestUser == null || latestUser.isBlocked() || latestUser.isDeleted()) {
                throw new IllegalStateException("Your account cannot create a request right now.");
            }
            if (latestUser.getTotalCredits() < Constants.REQUEST_CREDIT_COST) {
                throw new IllegalStateException("You need at least 1 credit before requesting blood.");
            }

            request.setId(doc.getId());
            request.setStatus(Constants.STATUS_PENDING_REVIEW);
            request.setActive(false);
            request.setMatchedDonorCount(0);
            request.setCreatedAt(Timestamp.now());
            request.setUpdatedAt(Timestamp.now());
            transaction.set(doc, request);
            return doc.getId();
        }).addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void observeHomeRequests(String currentUid, MutableLiveData<List<BloodRequest>> liveData) {
        firestore.collection(Constants.BLOOD_REQUESTS)
                .whereEqualTo("status", Constants.STATUS_PUBLISHED)
                .whereEqualTo("active", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<BloodRequest> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        BloodRequest item = snapshot.toObject(BloodRequest.class);
                        if (item == null) return;
                        if (currentUid != null && currentUid.equals(item.getRequesterId())) return;
                        list.add(item);
                    });
                    Collections.sort(list, (left, right) -> {
                        if (left.isEmergency() == right.isEmergency()) return 0;
                        return left.isEmergency() ? -1 : 1;
                    });
                    liveData.postValue(list);
                });
    }

    public void observeFeedRequests(String currentUid, MutableLiveData<List<BloodRequest>> liveData) {
        firestore.collection(Constants.BLOOD_REQUESTS)
                .whereEqualTo("status", Constants.STATUS_PUBLISHED)
                .whereEqualTo("active", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<BloodRequest> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        BloodRequest item = snapshot.toObject(BloodRequest.class);
                        if (item == null) return;
                        if (currentUid != null && currentUid.equals(item.getRequesterId())) return;
                        list.add(item);
                    });
                    liveData.postValue(list);
                });
    }

    public void observeMyRequests(String uid, MutableLiveData<List<BloodRequest>> liveData) {
        firestore.collection(Constants.BLOOD_REQUESTS)
                .whereEqualTo("requesterId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<BloodRequest> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        BloodRequest item = snapshot.toObject(BloodRequest.class);
                        if (item != null) list.add(item);
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
                        BloodRequest item = snapshot.toObject(BloodRequest.class);
                        if (item != null) list.add(item);
                    });
                    liveData.postValue(list);
                });
    }

    public void cancelRequest(BloodRequest request, String reason, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId())
                .update(
                        "status", Constants.STATUS_CANCELLED,
                        "active", false,
                        "cancellationReason", reason,
                        "closedAt", Timestamp.now(),
                        "updatedAt", Timestamp.now()
                )
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateLiveRequestByAdmin(BloodRequest request, String adminUid, FirestoreCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("patientName", request.getPatientName());
        updates.put("bloodGroup", request.getBloodGroup());
        updates.put("requiredUnits", request.getRequiredUnits());
        updates.put("hospitalName", request.getHospitalName());
        updates.put("fullAddress", request.getFullAddress());
        updates.put("contactNumber", request.getContactNumber());
        updates.put("requiredTimestamp", request.getRequiredTimestamp());
        updates.put("notes", request.getNotes());
        updates.put("emergency", request.isEmergency());
        updates.put("updatedAt", Timestamp.now());
        updates.put("reviewedBy", adminUid);
        firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId())
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void removeLiveRequestByAdmin(BloodRequest request, String adminUid, String reason, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.BLOOD_REQUESTS).document(request.getId())
                .update(
                        "status", Constants.STATUS_CANCELLED,
                        "active", false,
                        "cancellationReason", reason == null || reason.trim().isEmpty() ? "Removed by admin" : reason,
                        "reviewedBy", adminUid,
                        "closedAt", Timestamp.now(),
                        "updatedAt", Timestamp.now()
                )
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
