package com.redpulse.app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.redpulse.app.data.model.User;
import com.redpulse.app.data.repository.UserRepository;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final UserRepository repository = new UserRepository();

    public LiveData<User> getUser() { return user; }

    public void observeUser(String uid) { repository.observeUser(uid, user); }

    public void updateAvailability(String uid, boolean available) {
        repository.updateAvailability(uid, available, new com.redpulse.app.utils.FirestoreCallback<Void>() {
            @Override public void onSuccess(Void data) {}
            @Override public void onError(String message) {}
        });
    }
}
