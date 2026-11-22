package com.redpulse.app.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.redpulse.app.data.model.NotificationItem;
import com.redpulse.app.data.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.List;

public class NotificationsViewModel extends ViewModel {
    private final MutableLiveData<List<NotificationItem>> notifications = new MutableLiveData<>(new ArrayList<>());
    private final NotificationRepository repository = new NotificationRepository();

    public LiveData<List<NotificationItem>> getNotifications() { return notifications; }
    public void observe(String uid) { repository.observeMyNotifications(uid, notifications); }
}
