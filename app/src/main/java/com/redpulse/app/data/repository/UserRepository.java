package com.redpulse.app.data.repository;

import android.location.Location;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.redpulse.app.data.model.User;
import com.redpulse.app.utils.Constants;
import com.redpulse.app.utils.FirestoreCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void createUserProfile(User user, FirestoreCallback<Void> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", user.getUid());
        map.put("fullName", user.getFullName());
        map.put("age", user.getAge());
        map.put("bloodGroup", user.getBloodGroup());
        map.put("address", user.getAddress());
        map.put("addressLine", user.getAddressLine());
        map.put("division", user.getDivision());
        map.put("district", user.getDistrict());
        map.put("zila", user.getZila());
        map.put("cityCorporation", user.getCityCorporation());
        map.put("upazila", user.getUpazila());
        map.put("unionName", user.getUnionName());
        map.put("email", user.getEmail());
        map.put("mobileNumber", user.getMobileNumber());
        map.put("role", user.getRole());
        map.put("searchText", buildSearchText(user));
        map.put("totalCredits", Constants.DEFAULT_REGISTRATION_CREDIT);
        map.put("available", true);
        map.put("verified", false);
        map.put("phoneVerified", false);
        map.put("blocked", false);
        map.put("deleted", false);
        map.put("createdAt", Timestamp.now());
        map.put("updatedAt", Timestamp.now());

        firestore.collection(Constants.USERS).document(user.getUid()).set(map)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void observeUser(String uid, MutableLiveData<User> liveData) {
        firestore.collection(Constants.USERS).document(uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || !value.exists()) return;
                    User user = value.toObject(User.class);
                    if (user != null) liveData.postValue(user);
                });
    }

    public void fetchUser(String uid, FirestoreCallback<User> callback) {
        firestore.collection(Constants.USERS).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError("User profile not found");
                        return;
                    }
                    User user = snapshot.toObject(User.class);
                    if (user == null) {
                        callback.onError("Failed to load profile");
                        return;
                    }
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateAvailability(String uid, boolean available, FirestoreCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("available", available);
        updates.put("updatedAt", Timestamp.now());
        firestore.collection(Constants.USERS).document(uid).update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void findDonors(String bloodGroup, Double userLat, Double userLng, MutableLiveData<List<User>> liveData) {
        if (TextUtils.isEmpty(bloodGroup)) {
            liveData.postValue(new ArrayList<>());
            return;
        }
        final String currentUid = FirebaseAuth.getInstance().getUid();
        firestore.collection(Constants.USERS)
                .whereEqualTo("bloodGroup", bloodGroup)
                .whereEqualTo("available", true)
                .whereEqualTo("blocked", false)
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user == null) continue;
                        if (currentUid != null && currentUid.equals(user.getUid())) continue;
                        if (userLat != null && userLng != null && user.getLatitude() != null && user.getLongitude() != null) {
                            float[] results = new float[1];
                            Location.distanceBetween(userLat, userLng, user.getLatitude(), user.getLongitude(), results);
                            user.setDistanceKm(results[0] / 1000.0);
                        }
                        users.add(user);
                    }
                    Collections.sort(users, (left, right) -> {
                        Double leftDistance = left.getDistanceKm();
                        Double rightDistance = right.getDistanceKm();
                        if (leftDistance == null && rightDistance == null) {
                            return safe(left.getFullName()).compareToIgnoreCase(safe(right.getFullName()));
                        }
                        if (leftDistance == null) return 1;
                        if (rightDistance == null) return -1;
                        return Comparator.<Double>naturalOrder().compare(leftDistance, rightDistance);
                    });
                    liveData.postValue(users);
                })
                .addOnFailureListener(e -> liveData.postValue(new ArrayList<>()));
    }

    public void observeAllUsers(MutableLiveData<List<User>> liveData) {
        firestore.collection(Constants.USERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<User> users = new ArrayList<>();
                    value.getDocuments().forEach(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) users.add(user);
                    });
                    liveData.postValue(users);
                });
    }

    private String buildSearchText(User user) {
        return (safe(user.getFullName()) + ' ' + safe(user.getEmail()) + ' ' + safe(user.getMobileNumber()) + ' ' +
                safe(user.getBloodGroup()) + ' ' + safe(user.getAddress()) + ' ' + safe(user.getAddressLine())).toLowerCase(Locale.getDefault());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
