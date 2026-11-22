package com.redpulse.app.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.redpulse.app.data.model.AdminNotice;
import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.repository.RequestRepository;
import com.redpulse.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<AdminNotice>> notices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BloodRequest>> homeRequests = new MutableLiveData<>(new ArrayList<>());
    private final RequestRepository requestRepository = new RequestRepository();

    public LiveData<List<AdminNotice>> getNotices() { return notices; }
    public LiveData<List<BloodRequest>> getHomeRequests() { return homeRequests; }

    public void loadHomeData(String currentUid) {
        FirebaseFirestore.getInstance().collection(Constants.ADMIN_NOTICES)
                .whereEqualTo("active", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<AdminNotice> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        AdminNotice item = snapshot.toObject(AdminNotice.class);
                        if (item != null) list.add(item);
                    });
                    notices.postValue(list);
                });

        requestRepository.observeHomeRequests(currentUid, homeRequests);
    }
}
