package com.redpulse.app.ui.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;

public class DonorViewModel extends ViewModel {
    private final MutableLiveData<List<User>> donors = new MutableLiveData<>(new ArrayList<>());
    private final UserRepository repository = new UserRepository();

    public LiveData<List<User>> getDonors() { return donors; }

    public void search(String bloodGroup, Double userLat, Double userLng) {
        repository.findDonors(bloodGroup, userLat, userLng, donors);
    }
}
