package com.redpulse.app.ui.request;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.redpulse.app.data.model.BloodRequest;
import com.redpulse.app.data.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;

public class RequestViewModel extends ViewModel {
    private final MutableLiveData<List<BloodRequest>> requests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BloodRequest>> feedRequests = new MutableLiveData<>(new ArrayList<>());
    private final RequestRepository repository = new RequestRepository();

    public LiveData<List<BloodRequest>> getRequests() { return requests; }
    public LiveData<List<BloodRequest>> getFeedRequests() { return feedRequests; }

    public void observeMyRequests(String uid) { repository.observeMyRequests(uid, requests); }
    public void observeFeed(String uid) { repository.observeFeedRequests(uid, feedRequests); }
}
