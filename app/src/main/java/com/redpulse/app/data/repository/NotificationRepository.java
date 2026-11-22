package com.redpulse.app.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.redpulse.app.data.model.NotificationItem;
import com.redpulse.app.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void observeMyNotifications(String uid, MutableLiveData<List<NotificationItem>> liveData) {
        firestore.collection(Constants.NOTIFICATIONS)
                .whereEqualTo("targetUserId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<NotificationItem> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        NotificationItem item = snapshot.toObject(NotificationItem.class);
                        if (item != null) list.add(item);
                    });
                    liveData.postValue(list);
                });
    }
}
